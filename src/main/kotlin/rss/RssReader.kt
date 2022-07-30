package rss

import rss.model.RssChannel
import rss.model.RssImage
import rss.model.RssItem
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.*
import java.util.function.Function
import java.util.stream.Stream
import java.util.stream.StreamSupport
import java.util.zip.GZIPInputStream
import javax.net.ssl.SSLContext
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

object RssReader {
    private val httpClient: HttpClient

    init {
        val client: HttpClient = try {
            val context = SSLContext.getInstance("TLSv1.3")
            context.init(null, null, null)
            HttpClient.newBuilder().sslContext(context).connectTimeout(Duration.ofSeconds(25)).followRedirects(HttpClient.Redirect.NORMAL)
                .build()
        } catch (e: NoSuchAlgorithmException) {
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(25)).followRedirects(HttpClient.Redirect.NORMAL).build()
        } catch (e: KeyManagementException) {
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(25)).followRedirects(HttpClient.Redirect.NORMAL).build()
        }
        httpClient = client
    }

    @Throws(IOException::class)
    fun read(url: String): Stream<RssItem?> {
        return try {
            readAsync(url)[5, TimeUnit.MINUTES]
        } catch (e: CompletionException) {
            try {
                throw e.cause!!
            } catch (e2: IOException) {
                throw e2
            } catch (e2: Throwable) {
                throw AssertionError(e2)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException(e)
        } catch (e: ExecutionException) {
            throw IOException(e)
        } catch (e: TimeoutException) {
            throw IOException(e)
        }
    }

    fun read(inputStream: InputStream): Stream<RssItem?> {
        val itemIterator = RssItemIterator(inputStream)
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(itemIterator, Spliterator.ORDERED), false)
    }

    private fun readAsync(url: String): CompletableFuture<Stream<RssItem?>> {
        return sendAsyncRequest(url).thenApply(processResponse())
    }

    private fun sendAsyncRequest(url: String): CompletableFuture<HttpResponse<InputStream>> {
        val req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(25)).header("Accept-Encoding", "gzip").GET().build()
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
    }

    private fun processResponse(): Function<HttpResponse<InputStream>, Stream<RssItem?>> {
        return Function<HttpResponse<InputStream>, Stream<RssItem?>> { response ->
            try {
                if (response.statusCode() in 400..599) {
                    throw IOException("Response http status code: " + response.statusCode())
                }
                var inputStream: InputStream = response.body()
                if (Optional.of("gzip") == response.headers().firstValue("Content-Encoding")) {
                    inputStream = GZIPInputStream(inputStream)
                }
                inputStream = BufferedInputStream(inputStream)
                removeBadDate(inputStream)
                val itemIterator = RssItemIterator(inputStream)
                return@Function StreamSupport.stream(Spliterators.spliteratorUnknownSize(itemIterator, Spliterator.ORDERED), false)
            } catch (e: IOException) {
                throw CompletionException(e)
            }
        }
    }

    @Throws(IOException::class)
    private fun removeBadDate(inputStream: InputStream) {
        inputStream.mark(2)
        val firstChar = inputStream.read()
        if (firstChar != 13 && firstChar != 10 && !Character.isWhitespace(firstChar)) {
            inputStream.reset()
        } else if (firstChar == 13 || Character.isWhitespace(firstChar)) {
            val secondChar = inputStream.read()
            if (secondChar != 10 && !Character.isWhitespace(secondChar)) {
                inputStream.reset()
                inputStream.read()
            }
        }
    }

    private class RssItemIterator(private val inputStream: InputStream) : MutableIterator<RssItem?> {
        private val textBuilder: StringBuilder = StringBuilder()
        private var reader: XMLStreamReader? = null
        private var rssChannel: RssChannel? = null
        private var rssImage: RssImage? = null
        private var rssItem: RssItem? = null
        private var nextRssItem: RssItem? = null
        private var isChannelPart = true
        private var isImagePart = false
        private var elementName: String? = null

        init {
            try {
                val xmlInFact = XMLInputFactory.newInstance()
                // disable XML external entity (XXE) processing
                xmlInFact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, java.lang.Boolean.FALSE)
                xmlInFact.setProperty(XMLInputFactory.SUPPORT_DTD, java.lang.Boolean.FALSE)
                reader = xmlInFact.createXMLStreamReader(inputStream)
            } catch (exception: XMLStreamException) {
                exception.printStackTrace()
            }
        }

        fun peekNext() {
            if (nextRssItem == null) {
                nextRssItem = try {
                    next()
                } catch (e: NoSuchElementException) {
                    null
                }
            }
        }

        override fun hasNext(): Boolean {
            peekNext()
            return nextRssItem != null
        }

        override fun next(): RssItem? {
            if (nextRssItem != null) {
                val next: RssItem = nextRssItem as RssItem
                nextRssItem = null
                return next
            }
            try {
                while (reader!!.hasNext()) {
                    val type = reader!!.next() // do something here
                    if (type == XMLStreamConstants.CHARACTERS || type == XMLStreamConstants.CDATA) {
                        parseCharacters()
                    } else if (type == XMLStreamConstants.START_ELEMENT) {
                        parseStartElement()
                        parseAttributes()
                    } else if (type == XMLStreamConstants.END_ELEMENT) {
                        val itemParsed = parseEndElement()
                        if (itemParsed) return rssItem
                    }
                }
            } catch (exception: XMLStreamException) {
                exception.printStackTrace()
            }
            try {
                reader!!.close()
                inputStream.close()
            } catch (exception: XMLStreamException) {
                exception.printStackTrace()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
            throw NoSuchElementException()
        }

        override fun remove() { }

        fun parseDateTime(dateTime: String?): ZonedDateTime? {
            if (dateTime == null) {
                return null
            }
            var dateTimeFormatter: DateTimeFormatter? = null
            when (dateTime.length) {
                in 29..31 -> {
                    dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
                }
                25 -> {
                    dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                }
                19 -> {
                    dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                }
            }
            if (dateTimeFormatter == null) {
                return null
            }
            try {
                return ZonedDateTime.parse(dateTime, dateTimeFormatter)
            } catch (ignored: Exception) {
            }
            return null
        }

        fun parseStartElement() {
            textBuilder.setLength(0)
            elementName = reader!!.localName
            if ("channel" == elementName || "feed" == elementName) {
                rssChannel = RssChannel()
                isChannelPart = true
            } else if ("item" == elementName || "entry" == elementName) {
                rssItem = RssItem()
                rssItem!!.rssChannel = rssChannel
                isChannelPart = false
            } else if ("guid" == elementName) {
                val value = reader!!.getAttributeValue(null, "isPermaLink")
                if (rssItem != null) rssItem!!.isPermalink = java.lang.Boolean.parseBoolean(value)
            } else if ("image" == elementName) {
                rssImage = RssImage()
                rssChannel?.rssImage = rssImage
                isImagePart = true
            }
        }

        fun parseAttributes() {
            if (reader?.localName == "link") {
                val rel = reader!!.getAttributeValue(null, "rel")
                val link = reader!!.getAttributeValue(null, "href")
                val isAlternate = "alternate" == rel
                if (link != null && isAlternate) {
                    if (isChannelPart) {
                        rssChannel!!.url = link
                    } else {
                        rssItem!!.url = link
                    }
                }
            }
        }

        fun parseEndElement(): Boolean {
            val name = reader?.localName
            val text = textBuilder.trim().toString()
            if ("image" == name) {
                isImagePart = false
            } else if (isImagePart) {
                parseImageCharacters(elementName, text)
            } else if (isChannelPart) {
                parseChannelCharacters(elementName, text)
            } else {
                parseItemCharacters(elementName, rssItem, text)
            }
            textBuilder.setLength(0)
            return "item" == name || "entry" == name
        }

        fun parseCharacters() {
            var text = reader!!.text ?: return
            text = text.trim()
            if (text.isBlank()) {
                return
            }
            textBuilder.append(text)
        }

        fun parseChannelCharacters(elementName: String?, text: String?) {
            if (rssChannel == null || text.isNullOrBlank()) {
                return
            }
            when (elementName) {
                "title" -> {
                    rssChannel!!.title = text
                }
                "description", "subtitle" -> {
                    rssChannel!!.description = text
                }
                "link" -> {
                    rssChannel!!.url = text
                }
                "category" -> {
                    rssChannel!!.category = text
                }
                "language" -> {
                    rssChannel!!.language = text
                }
                "copyright", "rights" -> {
                    rssChannel!!.copyright = text
                }
                "generator" -> {
                    rssChannel!!.generator = text
                }
                "ttl" -> {
                    rssChannel!!.ttl = text
                }
                "pubDate" -> {
                    rssChannel!!.publishedDateTime = parseDateTime(text)
                }
                "lastBuildDate", "updated" -> {
                    rssChannel!!.lastBuildDateTime = parseDateTime(text)
                }
                "managingEditor" -> {
                    rssChannel!!.managingEditor = text
                }
                "webMaster" -> {
                    rssChannel!!.webMaster = text
                }
            }
        }

        fun parseImageCharacters(elementName: String?, text: String?) {
            if (rssImage == null || text.isNullOrBlank()) {
                return
            }
            when (elementName) {
                "title" -> {
                    rssImage!!.title = text
                }
                "link" -> {
                    rssImage!!.link = text
                }
                "url" -> {
                    rssImage!!.url = text
                }
                "description" -> {
                    rssImage!!.description = text
                }
                "height" -> {
                    rssImage!!.height = text.toInt()
                }
                "width" -> {
                    rssImage!!.width = text.toInt()
                }
                "image" -> {
                    isImagePart = false
                }
            }
        }

        fun parseItemCharacters(elementName: String?, rssItem: RssItem?, text: String?) {
            if (text.isNullOrBlank()) {
                return
            }
            when (elementName) {
                "guid", "id" -> {
                    rssItem!!.id = text
                }
                "title" -> {
                    rssItem!!.title = text
                }
                "description", "summary", "content" -> {
                    rssItem!!.description = text
                }
                "link" -> {
                    if (rssItem!!.id == null) {
                        rssItem.id = text
                    }
                    rssItem.url = text
                }
                "author" -> {
                    rssItem!!.author = text
                }
                "category" -> {
                    rssItem!!.category = text
                }
                "pubDate", "published" -> {
                    rssItem!!.publishedDateTime = parseDateTime(text)
                }
                "updated" -> {
                    rssItem!!.updatedDateTime = parseDateTime(text)
                }
            }
        }
    }
}
