package cloud.dywy.infrastructure.invitation.repository

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import cloud.dywy.AbstractIntegrationTest
import cloud.dywy.domain.guest.entity.Guest
import cloud.dywy.domain.guest.entity.GuestFixtures
import cloud.dywy.domain.invitation.entity.DeliveryMethod
import cloud.dywy.domain.invitation.entity.PostalAddressFixtures
import cloud.dywy.domain.guest.entity.GuestFixtures.albertEinstein
import cloud.dywy.domain.guest.entity.GuestFixtures.emmaWilson
import cloud.dywy.domain.guest.entity.GuestFixtures.janeDoe
import cloud.dywy.domain.guest.entity.GuestFixtures.liamMiller
import cloud.dywy.domain.guest.entity.GuestFixtures.marieCurie
import cloud.dywy.domain.guest.entity.GuestFixtures.oliverBennett
import cloud.dywy.domain.guest.entity.GuestFixtures.pierreCurie
import cloud.dywy.domain.guest.entity.GuestId
import cloud.dywy.domain.guest.entity.Language
import cloud.dywy.domain.guest.repository.Guests
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bestManInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.communityDinnerInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.friendsInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.handDeliveredInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.nonExistingInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.scienceConferenceInvitation
import cloud.dywy.domain.invitation.entity.InvitationFixtures.scienceConferenceInvitationUpdated
import cloud.dywy.domain.invitation.entity.Invitation
import cloud.dywy.domain.invitation.entity.InvitationAccessToken
import cloud.dywy.domain.invitation.entity.InvitationId
import cloud.dywy.domain.invitation.entity.InvitationListCriteria
import cloud.dywy.domain.invitation.entity.InvitationPage
import cloud.dywy.domain.invitation.repository.Invitations
import cloud.dywy.domain.shared.Dates.nowUtcMillis
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithUnknownProperty
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithInvalidPostalAddress
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.jsonWithoutAccessToken
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.malformedGuestId
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.rawTokenWithMalformedGuestId
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.rawJson
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.rawTokenPostedWithoutAddress
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.rawTokenWithUnknownGuest
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.rawTokenWithoutGuests
import cloud.dywy.infrastructure.invitation.repository.InvitationDataFixtures.unknownGuestId
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class InvitationsExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var invitationsRepository: Invitations

    @Autowired
    private lateinit var guestRepository: Guests

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should insert invitation with multiple guests`() {
        val initialCount = invitationCount()

        guestRepository.add(emmaWilson)
        guestRepository.add(liamMiller)
        val createdInvitation = invitationsRepository.add(friendsInvitation)

        assertThat(invitationCount()).isEqualTo(initialCount + 1)
        assertThat(invitationById(friendsInvitation.id)).isEqualTo(createdInvitation)
    }

    @Test
    fun `should load guest language when fetching invitation guests`() {
        guestRepository.add(oliverBennett)
        val invitation = Invitation(
            label = "Language check",
            description = "",
            guests = setOf(oliverBennett),
        )
        invitationsRepository.add(invitation)

        val loadedGuest = invitationsRepository.findById(invitation.id)
            ?.guests
            ?.first { it.id == oliverBennett.id }

        assertThat(loadedGuest?.language).isEqualTo(Language.EN)
    }

    @Test
    fun `should update existing invitation details and guests`() {
        guestRepository.add(albertEinstein)
        guestRepository.add(marieCurie)

        val invitation = scienceConferenceInvitation
        val createdInvitation = invitationsRepository.add(invitation)
        val initialCount = invitationCount()

        val invitationToUpdate = scienceConferenceInvitationUpdated

        val updatedInvitation = invitationsRepository.update(invitationToUpdate)

        assertThat(updatedInvitation).isEqualTo(invitationToUpdate)
        assertThat(invitationsRepository.findById(createdInvitation.id)).isEqualTo(invitationToUpdate)
        assertThat(invitationCount()).isEqualTo(initialCount)
        assertThat(invitationsRepository.findAssignedGuestIds(setOf(albertEinstein.id))).isEqualTo(emptySet())
        assertThat(invitationsRepository.findAssignedGuestIds(setOf(marieCurie.id))).isEqualTo(setOf(marieCurie.id))
    }

    @Test
    fun `should persist and reload posted delivery method with postal address`() {
        val invitation = communityDinnerInvitation
        guestRepository.add(GuestFixtures.ryanEvans)

        invitationsRepository.add(invitation)

        val reloaded = invitationsRepository.findById(invitation.id)
        assertThat(reloaded?.deliveryMethod).isEqualTo(DeliveryMethod.POSTED)
        assertThat(reloaded?.postalAddress).isEqualTo(PostalAddressFixtures.kampala)
        assertThat(invitationById(invitation.id)).isEqualTo(invitation)
    }

    @Test
    fun `should persist and update hand delivered method without postal address`() {
        val invitation = handDeliveredInvitation
        guestRepository.add(GuestFixtures.joyceClement)
        invitationsRepository.add(invitation)
        assertThat(invitationsRepository.findById(invitation.id)?.deliveryMethod).isEqualTo(DeliveryMethod.HAND_DELIVERED)

        val posted = invitation.copy(
            postalAddress = PostalAddressFixtures.geneva,
            deliveryMethod = DeliveryMethod.POSTED,
            updateDate = nowUtcMillis(),
        )
        invitationsRepository.update(posted)

        val reloaded = invitationsRepository.findById(invitation.id)
        assertThat(reloaded?.deliveryMethod).isEqualTo(DeliveryMethod.POSTED)
        assertThat(reloaded?.postalAddress).isEqualTo(PostalAddressFixtures.geneva)
    }

    @Test
    fun `should return null when trying to update non-existing invitation`() {
        guestRepository.add(pierreCurie)

        val nonExisting = nonExistingInvitation(pierreCurie, updateDate = nowUtcMillis())

        val updatedInvitation = invitationsRepository.update(nonExisting)

        assertThat(updatedInvitation).isNull()
    }

    @Test
    fun `should find invitation by id`() {
        val found = invitationsRepository.findById(bridesMaidInvitation.id)

        assertThat(found).isEqualTo(bridesMaidInvitation)
    }

    @Test
    fun `should return null when invitation id does not exist`() {
        val missing = invitationsRepository.findById(InvitationId.fromString("019f6c83-cd2d-7357-833b-e7cda750ba32"))

        assertThat(missing).isNull()
    }

    @Test
    fun `should list invitations with pagination and id ordering`() {
        val itemCount = invitationCount()
        val firstPage: InvitationPage = invitationsRepository.list(InvitationListCriteria(page = 0, size = 1))
        val secondPage = invitationsRepository.list(InvitationListCriteria(page = 1, size = 1))

        assertThat(firstPage).all {
            prop(InvitationPage::items).isEqualTo(listOf(bridesMaidInvitation))
            prop(InvitationPage::page).isEqualTo(0)
            prop(InvitationPage::size).isEqualTo(1)
            prop(InvitationPage::totalItems).isEqualTo(itemCount.toLong())
            prop(InvitationPage::totalPages).isEqualTo(itemCount)
        }

        assertThat(secondPage).all {
            prop(InvitationPage::items).isEqualTo(listOf(bestManInvitation))
            prop(InvitationPage::page).isEqualTo(1)
            prop(InvitationPage::size).isEqualTo(1)
            prop(InvitationPage::totalItems).isEqualTo(itemCount.toLong())
            prop(InvitationPage::totalPages).isEqualTo(itemCount)
        }
    }

    @Test
    fun `should return empty items when page has no invitations`() {
        val itemCount = invitationCount()

        val outOfRangePage = invitationsRepository.list(InvitationListCriteria(page = itemCount, size = 1))

        assertThat(outOfRangePage).all {
            prop(InvitationPage::items).isEqualTo(emptyList())
            prop(InvitationPage::page).isEqualTo(itemCount)
            prop(InvitationPage::size).isEqualTo(1)
            prop(InvitationPage::totalItems).isEqualTo(itemCount.toLong())
            prop(InvitationPage::totalPages).isEqualTo(itemCount)
        }
    }

    @Test
    fun `should find assigned guest ids in batch`() {
        val unassignedGuestId = GuestId.fromString("019fa9fa-b235-75f8-a499-2c8ce31e1e6c")

        val assignedGuestIds = invitationsRepository.findAssignedGuestIds(setOf(janeDoe.id, unassignedGuestId))

        assertThat(assignedGuestIds).isEqualTo(setOf(janeDoe.id))
    }

    @Test
    fun `should resolve invitation by access token`() {
        val found = invitationsRepository.findInvitationByAccessToken(bridesMaidInvitation.accessToken)

        assertThat(found).isEqualTo(bridesMaidInvitation)
    }

    @Test
    fun `should reject duplicate access token values at database level`() {
        assertFailsWith<DataIntegrityViolationException> {
            jdbcTemplate.update(
                """
                update invitation
                set invitation_data = jsonb_set(invitation_data, '{accessToken}', to_jsonb(?::text))
                where id = ?
                """.trimIndent(),
                bridesMaidInvitation.accessToken.value,
                bestManInvitation.id.value.toJavaUuid(),
            )
        }
    }

    @Test
    fun `should load invitation whose stored json has unknown properties`() {
        val id = insertRawInvitation(jsonWithUnknownProperty)

        val loaded = invitationsRepository.findById(InvitationId(id.toKotlinUuid()))

        assertThat(loaded?.label).isEqualTo("Raw")
    }

    @Test
    fun `should fail with dedicated exception when stored guest does not exist`() {
        val id = insertRawInvitation(
            rawJson(guestIds = listOf(unknownGuestId), accessToken = rawTokenWithUnknownGuest)
        )

        assertFailsWith<InconsistentInvitationDataException> {
            invitationsRepository.findById(InvitationId(id.toKotlinUuid()))
        }
    }

    @Test
    fun `should fail with dedicated exception when stored guest id is malformed`() {
        val id = insertRawInvitation(
            rawJson(guestIds = listOf(malformedGuestId), accessToken = rawTokenWithMalformedGuestId)
        )

        assertFailsWith<InconsistentInvitationDataException> {
            invitationsRepository.findById(InvitationId(id.toKotlinUuid()))
        }
    }

    @Test
    fun `should fail with dedicated exception when stored postal address is invalid`() {
        val id = insertRawInvitation(jsonWithInvalidPostalAddress)

        assertFailsWith<InconsistentInvitationDataException> {
            invitationsRepository.findById(InvitationId(id.toKotlinUuid()))
        }
    }

    @Test
    fun `should reject posted invitation without postal address at database level`() {
        assertFailsWith<DataIntegrityViolationException> {
            insertRawInvitation(
                rawJson(deliveryMethod = "POSTED", accessToken = rawTokenPostedWithoutAddress)
            )
        }
    }

    @Test
    fun `should reject invitation without guest ids at database level`() {
        assertFailsWith<DataIntegrityViolationException> {
            insertRawInvitation(rawJson(guestIds = emptyList(), accessToken = rawTokenWithoutGuests))
        }
    }

    @Test
    fun `should reject invitation without access token at database level`() {
        assertFailsWith<DataIntegrityViolationException> {
            insertRawInvitation(jsonWithoutAccessToken)
        }
    }

    @Test
    fun `should reject invitation with blank access token at database level`() {
        assertFailsWith<DataIntegrityViolationException> {
            insertRawInvitation(rawJson(accessToken = " "))
        }
    }

    private fun insertRawInvitation(json: String): java.util.UUID {
        val id = java.util.UUID.randomUUID()
        jdbcTemplate.update(
            """
            insert into invitation (id, version, creation_date, update_date, invitation_data)
            values (?, 1, now(), now(), ?::jsonb)
            """.trimIndent(),
            id,
            json,
        )
        return id
    }

    private fun invitationCount() =
        jdbcTemplate.queryForObject("select count(*) from invitation", Int::class.java) ?: 0

    private fun invitationById(invitationId: InvitationId): Invitation =
        jdbcTemplate.queryForObject(
            """
            select i.id,
                   i.version,
                   i.creation_date,
                   i.update_date,
                   i.invitation_data,
                   array_agg(g.id order by g.id) as guest_ids,
                   array_agg(g.version order by g.id) as guest_versions,
                   array_agg(g.creation_date order by g.id) as guest_creation_dates,
                   array_agg(g.update_date order by g.id) as guest_update_dates,
                   array_agg(g.deletion_date order by g.id) as guest_deletion_dates,
                   array_agg(g.first_name order by g.id) as guest_first_names,
                   array_agg(g.last_name order by g.id) as guest_last_names,
                   array_agg(g.email order by g.id) as guest_emails
            from invitation i
            inner join guest g on jsonb_exists(i.invitation_data -> 'guestIds', g.id::text)
            where i.id = ?
            group by i.id, i.version, i.creation_date, i.update_date, i.invitation_data
        """.trimIndent(),
            { rs, _ ->
                val objectMapper = jacksonObjectMapper()
                val payload = objectMapper.readValue<InvitationData>(rs.getString("invitation_data"))
                val guestIds = (rs.getArray("guest_ids").array as Array<*>).map { GuestId.fromString(it.toString()) }
                val guestVersions = (rs.getArray("guest_versions").array as Array<*>).map { (it as Number).toLong() }
                val guestCreationDates = (rs.getArray("guest_creation_dates").array as Array<*>).map { (it as Timestamp).toLocalDateTime() }
                val guestUpdateDates = (rs.getArray("guest_update_dates").array as Array<*>).map { (it as Timestamp).toLocalDateTime() }
                val guestDeletionDates = (rs.getArray("guest_deletion_dates").array as Array<*>).map { (it as Timestamp?)?.toLocalDateTime() }
                val guestFirstNames = (rs.getArray("guest_first_names").array as Array<*>).map { it.toString() }
                val guestLastNames = (rs.getArray("guest_last_names").array as Array<*>).map { it.toString() }
                val guestEmails = (rs.getArray("guest_emails").array as Array<*>).map { it.toString() }

                val guests = guestIds.indices.map { index ->
                    Guest(
                        id = guestIds[index],
                        version = guestVersions[index],
                        creationDate = guestCreationDates[index],
                        updateDate = guestUpdateDates[index],
                        deletionDate = guestDeletionDates[index],
                        firstName = guestFirstNames[index],
                        lastName = guestLastNames[index],
                        email = guestEmails[index],
                    )
                }.toSet()

                Invitation(
                    id = InvitationId.fromString(rs.getString("id")),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    label = payload.label,
                    description = payload.description,
                    postalAddress = payload.postalAddress,
                    deliveryMethod = payload.deliveryMethod,
                    guests = guests,
                    accessToken = InvitationAccessToken(payload.accessToken),
                )
            },
            invitationId.value.toJavaUuid())

}


