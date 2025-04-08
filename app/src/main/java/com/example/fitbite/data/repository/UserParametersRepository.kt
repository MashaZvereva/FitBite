package com.example.fitbite.data.repository

import android.util.Log
import com.example.fitbite.data.model.UserParameters
import com.example.fitbite.data.network.RetrofitInstance

//class UserParametersRepository {
//
//    suspend fun getUserParameters(): UserParameters? {
//        return try {
//            val response = RetrofitInstance.api.getUserParameters()
                //            Log.d("UserParametersRepository", "UserParameters fetched: $response")
                //            response
                //        } catch (e: Exception) {
//            Log.e("UserParametersRepository", "Failed to fetch UserParameters: ${e.message}")
                //            null
                //        }
            //    }
//
//    suspend fun updateUserParameters(userParams: UserParameters): Boolean {
//        return try {
//            val response = RetrofitInstance.api.updateUserParameters(userParams)
                //            if (response.isSuccessful) {
//                Log.d("UserParametersRepository", "UserParameters updated successfully.")
                        //                true
                        //            } else {
//                Log.e("UserParametersRepository", "Update failed with code: ${response.code()}")
                        //                false
                        //            }
                //        } catch (e: Exception) {
//            Log.e("UserParametersRepository", "Failed to update parameters: ${e.message}")
                //            false
                //        }
            //    }
//}
