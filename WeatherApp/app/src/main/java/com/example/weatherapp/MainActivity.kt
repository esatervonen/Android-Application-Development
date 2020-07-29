package com.example.weatherapp

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.ui.main.SectionsPagerAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), AskCityDialogFragment.AddDialogListener, RewardedVideoAdListener {

        lateinit var mAdView : AdView
        private lateinit var mRewardedVideoAd: RewardedVideoAd

        // example call is : https://api.openweathermap.org/data/2.5/weather?q=Jyväskylä&APPID=YOUR_API_KEY&units=metric&lang=fi
        val API_LINK: String = "https://api.openweathermap.org/data/2.5/weather?q="
        val API_ICON: String = "https://openweathermap.org/img/w/"
        val API_KEY: String = "c2e7510371c3060a0c07d5373c02cc23"

        // add a few test cities
        val cities: MutableList<String> =
            mutableListOf("Oulu")

        // city index, used when data will be loaded
        var index: Int = 0

        companion object {
            var forecasts: MutableList<Forecast> = mutableListOf()
        }

        // load forecast
        private fun loadWeatherForecast(city: String) {
            // url for loading
            val url = "$API_LINK$city&APPID=$API_KEY&units=metric&lang=fi"

            // JSON object request with Volley
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
                    try {
                        // load OK - parse data from the loaded JSON
                        val mainJSONObject = response.getJSONObject("main")
                        val weatherArray = response.getJSONArray("weather")
                        val firstWeatherObject = weatherArray.getJSONObject(0)

                        // city, condition, temperature
                        val city = response.getString("name")
                        val condition = firstWeatherObject.getString("main")
                        val temperature = mainJSONObject.getString("temp") + " °C"
                        // time
                        val weatherTime: String = response.getString("dt")
                        val weatherLong: Long = weatherTime.toLong()
                        val formatter: DateTimeFormatter =
                            DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm:ss")
                        val dt = Instant.ofEpochSecond(weatherLong).atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(formatter).toString()
                        // icon
                        val weatherIcon = firstWeatherObject.getString("icon")
                        val url = "$API_ICON$weatherIcon.png"
                        var add = true

                        // add forecast object to the list if it's not already there
                        forecasts.forEach {
                            if (it.city == city) add = false
                        }
                        if(add) forecasts.add(Forecast(city, condition, temperature, dt, url))
                        // use Logcat window to check that loading really works
                        Log.d("WEATHER", "**** weatherCity = " + forecasts[index].city)
                        // load another city if not loaded yet
                        if ((++index) < cities.size) loadWeatherForecast(cities[index])
                        else {
                            Log.d("WEATHER", "*** ALL LOADED!")
                        }
                        setUI()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("WEATHER", "***** error: $e")
                        // hide progress bar
                        progressBar.visibility = View.INVISIBLE
                        // show Toast -> should be done better!!!
                        Toast.makeText(this, "Error loading weather forecast!", Toast.LENGTH_LONG)
                            .show()
                    }
                },
                Response.ErrorListener { error -> Log.d("PTM", "Error: $error") })

            // start loading data with Volley
            val queue = Volley.newRequestQueue(applicationContext)
            queue.add(jsonObjectRequest)

        }


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            MobileAds.initialize(this){}
            // Use an activity context to get the rewarded video instance.
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
            mRewardedVideoAd.rewardedVideoAdListener = this
            loadRewardedVideoAd()

            mAdView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)

            // Load weather forecasts
            loadWeatherForecast(cities[index])



        }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build())
    }

    override fun onRewarded(reward: RewardItem) {
        Toast.makeText(this, "onRewarded! currency: ${reward.type} amount: ${reward.amount}",
            Toast.LENGTH_SHORT).show()
        // Reward the user.
    }

    override fun onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdLoaded() {
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoCompleted() {
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show()
    }

    private fun setUI() {
        // hide progress bar
        progressBar.visibility = View.INVISIBLE
        // add adapber
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        // add fab
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            // create and show dialog
            val dialog = AskCityDialogFragment()
            dialog.show(supportFragmentManager, "AskNewItemDialogFragment")
        }

    }

    override fun onDialogPositiveClick(city: String) {
        // Create a Handler Object
        val handler = Handler(Handler.Callback {
            // Toast message
            Toast.makeText(applicationContext,it.data.getString("message"), Toast.LENGTH_SHORT).show()

            true
        })
        // Create a new Thread to insert data to list
        Thread(Runnable {

            cities.add(city)
            loadWeatherForecast(cities[index])

            val message = Message.obtain()
            message.data.putString("message","$city added!")
            handler.sendMessage(message)
        }).start()
    }
}
