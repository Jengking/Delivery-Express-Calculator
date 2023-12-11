## Courier Delivery Demo Project
This project is to demonstrate 2 key calculations - package delivery price with or without offer code and the estimations time of delivery. The project uses Android Jetpack Compose Component and MVVM design patterns.

## Setup
To build the project, the latest version of Android studio is required and Android emulator need to be setup if no emulator have been setup. 
- To clone this project, at this project main page click Code button > Https > Copy the URL.
- Open Android Studio, File > New > Project from Version Control.
- Enter the copied URL and press the Clone button.
- Wait until all the processes is done.

### Main Screen
- The main screen is divided into 2 section, calculation for delivery price and Delivery time estimations

#### Delivery Price Calculations
This section is where the delivery price calculation is done. 
- Enter the desire value and it will calculate the price
- Press Add package to Storage button to save the data so it can be use in the Delivery time estimation section
- Enter as many package data as much as needed
  
![Screenshot_20231211_132401](https://github.com/Jengking/Delivery-Express-Calculator/assets/18139943/88b23839-b169-4008-ab0f-38ec7297a210)


#### Delivery Time Estimations
This where the delivery time estimation is done, currently it is only limited to 2 vehicles. Each vehicles has the max speed of 70 km per hour and max weight of 200 kg.
The mechanics for this section:
 - it will receive the saved packages from the first section as in storage
 - for each package it can assign a delivery vehicle to be use for delivery by pressing '+' - Assuming the vehicle the max carry weight is not yet reached
 - Once the vehicle max carry weight reached or slightly over the carry weight - the vehicle will move out for delivery
 - While the vehicle is in the delivery state, the operator can mark the package as delivered by pressing 'X' - Assuming the delivery event has occur
   
![Screenshot_20231211_132638](https://github.com/Jengking/Delivery-Express-Calculator/assets/18139943/4e1d69d3-c48d-4bd8-a1bc-b01781aa169e)


