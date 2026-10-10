package io.junseok.whistle.common

import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsTest {
    @Test
    fun `따옴표 역슬래시 줄바꿈을 이스케이프한다`() {
        assertEquals("\"a\\\"b\\\\c\\nd\"", "a\"b\\c\nd".toJsonString())
    }

    @Test
    fun `그 외 제어 문자는 유니코드 시퀀스로 바꾼다`() {
        assertEquals("\"\\u0009\\u000d\"", "\t\r".toJsonString())
    }

    @Test
    fun `한글과 이모지는 그대로 둔다`() {
        assertEquals("\"한글 🚨\"", "한글 🚨".toJsonString())
    }

    @Test
    fun `빈 문자열은 빈 리터럴이 된다`() {
        assertEquals("\"\"", "".toJsonString())
    }
}
