package io.junseok.whistle.slack

import io.junseok.whistle.enums.Level
import kotlin.test.Test
import kotlin.test.assertEquals

class SlackTextTest {
    private fun msg(
        title: String = "t",
        body: String = "b",
        level: Level = Level.ERROR,
        mentions: List<Mention> = emptyList(),
        link: String? = null,
        fields: Map<String, String> = emptyMap(),
        exception: Throwable? = null
    ) = SlackMessage(title, body, level, mentions, link, fields, exception)

    @Test
    fun `헤더는 멘션 이모지 제목 순으로 조립되고 제목은 이스케이프한다`() {
        assertEquals("<!here> 🚨 *결제 &lt;실패&gt;*", msg(title = "결제 <실패>", mentions = listOf(Mention.Here)).toHeader())
    }

    @Test
    fun `레벨과 멘션 종류에 따라 이모지와 태그가 바뀐다`() {
        assertEquals("<!channel> ⚠️ *t*", msg(level = Level.WARN, mentions = listOf(Mention.Channel)).toHeader())
        assertEquals("ℹ️ *t*", msg(level = Level.INFO).toHeader())
    }

    @Test
    fun `여러 명을 멤버 ID 로 동시에 멘션한다`() {
        assertEquals(
            "<!here> <@U01> <@U02> 🚨 *t*",
            msg(mentions = listOf(Mention.Here, Mention.User("U01"), Mention.User("U02"))).toHeader()
        )
    }

    @Test
    fun `본문 아래에 링크가 붙고 링크가 없으면 본문만 남는다`() {
        assertEquals("orderId=1\n<https://x.com|자세히 보기>", msg(body = "orderId=1", link = "https://x.com").toContent())
        assertEquals("b", msg().toContent())
    }

    @Test
    fun `링크의 쿼리스트링 앰퍼샌드도 이스케이프한다`() {
        assertEquals("b\n<https://x.com/o?id=1&amp;tab=2|자세히 보기>", msg(link = "https://x.com/o?id=1&tab=2").toContent())
    }

    @Test
    fun `본문의 mrkdwn 제어 문자도 이스케이프한다`() {
        assertEquals("a&amp;b&lt;c&gt;", msg(body = "a&b<c>").toContent())
    }

    @Test
    fun `레벨 색상 바와 필드가 attachments 로 들어간다`() {
        assertEquals(
            """{"text":"<!here> 🚨 *t*","attachments":[{"color":"#E01E5A","text":"b","fields":[""" +
                """{"title":"위치","value":"A.kt:20","short":true},{"title":"&lt;회원&gt;","value":"57","short":true}],""" +
                """"mrkdwn_in":["text"]}]}""",
            msg(mentions = listOf(Mention.Here), fields = linkedMapOf("위치" to "A.kt:20", "<회원>" to "57")).toPayload()
        )
    }

    @Test
    fun `레벨마다 색상이 다르고 필드가 없으면 빈 배열이다`() {
        assertEquals(
            """{"text":"ℹ️ *t*","attachments":[{"color":"#36C5F0","text":"b","fields":[],"mrkdwn_in":["text"]}]}""",
            msg(level = Level.INFO).toPayload()
        )
        assertEquals(true, msg(level = Level.WARN).toPayload().contains(""""color":"#ECB22E""""))
    }

    @Test
    fun `예외는 본문과 링크 사이에 이스케이프된 코드 블록으로 붙는다`() {
        val e = IllegalStateException("a<b>")
        e.stackTrace = arrayOf(StackTraceElement("C", "m", "C.kt", 1))

        assertEquals(
            "b\n```\njava.lang.IllegalStateException: a&lt;b&gt;\n\tat C.m(C.kt:1)\n```\n<https://x.com|자세히 보기>",
            msg(link = "https://x.com", exception = e).toContent()
        )
    }

    @Test
    fun `긴 stacktrace 는 앞부분만 남기고 생략한 줄 수를 표시한다`() {
        val e = IllegalStateException("boom")
        e.stackTrace = Array(MAX_STACK_LINES + 5) { StackTraceElement("C", "m$it", "C.kt", it) }

        val block = msg(exception = e).toContent().lines().drop(1)

        assertEquals("```", block.first())
        assertEquals(MAX_STACK_LINES, block.size - 3)
        assertEquals("... 6줄 생략", block[block.size - 2])
        assertEquals("```", block.last())
    }

    @Test
    fun `SUCCESS 는 체크 이모지와 초록 색상 바로 표시된다`() {
        assertEquals("✅ *t*", msg(level = Level.SUCCESS).toHeader())
        assertEquals(true, msg(level = Level.SUCCESS).toPayload().contains(""""color":"#2EB67D""""))
    }

    @Test
    fun `SlackMessage 는 제목과 본문만으로 만들 수 있다`() {
        assertEquals(SlackMessage("t", "b", Level.ERROR, emptyList(), null), SlackMessage("t", "b"))
    }
}
