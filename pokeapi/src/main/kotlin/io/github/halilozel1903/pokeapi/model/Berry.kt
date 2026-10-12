package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /berry/{id or name}`. Growth time is in hours per stage, size in millimetres. */
@Serializable
public data class Berry(
    public val id: Int,
    public val name: String,
    @SerialName("growth_time") public val growthTime: Int = 0,
    @SerialName("max_harvest") public val maxHarvest: Int = 0,
    @SerialName("natural_gift_power") public val naturalGiftPower: Int = 0,
    public val size: Int = 0,
    public val smoothness: Int = 0,
    @SerialName("soil_dryness") public val soilDryness: Int = 0,
    public val firmness: NamedApiResource? = null,
    public val flavors: List<BerryFlavorMap> = emptyList(),
    public val item: NamedApiResource? = null,
    @SerialName("natural_gift_type") public val naturalGiftType: NamedApiResource? = null,
)

@Serializable
public data class BerryFlavorMap(
    public val potency: Int,
    public val flavor: NamedApiResource,
)
