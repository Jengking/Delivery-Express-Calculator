package com.express.packagecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.express.packagecalculator.events.AppEvents
import com.express.packagecalculator.events.UiEvent
import com.express.packagecalculator.model.Lorry
import com.express.packagecalculator.model.Package
import com.express.packagecalculator.model.VehicalStatus
import com.express.packagecalculator.ui.theme.PackageCalculatorTheme
import com.express.packagecalculator.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PackageCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MainContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent() {
    val viewModel: MainViewModel = viewModel()
    val labelItems = listOf("Package Calculator", "Delivery Estimation")
    val pagerState = rememberPagerState(initialPage = 0)
    var tabIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = tabIndex) {
            labelItems.forEachIndexed { index, s ->
                Tab(selected = tabIndex == index, onClick = {
                    tabIndex = index
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }) {
                    Text(text = s)
                }
            }
        }

        HorizontalPager(
            pageCount = labelItems.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    PackageScreen(viewModel)
                }

                else -> {
                    DeliveryScreen(viewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeliveryScreen(viewModel: MainViewModel = viewModel()) {

    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is UiEvent.ShowError -> {
                    scope.launch {
                        snackbarState.showSnackbar(message = event.message, withDismissAction = false, duration = SnackbarDuration.Short)
                    }
                }
            }
        }
    }

    Box {
        SnackbarHost(hostState = snackbarState, modifier = Modifier.align(Alignment.BottomCenter))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Package Remaining: ${viewModel.storage.size}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vehicles Available: ${viewModel.availableVehicle.size}",
                    modifier = Modifier.weight(.5f)
                )
                Text(text = "Current Time: ${String.format("%.2f hrs", viewModel.calculatedEstimationTime.value)}", modifier = Modifier.weight(.5f))
            }
            Divider()
            //available package
            if (viewModel.storage.isNotEmpty()) {
                Text(text = "Packages in Storage")
                LazyColumn(modifier = Modifier.fillMaxWidth(), userScrollEnabled = false) {
                    viewModel.storage.forEachIndexed { index, p ->
                        item {
                            StorageItem(availableVehicle = viewModel.availableVehicle, item = p,
                                onError = {
                                    viewModel.onEvent(AppEvents.OnErrorEvent("Reach max weight capacity or no vehicle available."))
                                },
                                onClicked = {
                                    viewModel.onEvent(
                                        AppEvents.AddPackageToVehicle(
                                            pack = p,
                                            vehicle = it
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                Divider()
            }
            //vehicles info
            val atLoading :List<Lorry> = viewModel.availableVehicle.filter { it.status == VehicalStatus.LOADING }
            if (atLoading.isNotEmpty()) {
                Text(text = "Vehicle Load Information")
                val loadingVehicle = viewModel.availableVehicle.filter { it.packages.isNotEmpty() }
                LazyColumn(modifier = Modifier.fillMaxWidth(), userScrollEnabled = true) {
                    items(loadingVehicle) {
                        DeliveryItem(lorry = it)
                    }
                }
                Divider()
            }
            //in transit
            val inTransit = viewModel.inTransitVehicles.filter { it.status == VehicalStatus.DELIVERING }
            if (inTransit.isNotEmpty()) {
                Text(text = "Vehicle Delivery Estimations")
                LazyColumn(modifier = Modifier.fillMaxWidth(), userScrollEnabled = false) {
                    items(inTransit) {
                        DeliveryItem(lorry = it) { vehicleName, pack ->
                            viewModel.onEvent(AppEvents.OnPackageDelivered(vehicleName, pack))
                        }
                    }
                }
                Divider()
            }
            //Estimation
            val loadingTotal = viewModel.availableVehicle.filter { lorry -> lorry.packages.sumOf { it.travelTime } != 0.0  }
            if (loadingTotal.isNotEmpty()) {
                Column {
                    loadingTotal.forEach { lorry ->
                        val packTime = (lorry.packages.sumOf { it.travelTime }) * 2
                        if (packTime != 0.0) {
                            Text(text = "${lorry.name} will be available after ${String.format("%.2f hrs", packTime)}")
                        }
                    }
                    Divider()
                }
            }

            val deliveryTotal = viewModel.inTransitVehicles.filter { lorry -> lorry.packages.sumOf { it.travelTime } != 0.0 }
            if (deliveryTotal.isNotEmpty()) {
                if (deliveryTotal.size > 1) {
                    deliveryTotal.forEachIndexed { index, lorry ->
                        val packTime = (lorry.packages.sumOf { it.travelTime }) * 2
                        if (index == 0) {
                            Text(text = "${lorry.name} will be available first after ${String.format("%.2f hrs", packTime)}")
                        } else {
                            Text(text = "${lorry.name} will be available after ${String.format("%.2f hrs", packTime)}")
                        }
                    }
                } else {
                    val packTime = (deliveryTotal[0].packages.sumOf { it.travelTime }) * 2
                    if (packTime != 0.0) {
                        Text(text = "${deliveryTotal[0].name} will be available first after ${String.format("%.2f hrs", packTime)}")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PackageScreen(viewModel: MainViewModel = viewModel()) {

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val packageWeight = remember { mutableStateOf("") }
    val deliveryDistance = remember { mutableStateOf("") }
    val offerCode = remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Package weight(kg):", modifier = Modifier.weight(1f))
            BasicInputField(
                interactionSource = interactionSource,
                modifier = Modifier,
                value = packageWeight.value,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                onAction = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ) {
                packageWeight.value = it
                if (packageWeight.value.isNotEmpty()) {
                    viewModel.onEvent(AppEvents.OnWeightInputChanged(packageWeight.value.toDouble()))
                }
                return@BasicInputField
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Delivery Distances(km):", modifier = Modifier.weight(1f))
            BasicInputField(
                interactionSource = interactionSource,
                modifier = Modifier,
                value = deliveryDistance.value,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                onAction = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ) {
                deliveryDistance.value = it
                if (deliveryDistance.value.isNotEmpty()) {
                    viewModel.onEvent(AppEvents.OnDistanceInputChanged(deliveryDistance.value.toDouble()))
                }
                return@BasicInputField
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Offer code (If any)", modifier = Modifier.weight(1f))
            BasicInputField(
                interactionSource = interactionSource,
                modifier = Modifier,
                value = offerCode.value,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                onAction = KeyboardActions {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ) {
                offerCode.value = it
                if (offerCode.value.isNotEmpty()) {
                    viewModel.onEvent(AppEvents.OnOfferCodeChanged(offerCode.value))
                }
                return@BasicInputField
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Delivery Cost", modifier = Modifier.weight(1f))
            Text(text = String.format("%.2f", viewModel.calculatedCost.value))
        }

        Divider()

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Discount")
                Text(text = viewModel.criteriaMet.value)
            }
            Text(text = String.format("-%.2f", viewModel.subtractAmount.value))
        }

        Divider()

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Total Cost", modifier = Modifier.weight(1f))
            Text(text = String.format("%.2f", viewModel.totalDeliveryCost.value))
        }

        Divider()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp), contentAlignment = Alignment.Center
        ) {

            Button(
                onClick = {
                    if (packageWeight.value.isNotEmpty() && deliveryDistance.value.isNotEmpty()) {
                        val pack = Package(
                            weight = packageWeight.value.toDouble(),
                            distance = deliveryDistance.value.toDouble(),
                            discountCode = offerCode.value,
                            totalCost = viewModel.totalDeliveryCost.value,
                            name = "PKG${Random.nextInt(1, 1000)}"
                        )
                        viewModel.onEvent(AppEvents.AddPackageToStorage(pack))
                        packageWeight.value = "0"
                        deliveryDistance.value = "0"
                        offerCode.value = ""
                    }
                },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = "Add Package to storage", color = Color.White)
            }
        }
    }
}

@Composable
private fun DeliveryItem(
    lorry: Lorry,
    onDeliverPackage: (String, Package) -> Unit = { vehicleName, packageItem ->}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = if (lorry.packages.size > 1) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val statuslbl = when(lorry.status) {
                VehicalStatus.LOADING -> "Loaded "
                VehicalStatus.DELIVERING -> "Delivering "
                else -> ""
            }
            Text(text = lorry.name, style = TextStyle(fontWeight = FontWeight.Bold))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lorry.packages.forEach {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "$statuslbl ${it.name}", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(text = "${it.distance} km/${lorry.speed} km/hrs", color = Color.DarkGray, style = TextStyle(fontSize = 12.sp))
                        }
                        Text(text = String.format("%.2f hrs", it.travelTime), color = Color.Black, style = TextStyle(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)

                        if (lorry.status == VehicalStatus.DELIVERING) {
                            Image(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "deliver package",
                                modifier = Modifier.clickable {
                                    onDeliverPackage(lorry.name, it)
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageItem(
    availableVehicle: List<Lorry> = emptyList(),
    item: Package,
    onClicked: (Lorry) -> Unit = {},
    onError: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.name, style = TextStyle(fontWeight = FontWeight.Bold))
                Text(text = "${item.weight}kg")
                Text(text = "${item.distance}km")
            }

            Image(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.clickable {
                    expanded = !expanded
                })

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                availableVehicle.forEachIndexed { index, lorry ->
                    DropdownMenuItem(text = {
                        Text(text = lorry.name)
                    }, onClick = {
                        expanded = false
                        val totalWeight = lorry.packages.sumOf { it.weight } + item.weight
                        if (totalWeight <= lorry.maxWeight) {
                            onClicked(lorry)
                        } else {
                            onError()
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun BasicInputField(
    modifier: Modifier = Modifier,
    value: String = "",
    minHeight: Dp = 20.dp,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Done,
    onAction: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource,
    onValueChanged: (String) -> Unit = {}
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = onValueChanged,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = onAction,
        singleLine = isSingleLine,
        maxLines = 1,
        textStyle = TextStyle(
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            color = Color.Black
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .background(color = Color.Transparent)
            .drawWithContent {
                val strokeWidth = 1.dp.value * density
                val x = 0.dp.toPx()
                val y = size.height - strokeWidth / 2

                drawContent()
                drawLine(
                    color = if (isFocused) Color.Black else Color.LightGray,
                    start = Offset(x, y),
                    end = Offset(size.width - x, y),
                    strokeWidth = strokeWidth
                )
            }
            .defaultMinSize(minHeight = minHeight)
    )
}




