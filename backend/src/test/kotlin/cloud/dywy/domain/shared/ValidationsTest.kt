package cloud.dywy.domain.shared

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.containsExactly
import kotlin.test.Test

class ValidationsTest {

    @Test
    fun `should add the error for blank values`() {
        val errors = mutableListOf<String>()

        errors.addIfBlank("", "empty")
        errors.addIfBlank("   ", "spaces")

        assertThat(errors).containsExactly("empty", "spaces")
    }

    @Test
    fun `should not add an error for non blank values`() {
        val errors = mutableListOf<String>()

        errors.addIfBlank("value", "error")

        assertThat(errors).isEmpty()
    }

    @Test
    fun `should add the error only for blank present values`() {
        val errors = mutableListOf<String>()

        errors.addIfPresentAndBlank(" ", "blank")
        errors.addIfPresentAndBlank(null, "null")
        errors.addIfPresentAndBlank("value", "value")

        assertThat(errors).containsExactly("blank")
    }

    @Test
    fun `should add the error only when the regex does not match`() {
        val errors = mutableListOf<String>()
        val twoLetters = Regex("^[A-Z]{2}$")

        errors.addIfNotMatches("FR", twoLetters, "valid")
        errors.addIfNotMatches("fr", twoLetters, "lowercase")
        errors.addIfNotMatches("FRA", twoLetters, "too long")

        assertThat(errors).containsExactly("lowercase", "too long")
    }

    @Test
    fun `should accept uuids and reject other values with the uuid regex`() {
        val errors = mutableListOf<String>()

        errors.addIfNotMatches("dca71c6f-4b29-43a0-80df-426786ca9075", UUID_REGEX, "valid uuid")
        errors.addIfNotMatches("not-a-uuid", UUID_REGEX, "invalid uuid")

        assertThat(errors).containsExactly("invalid uuid")
    }
}

