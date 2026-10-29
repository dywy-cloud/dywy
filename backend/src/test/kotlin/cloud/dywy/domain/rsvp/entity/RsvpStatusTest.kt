package cloud.dywy.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

class RsvpStatusTest {

    @ParameterizedTest
    @EnumSource(RsvpStatus::class)
    fun `should parse each status by its name`(status: RsvpStatus) {
        assertThat(RsvpStatus.parse(status.name)).isEqualTo(status)
    }

    @ParameterizedTest
    @CsvSource(
        "partially_accepted, PARTIALLY_ACCEPTED",
        "Pending, PENDING",
        "'  declined_all  ', DECLINED_ALL",
    )
    fun `should parse ignoring case and surrounding whitespace`(value: String, expected: RsvpStatus) {
        assertThat(RsvpStatus.parse(value)).isEqualTo(expected)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["MAYBE", "ACCEPTED", "", "  "])
    fun `should return null for a null, unknown or blank value`(value: String?) {
        assertThat(RsvpStatus.parse(value)).isNull()
    }
}
