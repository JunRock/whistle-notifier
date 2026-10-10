package io.junseok.whistle.spring

import io.junseok.whistle.Channel
import io.junseok.whistle.Whistle
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class WhistleAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun whistle(channels: ObjectProvider<Channel<*>>) = Whistle(channels.orderedStream().toList())
}
