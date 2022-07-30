package core

import app.App
import core.firebase.FirebaseManager
import core.model.Chat

open class FeedsFlow(args: Array<String>) : App(args) {
    override fun onStart() {
        val chats = FirebaseManager.getChatList()
        chats.forEach { chat ->
            run {
                if (chat.active) {
                    println("\n[RUNNING] ${chat.title}")
                    processChat(chat)
                    println("[DONE] ${chat.title}")
                } else {
                    println("[SKIPPING] Chat \" ${chat.title} \" is inactive")
                }
            }
        }
    }

    private fun processChat(chat: Chat) {
        println(chat.feeds)
    }
}
