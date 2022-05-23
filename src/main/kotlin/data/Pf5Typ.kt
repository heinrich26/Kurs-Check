package data

import com.google.gson.annotations.SerializedName

enum class Pf5Typ(val repr: String) {
    @SerializedName("schriftl") SCHRIFTLICH("schriftl."), @SerializedName("praes") PRAESENTATION("Präs."), @SerializedName("wettbewerb") WETTBEWERB("Wettbewerb")
}