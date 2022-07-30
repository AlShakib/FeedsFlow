package core.model

import extension.format
import java.time.ZonedDateTime

data class Feed(val url: String = "", val title: String = "", val type: String = Type.NONE.toString(), val format: String = "",
                val parseMode: String = ParseMode.NONE.toString(), val disableWebPagePreview: Boolean = false,
                val disableNotification: Boolean = false, val protectContent: Boolean = false,
                val active: Boolean = false, val sentItems: ArrayList<SentItem> = ArrayList()) : Comparable<Feed> {

    override fun compareTo(other: Feed): Int {
        return if (title.isEmpty() || other.title.isEmpty()) {
            -1
        } else title.compareTo(other.title)
    }

    enum class Type(private val value: String) {
        NONE(""),
        RSS("rss"),
        YOUTUBE("facebook"),
        FACEBOOK("youtube");

        override fun toString(): String {
            return value;
        }
    }

    enum class ParseMode(private val value: String) {
        NONE(""),
        HTML("HTML"),
        MARKDOWN("MarkdownV2");

        override fun toString(): String {
            return value;
        }
    }

    open class Item : Comparable<Item> {
        val parseDateTime: ZonedDateTime = ZonedDateTime.now()
        var id: String = ""
        var title: String = ""
        var url: String = ""
        var feed: Feed = Feed()

        override fun compareTo(other: Item): Int {
            return other.parseDateTime.compareTo(parseDateTime)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Item

            if (parseDateTime != other.parseDateTime) return false
            if (id != other.id) return false
            if (title != other.title) return false
            if (url != other.url) return false
            if (feed != other.feed) return false

            return true
        }

        override fun hashCode(): Int {
            var result = parseDateTime.hashCode()
            result = 31 * result + id.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + url.hashCode()
            result = 31 * result + feed.hashCode()
            return result
        }

        override fun toString(): String {
            return "Item(parseDateTime=${parseDateTime.format()}, id='$id', title='$title', url='$url', feed=$feed)"
        }
    }
}
