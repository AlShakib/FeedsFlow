package core.model

import java.util.*

data class SentItem(
    val sentDate: Date = Date(),
    val url: String = "<Unknown URL>",
    val title: String = "<Unknown Title>"
) :
    Comparable<SentItem> {

    override fun compareTo(other: SentItem): Int {
        return other.sentDate.compareTo(sentDate)
    }
}
