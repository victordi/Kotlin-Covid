package com.idp.model

import java.time.LocalDate

sealed interface Data
data class National(val date: LocalDate, val cases: Int, val deaths: Int): Data
data class State(val date: LocalDate, val state: String, val fips: Int, val cases: Int, val deaths: Int): Data
data class County(val date: LocalDate, val county: String, val state: String, val fips: Int, val cases: Int, val deaths: Int): Data
