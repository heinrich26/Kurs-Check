package data

import com.google.gson.annotations.SerializedName

enum class Wahlmoeglichkeit(val n: Int) {
    @SerializedName("1-2") ERSTES_ZWEITES(2),
    @SerializedName("1-3") ERSTES_DRITTES(3),
    @SerializedName("2-4") ZWEITES_VIERTES(3),
    @SerializedName("3-4") DRITTES_VIERTES(2),
    @SerializedName("1-4") DURCHGEHEND(4)
}