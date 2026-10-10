package io.junseok.whistle.slack

import io.junseok.whistle.enums.Level

private const val LINK_LABEL = "자세히 보기"

/**
 * Slack mrkdwn 형식
 */
internal fun SlackMessage.toText() = listOfNotNull(
    (mentions.map { it.tag } + "${level.emoji} *${escape(title)}*").joinToString(" "),
    escape(body),
    link?.let { "<${escape(it)}|$LINK_LABEL>" }
).joinToString("\n")

private val Level.emoji
    get() = when (this) {
        Level.INFO -> "ℹ️"
        Level.WARN -> "⚠️"
        Level.ERROR -> "🚨"
    }

/*
 * Slack mrkdwn 제어 문자
 */
private fun escape(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
