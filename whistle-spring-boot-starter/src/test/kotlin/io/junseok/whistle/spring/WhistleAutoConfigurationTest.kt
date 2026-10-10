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
    fun `webhooks 가 있으면 Slack 채널과 Whistle 빈이 등록되고 웹훅 이름과 timeout 이 바인딩된다`() {
        runner
            .withPropertyValues(
                "whistle.slack.webhooks.error=https://hooks.slack.com/e",
                "whistle.slack.webhooks.job-alarm=https://hooks.slack.com/j",
                "whistle.slack.timeout=5s"
            )
            .run { ctx ->
                ctx.getBean(Slack::class.java)
                ctx.getBean(Whistle::class.java)
                val props = ctx.getBean(SlackProperties::class.java)
                assertEquals(setOf("error", "job-alarm"), props.webhooks.keys)
                assertEquals(Duration.ofSeconds(5), props.timeout)
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
    fun `웹훅 설정이 없어도 호출하면 예외 없이 건너뛴다`() {
        runner.run { ctx ->
            ctx.getBean(Whistle::class.java).slack("error", "t", "b")
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
