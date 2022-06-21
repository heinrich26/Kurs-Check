package com.kurswahlApp.data

import com.fasterxml.jackson.annotation.JsonProperty

enum class RegelScope {
    @JsonProperty("1-4") PF1_4,
    @JsonProperty("1-5") PF1_5,
    @JsonProperty("5") PF5,
    @JsonProperty("1-2") LK1_2
}