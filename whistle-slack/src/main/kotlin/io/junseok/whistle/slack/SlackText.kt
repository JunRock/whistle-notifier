package io.junseok.whistle.slack

import io.junseok.whistle.common.toJsonString
import io.junseok.whistle.enums.Level

private const val LINK_LABEL = "자세히 보기"
private const val CODE_FENCE = "```"

/*
 * 원인과 발생 위치는 앞부분에 모여 있고 전체는 로그에 남음, 메시지 길이 제한 회피
 */
internal const val MAX_STACK_LINES = 20

private const val TEXT = "text"
private const val ATTACHMENTS = "attachments"
private const val COLOR = "color"
private const val FIELDS = "fields"
private const val FIELD_TITLE = "title"
private const val FIELD_VALUE = "value"
private const val FIELD_SHORT = "short"
private const val MRKDWN_IN = "mrkdwn_in"

private const val COLOR_INFO = "#36C5F0"
private const val COLOR_WARN = "#ECB22E"
private const val COLOR_ERROR = "#E01E5A"
private const val COLOR_SUCCESS = "#2EB67D"

/**
 * 멘션과 제목은 알림 미리보기와 멘션 알림을 위해 최상위 text 에 둠
 */
internal fun SlackMessage.toPayload(): String = obj(
    TEXT to toHeader().toJsonString(),
    ATTACHMENTS to arr(
        listOf(
            obj(
                COLOR to level.color.toJsonString(),
                TEXT to toContent().toJsonString(),
                FIELDS to arr(fields.map { (k, v) -> field(k, v) }),
                MRKDWN_IN to arr(listOf(TEXT.toJsonString()))
            )
        )
    )
)

/**
 * Slack mrkdwn 형식
 */
internal fun SlackMessage.toHeader() = (mentions.map { it.tag } + "${level.emoji} *${escape(title)}*").joinToString(" ")

internal fun SlackMessage.toContent() = listOfNotNull(
    escape(body),
    exception?.let { stackBlock(it) },
    link?.let { "<${escape(it)}|$LINK_LABEL>" }
).joinToString("\n")

private fun stackBlock(e: Throwable): String {
    val lines = e.stackTraceToString().trimEnd().lines()
    val kept = lines.take(MAX_STACK_LINES)
    val omitted = lines.size - kept.size
    val tail = if (omitted > 0) listOf("... ${omitted}줄 생략") else emptyList()
    return (listOf(CODE_FENCE) + kept.map(::escape) + tail + CODE_FENCE).joinToString("\n")
}

private fun field(title: String, value: String) = obj(
    FIELD_TITLE to escape(title).toJsonString(),
    FIELD_VALUE to escape(value).toJsonString(),
    FIELD_SHORT to true.toString()
)

/*
 * 값은 이미 JSON 으로 직렬화된 문자열
 */
private fun obj(vararg entries: Pair<String, String>) =
    entries.joinToString(",", "{", "}") { (k, v) -> "${k.toJsonString()}:$v" }

private fun arr(items: List<String>) = items.joinToString(",", "[", "]")

private val Level.emoji
    get() = when (this) {
        Level.INFO -> "ℹ️"
        Level.WARN -> "⚠️"
        Level.ERROR -> "🚨"
        Level.SUCCESS -> "✅"
    }

private val Level.color
    get() = when (this) {
        Level.INFO -> COLOR_INFO
        Level.WARN -> COLOR_WARN
        Level.ERROR -> COLOR_ERROR
        Level.SUCCESS -> COLOR_SUCCESS
    }

/*
 * Slack mrkdwn 제어 문자
 */
private fun escape(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
