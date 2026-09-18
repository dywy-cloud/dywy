package cloud.dywy.infrastructure.rsvp.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeAnswers
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeAnswersJson
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.veggieAnswers
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.veggieAnswersJson
import cloud.dywy.domain.rsvp.entity.RsvpAnswers
import kotlin.test.Test

class GuestRsvpTableTest {

    @Test
    fun `should serialize answers with a song to json`() {
        assertThat(johnDoeAnswers.toJson()).isEqualTo(johnDoeAnswersJson)
    }

    @Test
    fun `should deserialize answers with a song from json`() {
        assertThat(RsvpAnswers.fromString(johnDoeAnswersJson)).isEqualTo(johnDoeAnswers)
    }

    @Test
    fun `should serialize answers without a song to json`() {
        assertThat(veggieAnswers.toJson()).isEqualTo(veggieAnswersJson)
    }

    @Test
    fun `should deserialize answers without a song from json`() {
        assertThat(RsvpAnswers.fromString(veggieAnswersJson)).isEqualTo(veggieAnswers)
    }
}