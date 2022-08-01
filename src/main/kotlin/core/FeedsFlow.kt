package core

import app.App
import core.firebase.FirebaseManager
import core.model.Chat
import core.model.Feed
import core.model.SentItem
import core.reader.FormatToken
import core.reader.rss.RssReader
import core.telegram.TelegramBot
import core.telegram.method.SendMessage
import extension.toPlurals
import extension.wrap

open class FeedsFlow(args: Array<String>) : App(args) {
    private val maximumTextLength = 4096

    override fun onStart() {
        val chats = FirebaseManager.getChatList()
        chats.forEach { chat ->
            run {
                if (chat.active) {
                    println("\n[START] ${chat.title}")
                    val feedItems = processChat(chat)
                    if (feedItems.isNotEmpty()) {
                        println("[SENDING] ${chat.title}")
                        if (sendFeedItems(chat, feedItems)) {
                            FirebaseManager.saveChat(chat)
                        }
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
        var feedCount = 0
        chat.feeds.forEach { feed ->
            run {
                if (feed.active) {
                    if (feed.url.isNotBlank()) {
                        try {
                            ++feedCount
                            var newItemCount = 0
                            when(feed.type) {
                                Feed.Type.RSS.toString() -> {
                                    if (feed.format.isBlank()) {
                                        feed.parseMode = Feed.ParseMode.HTML.toString()
                                        feed.format = "<a href=\"${FormatToken.URL.value}\">${FormatToken.TITLE.value}</a>"
                                    }
                                    val list = RssReader.read(feed)
                                    newItemCount = list.size
                                    feedItems.addAll(list)
                                }
                                Feed.Type.FACEBOOK.toString() -> {}
                                Feed.Type.YOUTUBE.toString() -> {}
                                else -> {}
                            }
                            println("    [${feedCount.toString().padStart(2, '0')}] ${feed.title}: $newItemCount new ${newItemCount.toPlurals("item", "items")} found")
                        } catch (exception: Exception) {
                            println("    [x] Can not parse feed: \"${feed.title}\": ${feed.url}")
                            exception.printStackTrace()
                        }
                    } else {
                        println("    [x] No feed url found for \"${feed.title}\"")
                    }
                } else {
                    println("    [x] Feed \"${feed.title}\" is inactive")
                }
            }
        }
        feedItems.sort()
        return feedItems
    }

    private fun sendFeedItems(chat: Chat, items: List<Feed.Item>): Boolean {
        var count = 0
        items.forEach { item ->
            run {
                val disableWebPagePreview = if (chat.disableWebPagePreview) { true } else item.feed.disableWebPagePreview
                val disableNotification = if (chat.disableNotification) { true } else item.feed.disableNotification
                val protectContent = if (chat.protectContent) { true } else item.feed.protectContent
                val parseMode = item.feed.parseMode
                val text = item.formattedText
                if (text.isNotBlank()) {
                    val textList = text.wrap(maximumTextLength)
                    textList.forEach { wrappedText ->
                        run {
                            val sendMessage = SendMessage(chatId = chat.chatId, text = wrappedText, parseMode = parseMode,
                                disableWebPagePreview = disableWebPagePreview, disableNotification = disableNotification,
                                protectContent = protectContent)
                            val response = TelegramBot.execute(sendMessage)
                            if (response.ok) {
                                ++count
                                item.feed.sentItems.add(SentItem(url = item.url, title =  item.title))
                                println("    [${count.toString().padStart(2, '0')}] ${item.feed.title}: \"${item.title}\"")
                            } else {
                                println("    [x] ${response.errorCode} -> \"${response.description}\"")
                            }
                        }
                    }
                } else {
                    println("    [x] Nothing to send!")
                }
            }
        }
        return count > 0
    }
}
