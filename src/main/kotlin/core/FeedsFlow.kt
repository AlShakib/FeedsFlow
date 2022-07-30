package core

import app.App
import core.firebase.FirebaseManager
import core.model.Chat
import core.model.Feed
import core.model.SentItem
import extension.format
import extension.toPlurals
import rss.RssReader
import rss.model.RssItem

open class FeedsFlow(args: Array<String>) : App(args) {
    override fun onStart() {
        val chats = FirebaseManager.getChatList()
        chats.forEach { chat ->
            run {
                if (chat.active) {
                    println("\n[PROCESSING] ${chat.title}")
                    val feedItems = processChat(chat)
                    if (feedItems.isNotEmpty()) {
                        println("[SENDING] ${chat.title}")
                        val sentItems = sendFeedItems(feedItems)
                    }
                    println("[DONE] ${chat.title}")
                } else {
                    println("[SKIPPING] Chat \"${chat.title}\" is inactive")
                }
            }
        }
    }

    private fun processChat(chat: Chat): List<Feed.Item> {
        val feedItems = ArrayList<Feed.Item>()
        chat.feeds.forEach { feed ->
            run {
                if (feed.active) {
                    if (feed.url.isNotBlank()) {
                        println("    [PARSING] ${feed.title}")
                        try {
                            var count = 0
                            when(feed.type) {
                                Feed.Type.RSS.toString() -> {
                                    val list = parseRssFeed(feed)
                                    count = list.size
                                    feedItems.addAll(list)
                                }
                                Feed.Type.FACEBOOK.toString() -> {}
                                Feed.Type.YOUTUBE.toString() -> {}
                                else -> {}
                            }
                            println("    [DONE] $count new ${count.toPlurals("item", "items")} found for \"${feed.title}\"")
                        } catch (exception: Exception) {
                            println("    [ERROR] Can not parse feed: \"${feed.title}\": ${feed.url}")
                            exception.printStackTrace()
                        }
                    } else {
                        println("    [SKIPPING] No feed url found for \"${feed.title}\"")
                    }
                } else {
                    println("    [SKIPPING] Feed \"${feed.title}\" is inactive")
                }
            }
        }
        feedItems.sort()
        return feedItems
    }

    private fun sendFeedItems(items: List<Feed.Item>): List<SentItem> {
        val sentItems = ArrayList<SentItem>()
        items.forEach { item ->
            run {
                if (item is RssItem) {
                    println("    [SENT] ${item.publishedDateTime?.format()} -> ${item.title} (${item.url})")
                }
            }
        }
        return sentItems
    }

    private fun parseRssFeed(feed: Feed): List<RssItem> {
        val rssFeedItemList = ArrayList<RssItem>()
        val sentItemUrlSet = HashSet<String>()
        feed.sentItems.forEach { sentItem ->
            run {
                sentItemUrlSet.add(sentItem.url)
            }
        }

        val stream = RssReader.read(feed.url)
        stream.sorted().forEach { rssItem ->
            run {
                rssItem?.let {
                    if (rssItem.url.isNotBlank() && !sentItemUrlSet.contains(rssItem.url)) {
                        it.feed = feed
                        rssFeedItemList.add(it)
                    }
                }
            }
        }
        return rssFeedItemList
    }
}
