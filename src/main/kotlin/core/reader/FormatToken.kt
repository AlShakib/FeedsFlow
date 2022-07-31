package core.reader

enum class FormatToken(val value: String) {
    TITLE("{{TITLE}}"),
    URL("{{URL}}"),
    AUTHOR_NAME("{{AUTHOR_NAME}}"),
    AUTHOR_EMAIL("{{AUTHOR_EMAIL}}"),
    AUTHOR_URL("{{AUTHOR_URL}}"),
    PUBLISHED_DATE("{{PUBLISHED_DATE}}"),
    UPDATED_DATE("{{UPDATED_DATE}}"),
    CONTENTS("{{CONTENTS}}"),
    FEED_TITLE("{{FEED_TITLE}}"),
    FEED_URL("{{FEED_URL}}");

    override fun toString(): String {
        return value;
    }
}
