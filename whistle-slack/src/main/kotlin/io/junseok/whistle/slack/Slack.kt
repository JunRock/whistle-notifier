package io.junseok.whistle.slack

import io.junseok.whistle.Channel
import io.junseok.whistle.common.WebhookClient
import io.junseok.whistle.common.toJsonString
import java.time.Duration
import java.util.concurrent.CompletableFuture

class Slack(
    webhookUrl: String,
    timeout: Duration = WebhookClient.DEFAULT_TIMEOUT
) : Channel<SlackMessage> {
    private val webhook = WebhookClient(webhookUrl, timeout)

    override val messageType = SlackMessage::class

    override fun send(message: SlackMessage): CompletableFuture<*> =
        webhook.post("""{"$TEXT_FIELD":${message.toText().toJsonString()}}""")

    private companion object {
        const val TEXT_FIELD = "text"
    }
}
