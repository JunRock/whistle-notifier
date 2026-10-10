package io.junseok.whistle.slack

import io.junseok.whistle.Whistle
import io.junseok.whistle.enums.Level

fun Whistle.slack(
    webhook: String,
    title: String,
    body: String,
    level: Level = Level.ERROR,
    mentions: List<Mention> = emptyList(),
    link: String? = null,
    fields: Map<String, String> = emptyMap(),
    exception: Throwable? = null
) = fire(SlackMessage(webhook, title, body, level, mentions, link, fields, exception))
