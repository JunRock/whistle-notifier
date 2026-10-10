package io.junseok.whistle.slack

import io.junseok.whistle.enums.Level

data class SlackMessage(
    val title: String,
    val body: String,
    val level: Level = Level.ERROR,
    val mentions: List<Mention> = emptyList(),
    val link: String? = null,
    val fields: Map<String, String> = emptyMap(),
    val exception: Throwable? = null
)
