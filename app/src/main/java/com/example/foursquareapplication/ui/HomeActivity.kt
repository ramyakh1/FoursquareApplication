package com.example.foursquareapplication.ui

import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.foursquareapplication.adapter.HomeTabAdapter
import com.example.foursquareapplication.R
import com.example.foursquareapplication.helper.Constants
import com.example.foursquareapplication.helper.GPSTracker
import com.example.foursquareapplication.helper.LocationService
import com.example.foursquareapplication.ui.authentication.SignInActivity
import com.example.foursquareapplication.ui.search.SearchActivity
import com.example.foursquareapplication.viewmodel.LocationViewModel
import com.example.foursquareapplication.viewmodel.PlaceViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlin.math.log

class HomeActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var gpsTracker : GPSTracker
    private lateinit var locationService: LocationService
    private lateinit var listener : LocationListener
    private lateinit var placeViewModel: PlaceViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slide_menu)
        getUserLocation()
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences(Constants.USER_PREFERENCE, MODE_PRIVATE)
        placeViewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_home)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.menu_icon)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawer = findViewById<DrawerLayout>(R.id.drawer)
        val nav_view = findViewById<NavigationView>(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUserProfile(nav_view)
        disableLogout(nav_view)
        val tabs = findViewById<TabLayout>(R.id.tab_home)
        val pager = findViewById<ViewPager>(R.id.home_pager)

        tabs.addTab(tabs.newTab().setText("Near you"))
        tabs.addTab(tabs.newTab().setText("Toppick"))
        tabs.addTab(tabs.newTab().setText("Popular"))
        tabs.addTab(tabs.newTab().setText("Lunch"))
        tabs.addTab(tabs.newTab().setText("Coffee"))
        tabs.tabGravity = TabLayout.GRAVITY_FILL


        val adapter = HomeTabAdapter(supportFragmentManager, tabs.tabCount)
        pager.adapter = adapter

        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        pager.offscreenPageLimit = 3
        val intent=intent
        val getPosition=intent.getIntExtra("position",0)
        pager.currentItem = getPosition

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.fav -> {
                    val intent = Intent(this, FavouriteActivity::class.java)
                    startActivity(intent)
                }

                R.id.addReview ->{
                    val intent = Intent(this, FeedbackActivity::class.java)
                    startActivity(intent)
                }
               R.id.aboutUs ->{
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                }
                R.id.logOut ->{
                    logOutUser()
                }
                else ->{
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
            drawer.closeDrawers()
            true
        }
    }

    private fun disableLogout(navView : NavigationView?) {
        val menu = navView?.menu
        val logout = menu?.findItem(R.id.logOut)
        logout?.isEnabled = sharedPreferences.contains(Constants.USER_ID)
    }

    private fun logOutUser() {
        val editor = sharedPreferences.edit()
        editor.remove(Constants.USER_ID)
        editor.apply()
        startActivity(Intent(this,SignInActivity::class.java))
        finish()
    }

    private fun setUserProfile(navView: NavigationView?) {
        val header = navView?.getHeaderView(0)
        val profilePicture = header?.findViewById<ImageView>(R.id.profile_picture)
        val userName = header?.findViewById<TextView>(R.id.user_name)
        if(sharedPreferences.contains(Constants.USER_ID)){
            val name = sharedPreferences.getString(Constants.USER_NAME,"")
            val image = sharedPreferences.getString(Constants.USER_IMAGE,"")
            userName?.text = name
            if (profilePicture != null) {
                Glide.with(applicationContext).load(image).into(profilePicture)
            }
        }
        val userDetails = header?.findViewById<ConstraintLayout>(R.id.user_details)
        userDetails?.setOnClickListener {
            if(sharedPreferences.contains(Constants.GUEST_USER)){
                startActivity(Intent(this,SignInActivity::class.java))
            }
        }
    }

    private fun getUserLocation() {
        gpsTracker = GPSTracker(this)
        locationService = LocationService()
        locationService.init(this)
        setGPSLocationListener()
        getGPSLocation()

    }
    private fun setGPSLocationListener(){
        listener = object : LocationListener{
            override fun onLocationChanged(location: Location) {
                gpsTracker.stopGps(listener)
                locationViewModel.setLocation(location)
                //Toast.makeText(applicationContext, "$location", Toast.LENGTH_SHORT).show()

            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?
            ) {}

        }
    }

    private fun getGPSLocation(){
        if(gpsTracker.canGetLocation())
            gpsTracker.getLocation(listener)
        else
            getServiceLocation()
    }


    private fun getServiceLocation(){
        locationService.getLocation(this, { location ->
            locationViewModel.setLocation(location as Location)

        },{
            Toast.makeText(this, "Cannot fetch location", Toast.LENGTH_SHORT).show()
        }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(gpsTracker.canGetLocation()){
            gpsTracker.getLocation(listener)
        }else {
            locationService.onRequestPermissionsResult(this, requestCode, grantResults)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationService.onActivityResult(this,requestCode,resultCode)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menu?.findItem(R.id.menu_search_home)?.setOnMenuItemClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu?.findItem(R.id.menu_filter_home)?.setOnMenuItemClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("filter","filter")
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

