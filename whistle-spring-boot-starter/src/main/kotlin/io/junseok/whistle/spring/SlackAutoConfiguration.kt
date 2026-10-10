package io.junseok.whistle.spring

import io.junseok.whistle.slack.Slack
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(SlackProperties::class)
class SlackAutoConfiguration {
    @Bean
    fun whistleSlack(props: SlackProperties) = Slack(props.webhooks, props.timeout)
}
