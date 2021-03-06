package com.example.foursquareapplication.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foursquareapplication.helper.Constants
import com.example.foursquareapplication.model.Resource
import com.example.foursquareapplication.model.User
import com.example.foursquareapplication.network.AuthenticationApi
import com.example.foursquareapplication.network.FourSquareApiInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainRepository(private val application: Application) {
    private val authenticationApi =
        FourSquareApiInstance.getApiInstance(AuthenticationApi::class.java)


    fun registerUser(user: HashMap<String, String>): LiveData<User> {
        val registerUser: MutableLiveData<User> = MutableLiveData()
        val registerDetails = authenticationApi.registerUser(user)
        registerDetails.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.d("uservalue", response.body().toString())
                if (response.isSuccessful) {
                    registerUser.value = response.body()
                } else {
                    Toast.makeText(application, response.raw().toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                registerUser.value = (null)
                Toast.makeText(application, t.message, Toast.LENGTH_SHORT).show()
            }

        })
        return registerUser
    }

    fun authenticateUser(user: HashMap<String, String>): MutableLiveData<Resource<User>> {
        val loginUser: MutableLiveData<Resource<User>> = MutableLiveData()
        val authenticateCall = authenticationApi.authenticateUser(user)
        loginUser.postValue(Resource.loading())
        authenticateCall.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    if(response.body()?.getStatus() == Constants.STATUS_OK) {
                        loginUser.postValue(Resource.success(response.body()))
                    }else{
                        loginUser.postValue(Resource.error(response.body()?.getMessage().toString()))
                    }

                } else {
                    loginUser.postValue(Resource.error("Couldn't login! Check username or password"))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {

                loginUser.postValue(Resource.error(t.message.toString()))

            }

        })
        return loginUser
    }

    fun generateOtp(email : HashMap<String,String>) : LiveData<Resource<User>> {

        val userOtp : MutableLiveData<Resource<User>> = MutableLiveData()
        val generateOtpCall = authenticationApi.generateOtp(email)
        userOtp.postValue(Resource.loading())
        generateOtpCall.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {

                if(response.isSuccessful){
                    if(response.body()?.getStatus() == Constants.STATUS_OK)
                        userOtp.value = Resource.success(response.body())
                    else
                        userOtp.postValue(Resource.error(response.body()?.getMessage().toString()))
                }else{
                    userOtp.postValue(Resource.error("Could not send OTP!!"))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                userOtp.postValue(Resource.error(t.message.toString()))
            }

        })
        return userOtp
    }

    fun validateOtp( otp : HashMap<String,String>): LiveData<Resource<User>> {
        val userOtp : MutableLiveData<Resource<User>> = MutableLiveData()
        val validateOtpCall = authenticationApi.validateOtp(otp)
        userOtp.postValue(Resource.loading())
        validateOtpCall.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    if(response.body()?.getStatus() == Constants.STATUS_OK)
                        userOtp.value = Resource.success(response.body())
                    else
                        userOtp.postValue(Resource.error(response.body()?.getMessage().toString()))
                } else {
                    userOtp.postValue(Resource.error("Could not validate OTP"))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                userOtp.postValue(Resource.error(t.message.toString()))
            }

        })
          return userOtp
        }

    fun confirmPassword(password: HashMap<String, String>): LiveData<Resource<User>> {
        val userPassword : MutableLiveData<Resource<User>> = MutableLiveData()
        val validateOtpCall = authenticationApi.confirmPassword(password)
        userPassword.postValue(Resource.loading())
        validateOtpCall.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    if(response.body()?.getStatus() == Constants.STATUS_OK)
                        userPassword.value = Resource.success(response.body())
                    else
                        userPassword.postValue(Resource.error(response.body()?.getMessage().toString()))
                } else {
                    userPassword.postValue(Resource.error("Could not update password!!"))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
               userPassword.postValue(Resource.error(t.message.toString()))
            }

        })
        return userPassword
    }
}