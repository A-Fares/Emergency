package com.afares.emergency.data.model

data class Hospital(
    val id: String?="",
    val name: String="",
    val city: List<String>?= listOf(""),
    val mail: String?=""
) {
    constructor(id: String, name: String) : this(id, name, null, null)

    override fun toString(): String {
        return name
    }
}
