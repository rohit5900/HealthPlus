package com.yourpackage; // Make sure this matches your actual package name

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserHealthDetails(
    val name: String = "",
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val age: Int = 0,
    @ServerTimestamp val lastUpdated: Date? = null // Optional: to track when it was saved
) {
    // No-argument constructor is required by Firestore for deserialization
    constructor() : this("", 0.0, 0.0, 0)
}
