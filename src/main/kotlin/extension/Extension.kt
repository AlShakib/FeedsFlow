package extension

import java.time.ZonedDateTime
import java.util.*

fun ZonedDateTime.toDate(): Date {
    return Date.from(this.toInstant())
}

/**
 * <p>Removes one newline from end of a String if it's there,
 * otherwise leave it alone.  A newline is &quot;{@code \n}&quot;,
 * &quot;{@code \r}&quot;, or &quot;{@code \r\n}&quot;.</p>
 *
 *
 * <pre>
 * StringUtils.chomp(null)          = null
 * StringUtils.chomp("")            = ""
 * StringUtils.chomp("abc \r")      = "abc "
 * StringUtils.chomp("abc\n")       = "abc"
 * StringUtils.chomp("abc\r\n")     = "abc"
 * StringUtils.chomp("abc\r\n\r\n") = "abc\r\n"
 * StringUtils.chomp("abc\n\r")     = "abc\n"
 * StringUtils.chomp("abc\n\rabc")  = "abc\n\rabc"
 * StringUtils.chomp("\r")          = ""
 * StringUtils.chomp("\n")          = ""
 * StringUtils.chomp("\r\n")        = ""
 * </pre>
 *
 * @return String without newline, {@code null} if null String input
 */
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
