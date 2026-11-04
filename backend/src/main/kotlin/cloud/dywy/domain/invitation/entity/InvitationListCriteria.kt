package cloud.dywy.domain.invitation.entity

data class InvitationListCriteria(
    val page: Int = 0,
    val size: Int = DEFAULT_SIZE,
    val label: String? = null,
) {
    init {
        require(page >= 0) { "page must not be negative" }
        require(size > 0) { "size must be positive" }
    }

    companion object {
        const val DEFAULT_SIZE = 10
    }
}

