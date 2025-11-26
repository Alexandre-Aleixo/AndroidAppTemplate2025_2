package com.ifpr.androidapptemplate.data

import com.google.firebase.auth.FirebaseAuth


object UserAuth {

    private val firebaseAuth = FirebaseAuth.getInstance()


    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid


    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null
}