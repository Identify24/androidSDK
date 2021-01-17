package com.identify.sdk.repository.model.mrz

import java.security.PublicKey

data class EDocument (
    var docType: DocType? = null,
    var personDetails: PersonDetails? = null,
    var additionalPersonDetails: AdditionalPersonDetails? = null,
    var docPublicKey: PublicKey? = null
)