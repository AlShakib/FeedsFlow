package rss.model

import java.time.ZonedDateTime

data class RssChannel(var title: String? = null, var description: String? = null, var category: String? = null,
                      var language: String? = null, var url: String? = null, var copyright: String? = null,
                      var generator: String? = null, var ttl: String? = null, var managingEditor: String? = null,
                      var webMaster: String? = null, var publishedDateTime: ZonedDateTime? = null,
                      var lastBuildDateTime: ZonedDateTime? = null, var rssImage: RssImage? = null)
