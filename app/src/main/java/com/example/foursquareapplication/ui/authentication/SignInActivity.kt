package com.example.foursquareapplication.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.foursquareapplication.R
import com.example.foursquareapplication.databinding.ActivitySigninBinding
import com.example.foursquareapplication.helper.Constants
import com.example.foursquareapplication.model.Status
import com.example.foursquareapplication.ui.HomeActivity
import com.example.foursquareapplication.viewmodel.AuthenticationViewModel
import kotlin.math.sign

class SignInActivity : AppCompatActivity() {
    private lateinit var signInBinding: ActivitySigninBinding
    private lateinit var authenticationViewModel : AuthenticationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signInBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(signInBinding.root)

        authenticationViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(AuthenticationViewModel::class.java)

        val createAccount = findViewById<TextView>(R.id.create_account)
        createAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }


        setSkipLoginListener()
        setSignUpOnClickListener()
        setForgotPasswordOnclickListener()
    }

    private fun setSkipLoginListener() {
        signInBinding.signinSkip.setOnClickListener {
            val sharedPreferences = getSharedPreferences(
                Constants.USER_PREFERENCE,
                MODE_PRIVATE
            )
            val sharedEditor = sharedPreferences.edit()
            sharedEditor.putBoolean(Constants.GUEST_USER,true)
            sharedEditor.apply()
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }
    }

    private fun setForgotPasswordOnclickListener() {
       signInBinding.signInForgotPassword.setOnClickListener {
           forgotPassword()
       }
    }

    private fun forgotPassword() {
        val signInUserName = signInBinding.signinUsername.text.toString()
        if(signInUserName.isEmpty()){
            signInBinding.signinUsername.error = "Enter Registered Email to Send OTP"
            signInBinding.signinUsername.requestFocus()
        }else{
            val email = hashMapOf("email" to signInUserName)
            authenticationViewModel.generateOtp(email).observe(this){
                when(it.status){
                    Status.LOADING ->{showProgressBar()}
                    Status.SUCCESS ->{
                        val response = it.data
                        Toast.makeText(applicationContext, response?.getMessage(), Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                        val intent = Intent(this,ForgotPassword::class.java)
                        intent.putExtra("email" , signInUserName)
                        startActivity(intent)
                    }
                    Status.ERROR -> {
                        hideProgressBar()
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    private fun setSignUpOnClickListener() {
        signInBinding.login.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        val signInUserName = signInBinding.signinUsername.text.toString()
        val signInPassword = signInBinding.siginPassword.text.toString()
        if(validateUserInputs(signInUserName,signInPassword)) {
                        val loginUser = hashMapOf("email" to signInUserName, "password" to signInPassword)
                        val authenticateUser = authenticationViewModel.authenticateUser(loginUser)
                        authenticateUser.observe(this, {

                            when (it.status) {
                                Status.LOADING -> {
                                    showProgressBar()
                                }
                                Status.SUCCESS -> {
                                    val data = it.data
                                    if (data != null && data.getData().getUserData()!=null) {
                                        val sharedPreferences = getSharedPreferences(
                                            Constants.USER_PREFERENCE,
                                            MODE_PRIVATE
                                        )
                                        val sharedEditor = sharedPreferences.edit()
                                        val userId =
                                            data.getData().getUserData().getUserId().toString()
                                        val userName = data.getData().getUserData().getUserName()
                                        val userImage = data.getData().getUserData().getImage()
                                        val token = data.getData().getToken()
                                        sharedEditor.putString(Constants.USER_ID, userId)
                                        sharedEditor.putString(Constants.USER_NAME, userName)
                                        sharedEditor.putString(Constants.USER_IMAGE, userImage)
                                        sharedEditor.putString(Constants.USER_TOKEN, token)
                                        sharedEditor.remove(Constants.GUEST_USER)
                                        sharedEditor.apply()
                                        hideProgressBar()
                                        authenticateUser.removeObservers(this)
                                        startActivity(Intent(this, HomeActivity::class.java))
                                        finish()
                                    }
                                }
                                Status.ERROR -> {
                                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                                    hideProgressBar()
                                }

                            }

                        })}
                }

    private fun showProgressBar() {
        signInBinding.progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar(){
        signInBinding.progressBar.visibility = View.GONE
    }

    private fun validateUserInputs(email: String, password: String): Boolean {
        var isValid = false
        when{
            email.isEmpty() ->{
                signInBinding.signinUsername.error = "Enter Email Address"
                signInBinding.signinUsername.requestFocus()

            }

            password.isEmpty()->{
                signInBinding.siginPassword.error = "Enter Password"
                signInBinding.siginPassword.requestFocus()

            }

            else-> isValid = true
        }
        return isValid


    }
}
