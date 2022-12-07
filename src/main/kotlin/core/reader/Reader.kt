package core.reader

import core.model.Feed
import extension.chomp
import extension.format
import org.jsoup.Jsoup
import java.util.*

abstract class Reader<Entry> {

    fun read(url: String): List<Entry> {
        return onEntryList(Feed(url))
    }

    fun read(feed: Feed): List<Feed.Item> {
        val entryList = onEntryList(feed)
        if (entryList.isEmpty()) {
            return emptyList()
        }
        val sentUrls = HashSet<String>()
        feed.sentItems.forEach { item ->
            sentUrls.add(item.url)
        }
        val items = ArrayList<Feed.Item>()
        val cachedSentItemSize: Int = entryList.size * 3
        if (cachedSentItemSize > feed.cachedSentItemSize) {
            feed.cachedSentItemSize = cachedSentItemSize
        }
        entryList.forEach { entry ->
            val url = onUrl(feed, entry)
            if (url.isNotBlank() && !sentUrls.contains(url)) {
                val id = parseValue(onId(feed, entry))
                val title = parseValue(onTitle(feed, entry))
                val authorName = parseValue(onAuthorName(feed, entry))
                val authorEmail = parseValue(onAuthorEmail(feed, entry))
                val authorUrl = parseValue(onAuthorUrl(feed, entry))
                val contents = parseValue(onContents(feed, entry))
                val publishedDate = parseValue(onPublishedDate(feed, entry))
                val updatedDate = parseValue(onUpdatedDate(feed, entry))
                var formattedText = url
                if (feed.format.isNotBlank()) {
                    val pairList = ArrayList<Pair<String, String>>()
                    pairList.add(Pair(FormatToken.URL.value, url))
                    pairList.add(Pair(FormatToken.TITLE.value, title))
                    pairList.add(Pair(FormatToken.AUTHOR_NAME.value, authorName))
                    pairList.add(Pair(FormatToken.AUTHOR_EMAIL.value, authorEmail))
                    pairList.add(Pair(FormatToken.AUTHOR_URL.value, authorUrl))
                    pairList.add(Pair(FormatToken.CONTENTS.value, contents))
                    pairList.add(Pair(FormatToken.PUBLISHED_DATE.value, publishedDate))
                    pairList.add(Pair(FormatToken.UPDATED_DATE.value, updatedDate))
                    pairList.add(Pair(FormatToken.FEED_TITLE.value, feed.title))
                    pairList.add(Pair(FormatToken.FEED_URL.value, feed.url))
                    formattedText = replaceEach(feed.format, pairList).chomp()
                }
                items.add(Feed.Item(parseDate = Date(), feed = feed, id = id, url = url,
                    title = title, formattedText = formattedText, publishedDate = onPublishedDate(feed, entry)))
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
        return Jsoup.parse(value.toString()).text().trim()
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

    protected abstract fun onEntryList(feed: Feed): List<Entry>
    protected abstract fun onId(feed: Feed, entry: Entry): String
    protected abstract fun onUrl(feed: Feed, entry: Entry): String
    protected abstract fun onTitle(feed: Feed, entry: Entry): String?
    protected abstract fun onPublishedDate(feed: Feed, entry: Entry): Date?
    protected abstract fun onUpdatedDate(feed: Feed, entry: Entry): Date?
    protected abstract fun onContents(feed: Feed, entry: Entry): String?
    protected abstract fun onAuthorName(feed: Feed, entry: Entry): String?
    protected abstract fun onAuthorEmail(feed: Feed, entry: Entry): String?
    protected abstract fun onAuthorUrl(feed: Feed, entry: Entry): String?
}
