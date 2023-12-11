package com.express.packagecalculator.model

data class Package(
    val weight: Double = 0.0,
    val distance: Double = 0.0,
    val totalCost: Double = 0.0,
    val discountCode: String = "",
    var name: String = "",
    var travelTime: Double = 0.0
)

data class Lorry(
    val name: String = "",
    val packages: ArrayList<Package> = arrayListOf(),
    val speed: Int = 70,
    val maxWeight: Int = 200,
    var status: VehicalStatus = VehicalStatus.AVAILABLE
)

enum class VehicalStatus {
    AVAILABLE,
    LOADING,
    DELIVERING
}