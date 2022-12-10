package core.model

data class Chat(
    val documentId: String = "",
    val chatId: String = "",
    val testChatId: String = "",
    val title: String = "",
    val disableWebPagePreview: Boolean = false,
    val disableNotification: Boolean = false,
    val protectContent: Boolean = false,
    val active: Boolean = false,
    val feeds: ArrayList<Feed> = ArrayList()
)
