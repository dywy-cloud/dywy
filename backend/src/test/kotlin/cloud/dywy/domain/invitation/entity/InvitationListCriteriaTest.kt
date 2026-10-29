package cloud.dywy.domain.invitation.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

class InvitationListCriteriaTest {

    @Test
    fun `should accept the first page`() {
        assertThat(InvitationListCriteria(page = 0, size = 1).page).isEqualTo(0)
    }

    @Test
    fun `should reject a negative page`() {
        assertFailsWith<IllegalArgumentException> { InvitationListCriteria(page = -1) }
    }

    @Test
    fun `should reject a size that is not positive`() {
        assertFailsWith<IllegalArgumentException> { InvitationListCriteria(size = 0) }
    }
}

