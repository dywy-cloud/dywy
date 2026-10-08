package cloud.dywy.domain.invitation.entity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class PostalAddressTest {

    @Test
    fun `should accept an address with only the required fields`() {
        val address = PostalAddress(line1 = "Plot 1 Nile Avenue", locality = "Kampala", countryCode = "UG")

        assertThat(address.postalCode).isEqualTo(null)
    }

    @Test
    fun `should report all validation errors at once`() {
        assertFailure {
            PostalAddress(
                line1 = " ",
                line2 = " ",
                line3 = " ",
                postalCode = " ",
                locality = " ",
                region = " ",
                countryCode = "usa",
            )
        }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage(
                "Invalid postal address: line1 must not be blank; line2 must not be blank when provided; line3 must not be blank when provided; postalCode must not be blank when provided; locality must not be blank; region must not be blank when provided; countryCode must be an ISO 3166-1 alpha-2 code"
            )
    }

    @Test
    fun `should reject a blank country code`() {
        assertFailure { PostalAddress(line1 = "1 Main St", locality = "Paris", countryCode = " ") }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Invalid postal address: countryCode must not be blank")
    }

    @Test
    fun `should reject an uppercase but unassigned country code`() {
        listOf("ZZ", "AA", "XX").forEach { code ->
            assertFailure { PostalAddress(line1 = "1 Main St", locality = "Paris", countryCode = code) }
                .isInstanceOf(IllegalArgumentException::class)
                .hasMessage("Invalid postal address: countryCode must be an ISO 3166-1 alpha-2 code")
        }
    }

    @Test
    fun `should accept assigned country codes`() {
        listOf("FR", "CH", "CA", "GB", "UG").forEach { code ->
            assertThat(PostalAddress(line1 = "1 Main St", locality = "Town", countryCode = code).countryCode)
                .isEqualTo(code)
        }
    }

    @Test
    fun `should reject a lowercase country code`() {
        assertFailure { PostalAddress(line1 = "1 Main St", locality = "Paris", countryCode = "fr") }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Invalid postal address: countryCode must be an ISO 3166-1 alpha-2 code")
    }
}

