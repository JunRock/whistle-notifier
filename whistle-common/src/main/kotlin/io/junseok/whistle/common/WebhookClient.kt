package io.junseok.whistle.common

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * HTTP 웹훅 채널(Slack·Discord 등) 공용 JSON POST
 */
class WebhookClient(
    url: String,
    timeout: Duration = DEFAULT_TIMEOUT,
    private val client: HttpClient = SHARED_CLIENT
) {
    private val template = HttpRequest.newBuilder(url.toHttpUri()).timeout(timeout).header(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)

    fun post(json: String): CompletableFuture<*> =
        client.sendAsync(request(json), BodyHandlers.ofString()).thenApply(HttpResponse<String>::requireSuccess)

    private fun request(json: String) = template.copy().POST(BodyPublishers.ofString(json)).build()

    companion object {
        val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(3)
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"

        /*
         * 채널 수만큼 스레드·커넥션 풀이 생기지 않도록 공유
         */
        private val SHARED_CLIENT: HttpClient = HttpClient.newBuilder().connectTimeout(DEFAULT_TIMEOUT).build()
    }
}
