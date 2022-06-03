package data

import com.fasterxml.jackson.annotation.JsonProperty

enum class WahlzeileLinientyp {
    @JsonProperty("keine") KEINE,
    @JsonProperty("gestrichelt") GESTRICHELT,
    @JsonProperty("keine|durchgezogen") KEINE_DURCHGEZOGEN,
    @JsonProperty("durchgezogen") DURCHGEZOGEN
}