package com.ifpr.androidapptemplate.ui.usuario

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class PerfilUsuarioFragment : Fragment() {

    private lateinit var userProfileImageView: ImageView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerEnderecoEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var inputLayoutSenha: TextInputLayout
    private lateinit var inputLayoutConfirmarSenha: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button
    private lateinit var changePhotoButton: View
    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentBase64Image: String? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            userProfileImageView.setImageURI(it)
            currentBase64Image = uriToBase64(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false)
        auth = FirebaseAuth.getInstance()

        userProfileImageView = view.findViewById(R.id.userProfileImageView)
        registerNameEditText = view.findViewById(R.id.registerNameEditText)
        registerEmailEditText = view.findViewById(R.id.registerEmailEditText)
        registerEnderecoEditText = view.findViewById(R.id.registerEnderecoEditText)
        registerPasswordEditText = view.findViewById(R.id.registerPasswordEditText)
        registerConfirmPasswordEditText = view.findViewById(R.id.registerConfirmPasswordEditText)
        inputLayoutSenha = view.findViewById(R.id.input_layout_senha)
        inputLayoutConfirmarSenha = view.findViewById(R.id.input_layout_confirmar_senha)
        registerButton = view.findViewById(R.id.salvarButton)
        sairButton = view.findViewById(R.id.sairButton)
        changePhotoButton = view.findViewById(R.id.changePhotoButton)

        try {
            usersReference = FirebaseDatabase.getInstance().getReference("users")
        } catch (e: Exception) {
            Log.e("DatabaseReference", "Erro ao obter referência", e)
        }

        val user = auth.currentUser

        if (user != null) {
            sairButton.visibility = View.VISIBLE
            inputLayoutSenha.visibility = View.GONE
            inputLayoutConfirmarSenha.visibility = View.GONE
            registerEmailEditText.isEnabled = false
        }

        registerButton.setOnClickListener { updateUser() }
        sairButton.setOnClickListener { signOut() }
        changePhotoButton.setOnClickListener { selectImageLauncher.launch("image/*") }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var userFirebase = auth.currentUser
        if(userFirebase != null){
            registerNameEditText.setText(userFirebase.displayName)
            registerEmailEditText.setText(userFirebase.email)
            recuperarDadosUsuario(userFirebase.uid)
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

                Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Base64Converter", "Erro ao converter Uri para Base64: ${e.message}")
            null
        }
    }

    private fun displayBase64Image(base64: String?) {
        if (base64 != null && base64.isNotEmpty()) {
            try {
                val decodedString = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                userProfileImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("DisplayImage", "Erro ao decodificar Base64: ${e.message}")
                userProfileImageView.setImageResource(R.drawable.ic_person)
            }
        } else {
            userProfileImageView.setImageResource(R.drawable.ic_person)
        }
    }

    fun recuperarDadosUsuario(usuarioKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.child(usuarioKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        registerEnderecoEditText.setText(it.endereco ?: "")

                        currentBase64Image = it.base64Image
                        displayBase64Image(currentBase64Image)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
            }
        })
    }

    private fun updateUser() {
        val name = registerNameEditText.text.toString().trim()
        val endereco = registerEnderecoEditText.text.toString().trim()
        val user = auth.currentUser

        if (user != null) {
            val usuario = Usuario(user.uid, name, user.email, endereco, currentBase64Image)
            saveUserToDatabase(usuario)
        } else {
            Toast.makeText(context, "Não foi possível encontrar o usuário logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        if (usuario.key != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(usuario.nome)
                .build()

            auth.currentUser?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        usersReference.child(usuario.key.toString()).setValue(usuario)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                requireActivity().supportFragmentManager.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Falha ao atualizar o usuário no Database", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Falha ao atualizar o nome do usuário no Auth", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "ID inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(context, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }
}