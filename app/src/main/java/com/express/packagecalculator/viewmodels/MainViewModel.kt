package com.express.packagecalculator.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.express.packagecalculator.events.AppEvents
import com.express.packagecalculator.events.UiEvent
import com.express.packagecalculator.model.Lorry
import com.express.packagecalculator.model.Package
import com.express.packagecalculator.model.VehicalStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val baseDeliveryCost = 100
    private val inputWeightInKg = mutableStateOf(0.0)
    private val inputDistanceInKm = mutableStateOf(0.0)
    private val inputOfferCode = mutableStateOf("")
    val calculatedCost = mutableStateOf(0.0)
    val totalDeliveryCost = mutableStateOf(0.0)
    val subtractAmount = mutableStateOf(0.0)
    val criteriaMet = mutableStateOf("")
    val calculatedEstimationTime = mutableStateOf(0.0)

    private val offers: HashMap<String, Int> = HashMap()
    private val _storage = mutableStateListOf<Package>()
    val storage: List<Package> = _storage
    private val _vehicles = mutableStateListOf<Lorry>().apply {
        add(Lorry(name = "Vehicle01"))
        add(Lorry(name = "Vehicle02"))
    }
    private val _availableVehicle = mutableStateListOf<Lorry>()
    val availableVehicle: List<Lorry> = _availableVehicle

    private val _inTransitVehicle = mutableStateListOf<Lorry>()
    val inTransitVehicles: List<Lorry> = _inTransitVehicle

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        offers["OFR001"] = 10
        offers["OFR002"] = 7
        offers["OFR003"] = 5
        getVehiclesAvailability()
    }

    fun onEvent(event: AppEvents) {
        when(event) {
            is AppEvents.OnDistanceInputChanged -> {
                viewModelScope.launch {
                    inputDistanceInKm.value = event.value
                    calculateTotalDeliveryCost()
                }
            }
            is AppEvents.OnWeightInputChanged -> {
                viewModelScope.launch {
                    inputWeightInKg.value = event.value
                    calculateTotalDeliveryCost()
                }
            }
            is AppEvents.OnOfferCodeChanged -> {
                viewModelScope.launch {
                    inputOfferCode.value = event.value
                    calculateTotalDeliveryCost()
                }
            }
            is AppEvents.AddPackageToStorage -> {
                viewModelScope.launch {
                    _storage.add(event.pack)
                    reset()
                }
            }
            is AppEvents.AddPackageToVehicle -> {
                viewModelScope.launch {
                    val lorry = _availableVehicle.findLast { it.name == event.vehicle.name }
                    if (lorry != null) {
                        val item = event.pack
                        item.travelTime = item.distance / lorry.speed
                        lorry.packages.add(item)
                        _storage.remove(event.pack)
                        getVehiclesAvailability()
                    }
                }
            }
            is AppEvents.OnPackageDelivered -> {
                viewModelScope.launch {
                    val lorry = _inTransitVehicle.find { it.name == event.vehicleName }
                    lorry?.packages?.remove(event.pack)
                    if (checkVehicleFinishDelivery(lorry)) {
                        _inTransitVehicle.remove(lorry)
                        lorry!!.status = VehicalStatus.AVAILABLE
                        _availableVehicle.add(lorry)
                    }
                    calculatedEstimationTime.value = calculateEstimatedTime()
                }
            }

            is AppEvents.OnErrorEvent -> {
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowError(message = event.message))
                }
            }
            else -> {}
        }
    }

    private fun checkVehicleFinishDelivery(vehicle: Lorry?): Boolean {
        return vehicle != null && vehicle.packages.size == 0 && vehicle.packages.sumOf { it.weight } == 0.0
    }

    private fun calculateEstimatedTime(): Double {
        var time = 0.0
        _inTransitVehicle.forEach {
            time += it.packages.sumOf { pack -> pack.travelTime }
        }
        return time * 2
    }

    private fun getVehiclesAvailability() {
        _vehicles.forEach { lorry ->
            val totalWeight = lorry.packages.sumOf { it.weight }
            if (totalWeight >= lorry.maxWeight) {
                if (!_inTransitVehicle.contains(lorry)) {
                    _availableVehicle.remove(lorry)
                    lorry.status = VehicalStatus.DELIVERING
                    _inTransitVehicle.add(lorry)
                    calculatedEstimationTime.value = calculateEstimatedTime()
                }
            } else {
                if (!_availableVehicle.contains(lorry)) {
                    lorry.status = VehicalStatus.LOADING
                    _availableVehicle.add(lorry)
                }
            }
        }
    }

    private fun reset() {
        inputDistanceInKm.value = 0.0
        inputWeightInKg.value = 0.0
        inputOfferCode.value = ""
        calculatedCost.value = 0.0
        subtractAmount.value = 0.0
        totalDeliveryCost.value = 0.0
    }

    private fun calculateTotalDeliveryCost() {
        viewModelScope.launch {
            calculatedCost.value = baseDeliveryCost + (inputWeightInKg.value * 10) + (inputDistanceInKm.value * 5)
            val discount = if (offers.containsKey(inputOfferCode.value)) offers.getValue(inputOfferCode.value) else 0
            Log.e("dis", "$discount")
            val isCriteriaMet = when(discount) {
                10 -> {
                    inputDistanceInKm.value <= 200 && inputWeightInKg.value in 70.0 .. 200.0
                }
                7 -> {
                    inputDistanceInKm.value in 50.0..150.0 && inputWeightInKg.value in 100.0 .. 250.0
                }
                5 -> {
                    inputDistanceInKm.value in 50.0..250.0 && inputWeightInKg.value in 10.0 .. 150.0
                }
                else -> false
            }

            if (isCriteriaMet) {
                val subtract = (discount.toDouble() / 100) * calculatedCost.value
                criteriaMet.value = "(${discount}% of ${String.format("%.2f", calculatedCost.value)} i.e: Delivery Cost)"
                subtractAmount.value = subtract
                totalDeliveryCost.value = calculatedCost.value - subtractAmount.value
            } else {
                criteriaMet.value = "(Offer not applicable as criteria not met)"
                subtractAmount.value = 0.0
                totalDeliveryCost.value = calculatedCost.value
            }
        }
    }
}