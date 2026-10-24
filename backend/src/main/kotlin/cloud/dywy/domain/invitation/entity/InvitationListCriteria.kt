package cloud.dywy.domain.invitation.entity

data class InvitationListCriteria(
    val page: Int = 0,
    val size: Int = DEFAULT_SIZE,
    val label: String? = null,
) {
    companion object {
        const val DEFAULT_SIZE = 10
    }
}

