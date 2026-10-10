package io.junseok.whistle.slack

import io.junseok.whistle.enums.Level

data class SlackMessage(
    val title: String,
    val body: String,
    val level: Level,
    val mentions: List<Mention>,
    val link: String?,
    val fields: Map<String, String> = emptyMap(),
    val exception: Throwable? = null
)
