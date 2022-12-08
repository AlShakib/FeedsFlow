package core.reader

enum class FormatToken {
    TITLE,
    URL,
    AUTHOR_NAME,
    AUTHOR_EMAIL,
    AUTHOR_URL,
    PUBLISHED_DATE,
    UPDATED_DATE,
    CONTENTS,
    FEED_TITLE,
    FEED_URL;

    fun getToken(): String {
        return "{{$name}}"
    }
}
