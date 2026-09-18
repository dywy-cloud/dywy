package cloud.dywy.api.guest.response

import cloud.dywy.domain.guest.entity.GuestFixtures

object GuestResponseFixtures {
    val johnDoe = GuestResponse(
        id = "${GuestFixtures.johnDoe.id}",
        version = GuestFixtures.johnDoe.version,
        creationDate = "${GuestFixtures.creationDate}",
        updateDate = "${GuestFixtures.creationDate}",
        firstName = GuestFixtures.johnDoe.firstName,
        lastName = GuestFixtures.johnDoe.lastName,
        email = GuestFixtures.johnDoe.email,
        language = GuestFixtures.johnDoe.language.name
    )
}
