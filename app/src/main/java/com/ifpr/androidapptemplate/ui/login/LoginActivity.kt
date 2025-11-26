package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText // Importação útil
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.usuario.CadastroUsuarioActivity

class LoginActivity : AppCompatActivity() {

    // Agora usa TextInputEditText ou apenas EditText, dependendo da sua preferência
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton // Usando MaterialButton
    private lateinit var registerButton: MaterialButton // Renomeado de registerLink para registerButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "signInWithEmail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)

        // Inicializa o Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // CORREÇÃO: Usando os novos IDs do activity_login.xml
        emailEditText = findViewById(R.id.loginEmailEditText)
        passwordEditText = findViewById(R.id.loginPasswordEditText)
        loginButton = findViewById(R.id.loginButton) // Novo ID do botão de login
        registerButton = findViewById(R.id.registerButton) // Novo ID do botão de registro
        btnGoogleSignIn = findViewById<SignInButton>(R.id.googleSignInButton) // ID corrigido para Google Sign In

        // CORREÇÃO: Usando o novo botão de registro
        registerButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                CadastroUsuarioActivity::class.java
            )
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }

        // Configuration do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up the sign-in button click handler
        btnGoogleSignIn.setOnClickListener {
            signInGoogle()
        }
    }

    // ... (O restante da Activity permanece o mesmo)

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Navegue para a proxima atividade
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(
                applicationContext,
                "Email ou senha incorretos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido, navegar para a atividade principal ou atualizar UI
                    Log.d(TAG, "signInWithGoogle:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    // Tratar falha de login
                    Log.w(TAG, "signInWithGoogle:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Tratar falha de login
            }
        }
    }
}