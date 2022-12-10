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
    private var isReleaseMode: Boolean = false
    private var testChatId: String = ""
    private val maximumTextLength = 4096
    private val maximumMessagesPerChat = 20

    init {
        var release = false
        try {
            release = System.getenv("RELEASE_MODE").toBoolean()
        } catch (_: Exception) {
        }
        if (!release) {
            println("==> Feeds flow is running as test mode. Please set environment variable RELEASE_MODE=True to run as release mode.")
            try {
                testChatId = System.getenv("TEST_CHAT_ID")
            } catch (_: Exception) {
            }
        }
        FirebaseManager.init(release)
        isReleaseMode = release
    }

    override fun onStart() {
        val chats = FirebaseManager.getInstance().getChatList()
        chats.forEach { chat ->
            if (chat.active) {
                println("\n[START] ${chat.title}")
                val feedItems = processChat(chat)
                if (feedItems.isNotEmpty()) {
                    println("[SENDING] ${chat.title}")
                    if (sendFeedItems(chat, feedItems)) {
                        FirebaseManager.getInstance().saveChat(chat)
                    }
                }
                println("[DONE] ${chat.title}")
            } else {
                println("[SKIPPING] Chat \"${chat.title}\" is inactive")
            }
        }
    }

    private fun processChat(chat: Chat): List<Feed.Item> {
        val feedItems = ArrayList<Feed.Item>()
        var feedCount = 0
        chat.feeds.forEach { feed ->
            if (feed.active) {
                if (feed.url.isNotBlank()) {
                    try {
                        ++feedCount
                        var newItemCount = 0
                        when (feed.type) {
                            Feed.Type.RSS -> {
                                if (feed.format.isBlank()) {
                                    feed.parseMode = Feed.ParseMode.HTML
                                    feed.format = "<a href=\"${FormatToken.URL.getToken()}\">${FormatToken.TITLE.getToken()}</a>"
                                }
                                val list = RssReader.read(feed)
                                newItemCount = list.size
                                feedItems.addAll(list)
                            }

                            Feed.Type.FACEBOOK -> {}
                            Feed.Type.YOUTUBE -> {}
                            else -> {}
                        }
                        println(
                            "    [${
                                feedCount.toString().padStart(2, '0')
                            }] ${feed.title}: $newItemCount new ${newItemCount.toPlurals("item", "items")} found"
                        )
                    } catch (exception: Exception) {
                        println("    [x] Can not parse feed: \"${feed.title}\": ${feed.url}")
                    }
                } else {
                    println("    [x] No feed url found for \"${feed.title}\"")
                }
            } else {
                println("    [x] Feed \"${feed.title}\" is inactive")
            }
        }
        feedItems.sort()
        return feedItems
    }

    private fun sendFeedItems(chat: Chat, items: List<Feed.Item>): Boolean {
        val chatId = if (isReleaseMode) {
            val id = chat.chatId
            if (id.isBlank()) {
                println("[x] Can not send to \"${chat.title}\". No valid chat id is found.")
                return false
            }
            id
        } else {
            val id = testChatId.ifBlank { chat.chatId }
            if (id.isBlank()) {
                println(
                    "[x] Can not send to \"${chat.title}\". No valid chat id is found. " +
                            "Please set environment variable TEST_CHAT_ID with a valid chat id."
                )
                return false
            }
            id
        }
        var count = 0
        var sentItemCount = 0
        var isSkippedStarted = false
        items.forEach { item ->
            val disableWebPagePreview = if (chat.disableWebPagePreview) {
                true
            } else item.feed.disableWebPagePreview
            val disableNotification = if (chat.disableNotification) {
                true
            } else item.feed.disableNotification
            val protectContent = if (chat.protectContent) {
                true
            } else item.feed.protectContent
            val parseMode = item.feed.parseMode
            val text = item.formattedText
            if (text.isNotBlank()) {
                val textList = text.wrap(maximumTextLength)
                if (textList.size <= maximumMessagesPerChat) {
                    sentItemCount += textList.size
                    if (!isSkippedStarted && sentItemCount <= maximumMessagesPerChat) {
                        ++count
                        var isOk = true
                        val validParseMode = if (textList.size <= 1 && parseMode != Feed.ParseMode.NONE) parseMode.toString() else ""
                        textList.forEach { wrappedText ->
                            if (isOk) {
                                val sendMessage = SendMessage(
                                    chatId = chatId, text = wrappedText, parseMode = validParseMode,
                                    disableWebPagePreview = disableWebPagePreview, disableNotification = disableNotification,
                                    protectContent = protectContent
                                )
                                val response = TelegramBot.execute(sendMessage)
                                isOk = response.ok
                                if (!response.ok) {
                                    println("    [x] ${response.errorCode} -> \"${response.description}\" -> \"${item.title}\"")
                                }
                            }
                        }
                        if (isOk) {
                            item.feed.sentItems.add(SentItem(url = item.url, title = item.title))
                            println("    [${count.toString().padStart(2, '0')}] ${item.feed.title}: \"${item.title}\"")
                        }
                    } else {
                        isSkippedStarted = true
                        println("    [x] Skipped -> \"${item.title}\"")
                    }
                } else {
                    println("    [x] ${item.feed.title}: Message too large! -> \"${item.title}\"")
                }
            } else {
                println("    [x] Nothing to send!")
            }
        }
        return count > 0
    }
}
