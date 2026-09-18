package cloud.dywy.application.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommand
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeAttending
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeAttendingOtherSong
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeAttendingVeggie
import cloud.dywy.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeDeclined
import cloud.dywy.application.rsvp.result.SubmitGuestRsvpResult
import cloud.dywy.domain.guest.entity.GuestFixtures.johnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.bohemianRhapsody
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeAnswers
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithSyncedChoices
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.repository.GuestRsvps
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestRsvpSubmitterTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var playlistSynchronizer: PlaylistSynchronizer
    private lateinit var guestRsvpSubmitter: GuestRsvpSubmitter

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk()
        playlistSynchronizer = mockk(relaxed = true)
        guestRsvpSubmitter = GuestRsvpSubmitter(guestRsvps, playlistSynchronizer)
    }

    @Test
    fun `should report a created rsvp when the guest has none`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.ATTENDING))

        assertThat(result).isInstanceOf(SubmitGuestRsvpResult.Created::class)
    }

    @Test
    fun `should create the rsvp at the initial version`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.ATTENDING))

        assertThat((result as SubmitGuestRsvpResult.Created).rsvp.version).isEqualTo(1L)
    }

    @Test
    fun `should report an updated rsvp when the guest already has one`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvp
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.DECLINED))

        assertThat(result).isInstanceOf(SubmitGuestRsvpResult.Updated::class)
    }

    @Test
    fun `should bump the version when updating an existing rsvp`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvp
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.DECLINED))

        assertThat((result as SubmitGuestRsvpResult.Updated).rsvp.version).isEqualTo(2L)
    }

    @Test
    fun `should persist the answers when creating an attending rsvp`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(johnDoeAttending)

        assertThat((result as SubmitGuestRsvpResult.Created).rsvp.answers).isEqualTo(johnDoeAnswers)
    }

    @Test
    fun `should sync the chosen song to the playlist when attending`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeAttending)

        verify(exactly = 1) { playlistSynchronizer.syncAsync(johnDoe.id, laVieEnRose) }
    }

    @Test
    fun `should not sync when the chosen song is unchanged`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvpWithSyncedChoices
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeAttending)

        verify(exactly = 0) { playlistSynchronizer.syncAsync(any(), any()) }
    }

    @Test
    fun `should not sync any song when the guest declines`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.DECLINED))

        verify(exactly = 0) { playlistSynchronizer.syncAsync(any(), any()) }
    }

    @Test
    fun `should remove a previously synced song when it is dropped`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvpWithSyncedChoices
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeAttendingVeggie)

        verify(exactly = 1) { playlistSynchronizer.unsyncAsync(laVieEnRose.deezerId) }
    }

    @Test
    fun `should remove a previously synced song when the guest declines`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvpWithSyncedChoices
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeDeclined)

        verify(exactly = 1) { playlistSynchronizer.unsyncAsync(laVieEnRose.deezerId) }
    }

    @Test
    fun `should remove the old song and sync the new one when the song is replaced`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvpWithSyncedChoices
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeAttendingOtherSong)

        verify(exactly = 1) { playlistSynchronizer.unsyncAsync(laVieEnRose.deezerId) }
        verify(exactly = 1) { playlistSynchronizer.syncAsync(johnDoe.id, bohemianRhapsody) }
    }

    @Test
    fun `should not remove an unchanged synced song`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvpWithSyncedChoices
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeAttending)

        verify(exactly = 0) { playlistSynchronizer.unsyncAsync(any()) }
    }

    @Test
    fun `should not remove a dropped song that was never synchronized`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvpWithChoices
        every { guestRsvps.save(any()) } answers { firstArg() }

        guestRsvpSubmitter.submit(johnDoeAttendingVeggie)

        verify(exactly = 0) { playlistSynchronizer.unsyncAsync(any()) }
    }
}



