package com.example.campus_eats_app_kt.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkRestaurant(
    @param:Json(name = "restaurantID") val restaurantID: Int,
    @param:Json(name = "restaurantName") val restaurantName: String,
    @param:Json(name = "address") val address: String,
    @param:Json(name = "type") val type: String,
    @param:Json(name = "parkingLot") val parkingLot: Boolean,
)

@JsonClass(generateAdapter = true)
data class NetworkMenuItem(
    @param:Json(name = "itemID") val itemID: Int,
    @param:Json(name = "itemName") val itemName: String,
    @param:Json(name = "itemDescription") val itemDescription: String,
    @param:Json(name = "itemPrice") val itemPrice: Double,
    @param:Json(name = "restaurantName") val restaurantName: String,
    @param:Json(name = "restaurantID") val restaurantID: Int,
    @param:Json(name = "imageUrl") val imageUrl: String,
)

@JsonClass(generateAdapter = true)
data class NetworkUser(
    @param:Json(name = "userEmail") val userEmail: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "usercode") val usercode: String,
)

@JsonClass(generateAdapter = true)
data class UserCodeResponse(
    @param:Json(name = "usercode") val usercode: String,
)

@JsonClass(generateAdapter = true)
data class MasterOrder(
    @param:Json(name = "masterID") val masterID: Int,
    @param:Json(name = "userID") val userID: String,
    @param:Json(name = "usercode") val usercode: String,
    @param:Json(name = "restaurantID") val restaurantID: Int,
    @param:Json(name = "grandtotal") val grandtotal: Double,
)

@JsonClass(generateAdapter = true)
data class IndividualOrder(
    @param:Json(name = "orderID") val orderID: Int,
    @param:Json(name = "user") val user: NetworkUser?,
    @param:Json(name = "userID") val userID: String,
    @param:Json(name = "itemName") val itemName: String,
    @param:Json(name = "quantity") val quantity: Int,
    @param:Json(name = "itemPrice") val itemPrice: Double,
    @param:Json(name = "totalPrice") val totalPrice: Double,
    @param:Json(name = "masterID") val masterID: Int,
)

@JsonClass(generateAdapter = true)
data class OrderResponse(
    @param:Json(name = "fullorder") val fullorder: List<IndividualOrder>,
    @param:Json(name = "grandTotal") val grandTotal: Double,
)

@JsonClass(generateAdapter = true)
data class DeleteResponse(
    @param:Json(name = "message") val message: String,
)

@JsonClass(generateAdapter = true)
data class DeleteMasterOrderResponse(
    @param:Json(name = "singleorders") val singleorders: List<IndividualOrder>,
)

@JsonClass(generateAdapter = true)
data class DeleteSingleOrderResponse(
    @param:Json(name = "orderexits") val orderexits: IndividualOrder,
)

@JsonClass(generateAdapter = true)
data class OrderRequest(
    @param:Json(name = "menuDTO") val menuDTO: List<OrderItemRequest>,
)

@JsonClass(generateAdapter = true)
data class OrderItemRequest(
    @param:Json(name = "itemName") val itemName: String,
    @param:Json(name = "quantity") val quantity: Int,
)

@JsonClass(generateAdapter = true)
data class RestaurantRequest(
    @param:Json(name = "restaurantName") val restaurantName: String,
    @param:Json(name = "address") val address: String,
    @param:Json(name = "type") val type: String,
    @param:Json(name = "parkingLot") val parkingLot: Boolean,
)

@JsonClass(generateAdapter = true)
data class MenuItemRequest(
    @param:Json(name = "itemName") val itemName: String,
    @param:Json(name = "itemPrice") val itemPrice: Double,
    @param:Json(name = "itemDescription") val itemDescription: String,
    @param:Json(name = "imageUrl") val imageUrl: String,
)

@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    @param:Json(name = "userEmail") val userEmail: String,
    @param:Json(name = "password") val password: String,
)
