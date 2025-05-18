package com.example.monero_jetpack

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.firestore.FirebaseFirestore

// Ekran startowy przy starcie aplikacji
@Composable
fun StartupScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val credentialManager = CredentialManager.create(context)

    // Pobierz z local.properties
    val clientId = BuildConfig.CLIENT_ID

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val signInRequest = GetCredentialRequest.Builder()
                            .addCredentialOption(
                                GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(clientId)
                                    .setAutoSelectEnabled(true)
                                    .setNonce("nonce")
                                    .build()
                            ).build()

                        try {
                            val result = credentialManager.getCredential(
                                request = signInRequest,
                                context = context
                            )
                            handleSignIn(result, context, navController, firestore, auth)
                        } catch (e: GetCredentialException) {
                            try {
                                val fallbackRequest = GetCredentialRequest.Builder()
                                    .addCredentialOption(
                                        GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(clientId)
                                            .setNonce("nonce")
                                            .build()
                                    ).build()

                                val fallbackResult = credentialManager.getCredential(
                                    request = fallbackRequest,
                                    context = context
                                )
                                handleSignIn(
                                    fallbackResult,
                                    context,
                                    navController,
                                    firestore,
                                    auth
                                )
                            } catch (ex: Exception) {
                                Log.e("GoogleLogin", "Sign-in failed: ${ex.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icons8_google),
                    contentDescription = "Google Icon",
                    modifier = Modifier
                        .height(20.dp)
                        .aspectRatio(1f)
                        .align(Alignment.CenterVertically),
                )
                Text(text = "Continue with Google", modifier = Modifier.padding(6.dp))
            }

            OrDivider()

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Log in with Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("register") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Register with Email")
            }
        }
    }
}

// Obsługa logowania google
fun handleSignIn(
    result: GetCredentialResponse,
    context: android.content.Context,
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val credential = result.credential
    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener {
                            val user = auth.currentUser
                            user?.let {
                                val usersCollection = FirebaseFirestore.getInstance().collection("users")
                                val userDocRef = usersCollection.document(user.uid)

                                userDocRef.get().addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        val userData = mapOf(
                                            "user_id" to 0,
                                            "username" to (user.displayName ?: ""),
                                            "email" to (user.email ?: ""),
                                            "address_token" to "",
                                            "password" to "google_oauth",
                                            "hasWallet" to false
                                        )
                                        userDocRef.set(userData)
                                    }

                                    val hasWallet = document.getBoolean("hasWallet") == true
                                    navController.navigate(if (hasWallet) "home" else "add_wallet") {
                                        popUpTo("startup") { inclusive = true }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Firebase sign-in failed", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("GoogleLogin", "Invalid token: ${e.message}")
                }
            } else {
                Log.e("GoogleLogin", "Unsupported credential type")
            }
        }
        else -> Log.e("GoogleLogin", "Unexpected credential type")
    }
}

// Customowy rozdzielnik
@Composable
fun OrDivider(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.inverseSurface,
    thickness: Dp = 1.dp,
    text: String = "or"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(color = color, thickness = thickness, modifier = Modifier.weight(1f))
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp), color = color)
        Divider(color = color, thickness = thickness, modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
}