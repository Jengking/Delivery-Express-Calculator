package com.express.packagecalculator.events

import com.express.packagecalculator.model.Lorry
import com.express.packagecalculator.model.Package

sealed class AppEvents {
    data class OnWeightInputChanged(val value: Double): AppEvents()
    data class OnDistanceInputChanged(val value: Double): AppEvents()
    data class OnOfferCodeChanged(val value: String): AppEvents()
    data class AddPackageToStorage(val pack: Package): AppEvents()
    data class AddPackageToVehicle(val pack: Package, val vehicle: Lorry): AppEvents()
}
