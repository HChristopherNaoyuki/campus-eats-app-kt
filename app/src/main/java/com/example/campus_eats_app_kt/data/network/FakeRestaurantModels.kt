package com.example.campus_eats_app_kt.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkRestaurant(
    @Json(name = "restaurantID") val restaurantID: Int,
    @Json(name = "restaurantName") val restaurantName: String,
    @Json(name = "address") val address: String,
    @Json(name = "type") val type: String,
    @Json(name = "parkingLot") val parkingLot: Boolean
)

@JsonClass(generateAdapter = true)
data class NetworkMenuItem(
    @Json(name = "itemID") val itemID: Int,
    @Json(name = "itemName") val itemName: String,
    @Json(name = "itemDescription") val itemDescription: String,
    @Json(name = "itemPrice") val itemPrice: Double,
    @Json(name = "restaurantName") val restaurantName: String,
    @Json(name = "restaurantID") val restaurantID: Int,
    @Json(name = "imageUrl") val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class NetworkUser(
    @Json(name = "userEmail") val userEmail: String,
    @Json(name = "password") val password: String,
    @Json(name = "usercode") val usercode: String
)

@JsonClass(generateAdapter = true)
data class UserCodeResponse(
    @Json(name = "usercode") val usercode: String
)

@JsonClass(generateAdapter = true)
data class MasterOrder(
    @Json(name = "masterID") val masterID: Int,
    @Json(name = "userID") val userID: String,
    @Json(name = "usercode") val usercode: String,
    @Json(name = "restaurantID") val restaurantID: Int,
    @Json(name = "grandtotal") val grandtotal: Double
)

@JsonClass(generateAdapter = true)
data class IndividualOrder(
    @Json(name = "orderID") val orderID: Int,
    @Json(name = "user") val user: NetworkUser?,
    @Json(name = "userID") val userID: String,
    @Json(name = "itemName") val itemName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "itemPrice") val itemPrice: Double,
    @Json(name = "totalPrice") val totalPrice: Double,
    @Json(name = "masterID") val masterID: Int
)

@JsonClass(generateAdapter = true)
data class OrderResponse(
    @Json(name = "fullorder") val fullorder: List<IndividualOrder>,
    @Json(name = "grandTotal") val grandTotal: Double
)

@JsonClass(generateAdapter = true)
data class DeleteResponse(
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class DeleteMasterOrderResponse(
    @Json(name = "message") val message: String,
    @Json(name = "orderexits") val orderexits: List<MasterOrder>,
    @Json(name = "singleorders") val singleorders: List<IndividualOrder>
)

@JsonClass(generateAdapter = true)
data class DeleteSingleOrderResponse(
    @Json(name = "message") val message: String,
    @Json(name = "orderexits") val orderexits: IndividualOrder
)

@JsonClass(generateAdapter = true)
data class OrderRequest(
    @Json(name = "menuDTO") val menuDTO: List<OrderItemRequest>
)

@JsonClass(generateAdapter = true)
data class OrderItemRequest(
    @Json(name = "itemName") val itemName: String,
    @Json(name = "quantity") val quantity: Int
)

@JsonClass(generateAdapter = true)
data class RestaurantRequest(
    @Json(name = "restaurantName") val restaurantName: String,
    @Json(name = "address") val address: String,
    @Json(name = "type") val type: String,
    @Json(name = "parkingLot") val parkingLot: Boolean
)

@JsonClass(generateAdapter = true)
data class MenuItemRequest(
    @Json(name = "itemName") val itemName: String,
    @Json(name = "itemPrice") val itemPrice: Double,
    @Json(name = "itemDescription") val itemDescription: String,
    @Json(name = "imageUrl") val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    @Json(name = "userEmail") val userEmail: String,
    @Json(name = "password") val password: String
)
