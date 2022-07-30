package rss.formatter

import core.formatter.Formatter
import rss.model.RssItem

object RssFormatter: Formatter<RssItem>() {

    override fun onFormatterPairList(item: RssItem): List<Pair<String, String>> {
        val list = ArrayList<Pair<String, String>>()
        list.add(Pair(RssFormatToken.ITEM_AUTHOR.value, parseValue(item.author)))
        list.add(Pair(RssFormatToken.ITEM_CATEGORY.value, parseValue(item.category)))
        list.add(Pair(RssFormatToken.ITEM_DESCRIPTION.value, parseValue(item.description)))
        list.add(Pair(RssFormatToken.ITEM_PUBLISHED_DATE.value, parseValue(item.publishedDateTime)))
        list.add(Pair(RssFormatToken.ITEM_UPDATED_DATE.value, parseValue(item.updatedDateTime)))
        list.add(Pair(RssFormatToken.ITEM_URL.value, parseValue(item.url)))
        list.add(Pair(RssFormatToken.ITEM_TITLE.value, parseValue(item.title)))
        list.add(Pair(RssFormatToken.CHANNEL_CATEGORY.value, parseValue(item.rssChannel?.category)))
        list.add(Pair(RssFormatToken.CHANNEL_COPYRIGHT.value, parseValue(item.rssChannel?.copyright)))
        list.add(Pair(RssFormatToken.CHANNEL_DESCRIPTION.value, parseValue(item.rssChannel?.description)))
        list.add(Pair(RssFormatToken.CHANNEL_GENERATOR.value, parseValue(item.rssChannel?.generator)))
        list.add(Pair(RssFormatToken.CHANNEL_LANGUAGE.value, parseValue(item.rssChannel?.language)))
        list.add(Pair(RssFormatToken.CHANNEL_LAST_BUILD_DATE.value, parseValue(item.rssChannel?.lastBuildDateTime)))
        list.add(Pair(RssFormatToken.CHANNEL_MANAGING_EDITOR.value, parseValue(item.rssChannel?.managingEditor)))
        list.add(Pair(RssFormatToken.CHANNEL_PUBLISHED_DATE.value, parseValue(item.rssChannel?.publishedDateTime)))
        list.add(Pair(RssFormatToken.CHANNEL_TITLE.value, parseValue(item.rssChannel?.title)))
        list.add(Pair(RssFormatToken.CHANNEL_TTL.value, parseValue(item.rssChannel?.ttl)))
        list.add(Pair(RssFormatToken.CHANNEL_URL.value, parseValue(item.rssChannel?.url)))
        list.add(Pair(RssFormatToken.CHANNEL_WEB_MASTER.value, parseValue(item.rssChannel?.webMaster)))
        list.add(Pair(RssFormatToken.CHANNEL_IMAGE_DESCRIPTION.value, parseValue(item.rssChannel?.rssImage?.description)))
        list.add(Pair(RssFormatToken.CHANNEL_IMAGE_HEIGHT.value, parseValue(item.rssChannel?.rssImage?.height)))
        list.add(Pair(RssFormatToken.CHANNEL_IMAGE_LINK.value, parseValue(item.rssChannel?.rssImage?.link)))
        list.add(Pair(RssFormatToken.CHANNEL_IMAGE_TITLE.value, parseValue(item.rssChannel?.rssImage?.title)))
        list.add(Pair(RssFormatToken.CHANNEL_IMAGE_URL.value, parseValue(item.rssChannel?.rssImage?.url)))
        list.add(Pair(RssFormatToken.CHANNEL_IMAGE_WIDTH.value, parseValue(item.rssChannel?.rssImage?.width)))
        return list
    }
}
