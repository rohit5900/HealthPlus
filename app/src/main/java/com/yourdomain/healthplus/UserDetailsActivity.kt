package com.yourdomain.healthplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    private var effectiveUserId: String? = null
    private var effectiveUserEmail: String? = null

    private lateinit var textViewUserDetailsDisplay: TextView
    private lateinit var progressBarUserDetails: ProgressBar
    private lateinit var buttonLogout: Button

    companion object {
        private const val TAG = "UserDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        Log.d(TAG, "onCreate: Activity starting.")

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        db = Firebase.firestore // Instance should now use settings from MyApplication


        try {
            textViewUserDetailsDisplay = findViewById(R.id.textViewUserDetailsDisplay)
            progressBarUserDetails = findViewById(R.id.progressBarUserDetails)
            buttonLogout = findViewById(R.id.buttonLogout)
            Log.d(TAG, "onCreate: UI elements initialized.")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error initializing UI elements.", e)
            Toast.makeText(this, "Error setting up display.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        buttonLogout.setOnClickListener {
            Log.d(TAG, "Logout button clicked.")
            auth.signOut()
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("FROM_SIGN_OUT", true) // Help LoginActivity know not to auto-login
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        if (currentUser != null) {
            effectiveUserId = currentUser!!.uid
            effectiveUserEmail = currentUser!!.email
            Log.i(TAG, "onCreate: Real Firebase user. UID: $effectiveUserId")
        } else {
            Log.e(TAG, "onCreate: ERROR - User not identified. Navigating to Login.")
            Toast.makeText(this, "Error: User not signed in.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        textViewUserDetailsDisplay.text = "User ID: $effectiveUserId\nEmail: $effectiveUserEmail\nLoading details..."

        if (effectiveUserId != null) {
            Log.d(TAG, "onCreate: Proceeding to load details for $effectiveUserId")
            loadUserDetailsFromFirestore(effectiveUserId!!)
        } else {
            textViewUserDetailsDisplay.append("\nCritical Error: User ID is null.")
            Log.e(TAG, "onCreate: Critical Error - effectiveUserId is null.")
            showLoading(false)
        }
    }

    private fun loadUserDetailsFromFirestore(userIdToLoad: String) {
        Log.i(TAG, "loadUserDetailsFromFirestore: Attempting for userId: '$userIdToLoad'")
        showLoading(true)

        db.collection("users").document(userIdToLoad).get()
            .addOnSuccessListener { documentSnapshot ->
                showLoading(false)
                if (documentSnapshot.exists()) {
                    Log.i(TAG, "loadUserDetailsFromFirestore: SUCCESS - Doc found for $userIdToLoad. Data: ${documentSnapshot.data}")
                    val name = documentSnapshot.getString("name") ?: "N/A"
                    val age = documentSnapshot.getLong("age")?.toString() ?: "N/A"

                    var detailsText = "\n--- Profile Details ---"
                    detailsText += "\nName: $name"
                    detailsText += "\nAge: $age"
                    // Potentially add more fields here if you display them
                    textViewUserDetailsDisplay.append(detailsText)
                } else {
                    Log.w(TAG, "loadUserDetailsFromFirestore: Document NOT FOUND for userId: $userIdToLoad")
                    textViewUserDetailsDisplay.append("\nProfile details not found. Please complete your profile.")
                    Toast.makeText(this, "Profile details not found.", Toast.LENGTH_SHORT).show()
                    // Optionally, you could redirect to CollectUserDetailsActivity here if the profile is truly empty
                    // val intent = Intent(this, CollectUserDetailsActivity::class.java)
                    // startActivity(intent)
                    // finish()
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.e(TAG, "loadUserDetailsFromFirestore: FAILURE for userId: '$userIdToLoad'. Msg: ${exception.message}", exception)
                var toastMessage = "Failed to load profile: ${exception.localizedMessage}"
                if (exception.message?.contains("offline") == true || exception.message?.contains("network error") == true) {
                    toastMessage = "Could not load profile. Please verify your internet connection or try again later. Cached data might be shown if available."
                } else if (exception.message?.contains("PERMISSION_DENIED") == true) {
                    toastMessage = "Error: Permission denied while loading profile."
                }
                textViewUserDetailsDisplay.append("\n$toastMessage")
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (::progressBarUserDetails.isInitialized) {
            progressBarUserDetails.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        if (::buttonLogout.isInitialized) {
            buttonLogout.isEnabled = !isLoading
        }
    }
}
