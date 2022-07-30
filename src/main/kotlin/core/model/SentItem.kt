package core.model

import java.util.*

data class SentItem(val url: String = "", val title: String = "", val sentDate: Date = Date()) : Comparable<SentItem> {

    override fun compareTo(other: SentItem): Int {
        return sentDate.compareTo(other.sentDate)
    }
}
