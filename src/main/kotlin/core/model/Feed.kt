package core.model

import java.util.*

data class Feed(var url: String = "", var title: String = "", var type: String = Type.NONE.toString(), var format: String = "",
                var parseMode: String = ParseMode.NONE.toString(), var disableWebPagePreview: Boolean = false,
                var disableNotification: Boolean = false, var protectContent: Boolean = false,
                var active: Boolean = false, var cachedSentItemSize: Int = 64, var sentItems: ArrayList<SentItem> = ArrayList()) : Comparable<Feed> {

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

    data class Item(val parseDate: Date, val feed: Feed, val id: String, val url: String, val title: String, val formattedText: String,
                    val publishedDate: Date?) : Comparable<Item> {

        override fun compareTo(other: Item): Int {
            if (publishedDate == null || other.publishedDate == null) {
                return -1
            }
            return publishedDate.compareTo(other.publishedDate)
        }
    }
}
