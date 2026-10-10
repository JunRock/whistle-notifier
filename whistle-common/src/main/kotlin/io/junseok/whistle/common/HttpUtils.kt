package io.junseok.whistle.common

import java.net.URI
import java.net.http.HttpResponse

private val HTTP_SCHEMES = setOf("http", "https")
private val SUCCESS_STATUS = 200..299
private const val MAX_ERROR_BODY = 200

/*
 * URL 은 시크릿이라 JDK 예외 메시지(URL 포함) 대신 고정 메시지
 */
fun String.toHttpUri(): URI = requireNotNull(
    runCatching { URI(this) }.getOrNull()?.takeIf { it.isHttp() }
) { "웹훅 URL은 http(s) 절대 URL이어야 함" }

private fun URI.isHttp() = scheme?.lowercase() in HTTP_SCHEMES && host != null

fun HttpResponse<String>.requireSuccess() {
    check(statusCode() in SUCCESS_STATUS) { "웹훅 응답 ${statusCode()}: ${body().take(MAX_ERROR_BODY)}" }
}
