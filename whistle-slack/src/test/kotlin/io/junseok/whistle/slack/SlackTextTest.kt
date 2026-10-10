package io.junseok.whistle.slack

import io.junseok.whistle.enums.Level
import kotlin.test.Test
import kotlin.test.assertEquals

class SlackTextTest {
    @Test
    fun `멘션 이모지 제목 본문 링크 순으로 조립된다`() {
        assertEquals(
            "<!here> 🚨 *결제 &lt;실패&gt;*\norderId=1\n<https://x.com|자세히 보기>",
            SlackMessage("결제 <실패>", "orderId=1", Level.ERROR, listOf(Mention.Here), "https://x.com").toText()
        )
    }

    @Test
    fun `멘션과 링크가 없으면 제목과 본문만 남는다`() {
        assertEquals("ℹ️ *t*\nb", SlackMessage("t", "b", Level.INFO, emptyList(), null).toText())
    }

    @Test
    fun `레벨과 멘션 종류에 따라 이모지와 태그가 바뀐다`() {
        assertEquals("<!channel> ⚠️ *t*\nb", SlackMessage("t", "b", Level.WARN, listOf(Mention.Channel), null).toText())
    }

    @Test
    fun `여러 명을 멤버 ID 로 동시에 멘션한다`() {
        assertEquals(
            "<!here> <@U01> <@U02> 🚨 *t*\nb",
            SlackMessage("t", "b", Level.ERROR, listOf(Mention.Here, Mention.User("U01"), Mention.User("U02")), null).toText()
        )
    }

    @Test
    fun `링크의 쿼리스트링 앰퍼샌드도 이스케이프한다`() {
        assertEquals(
            "🚨 *t*\nb\n<https://x.com/o?id=1&amp;tab=2|자세히 보기>",
            SlackMessage("t", "b", Level.ERROR, emptyList(), "https://x.com/o?id=1&tab=2").toText()
        )
    }

    @Test
    fun `본문의 mrkdwn 제어 문자도 이스케이프한다`() {
        assertEquals("🚨 *t*\na&amp;b&lt;c&gt;", SlackMessage("t", "a&b<c>", Level.ERROR, emptyList(), null).toText())
    }
}
