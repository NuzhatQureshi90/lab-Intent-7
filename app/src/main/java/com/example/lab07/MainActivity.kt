package com.example.lab07

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDesc: EditText
    private lateinit var tvRsvp: TextView

    // Receives RSVP result from DetailActivity
    private val rsvpLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val answer = result.data?.getStringExtra(DetailActivity.EXTRA_RSVP)
            tvRsvp.text = "RSVP Status: $answer"
        }
    }

    // Notification permission (Android 13+)
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scheduleReminder()
        else Toast.makeText(this, "Notifications blocked", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etTitle = findViewById(R.id.etTitle)
        etLocation = findViewById(R.id.etLocation)
        etDesc = findViewById(R.id.etDesc)
        tvRsvp = findViewById(R.id.tvRsvp)

        NotificationHelper.createChannel(this)

        // Bonus: receive shared text into Description
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            etDesc.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
        }

        // 1. Explicit intent with extras + result
        findViewById<Button>(R.id.btnDetails).setOnClickListener {
            val i = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_TITLE, etTitle.text.toString())
                putExtra(DetailActivity.EXTRA_LOCATION, etLocation.text.toString())
                putExtra(DetailActivity.EXTRA_DESC, etDesc.text.toString())
            }
            rsvpLauncher.launch(i)
        }

        // 2. Share (implicit, ACTION_SEND + chooser)
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            val message = "Event: ${etTitle.text}\n" +
                "Location: ${etLocation.text}\n" +
                "Description: ${etDesc.text}"
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Campus Event: ${etTitle.text}")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(Intent.createChooser(send, "Share via"))
        }

        // 3. Open location in maps (implicit, with error handling)
        findViewById<Button>(R.id.btnMap).setOnClickListener {
            val loc = etLocation.text.toString().trim()
            if (loc.isEmpty()) {
                Toast.makeText(this, "Enter a location first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val geo = Uri.parse("geo:0,0?q=" + Uri.encode(loc))
            try {
                startActivity(Intent(Intent.ACTION_VIEW, geo))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No map app found", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Reminder after 10 seconds
        findViewById<Button>(R.id.btnRemind).setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleReminder()
            }
        }

        // Bonus: call organizer (dialer, no permission needed)
        findViewById<Button>(R.id.btnCall).setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+923001234567")))
        }
    }

    private fun scheduleReminder() {
        // Capture values now so the notification shows what was entered
        val title = etTitle.text.toString().ifBlank { "Campus Event" }
        val loc = etLocation.text.toString()
        val desc = etDesc.text.toString()
        Toast.makeText(this, "Reminder set for 10 seconds", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            NotificationHelper.showEventReminder(this, 1, title, loc, desc)
        }, 10_000)
    }
}
