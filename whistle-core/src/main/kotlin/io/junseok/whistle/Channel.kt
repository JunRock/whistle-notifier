package io.junseok.whistle

import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * messageType 은 타입 소거 때문에 필요, Whistle 이 메시지 클래스로 채널 선택
 */
interface Channel<M : Any> {
    val messageType: KClass<M>

    fun send(message: M): CompletableFuture<*>
}
