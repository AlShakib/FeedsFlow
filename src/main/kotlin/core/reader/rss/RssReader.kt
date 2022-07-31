package core.reader.rss

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import core.model.Feed
import core.reader.Reader
import extension.toURI
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.time.Duration
import java.util.*
import java.util.zip.GZIPInputStream
import javax.net.ssl.SSLContext

object RssReader: Reader<SyndEntry>() {
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

    override fun onEntryList(feed: Feed): List<SyndEntry> {
        val httpRequest = HttpRequest.newBuilder(feed.url.toURI())
            .timeout(Duration.ofSeconds(25))
            .header("Accept-Encoding", "gzip")
            .GET()
            .build()
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() in 400..599) {
            throw IOException("Response http status code: " + response.statusCode())
        }
        var inputStream = response.body()
        if (Optional.of("gzip") == response.headers().firstValue("Content-Encoding")) {
            inputStream = GZIPInputStream(inputStream)
        }
        inputStream = BufferedInputStream(inputStream)
        val streamReader = InputStreamReader(inputStream)
        val feedInput = SyndFeedInput().build(streamReader)
        streamReader.close()
        return feedInput.entries
    }

    override fun onAuthorUrl(feed: Feed, entry: SyndEntry): String? {
        return if (entry.authors.isNullOrEmpty()) {
            null
        } else entry.authors[0].uri
    }

    override fun onAuthorEmail(feed: Feed, entry: SyndEntry): String? {
        return if (entry.authors.isNullOrEmpty()) {
            null
        } else entry.authors[0].email
    }

    override fun onAuthorName(feed: Feed, entry: SyndEntry): String? {
        return if (entry.authors.isNullOrEmpty()) {
            null
        } else entry.authors[0].name
    }

    override fun onContents(feed: Feed, entry: SyndEntry): String? {
        return if (entry.contents.isNullOrEmpty()) {
            null
        } else entry.contents[0].value
    }

    override fun onUpdatedDate(feed: Feed, entry: SyndEntry): Date? {
        return entry.updatedDate
    }

    override fun onPublishedDate(feed: Feed, entry: SyndEntry): Date? {
        return entry.publishedDate
    }

    override fun onTitle(feed: Feed, entry: SyndEntry): String? {
        return entry.title
    }

    override fun onUrl(feed: Feed, entry: SyndEntry): String {
        return entry.link ?: ""
    }

    override fun onId(feed: Feed, entry: SyndEntry): String {
        return entry.link ?: UUID.randomUUID().toString()
    }
}
