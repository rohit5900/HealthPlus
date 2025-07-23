package com.yourdomain.healthplus

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MyApplication : Application() {
    companion object {
        private const val TAG = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application starting, initializing Firebase and Firestore persistence.")

        // Initialize FirebaseApp - This is good practice to ensure it's done early.
        // If you are using firebase-bom and other Firebase services, they often auto-initialize,
        // but explicit initialization is safe.
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence
        try {
            val db = FirebaseFirestore.getInstance()
            // Check if settings have already been applied (e.g. on app restart if process wasn't killed)
            // This check might be more robust depending on how Firestore SDK handles re-application.
            // For simplicity, we apply it. If it causes issues, more complex checks might be needed.
            if (db.firestoreSettings.isPersistenceEnabled) {
                Log.d(TAG, "Firestore persistence is already enabled.")
            } else {
                val settings = FirebaseFirestoreSettings.Builder(db.firestoreSettings)
                    .setPersistenceEnabled(true)
                    // Optional: Configure cache size. Default is 40MB.
                    // Use CACHE_SIZE_UNLIMITED for no limit, or specify bytes.
                    .setCacheSizeBytes(100 * 1024 * 1024) // 100 MB cache size
                    .build()
                db.firestoreSettings = settings
                Log.i(TAG, "Firestore offline persistence enabled with 100MB cache size.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling Firestore persistence", e)
        }
    }
}
