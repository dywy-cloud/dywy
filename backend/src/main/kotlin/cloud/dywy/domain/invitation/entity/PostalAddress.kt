package cloud.dywy.domain.invitation.entity

import cloud.dywy.domain.shared.addIfBlank
import cloud.dywy.domain.shared.addIfPresentAndBlank
import java.util.Locale

data class PostalAddress(
    val line1: String,
    val line2: String? = null,
    val line3: String? = null,
    val postalCode: String? = null,
    val locality: String,
    val region: String? = null,
    val countryCode: String,
) {
    init {
        val errors = buildList {
            addIfBlank(line1, "line1 must not be blank")
            addIfPresentAndBlank(line2, "line2 must not be blank when provided")
            addIfPresentAndBlank(line3, "line3 must not be blank when provided")
            addIfPresentAndBlank(postalCode, "postalCode must not be blank when provided")
            addIfBlank(locality, "locality must not be blank")
            addIfPresentAndBlank(region, "region must not be blank when provided")

            if (countryCode.isBlank()) {
                add("countryCode must not be blank")
            } else if (countryCode !in ISO_COUNTRY_CODES) {
                add("countryCode must be an ISO 3166-1 alpha-2 code")
            }
        }

        require(errors.isEmpty()) {
            "Invalid postal address: ${errors.joinToString(separator = "; ")}"
        }
    }

    private companion object {
        val ISO_COUNTRY_CODES: Set<String> = Locale.getISOCountries().toSet()
    }
}


