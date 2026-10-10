package io.junseok.whistle

import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class WhistleTest {
    private class Recorder<M : Any>(override val messageType: KClass<M>) : Channel<M> {
        val sent = mutableListOf<M>()

        override fun send(message: M): CompletableFuture<*> {
            sent += message
            return CompletableFuture.completedFuture(Unit)
        }
    }

    private class Boom(private val result: () -> CompletableFuture<*>) : Channel<String> {
        override val messageType = String::class

        override fun send(message: String) = result()
    }

    @Test
    fun `메시지 타입에 맞는 채널로만 전달된다`() {
        val strings = Recorder(String::class)
        val ints = Recorder(Int::class)
        val whistle = Whistle(listOf(ints, strings))

        whistle.fire("m")
        whistle.fire(1)

        assertEquals(listOf("m"), strings.sent)
        assertEquals(listOf(1), ints.sent)
    }

    @Test
    fun `같은 타입 채널이 여러 개면 먼저 등록된 채널만 받는다`() {
        val first = Recorder(String::class)
        val second = Recorder(String::class)

        Whistle(listOf(first, second)).fire("m")

        assertEquals(listOf("m"), first.sent)
        assertEquals(emptyList(), second.sent)
    }

    @Test
    fun `채널이 던지거나 실패하거나 없어도 호출부로 전파되지 않는다`() {
        Whistle(listOf(Boom { throw IllegalStateException("sync") })).fire("m")
        Whistle(listOf(Boom { CompletableFuture.failedFuture<Unit>(IllegalStateException("async")) })).fire("m")
        Whistle(emptyList()).fire("m")
    }
}
