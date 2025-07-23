package com.yourdomain.healthplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignIn: Button
    private lateinit var textViewGoToSignUp: TextView
    private lateinit var progressBarLogin: ProgressBar

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d(TAG, "onCreate: Activity starting.")

        auth = Firebase.auth
        db = Firebase.firestore // Instance should now use settings from MyApplication

        try {
            editTextEmail = findViewById(R.id.editTextEmail)
            editTextPassword = findViewById(R.id.editTextPassword)
            buttonSignIn = findViewById(R.id.buttonSignIn)
            textViewGoToSignUp = findViewById(R.id.textViewGoToSignUp)
            progressBarLogin = findViewById(R.id.progressBarLogin)
            Log.d(TAG, "onCreate: UI elements initialized.")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error initializing UI. Check XML IDs.", e)
            Toast.makeText(this, "Error setting up login screen.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        showLoading(false)

        buttonSignIn.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty()) {
                editTextEmail.error = "Email is required"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.error = "Enter a valid email"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                editTextPassword.error = "Password is required"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }
            signInUser(email, password)
        }

        textViewGoToSignUp.setOnClickListener {
            Log.d(TAG, "Sign Up text clicked. Navigating to SignUpActivity (placeholder).")
            Toast.makeText(this, "Navigate to SignUpActivity (Not Implemented)", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, SignUpActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun signInUser(email: String, password: String) {
        showLoading(true)
        Log.d(TAG, "signInUser: Attempting to sign in with email: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "signInWithEmail:SUCCESS. User: ${auth.currentUser?.uid}")
                    val user = auth.currentUser
                    checkUserDetailsAndNavigate(user)
                } else {
                    showLoading(false)
                    Log.w(TAG, "signInWithEmail:FAILURE", task.exception)
                    var errorMessage = "Authentication failed."
                    if (task.exception?.message?.contains("network error") == true ||
                        task.exception?.message?.contains("offline") == true) {
                        errorMessage = "Authentication failed. Please check your internet connection."
                    } else {
                        errorMessage = "Authentication failed: ${task.exception?.localizedMessage ?: "Unknown error"}"
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null && !isComingFromSignOutIntent()) {
            Log.d(TAG, "onStart: User '${currentUser.uid}' already signed in. Checking details status.")
            showLoading(true)
            checkUserDetailsAndNavigate(currentUser)
        } else {
            Log.d(TAG, "onStart: No user signed in or explicitly signed out.")
            showLoading(false)
        }
    }

    private fun isComingFromSignOutIntent(): Boolean {
        return intent.getBooleanExtra("FROM_SIGN_OUT", false)
    }

    private fun checkUserDetailsAndNavigate(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            Log.e(TAG, "checkUserDetailsAndNavigate: FirebaseUser is null.")
            showLoading(false)
            return
        }

        Log.d(TAG, "checkUserDetailsAndNavigate: Checking Firestore for details for UID: ${firebaseUser.uid}")
        val userDocRef = db.collection("users").document(firebaseUser.uid)

        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                    Log.i(TAG, "User details FOUND for UID: ${firebaseUser.uid}. Navigating to UserDetailsActivity.")
                    navigateTo(UserDetailsActivity::class.java)
                } else {
                    Log.w(TAG, "User details NOT found or incomplete for UID: ${firebaseUser.uid}. Navigating to CollectUserDetailsActivity.")
                    navigateTo(CollectUserDetailsActivity::class.java)
                }
            }
            .addOnFailureListener { e ->
                showLoading(false) // Make sure loading is hidden on failure
                Log.e(TAG, "Error checking user details in Firestore for UID: ${firebaseUser.uid}", e)
                var toastMessage = "Error checking profile: ${e.localizedMessage}"
                if (e.message?.contains("offline") == true || e.message?.contains("network error") == true) {
                    toastMessage = "Could not check profile. Please verify your internet connection or try again later."
                } else if (e.message?.contains("PERMISSION_DENIED") == true) {
                    toastMessage = "Error: Permission denied while checking profile."
                }
                Toast.makeText(baseContext, toastMessage, Toast.LENGTH_LONG).show()
                // Decide on fallback behavior:
                // Stay on Login (current behavior implicitly if navigateTo is not called)
                // OR navigate to CollectUserDetailsActivity if appropriate (e.g. if error means document definitely not there)
                // OR navigate to UserDetailsActivity (it should also handle load failures)
            }
    }

    private fun navigateTo(activityClass: Class<*>) {
        showLoading(false)
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        if (this::progressBarLogin.isInitialized) {
            progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        if (this::buttonSignIn.isInitialized) {
            buttonSignIn.isEnabled = !isLoading
        }
        if (this::textViewGoToSignUp.isInitialized) {
            textViewGoToSignUp.isClickable = !isLoading
            textViewGoToSignUp.alpha = if(isLoading) 0.5f else 1.0f
        }
        if (this::editTextEmail.isInitialized) {
            editTextEmail.isEnabled = !isLoading
        }
        if (this::editTextPassword.isInitialized) {
            editTextPassword.isEnabled = !isLoading
        }
    }
}
