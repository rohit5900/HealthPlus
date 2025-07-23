package com.yourdomain.healthplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CollectUserDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    private lateinit var editTextName: EditText
    private lateinit var editTextAge: EditText
    private lateinit var buttonSaveDetails: Button
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "CollectUserDetailsAct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_user_details)

        Log.d(TAG, "onCreate: Activity starting.")

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        db = Firebase.firestore // Instance should now use settings from MyApplication

        if (currentUser == null) {
            Log.e(TAG, "onCreate: CurrentUser is NULL. Redirecting to Login.")
            Toast.makeText(this, "Error: You need to be signed in.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        Log.d(TAG, "onCreate: User ${currentUser?.uid} is collecting details.")

        try {
            editTextName = findViewById(R.id.editTextName)
            editTextAge = findViewById(R.id.editTextAge)
            buttonSaveDetails = findViewById(R.id.buttonSaveDetails)
            progressBar = findViewById(R.id.progressBarCollectDetails)
            Log.d(TAG, "onCreate: UI elements initialized.")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error initializing UI.", e)
            Toast.makeText(this, "Error setting up details screen.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        showLoading(false)

        buttonSaveDetails.setOnClickListener {
            Log.d(TAG, "Save Details button clicked.")
            collectAndSaveDetails()
        }
    }

    private fun collectAndSaveDetails() {
        val name = editTextName.text.toString().trim()
        val ageString = editTextAge.text.toString().trim()

        if (name.isEmpty()) {
            editTextName.error = "Name is required"
            editTextName.requestFocus()
            Log.w(TAG, "Validation failed: Name is empty.")
            return
        }

        if (ageString.isEmpty()) {
            editTextAge.error = "Age is required"
            editTextAge.requestFocus()
            Log.w(TAG, "Validation failed: Age is empty.")
            return
        }

        val age = ageString.toIntOrNull()
        if (age == null || age <= 0 || age > 120) {
            editTextAge.error = "Enter a valid age"
            editTextAge.requestFocus()
            Log.w(TAG, "Validation failed: Invalid age value: $ageString")
            return
        }

        showLoading(true)

        val userDetails = hashMapOf(
            "uid" to currentUser!!.uid,
            "email" to currentUser!!.email,
            "name" to name,
            "age" to age
        )

        Log.d(TAG, "Attempting to save details for UID: ${currentUser!!.uid}, Details: $userDetails")

        db.collection("users").document(currentUser!!.uid)
            .set(userDetails, SetOptions.merge())
            .addOnSuccessListener {
                showLoading(false)
                Log.i(TAG, "User details SAVED to Firestore for UID: ${currentUser!!.uid}. Sync pending if offline.")
                Toast.makeText(this, "Details saved! Will sync when online.", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, UserDetailsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e(TAG, "Error SAVING user details to Firestore for UID: ${currentUser!!.uid}", e)
                // Note: Even on failure here, if due to network, Firestore might have queued the write.
                // The Toast message should reflect that it might sync later.
                Toast.makeText(this, "Error saving details: ${e.message}. Data might be saved when online.", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if(this::progressBar.isInitialized) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        if(this::buttonSaveDetails.isInitialized) {
            buttonSaveDetails.isEnabled = !isLoading
        }
        if(this::editTextName.isInitialized) {
            editTextName.isEnabled = !isLoading
        }
        if(this::editTextAge.isInitialized) {
            editTextAge.isEnabled = !isLoading
        }
    }
}
