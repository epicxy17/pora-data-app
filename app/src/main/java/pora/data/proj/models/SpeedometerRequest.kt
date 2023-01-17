package pora.data.proj.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpeedometerRequest (
    @SerialName("data") val shows: List<List<Float>>,
    @SerialName("id") val id: String,
    @SerialName("date_time") val date: String,
)