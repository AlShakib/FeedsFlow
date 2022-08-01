package extension

import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

private val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
private val patternToWrapOn = Pattern.compile(" ")

fun Date.format(): String {
    return simpleDateFormat.format(this)
}

fun Int.toPlurals(singular: String, plural: String): String {
    return if (this == 1) {
        singular
    } else {
        plural
    }
}

fun String.toURI(): URI {
    return URI.create(this)
}

fun String.chomp(): String {
    if (isEmpty()) {
        return this
    }
    if (length == 1) {
        val ch = this[0]
        return if (ch == '\r' || ch == '\n') {
            ""
        } else this
    }
    var lastIdx = length - 1
    val last = this[lastIdx]
    if (last == '\n') {
        if (this[lastIdx - 1] == '\r') {
            lastIdx--
        }
    } else if (last != '\r') {
        lastIdx++
    }
    return substring(0, lastIdx)
}

fun String.wrap(length: Int): List<String> {
    val wrappedTextList = ArrayList<String>()
    val textLength = this.length
    var offset = 0
    var matcherSize = -1
    while (offset < textLength) {
        var spaceToWrapAt = -1
        val matcher: Matcher = patternToWrapOn.matcher(
            this.substring(offset, Int.MAX_VALUE.coerceAtMost(offset + length + 1).coerceAtMost(textLength))
        )
        if (matcher.find()) {
            if (matcher.start() == 0) {
                matcherSize = matcher.end()
                if (matcherSize != 0) {
                    offset += matcher.end()
                    continue
                }
                offset += 1
            }
            spaceToWrapAt = matcher.start() + offset
        }
        if (textLength - offset <= length) {
            break
        }
        while (matcher.find()) {
            spaceToWrapAt = matcher.start() + offset
        }
        if (spaceToWrapAt >= offset) {
            wrappedTextList.add(this.substring(offset, spaceToWrapAt))
            offset = spaceToWrapAt + 1
        } else {
            if (matcherSize == 0) {
                offset--
            }
            wrappedTextList.add(this.substring(offset, length + offset))
            offset += length
            matcherSize = -1
        }
    }
    if (matcherSize == 0 && offset < textLength) {
        offset--
    }
    wrappedTextList.add(this.substring(offset))
    return wrappedTextList
}
