package com.triple5.quizdown

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class SplashActivty : AppCompatActivity() {
    object Staticated {
        var SPLASH_TIME_OUT = 20000
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

            Handler().postDelayed({
                val startAct = Intent(this@SplashActivty, MainActivity::class.java)
                startActivity(startAct)
                // close this activity
                this.finish()
            }, SplashActivty.Staticated.SPLASH_TIME_OUT.toLong())
        }


    }





