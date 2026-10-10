package io.junseok.whistle

import java.lang.System.Logger.Level.WARNING

/**
 * 알림 진입점
 * 메시지 타입으로 채널 선택, 실패는 호출부로 전파되지 않음
 */
class Whistle(channels: List<Channel<*>>) {
    private val byType = channels.distinctBy { it.messageType }.associateBy { it.messageType.javaObjectType }

    fun <M : Any> fire(message: M) {
        @Suppress("UNCHECKED_CAST")
        val channel = byType[message.javaClass] as Channel<M>?

        if (channel == null) {
            log.log(WARNING, "${message.javaClass.simpleName} 를 처리할 채널이 설정되지 않아 알림을 건너뜀")
            return
        }

        try {
            channel.send(message).whenComplete { _, e ->
                if (e != null) log.log(WARNING, "${channel.javaClass.simpleName} 알림 전송 실패", e)
            }
        } catch (e: Exception) {
            log.log(WARNING, "${channel.javaClass.simpleName} 알림 전송 실패", e)
        }
    }

    private companion object {
        val log: System.Logger = System.getLogger(Whistle::class.java.name)
    }
}
