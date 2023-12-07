package com.express.packagecalculator.model

data class Package(
    val weight: Double = 0.0,
    val distance: Double = 0.0,
    val totalCost: Double = 0.0,
    val discountCode: String = "",
    var name: String = ""
)

data class Lorry(
    val name: String = "",
    val packages: ArrayList<Package> = arrayListOf(),
    val speed: Int = 100,
    val maxWeight: Int = 200
)