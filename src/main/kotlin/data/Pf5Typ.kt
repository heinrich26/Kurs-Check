package data

import com.fasterxml.jackson.annotation.JsonProperty

enum class Pf5Typ(val repr: String) {
    @JsonProperty("schriftl") SCHRIFTLICH("schriftl."),
    @JsonProperty("praes") PRAESENTATION("Präs."),
    @JsonProperty("wettbewerb") WETTBEWERB("Wettbewerb")
}