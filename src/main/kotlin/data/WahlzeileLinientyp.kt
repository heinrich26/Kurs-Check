package data

import com.fasterxml.jackson.annotation.JsonProperty

enum class WahlzeileLinientyp {
    @JsonProperty("keine") KEINE,
    @JsonProperty("gestrichelt") GESTRICHELT,
    @JsonProperty("durchgezogen") DURCHGEZOGEN
}