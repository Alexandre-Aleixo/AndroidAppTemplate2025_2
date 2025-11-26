package com.ifpr.androidapptemplate.data

import com.google.firebase.auth.FirebaseAuth

/**
 * Helper object para encapsular a lógica de autenticação do Firebase.
 */
object UserAuth {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Retorna o ID do usuário logado (UID) ou null se não houver ninguém logado.
     * Este UID é usado para estruturar os dados no Realtime Database.
     */
    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * Verifica se o usuário está logado.
     */
    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null
}