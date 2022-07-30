package extension

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a").withZone(ZoneOffset.UTC)

fun ZonedDateTime.toDate(): Date {
    return Date.from(this.toInstant())
}

fun ZonedDateTime.format(): String {
    return this.format(dateTimeFormatter)
}

fun Int.toPlurals(singular: String, plural: String): String {
    return if (this == 1) {
        singular
    } else {
        plural
    }
}

fun String.chomp(): String {
    if (isEmpty()) {
        return this;
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
