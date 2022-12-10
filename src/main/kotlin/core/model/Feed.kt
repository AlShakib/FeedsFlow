package core.model

import java.util.*

data class Feed(
    var url: String = "",
    var title: String = "",
    var type: Type = Type.NONE,
    var format: String = "",
    var parseMode: ParseMode = ParseMode.NONE,
    var disableWebPagePreview: Boolean = false,
    var disableNotification: Boolean = false,
    var protectContent: Boolean = false,
    var active: Boolean = false,
    var cachedSentItemSize: Int = 100,
    var sentItems: ArrayList<SentItem> = ArrayList()
) : Comparable<Feed> {

    override fun compareTo(other: Feed): Int {
        return title.compareTo(other.title)
    }

    enum class Type {
        NONE,
        RSS,
        YOUTUBE,
        FACEBOOK
    }

    enum class ParseMode {
        NONE,
        HTML,
        MARKDOWN
    }

    data class Item(
        val parseDate: Date, val feed: Feed, val id: String, val url: String, val title: String, val formattedText: String,
        val publishedDate: Date?
    ) : Comparable<Item> {

        override fun compareTo(other: Item): Int {
            if (publishedDate == null || other.publishedDate == null) {
                return -1
            }
            return publishedDate.compareTo(other.publishedDate)
        }
    }
}
