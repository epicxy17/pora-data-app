package pora.data.proj.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusStationsResponse (
    @SerialName("name") val name: String,
    @SerialName("_id") val id: String,
    @SerialName("location") val location: Location,
    @SerialName("busLines") val busLines: List<String>,
    )

@Serializable
data class Location (
    @SerialName("coordinates") val coordinates: List<Double>,
)