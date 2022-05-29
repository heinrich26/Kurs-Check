package data

import com.fasterxml.jackson.annotation.JsonProperty

enum class Wahlmoeglichkeit(val n: Int) {
    @JsonProperty("1-2") ERSTES_ZWEITES(2),
    @JsonProperty("1-3") ERSTES_DRITTES(3),
    @JsonProperty("2-4") ZWEITES_VIERTES(3),
    @JsonProperty("3-4") DRITTES_VIERTES(2),
    @JsonProperty("1-4") DURCHGEHEND(4);

    operator fun contains(wmoegl: Wahlmoeglichkeit): Boolean {
        return when (this) {
            ERSTES_ZWEITES -> wmoegl == ERSTES_ZWEITES
            ERSTES_DRITTES -> wmoegl == ERSTES_ZWEITES || wmoegl == ERSTES_DRITTES
            ZWEITES_VIERTES -> wmoegl == DRITTES_VIERTES || wmoegl == ZWEITES_VIERTES
            DRITTES_VIERTES -> wmoegl == DRITTES_VIERTES
            else /* DURCHGEGEHND */ -> true
        }
    }
}