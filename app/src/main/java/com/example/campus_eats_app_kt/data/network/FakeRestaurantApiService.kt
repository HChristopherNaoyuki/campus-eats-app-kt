package com.example.campus_eats_app_kt.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("unused")
interface FakeRestaurantApiService
{
    // Restaurant endpoints
    @GET("api/Restaurant")
    suspend fun getAllRestaurants(): Response<List<NetworkRestaurant>>

    @GET("api/Restaurant")
    suspend fun getRestaurantsByCategory(
        @Query("category") category: String
    ): Response<List<NetworkRestaurant>>

    @GET("api/Restaurant")
    suspend fun filterRestaurants(
        @Query("address") address: String?,
        @Query("name") name: String?
    ): Response<List<NetworkRestaurant>>

    @GET("api/Restaurant/{id}")
    suspend fun getRestaurantById(
        @Path("id") id: Int
    ): Response<List<NetworkRestaurant>>

    @GET("api/Restaurant/{restaurantId}/menu")
    suspend fun getRestaurantMenu(
        @Path("restaurantId") restaurantId: Int
    ): Response<List<NetworkMenuItem>>

    @GET("api/Restaurant/{restaurantId}/menu")
    suspend fun getSortedMenu(
        @Path("restaurantId") restaurantId: Int,
        @Query("sortbyprice") sortOrder: String?
    ): Response<List<NetworkMenuItem>>

    @GET("api/Restaurant/items")
    suspend fun getAllItems(): Response<List<NetworkMenuItem>>

    @GET("api/Restaurant/items")
    suspend fun searchItemsByName(
        @Query("ItemName") itemName: String
    ): Response<List<NetworkMenuItem>>

    @GET("api/Restaurant/items")
    suspend fun getAllItemsSorted(
        @Query("sortbyprice") sortOrder: String?
    ): Response<List<NetworkMenuItem>>

    @POST("api/Restaurant")
    suspend fun addRestaurant(
        @Body restaurant: RestaurantRequest
    ): Response<NetworkRestaurant>

    @POST("api/Restaurant/{restaurantId}/additem")
    suspend fun addMenuItem(
        @Path("restaurantId") restaurantId: Int,
        @Body menuItem: MenuItemRequest
    ): Response<NetworkMenuItem>

    // User endpoints
    @GET("api/User")
    suspend fun getAllUsers(): Response<List<NetworkUser>>

    @GET("api/User/getusercode")
    suspend fun getUserCode(
        @Query("UserEmail") email: String,
        @Query("Password") password: String
    ): Response<UserCodeResponse>

    @POST("api/User/register")
    suspend fun registerUser(
        @Body registration: RegistrationRequest
    ): Response<NetworkUser>

    @DELETE("api/User/{apikey}")
    suspend fun deleteUser(
        @Path("apikey") apiKey: String
    ): Response<DeleteResponse>

    @PUT("api/User/{apikey}")
    suspend fun updatePassword(
        @Path("apikey") apiKey: String,
        @Body newPassword: String
    ): Response<NetworkUser>

    // Order endpoints
    @GET("api/Order")
    suspend fun getUserOrders(
        @Query("apikey") apiKey: String
    ): Response<List<MasterOrder>>

    @GET("api/Order/{masterId}")
    suspend fun getOrderByMasterId(
        @Path("masterId") masterId: Int,
        @Query("apikey") apiKey: String
    ): Response<List<IndividualOrder>>

    @POST("api/Order/{restaurantId}/makeorder")
    suspend fun createOrder(
        @Path("restaurantId") restaurantId: Int,
        @Query("apikey") apiKey: String,
        @Body orderRequest: OrderRequest
    ): Response<OrderResponse>

    @DELETE("api/Order/master/{masterId}")
    suspend fun deleteMasterOrder(
        @Path("masterId") masterId: Int,
        @Query("apikey") apiKey: String
    ): Response<DeleteMasterOrderResponse>

    @DELETE("api/Order/{orderId}")
    suspend fun deleteSingleOrder(
        @Path("orderId") orderId: Int,
        @Query("apikey") apiKey: String
    ): Response<DeleteSingleOrderResponse>
}
