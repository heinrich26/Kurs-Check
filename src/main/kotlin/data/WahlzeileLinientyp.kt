package data

import com.google.gson.annotations.SerializedName

enum class WahlzeileLinientyp(val key: String) {
    @SerializedName("keine")
    KEINE("keine"),
    @SerializedName("gestrichelt")
    GESTRICHELT("gestrichelt"),
    @SerializedName("durchgezogen")
    DURCHGEZOGEN("durchgezogen")
}