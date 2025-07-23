package com.yourdomain.healthplus // Or your actual package

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat // For formatting the BMI

class ResultsActivity : AppCompatActivity() {

    // Declare TextViews for displaying results
    private lateinit var textViewBmiValue: TextView
    private lateinit var textViewOtherMetricValue: TextView
    private lateinit var buttonCloseResults: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results) // Link to the XML layout

        // Initialize UI elements
        textViewBmiValue = findViewById(R.id.textViewBmiValue)
        textViewOtherMetricValue = findViewById(R.id.textViewOtherMetricValue)
        buttonCloseResults = findViewById(R.id.buttonCloseResults)

        // Retrieve the data passed from UserDetailsActivity
        val bmi = intent.getDoubleExtra("USER_BMI_RESULT", -1.0) // -1.0 is a default value if not found
        val otherMetric = intent.getStringExtra("USER_OTHER_METRIC")

        // Display the retrieved data
        val df = DecimalFormat("#.##") // Format BMI to two decimal places
        if (bmi != -1.0) {
            textViewBmiValue.text = df.format(bmi)
        } else {
            textViewBmiValue.text = "N/A" // Or some error message
        }

        if (otherMetric != null) {
            textViewOtherMetricValue.text = otherMetric
        } else {
            textViewOtherMetricValue.text = "N/A"
        }

        // Handle the close button click
        buttonCloseResults.setOnClickListener {
            finish() // Closes this activity and returns to the previous one (or exits if it's the last)
        }
    }
}