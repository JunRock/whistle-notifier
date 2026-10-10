package io.junseok.whistle.slack

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.junseok.whistle.Whistle
import io.junseok.whistle.enums.Level
import java.net.InetSocketAddress
import java.net.http.HttpTimeoutException
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SlackTest {
    private val message = SlackMessage(CH, "t", "b", Level.ERROR)

    private fun withServer(handler: (HttpExchange) -> Unit, block: (url: String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/") { ex -> ex.use(handler) }
            start()
        }
        try {
            block("http://localhost:${server.address.port}/")
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `웹훅으로 JSON을 POST 한다`() {
        val method = CompletableFuture<String>()
        val contentType = CompletableFuture<String>()
        val body = CompletableFuture<String>()
        withServer({ ex ->
            method.complete(ex.requestMethod)
            contentType.complete(ex.requestHeaders.getFirst("Content-Type"))
            body.complete(ex.requestBody.readAllBytes().decodeToString())
            ex.sendResponseHeaders(200, -1)
        }) { url ->
            Whistle(listOf(Slack(mapOf(CH to url)))).slack(CH, "t", "b\"q")

            assertEquals("POST", method.get(3, SECONDS))
            assertEquals("application/json; charset=utf-8", contentType.get(3, SECONDS))
            assertEquals(
                """{"text":"🚨 *t*","attachments":[{"color":"#E01E5A","text":"b\"q","fields":[],"mrkdwn_in":["text"]}]}""",
                body.get(3, SECONDS)
            )
        }
    }

    @Test
    fun `2xx 가 아닌 응답은 실패로 처리하되 호출부로 전파되지 않고 에러 바디는 잘라서 담는다`() {
        val longBody = "x".repeat(1000).toByteArray()
        withServer({ ex ->
            ex.sendResponseHeaders(500, longBody.size.toLong())
            ex.responseBody.write(longBody)
        }) { url ->
            val slack = Slack(mapOf(CH to url))

            val e = assertFailsWith<ExecutionException> { slack.send(message).get(3, SECONDS) }
            val reason = e.cause!!.message!!
            assertTrue(reason.contains("500"))
            assertTrue(reason.length < 300, "에러 메시지가 ${reason.length}자")

            Whistle(listOf(slack)).slack(CH, "t", "b")
        }
    }

    @Test
    fun `서버가 응답하지 않으면 호출부는 즉시 반환되고 전송은 타임아웃으로 실패한다`() {
        val release = CountDownLatch(1)
        withServer({ ex ->
            release.await(10, SECONDS)
            ex.sendResponseHeaders(200, -1)
        }) { url ->
            try {
                val slack = Slack(mapOf(CH to url), Duration.ofSeconds(2))

                val elapsed = measureTimeMillis { Whistle(listOf(slack)).slack(CH, "t", "b") }
                assertTrue(elapsed < 1000, "호출부가 ${elapsed}ms 동안 블로킹됨")

                val e = assertFailsWith<ExecutionException> { slack.send(message).get(5, SECONDS) }
                assertIs<HttpTimeoutException>(e.cause)
            } finally {
                release.countDown()
            }
        }
    }

    @Test
    fun `서버가 없어도 예외가 나지 않는다`() {
        Whistle(listOf(Slack(mapOf(CH to "http://localhost:1/")))).slack(CH, "t", "b")
    }

    @Test
    fun `절대 URL 이 아니면 생성 시점에 실패한다`() {
        assertFailsWith<IllegalArgumentException> { Slack(mapOf(CH to "hooks.slack.com/xxx")) }
        assertFailsWith<IllegalArgumentException> { Slack(mapOf(CH to "")) }
    }

    @Test
    fun `메시지의 웹훅 이름에 해당하는 웹훅으로만 보낸다`() {
        val hits = mutableListOf<String>()
        val server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/") { ex ->
                ex.use {
                    synchronized(hits) { hits += ex.requestURI.path }
                    ex.sendResponseHeaders(200, -1)
                }
            }
            start()
        }
        try {
            val base = "http://localhost:${server.address.port}"
            val slack = Slack(mapOf("error" to "$base/error", "job" to "$base/job"))

            slack.send(SlackMessage("job", "t", "b")).get(3, SECONDS)

            assertEquals(listOf("/job"), hits)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `등록되지 않은 웹훅이면 실패로 처리되고 호출부로 전파되지 않는다`() {
        val slack = Slack(mapOf(CH to "http://localhost:1/"))

        val e = assertFailsWith<ExecutionException> { slack.send(SlackMessage("none", "t", "b")).get(3, SECONDS) }
        assertIs<IllegalArgumentException>(e.cause)

        Whistle(listOf(slack)).slack("none", "t", "b")
    }

    private companion object {
        const val CH = "error"
    }
}
