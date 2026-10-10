package io.junseok.whistle.slack

import io.junseok.whistle.Channel
import io.junseok.whistle.common.WebhookClient
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * 이름별 웹훅 설정
 */
class Slack(
    webhookUrls: Map<String, String>,
    timeout: Duration = WebhookClient.DEFAULT_TIMEOUT
) : Channel<SlackMessage> {
    private val webhooks = webhookUrls.mapValues { (_, url) -> WebhookClient(url, timeout) }

    override val messageType = SlackMessage::class

    override fun send(message: SlackMessage): CompletableFuture<*> {
        val webhook = webhooks[message.webhook]
            ?: return CompletableFuture.failedFuture<Unit>(IllegalArgumentException("$UNKNOWN_WEBHOOK: ${message.webhook}"))
        return webhook.post(message.toPayload())
    }

    private companion object {
        const val UNKNOWN_WEBHOOK = "등록되지 않은 Slack 웹훅"
    }
}
