package cloud.dywy.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import cloud.dywy.application.guest.command.AddGuestCommandFixtures.charlieDavis
import cloud.dywy.domain.guest.entity.GuestFixtures
import cloud.dywy.domain.guest.repository.Guests
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestAdderTest {

    private lateinit var guests: Guests
    private lateinit var guestAdder: GuestAdder

    @BeforeTest
    fun setUp() {
        guests = mockk()
        guestAdder = GuestAdder(guests)
    }

    @Test
    fun `should add a new guest`() {
        val guest = GuestFixtures.johnDoe
        every { guests.add(any()) } returns guest

        val result = guestAdder.add(charlieDavis)

        assertThat(result).isEqualTo(guest)
    }
}
