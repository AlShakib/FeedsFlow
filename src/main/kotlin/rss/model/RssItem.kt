package rss.model

import core.model.Feed
import java.time.ZonedDateTime

class RssItem : Feed.Item() {
    var description: String? = null
    var author: String? = null
    var category: String? = null
    var isPermalink = false
    var publishedDateTime: ZonedDateTime? = null
    var updatedDateTime: ZonedDateTime? = null
    var rssChannel: RssChannel? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as RssItem

        if (description != other.description) return false
        if (author != other.author) return false
        if (category != other.category) return false
        if (isPermalink != other.isPermalink) return false
        if (publishedDateTime != other.publishedDateTime) return false
        if (updatedDateTime != other.updatedDateTime) return false
        if (rssChannel != other.rssChannel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + isPermalink.hashCode()
        result = 31 * result + (publishedDateTime?.hashCode() ?: 0)
        result = 31 * result + (updatedDateTime?.hashCode() ?: 0)
        result = 31 * result + (rssChannel?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RssItem(description=$description, author=$author, category=$category, isPermalink=$isPermalink, " +
                "publishedDateTime=$publishedDateTime, updatedDateTime=$updatedDateTime, rssChannel=$rssChannel)"
    }

    override fun compareTo(other: Feed.Item): Int {
        if (other is RssItem) {
            val dateTime = publishedDateTime
            val otherTime = other.publishedDateTime
            if (dateTime != null && otherTime != null) {
                return dateTime.compareTo(otherTime)
            }
        }
        return super.compareTo(other)
    }
}
