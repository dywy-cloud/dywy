package cloud.dywy.infrastructure.sample

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import cloud.dywy.AbstractIntegrationTest
import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestListCriteria
import cloud.dywy.domain.guest.entity.GuestStatus
import cloud.dywy.domain.guest.entity.Language
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.rsvp.entity.GuestRsvp
import cloud.dywy.domain.rsvp.entity.Meal
import cloud.dywy.domain.rsvp.entity.RsvpAttendance
import cloud.dywy.infrastructure.guest.repository.GuestExposedRepository
import cloud.dywy.infrastructure.invitation.repository.InvitationsExposedRepository
import cloud.dywy.infrastructure.rsvp.repository.GuestRsvpExposedRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import javax.sql.DataSource
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SampleDataIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var guestsRepository: GuestExposedRepository

    @Autowired
    private lateinit var invitationsRepository: InvitationsExposedRepository

    @Autowired
    private lateinit var rsvpRepository: GuestRsvpExposedRepository

    @BeforeAll
    fun loadSampleData() {
        loadSampleScript()
    }

    @Test
    fun `should load the sample data twice without duplicating rows`() {
        val before = sampleRowCounts()

        loadSampleScript()

        assertThat(sampleRowCounts()).isEqualTo(before)
        assertThat(before.guests).isEqualTo(sampleGuests().size.toLong())
    }

    @Test
    fun `should read every sample invitation with its guests`() {
        val invitations = sampleInvitations()

        assertThat(invitations.size.toLong()).isEqualTo(sampleRowCounts().invitations)
    }

    @Test
    fun `should read every sample guest rsvp`() {
        val rsvps = sampleRsvps()

        assertThat(rsvps.size.toLong()).isEqualTo(sampleRowCounts().rsvps)
    }

    @Test
    fun `should cover both guest languages`() {
        val languages = sampleGuests().map { it.language }.toSet()

        assertThat(languages).containsAtLeast(*Language.entries.toTypedArray())
    }

    @Test
    fun `should include an archived guest`() {
        assertThat(sampleGuests().any { it.deletionDate != null }).isTrue()
    }

    @Test
    fun `should include an active guest without invitation`() {
        val activeGuestIds = sampleGuests().filter { it.deletionDate == null }.map { it.id }.toSet()

        val assigned = invitationsRepository.findAssignedGuestIds(activeGuestIds)

        assertThat((activeGuestIds - assigned).isNotEmpty()).isTrue()
    }

    @Test
    fun `should cover every delivery method and no delivery method`() {
        val methods = sampleInvitations().map { it.deliveryMethod }.toSet()

        assertThat(methods).containsAtLeast(*DeliveryMethod.entries.toTypedArray(), null)
    }

    @Test
    fun `should cover every meal`() {
        val meals = sampleRsvps().mapNotNull { it.answers?.meal }.toSet()

        assertThat(meals).containsAtLeast(*Meal.entries.toTypedArray())
    }

    @Test
    fun `should cover both rsvp attendances`() {
        val attendances = sampleRsvps().map { it.attendance }.toSet()

        assertThat(attendances).containsAtLeast(*RsvpAttendance.entries.toTypedArray())
    }

    @Test
    fun `should include a synchronized and an unsynchronized song`() {
        val songs = sampleRsvps().mapNotNull { it.answers?.song }

        assertThat(songs.any { it.synchronized }).isTrue()
        assertThat(songs.any { !it.synchronized }).isTrue()
    }

    private fun loadSampleScript() {
        ResourceDatabasePopulator(ClassPathResource("sample/sample-data.sql")).execute(dataSource)
    }

    private fun sampleGuests(): List<Guest> =
        guestsRepository.list(GuestListCriteria(page = 0, size = 200, status = GuestStatus.ALL))
            .items
            .filter { it.email.endsWith(SAMPLE_EMAIL_SUFFIX) }

    private fun sampleInvitations(): List<Invitation> {
        val sampleGuestIds = sampleGuests().map { it.id }.toSet()
        return invitationsRepository.list(InvitationListCriteria(page = 0, size = 200))
            .items
            .filter { invitation -> invitation.guests.any { it.id in sampleGuestIds } }
    }

    private fun sampleRsvps(): List<GuestRsvp> =
        sampleGuests().mapNotNull { rsvpRepository.findByGuestId(it.id) }

    private fun sampleRowCounts() = SampleRowCounts(
        guests = count("select count(*) from guest where email like '%$SAMPLE_EMAIL_SUFFIX'"),
        invitations = count(
            """
            select count(*) from invitation i where exists (
                select 1 from guest g
                where g.email like '%$SAMPLE_EMAIL_SUFFIX'
                  and jsonb_exists(i.invitation_data -> 'guestIds', g.id::text))
            """.trimIndent()
        ),
        rsvps = count(
            """
            select count(*) from guest_rsvp
            where guest_id in (select id from guest where email like '%$SAMPLE_EMAIL_SUFFIX')
            """.trimIndent()
        ),
        magicLinkTokens = count(
            """
            select count(*) from guest_magic_link_token
            where guest_id in (select id from guest where email like '%$SAMPLE_EMAIL_SUFFIX')
            """.trimIndent()
        ),
    )

    private fun count(sql: String) = jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L

    private data class SampleRowCounts(val guests: Long, val invitations: Long, val rsvps: Long, val magicLinkTokens: Long)

    private companion object {
        const val SAMPLE_EMAIL_SUFFIX = "@sample.dywy.cloud"
    }
}




