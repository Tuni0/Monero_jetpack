package com.example.monero_jetpack

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = remember { FirebaseFirestore.getInstance() }

    // Pola do wpisania danych
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Proste hashowanie hasła
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Tytuł
        Text("Register", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        // Nazwa użytkownika
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        // E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        // Hasło
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = password.length < 6,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        // Info o za krótkim haśle
        if (password.length < 6) {
            Text("Password must be at least 6 characters", color = MaterialTheme.colorScheme.error)
        }

        // Powtórzenie hasła
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            isError = confirmPassword != password,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        // Info o niezgodności haseł
        if (confirmPassword != password) {
            Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
        }

        // Komunikat błędu
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk rejestracji
        Button(
            onClick = {
                if (password.length < 6) {
                    errorMessage = "Password too short"
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                val hashedPassword = hashPassword(password)

                // Tworzenie konta w Firebase
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                // Pobierz istniejących użytkowników, żeby nadać nowe ID
                                firestore.collection("users")
                                    .get()
                                    .addOnSuccessListener { result ->
                                        val nextId = result.size() + 1

                                        // Dane nowego użytkownika
                                        val userData = hashMapOf(
                                            "user_id" to nextId,
                                            "username" to username,
                                            "email" to email,
                                            "password" to hashedPassword,
                                            "address_token" to "",
                                            "hasWallet" to false
                                        )

                                        // Zapis do Firestore
                                        firestore.collection("users").document(it.uid).set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Registered & saved",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Przejście do tworzenia portfela
                                                navController.navigate("add_wallet") {
                                                    popUpTo("startup") { inclusive = true }
                                                }

                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Firestore error: ${e.message}"
                                            }
                                    }
                            }
                        } else {
                            errorMessage = "Registration failed: ${task.exception?.message}"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Register")
        }
    }
}
