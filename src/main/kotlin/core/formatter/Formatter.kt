package core.formatter

import extension.chomp
import extension.format
import org.jsoup.Jsoup
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

abstract class Formatter<ITEM> {
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a").withZone(ZoneOffset.UTC)

    fun format(format: String, item: ITEM): String {
        val pairList = onFormatterPairList(item)
        val searchList = ArrayList<String>()
        val replacementList = ArrayList<String>()
        pairList.forEach { pair ->
            run {
                searchList.add(pair.first)
                replacementList.add(pair.second)
            }
        }
        return replaceEach(format, searchList, replacementList).chomp()
    }

    protected fun parseValue(value: Any?): String {
        if (value == null) {
            return ""
        }
        if (value is ZonedDateTime) {
            return value.format()
        }
        return Jsoup.parse(value.toString()).text()
    }

    private fun replaceEach(text: String, searchList: List<String>, replacementList: List<String>): String {
        val searchLength = searchList.size
        val replacementLength = replacementList.size

        // make sure lengths are ok, these need to be equal
        require(searchLength == replacementLength) {
            ("Search and Replace array lengths don't match: $searchLength vs $replacementLength")
        }
        val noMoreMatchesForReplIndex = BooleanArray(searchLength)
        var textIndex = -1
        var replaceIndex = -1
        var tempIndex: Int
        for (i in 0 until searchLength) {
            if (noMoreMatchesForReplIndex[i] || searchList[i].isEmpty()) {
                continue
            }
            tempIndex = text.indexOf(searchList[i])
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
        for (i in searchList.indices) {
            val greater = replacementList[i].length - searchList[i].length
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
            buf.append(replacementList[replaceIndex])
            start = textIndex + searchList[replaceIndex].length
            textIndex = -1
            replaceIndex = -1
            for (i in 0 until searchLength) {
                if (noMoreMatchesForReplIndex[i] || searchList[i].isEmpty()
                ) {
                    continue
                }
                tempIndex = text.indexOf(searchList[i], start)
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

    abstract fun onFormatterPairList(item: ITEM): List<Pair<String, String>>
}
