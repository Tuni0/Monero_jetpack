package com.example.monero_jetpack

import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
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
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import kotlinx.coroutines.tasks.await
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun StartupScreen(navController: NavController) {
    val context = LocalContext.current
    val oneTapClient = remember { Identity.getSignInClient(context) }
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    val clientId =
        "399010568004-a2tl0ct5kfgshg5u8529pqlbvc871265.apps.googleusercontent.com" // Replace with actual Web client ID

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.let {
                                    firestore.collection("users")
                                        .get()
                                        .addOnSuccessListener { result ->
                                            val nextId = result.size() + 1
                                            val userData = hashMapOf(
                                                "user_id" to nextId,
                                                "username" to (user.displayName ?: ""),
                                                "email" to (user.email ?: ""),
                                                "address_token" to "default",
                                                "password" to "google_oauth",
                                                "hasWallet" to false
                                            )
                                            firestore.collection("users").document(user.uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    navController.navigate("home") {
                                                        popUpTo("startup") { inclusive = true }
                                                    }
                                                }


                                        }
                                }
                                navController.navigate("home") {
                                    popUpTo("startup") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Firebase sign-in failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "No ID token found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "Google sign-in failed", e)
                Toast.makeText(context, "Google sign-in error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val addAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        Toast.makeText(context, "Wróć i spróbuj ponownie się zalogować", Toast.LENGTH_LONG).show()
    }


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
                    val signInRequest = BeginSignInRequest.Builder()
                        .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(clientId)
                                .setFilterByAuthorizedAccounts(true)
                                .build()
                        )
                        .setAutoSelectEnabled(true)
                        .build()

                    try {
                        val result = oneTapClient.beginSignIn(signInRequest).await()
                        launcher.launch(
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        )
                    } catch (e: Exception) {
                        // Fallback to sign-up if no accounts
                        val signUpRequest = BeginSignInRequest.Builder()
                            .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(clientId)
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                            )
                            .build()

                        try {
                            val signUpResult = oneTapClient.beginSignIn(signUpRequest).await()
                            launcher.launch(
                                IntentSenderRequest.Builder(signUpResult.pendingIntent.intentSender)
                                    .build()
                            )
                        } catch (e: Exception) {
                            Log.e("GoogleLogin", "Sign-up error: ${e.message}")
                            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                                putExtra("account_types", arrayOf("com.google"))
                            }
                            addAccountLauncher.launch(intent)
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )

        ) {
            Image(
                painter = painterResource(id = R.drawable.icons8_google),
                contentDescription = "Google Icon",
                modifier = Modifier
                    .height(20.dp) // 🟢 Similar to text height, tweak if needed
                    .aspectRatio(1f) // Keeps it square
                    .align(Alignment.CenterVertically),
            )
            Text(text = "Continue with Google", modifier = Modifier.padding(6.dp))
        }

        OrDivider()

        Button(
            onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Log in with Email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("register") }, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Register with Email")
        }
    }
}


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
        Divider(
            color = color,
            thickness = thickness,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = color
        )
        Divider(
            color = color,
            thickness = thickness,
            modifier = Modifier.weight(1f)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
    }

