package io.junseok.whistle.spring

import io.junseok.whistle.common.WebhookClient
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * kotlin-reflect 없이 바인딩되도록 JavaBean
 */
@ConfigurationProperties(SlackProperties.PREFIX)
class SlackProperties {
    lateinit var webhookUrl: String
    var timeout: Duration = WebhookClient.DEFAULT_TIMEOUT

    companion object {
        const val PREFIX = "whistle.slack"
        const val WEBHOOK_URL = "webhook-url"
    }
}
