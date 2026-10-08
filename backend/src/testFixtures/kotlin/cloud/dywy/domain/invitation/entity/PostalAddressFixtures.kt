package cloud.dywy.domain.invitation.entity

object PostalAddressFixtures {
    val paris = PostalAddress(
        line1 = "12 Rue de la Paix",
        postalCode = "75002",
        locality = "Paris",
        countryCode = "FR",
    )

    val geneva = PostalAddress(
        line1 = "8 Rue du Rhône",
        postalCode = "1204",
        locality = "Genève",
        countryCode = "CH",
    )

    val montreal = PostalAddress(
        line1 = "1000 Rue Sherbrooke O",
        line2 = "Appartement 42",
        postalCode = "H3A 3G4",
        locality = "Montréal",
        region = "QC",
        countryCode = "CA",
    )

    val london = PostalAddress(
        line1 = "10 Downing Street",
        postalCode = "SW1A 2AA",
        locality = "London",
        countryCode = "GB",
    )

    val kampala = PostalAddress(
        line1 = "Plot 1 Nile Avenue",
        locality = "Kampala",
        countryCode = "UG",
    )
}



