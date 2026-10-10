package io.junseok.whistle.spring

import io.junseok.whistle.Channel
import io.junseok.whistle.Whistle
import io.junseok.whistle.slack.Slack
import io.junseok.whistle.slack.slack
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WhistleAutoConfigurationTest {
    private class StubChannel : Channel<String> {
        val sent = mutableListOf<String>()

        override val messageType = String::class

        override fun send(message: String): CompletableFuture<*> {
            sent += message
            return CompletableFuture.completedFuture(Unit)
        }
    }

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(WhistleAutoConfiguration::class.java, SlackAutoConfiguration::class.java))

    @Test
    fun `webhook-url 이 있으면 Slack 채널과 Whistle 빈이 등록되고 timeout 도 바인딩된다`() {
        runner
            .withPropertyValues("whistle.slack.webhook-url=https://hooks.slack.com/x", "whistle.slack.timeout=5s")
            .run { ctx ->
                ctx.getBean(Slack::class.java)
                ctx.getBean(Whistle::class.java)
                assertEquals(Duration.ofSeconds(5), ctx.getBean(SlackProperties::class.java).timeout)
            }
    }

    @Test
    fun `등록된 Channel 빈이 Whistle 에 실제로 연결된다`() {
        val stub = StubChannel()

        runner.withBean("stub", Channel::class.java, Supplier { stub }).run { ctx ->
            ctx.getBean(Whistle::class.java).fire("m")

            assertEquals(listOf("m"), stub.sent)
        }
    }

    @Test
    fun `설정이 없으면 Slack 빈 없이 Whistle 만 등록되고 호출해도 예외가 없다`() {
        runner.run { ctx ->
            assertEquals(0, ctx.getBeansOfType(Slack::class.java).size)
            ctx.getBean(Whistle::class.java).slack("t", "b")
        }
    }

    @Test
    fun `사용자가 Whistle 빈을 정의하면 자동 설정은 물러난다`() {
        runner
            .withBean("custom", Whistle::class.java, Supplier { Whistle(emptyList()) })
            .run { ctx ->
                assertEquals(1, ctx.getBeansOfType(Whistle::class.java).size)
                assertTrue(ctx.containsBean("custom"))
            }
    }
}
