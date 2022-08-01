package core.reader

import core.model.Feed
import extension.format
import org.jsoup.Jsoup
import java.util.*

abstract class Reader<ENTRY> {

    fun read(url: String): List<ENTRY> {
        return onEntryList(Feed(url))
    }

    fun read(feed: Feed): List<Feed.Item> {
        val entryList = onEntryList(feed)
        if (entryList.isEmpty()) {
            return emptyList()
        }
        val sentUrls = HashSet<String>()
        feed.sentItems.forEach { item ->
            run {
                sentUrls.add(item.url)
            }
        }
        val items = ArrayList<Feed.Item>()
        val cachedSentItemSize: Int = entryList.size * 3
        if (cachedSentItemSize > feed.cachedSentItemSize) {
            feed.cachedSentItemSize = cachedSentItemSize
        }
        entryList.forEach { entry ->
            run {
                val url = onUrl(feed, entry)
                if (url.isNotBlank() && !sentUrls.contains(url)) {
                    val id = onId(feed, entry)
                    val title = onTitle(feed, entry)
                    val authorName = onAuthorName(feed, entry)
                    val authorEmail = onAuthorEmail(feed, entry)
                    val authorUrl = onAuthorUrl(feed, entry)
                    val contents = onContents(feed, entry)
                    val publishedDate = onPublishedDate(feed, entry)
                    val updatedDate = onUpdatedDate(feed, entry)
                    var formattedText = url
                    if (feed.format.isNotBlank()) {
                        val pairList = ArrayList<Pair<String, String>>()
                        pairList.add(Pair(FormatToken.URL.value, parseValue(url)))
                        pairList.add(Pair(FormatToken.TITLE.value, parseValue(title)))
                        pairList.add(Pair(FormatToken.AUTHOR_NAME.value, parseValue(authorName)))
                        pairList.add(Pair(FormatToken.AUTHOR_EMAIL.value, parseValue(authorEmail)))
                        pairList.add(Pair(FormatToken.AUTHOR_URL.value, parseValue(authorUrl)))
                        pairList.add(Pair(FormatToken.CONTENTS.value, parseValue(contents)))
                        pairList.add(Pair(FormatToken.PUBLISHED_DATE.value, parseValue(publishedDate)))
                        pairList.add(Pair(FormatToken.UPDATED_DATE.value, parseValue(updatedDate)))
                        pairList.add(Pair(FormatToken.FEED_TITLE.value, parseValue(feed.title)))
                        pairList.add(Pair(FormatToken.FEED_URL.value, parseValue(feed.url)))
                        formattedText = replaceEach(feed.format, pairList)
                    }
                    items.add(Feed.Item(parseDate = Date(), feed = feed, id = id, url = url,
                        title = title ?: "<Unknown Title>", formattedText = formattedText, publishedDate = onPublishedDate(feed, entry)))
                }
            }
        }
        return items
    }

    private fun parseValue(value: Any?): String {
        if (value == null) {
            return ""
        }
        if (value is Date) {
            return value.format()
        }
        return Jsoup.parse(value.toString()).text()
    }

    private fun replaceEach(text: String, pairList: List<Pair<String, String>>): String {
        if (pairList.isEmpty()) {
            return ""
        }
        val searchLength = pairList.size
        val noMoreMatchesForReplIndex = BooleanArray(searchLength)
        var textIndex = -1
        var replaceIndex = -1
        var tempIndex: Int
        for (i in 0 until searchLength) {
            if (noMoreMatchesForReplIndex[i] || pairList[i].first.isEmpty()) {
                continue
            }
            tempIndex = text.indexOf(pairList[i].first)
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true
            } else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex
                replaceIndex = i
            }
        }
        if (textIndex == -1) {
            return text
        }
        var start = 0
        var increase = 0
        for (i in pairList.indices) {
            val greater = pairList[i].second.length - pairList[i].first.length
            if (greater > 0) {
                increase += 3 * greater
            }
        }
        increase = increase.coerceAtMost(text.length / 5)
        val buf = StringBuilder(text.length + increase)
        while (textIndex != -1) {
            for (i in start until textIndex) {
                buf.append(text[i])
            }
            buf.append(pairList[replaceIndex].second)
            start = textIndex + pairList[replaceIndex].first.length
            textIndex = -1
            replaceIndex = -1
            for (i in 0 until searchLength) {
                if (noMoreMatchesForReplIndex[i] || pairList[i].first.isEmpty()
                ) {
                    continue
                }
                tempIndex = text.indexOf(pairList[i].first, start)
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true
                } else if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex
                    replaceIndex = i
                }
            }
        }
        val textLength = text.length
        for (i in start until textLength) {
            buf.append(text[i])
        }
        return buf.toString()
    }

    protected abstract fun onEntryList(feed: Feed): List<ENTRY>
    protected abstract fun onId(feed: Feed, entry: ENTRY): String
    protected abstract fun onUrl(feed: Feed, entry: ENTRY): String
    protected abstract fun onTitle(feed: Feed, entry: ENTRY): String?
    protected abstract fun onPublishedDate(feed: Feed, entry: ENTRY): Date?
    protected abstract fun onUpdatedDate(feed: Feed, entry: ENTRY): Date?
    protected abstract fun onContents(feed: Feed, entry: ENTRY): String?
    protected abstract fun onAuthorName(feed: Feed, entry: ENTRY): String?
    protected abstract fun onAuthorEmail(feed: Feed, entry: ENTRY): String?
    protected abstract fun onAuthorUrl(feed: Feed, entry: ENTRY): String?
}
