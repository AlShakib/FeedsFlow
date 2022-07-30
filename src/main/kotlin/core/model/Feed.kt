package core.model

import java.time.ZonedDateTime

data class Feed(val url: String = "", val title: String = "", val type: String = "", val format: String = "",
                val parseMode: ParseMode = ParseMode.HTML, val disableWebPagePreview: Boolean = false,
                val disableNotification: Boolean = false, val protectContent: Boolean = false,
                val active: Boolean = false, val sentItems: ArrayList<SentItem> = ArrayList()) : Comparable<Feed> {

    override fun compareTo(other: Feed): Int {
        return if (title.isEmpty() || other.title.isEmpty()) {
            -1
        } else title.compareTo(other.title)
    }

    enum class Type(private val value: String) {
        RSS("rss"),
        YOUTUBE("facebook"),
        FACEBOOK("youtube");

        override fun toString(): String {
            return value;
        }
    }

    enum class ParseMode(private val value: String) {
        HTML("HTML"),
        MARKDOWN("MarkdownV2");

        override fun toString(): String {
            return value;
        }
    }

    open class Item : Comparable<Item> {
        var id: String? = null
        var title: String? = null
        var url: String? = null
        var publishedDateTime: ZonedDateTime? = null
        var feed: Feed? = null

        constructor() {}

        constructor(item: Item) {
            id = item.id
            title = item.title
            url = item.url
            publishedDateTime = item.publishedDateTime
            feed = item.feed
        }

        override fun compareTo(other: Item): Int {
            return if (publishedDateTime == null || other.publishedDateTime == null) {
                -1
            } else publishedDateTime!!.compareTo(other.publishedDateTime)
        }

        override fun toString(): String {
            return "Item(id=$id, title=$title, url=$url, publishedDateTime=$publishedDateTime, feed=$feed)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Item

            if (id != other.id) return false
            if (title != other.title) return false
            if (url != other.url) return false
            if (publishedDateTime != other.publishedDateTime) return false
            if (feed != other.feed) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id?.hashCode() ?: 0
            result = 31 * result + (title?.hashCode() ?: 0)
            result = 31 * result + (url?.hashCode() ?: 0)
            result = 31 * result + (publishedDateTime?.hashCode() ?: 0)
            result = 31 * result + (feed?.hashCode() ?: 0)
            return result
        }
    }
}
