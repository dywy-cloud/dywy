package cloud.dywy.infrastructure.rsvp.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import cloud.dywy.AbstractIntegrationTest
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.lucasMartin
import cloud.dywy.domain.guest.entity.GuestFixtures.sofiaMartin
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.invitation.entity.InvitationFixtures.martinFamilyInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.nonExistingInvitationId
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.janeDoeRsvpWithSameSyncedChoicesAsJohnDoe
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpDeclined
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpMealOnly
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithSyncedChoices
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import cloud.dywy.domain.rsvp.entity.GuestRsvpFixtures.sofiaMartinRsvp
import cloud.dywy.domain.rsvp.entity.GuestRsvpId
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.lucasMartinPending
import cloud.dywy.domain.rsvp.entity.InvitationGuestRsvpFixtures.sofiaMartinAnswered
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.domain.rsvp.repository.PendingSongSync
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class GuestRsvpExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var guestRsvpsRepository: GuestRsvpExposedRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeTest
    fun cleanRsvps() {
        jdbcTemplate.execute("truncate table guest_rsvp")
    }

    @Test
    fun `should insert a new rsvp`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(rsvpById(johnDoeRsvp.id)).isEqualTo(johnDoeRsvp)
    }

    @Test
    fun `should upsert without creating a duplicate row`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        val updated = johnDoeRsvp.respond(RsvpAttendance.DECLINED, now = johnDoeRsvp.creationDate.plusDays(1))
        guestRsvpsRepository.save(updated)

        assertThat(rsvpCount()).isEqualTo(1)
        assertThat(rsvpById(johnDoeRsvp.id)).isEqualTo(updated)
    }

    @Test
    fun `should return the stored row when upserting ignoring a mismatched surrogate id`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        val mismatched = johnDoeRsvpDeclined.copy(
            id = GuestRsvpId(),
            creationDate = johnDoeRsvp.creationDate.plusDays(5),
        )
        val saved = guestRsvpsRepository.save(mismatched)

        assertThat(saved).isEqualTo(johnDoeRsvpDeclined)
    }

    @Test
    fun `should not create duplicate rows under concurrent submits for the same guest`() {
        val errors = ConcurrentLinkedQueue<Throwable>()
        val startGate = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)

        repeat(20) {
            executor.submit {
                startGate.await()
                runCatching {
                    guestRsvpsRepository.save(johnDoeRsvp.copy(id = GuestRsvpId()))
                }.onFailure(errors::add)
            }
        }

        startGate.countDown()
        executor.shutdown()
        val terminated = executor.awaitTermination(5, TimeUnit.SECONDS)

        assertThat(terminated).isTrue()
        assertThat(errors).isEmpty()
        assertThat(rsvpCount()).isEqualTo(1)
    }

    @Test
    fun `should find rsvp by guest id`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvp.guestId)).isEqualTo(johnDoeRsvp)
    }

    @Test
    fun `should find rsvps of several guests in a single query`() {
        guestRsvpsRepository.save(johnDoeRsvp)
        guestRsvpsRepository.save(janeDoeRsvpWithSameSyncedChoicesAsJohnDoe)
        val (found, statements) = withExecutedSql {
            guestRsvpsRepository.findByGuestIds(
                setOf(johnDoeRsvp.guestId, janeDoeRsvpWithSameSyncedChoicesAsJohnDoe.guestId)
            )
        }

        assertThat(found).containsOnly(johnDoeRsvp, janeDoeRsvpWithSameSyncedChoicesAsJohnDoe)
        assertThat(statements.size).isEqualTo(1)
    }

    @Test
    fun `should only return rsvps of the requested guests`() {
        guestRsvpsRepository.save(johnDoeRsvp)
        guestRsvpsRepository.save(janeDoeRsvpWithSameSyncedChoicesAsJohnDoe)

        assertThat(guestRsvpsRepository.findByGuestIds(setOf(johnDoeRsvp.guestId))).containsExactly(johnDoeRsvp)
    }

    @Test
    fun `should skip guests without rsvp in the batch lookup`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(guestRsvpsRepository.findByGuestIds(setOf(johnDoeRsvp.guestId, janeDoe.id)))
            .containsExactly(johnDoeRsvp)
    }

    @Test
    fun `should return nothing for an empty batch lookup`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(guestRsvpsRepository.findByGuestIds(emptySet())).isEmpty()
    }

    @Test
    fun `should list every guest of an invitation with the first name and the rsvp only when answered in a single query`() {
        guestRsvpsRepository.save(sofiaMartinRsvp)
        val (found, statements) = withExecutedSql {
            guestRsvpsRepository.findInvitationGuestRsvps(martinFamilyInvitation.id)
        }

        assertThat(found).containsExactly(*listOf(sofiaMartinAnswered, lucasMartinPending).sortedBy { it.guestId.toString() }.toTypedArray())
        assertThat(statements.size).isEqualTo(1)
    }

    @Test
    fun `should not list the guests or the rsvps of another invitation`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(guestRsvpsRepository.findInvitationGuestRsvps(martinFamilyInvitation.id).map { it.guestId })
            .containsOnly(sofiaMartin.id, lucasMartin.id)
    }

    @Test
    fun `should list nothing for an unknown invitation`() {
        assertThat(guestRsvpsRepository.findInvitationGuestRsvps(nonExistingInvitationId)).isEmpty()
    }

    @Test
    fun `should return null when guest has no rsvp`() {
        assertThat(guestRsvpsRepository.findByGuestId(janeDoe.id)).isNull()
    }

    @Test
    fun `should round-trip answers through the jsonb column`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpWithChoices.guestId)).isEqualTo(johnDoeRsvpWithChoices)
    }

    @Test
    fun `should not report an unsynced song as on the playlist`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.isSongOnPlaylist(laVieEnRose.deezerId)).isFalse()
    }

    @Test
    fun `should report a synced song as on the playlist`() {
        guestRsvpsRepository.save(johnDoeRsvpWithSyncedChoices)

        assertThat(guestRsvpsRepository.isSongOnPlaylist(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should report a chosen song as chosen by a guest even when not synced`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.isSongChosenByAnyGuest(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should not report a song no guest chose as chosen`() {
        guestRsvpsRepository.save(johnDoeRsvpMealOnly)

        assertThat(guestRsvpsRepository.isSongChosenByAnyGuest(laVieEnRose.deezerId)).isFalse()
    }

    @Test
    fun `should list a guest whose song is not synced yet`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.findGuestsWithUnsynchronizedSong())
            .containsExactly(PendingSongSync(johnDoeRsvpWithChoices.guestId, laVieEnRose))
    }

    @Test
    fun `should not list a guest whose song is already synced`() {
        guestRsvpsRepository.save(johnDoeRsvpWithSyncedChoices)

        assertThat(guestRsvpsRepository.findGuestsWithUnsynchronizedSong()).isEmpty()
    }

    @Test
    fun `should flag a guest's song as synchronized`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithChoices.guestId)

        assertThat(guestRsvpsRepository.isSongOnPlaylist(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should flip only the sync flag and preserve the other answers`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithChoices.guestId)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpWithChoices.guestId))
            .isEqualTo(johnDoeRsvpWithSyncedChoices)
    }

    @Test
    fun `should keep the guest-facing version when flagging a song as synchronized`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithChoices.guestId)

        assertThat(rsvpById(johnDoeRsvpWithChoices.id).version).isEqualTo(johnDoeRsvpWithChoices.version)
    }

    @Test
    fun `should do nothing when flagging a song synchronized for a guest without rsvp`() {
        guestRsvpsRepository.markSongSynchronized(janeDoe.id)

        assertThat(rsvpCount()).isEqualTo(0)
    }

    @Test
    fun `should do nothing when flagging a song synchronized for a guest without a song`() {
        guestRsvpsRepository.save(johnDoeRsvpMealOnly)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpMealOnly.guestId)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpMealOnly.guestId)).isEqualTo(johnDoeRsvpMealOnly)
    }

    @Test
    fun `should leave an already synchronized song untouched`() {
        guestRsvpsRepository.save(johnDoeRsvpWithSyncedChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithSyncedChoices.guestId)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpWithSyncedChoices.guestId)).isEqualTo(johnDoeRsvpWithSyncedChoices)
    }

    private fun rsvpCount() =
        jdbcTemplate.queryForObject("select count(*) from guest_rsvp", Int::class.java) ?: 0

    private fun rsvpById(id: GuestRsvpId): GuestRsvp =
        jdbcTemplate.queryForObject(
            """
            select id, guest_id, version, creation_date, update_date, attendance
            from guest_rsvp
            where id = ?
            """.trimIndent(),
            { rs, _ ->
                GuestRsvp(
                    id = GuestRsvpId.fromString(rs.getObject("id", UUID::class.java).toString()),
                    guestId = GuestId.fromString(rs.getObject("guest_id", UUID::class.java).toString()),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    attendance = RsvpAttendance.valueOf(rs.getString("attendance")),
                )
            },
            id.value.toJavaUuid()
        )
}



