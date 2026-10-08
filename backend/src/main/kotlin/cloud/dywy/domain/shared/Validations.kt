package cloud.dywy.domain.shared

val UUID_REGEX: Regex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")

fun MutableList<String>.addIfBlank(value: String, errorMessage: String) {
	if (value.isBlank()) add(errorMessage)
}

fun MutableList<String>.addIfPresentAndBlank(value: String?, errorMessage: String) {
	if (value?.isBlank() == true) add(errorMessage)
}

fun MutableList<String>.addIfNotMatches(value: String, regex: Regex, errorMessage: String) {
	if (!regex.matches(value)) add(errorMessage)
}