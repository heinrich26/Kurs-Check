package data

import com.google.gson.annotations.SerializedName

enum class Wahlmoeglichkeit {
    @SerializedName("1-2") ERSTES_ZWEITES,
    @SerializedName("1-3") ERSTES_DRITTES,
    @SerializedName("2-4") ZWEITES_VIERTES,
    @SerializedName("3-4") DRITTES_VIERTES,
    @SerializedName("1-4") DURCHGEHEND
}