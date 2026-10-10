package io.junseok.whistle.slack

import io.junseok.whistle.Whistle
import io.junseok.whistle.enums.Level

fun Whistle.slack(
    title: String,
    body: String,
    level: Level = Level.ERROR,
    mentions: List<Mention> = emptyList(),
    link: String? = null
) = fire(SlackMessage(title, body, level, mentions, link))
