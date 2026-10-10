package io.junseok.whistle.slack

/**
 * User.id 는 Slack 멤버 ID (U로 시작), 이름 불가
 */
sealed class Mention(internal val tag: String) {
    data object Channel : Mention("<!channel>")

    data object Here : Mention("<!here>")

    data class User(val id: String) : Mention("<@$id>")
}
