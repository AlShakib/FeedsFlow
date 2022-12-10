package core.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import core.model.Chat
import java.io.IOException
import java.util.concurrent.ExecutionException

class FirebaseManager private constructor(private val releaseMode: Boolean) {
    companion object {
        private const val COLLECTION_CHATS = "feeds-flow"
        private const val COLLECTION_CHATS_TEST = "feeds-flow-test"

        private var instance: FirebaseManager? = null

        fun init(release: Boolean = false) {
            synchronized(FirebaseManager::class) {
                val manager = FirebaseManager(release)
                instance = manager
                manager
            }
        }

        fun getInstance(): FirebaseManager {
            return instance ?: throw IllegalStateException("FirebaseManager is not initialized. Please call FirebaseManager.init() first.")
        }
    }

    private val firestore: Firestore

    init {
        try {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
            FirebaseApp.initializeApp(options)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        firestore = FirestoreClient.getFirestore()
    }

    private fun getCollection(): String {
        return if (releaseMode) COLLECTION_CHATS else COLLECTION_CHATS_TEST
    }

    fun getChatList(): List<Chat> {
        val chatList = ArrayList<Chat>()
        try {
            val documents = firestore.collection(getCollection()).get().get().documents
            for (snapshot in documents) {
                chatList.add(snapshot.toObject(Chat::class.java))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return chatList
    }

    fun getChat(id: String): Chat? {
        try {
            val snapshot = firestore.collection(getCollection()).document(id).get().get()
            if (snapshot != null) {
                return snapshot.toObject(Chat::class.java)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return null
    }

    fun saveChat(chat: Chat) {
        try {
            chat.feeds.forEach { feed ->
                feed.sentItems.sort()
                if (feed.sentItems.size > feed.cachedSentItemSize) {
                    val newList = feed.sentItems.dropLast(feed.sentItems.size - feed.cachedSentItemSize)
                    feed.sentItems.clear()
                    feed.sentItems.addAll(newList.sorted())
                }
            }
            chat.feeds.sort()
            firestore.collection(getCollection()).document(chat.documentId).set(chat).get()
        } catch (exception: InterruptedException) {
            exception.printStackTrace()
        } catch (exception: ExecutionException) {
            exception.printStackTrace()
        }
    }
}
