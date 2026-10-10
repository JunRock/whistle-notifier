package io.junseok.whistle.common

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class WebhookClientTest {
    @Test
    fun `http(s) 절대 URL 만 허용한다`() {
        WebhookClient("https://hooks.slack.com/services/T/B/X")
        WebhookClient("HTTP://localhost:8080/")

        assertFailsWith<IllegalArgumentException> { WebhookClient("hooks.slack.com/services/T/B/X") }
        assertFailsWith<IllegalArgumentException> { WebhookClient("ftp://hooks.slack.com/x") }
        assertFailsWith<IllegalArgumentException> { WebhookClient("http:///no-host") }
        assertFailsWith<IllegalArgumentException> { WebhookClient("") }
    }

    @Test
    fun `잘못된 URL 의 예외 메시지에 URL 이 노출되지 않는다`() {
        val secret = "https://hooks.slack.com/services/T/B/SECRET TOKEN"

        val e = assertFailsWith<IllegalArgumentException> { WebhookClient(secret) }

        assertFalse(e.message!!.contains("SECRET"))
    }
}
