package com.example.lab07

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_LOCATION = "EXTRA_LOCATION"
        const val EXTRA_DESC = "EXTRA_DESC"
        const val EXTRA_RSVP = "EXTRA_RSVP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        findViewById<TextView>(R.id.tvTitle).text =
            "Title: " + (intent.getStringExtra(EXTRA_TITLE) ?: "")
        findViewById<TextView>(R.id.tvLocation).text =
            "Location: " + (intent.getStringExtra(EXTRA_LOCATION) ?: "")
        findViewById<TextView>(R.id.tvDesc).text =
            "Description: " + (intent.getStringExtra(EXTRA_DESC) ?: "")

        findViewById<Button>(R.id.btnAttend).setOnClickListener { reply("I will attend") }
        findViewById<Button>(R.id.btnDecline).setOnClickListener { reply("I cannot attend") }
    }

    // Return the RSVP choice via the Activity Result API
    private fun reply(answer: String) {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_RSVP, answer))
        finish()
    }
}
