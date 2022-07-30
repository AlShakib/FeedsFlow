package core.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import core.model.Chat
import java.io.IOException
import java.util.concurrent.ExecutionException

object FirebaseManager {
    private const val COLLECTION_CHATS = "feeds-flow"
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

    fun getChatList(): List<Chat> {
        val chatList = ArrayList<Chat>()
        try {
            val documents = firestore.collection(COLLECTION_CHATS).get().get().documents
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
            val snapshot = firestore.collection(COLLECTION_CHATS).document(id).get().get()
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
                run {
                    feed.sentItems.sorted()
                }
            }
            chat.feeds.sorted()
            firestore.collection(COLLECTION_CHATS).document(chat.documentId).set(chat).get()
        } catch (exception: InterruptedException) {
            exception.printStackTrace()
        } catch (exception: ExecutionException) {
            exception.printStackTrace()
        }
    }
}
