package com.justai.junction.api

import com.google.gson.annotations.SerializedName

data class Quest (
    @SerializedName("id") val beaconId: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("background") val backgroundColor: String,
    val name: String,
    val mission: String,
    val briefing: String
)