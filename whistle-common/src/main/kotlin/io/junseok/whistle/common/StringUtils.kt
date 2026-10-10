package io.junseok.whistle.common

fun String.toJsonString(): String = buildString {
    append('"')
    for (c in this@toJsonString) {
        when {
            c == '"' -> append("\\\"")
            c == '\\' -> append("\\\\")
            c == '\n' -> append("\\n")
            c < ' ' -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
    }
    append('"')
}
