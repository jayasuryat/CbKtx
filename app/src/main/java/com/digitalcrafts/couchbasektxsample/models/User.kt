package com.digitalcrafts.couchbasektxsample.models

import com.digitalcrafts.couchbasektx.models.CbModel

data class User(
    val userId: String? = null,
    val firsName: String? = null,
    val lastName: String? = null,
    val location: Location? = null,
    val interests: List<Interests>? = null,
    val lastActive: String? = null,
    val majorInterest: String? = null,
    val background: String? = null,
) : CbModel() {

    init {
        type = TYPE
    }

    companion object {

        const val TYPE: String = "_cb_user"
    }
}

data class Location(
    val city: String? = null,
    val cityCode: Int? = null,
    val country: String? = null,
    val countryCode: Int? = null
)

data class Interests(
    val title: String? = null,
    val category: String? = null,
    val isSubscribed: Boolean? = null,
)