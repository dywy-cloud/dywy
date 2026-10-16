package cloud.dywy.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class RsvpAttendanceTest {

    @Test
    fun `should parse a matching value`() {
        assertThat(RsvpAttendance.parse("ATTENDING")).isEqualTo(RsvpAttendance.ATTENDING)
    }

    @Test
    fun `should parse ignoring case and surrounding whitespace`() {
        assertThat(RsvpAttendance.parse("  declined  ")).isEqualTo(RsvpAttendance.DECLINED)
    }

    @Test
    fun `should default to pending for an unknown value`() {
        assertThat(RsvpAttendance.parse("MAYBE")).isEqualTo(RsvpAttendance.PENDING)
    }

    @Test
    fun `should default to pending for a null value`() {
        assertThat(RsvpAttendance.parse(null)).isEqualTo(RsvpAttendance.PENDING)
    }
}

