package com.express.packagecalculator.events

sealed class UiEvent {
    data class ShowError(val message: String): UiEvent()
}