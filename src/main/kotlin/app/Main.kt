package app

import core.FeedsFlow

fun main(args: Array<String>) {
    val feedsFlow = FeedsFlow(args);
    feedsFlow.onStart()
}
