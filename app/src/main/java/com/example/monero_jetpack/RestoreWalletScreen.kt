package com.example.monero_jetpack
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

@Composable
fun RestoreWalletScreen(navController: NavController, viewModel: WalletViewModel) {
    val context = LocalContext.current
    val mnemonicInput = remember { mutableStateOf("") }
    val walletName = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Restore Wallet", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = walletName.value,
            onValueChange = { walletName.value = it },
            label = { Text("Wallet Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mnemonicInput.value,
            onValueChange = { mnemonicInput.value = it },
            label = { Text("Enter your 25-word mnemonic") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            singleLine = false,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val mnemonic = mnemonicInput.value.trim()
                val name = walletName.value.trim()
                if (mnemonic.isBlank() || name.isBlank()) {
                    Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val client = OkHttpClient()
                        val json = JSONObject().apply {
                            put("mnemonic", mnemonic)
                        }

                        val request = Request.Builder()
                            .url("https://wallet.goodnodedemo.ovh/account/entropy-from-mnemonic")
                            .post(
                                RequestBody.create(
                                    "application/json".toMediaTypeOrNull(),
                                    json.toString()
                                )
                            )
                            .build()

                        val response = client.newCall(request).execute()
                        if (!response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Restore failed: ${response.code}", Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }

                        val body = response.body?.string() ?: ""
                        val obj = JSONObject(body)
                        val entropy = obj.optString("entropy", "")

                        if (entropy.isBlank()) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Invalid entropy", Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }

                        val walletObject = JSONObject().apply {
                            put("mnemonic", mnemonic)
                            put("entropy", entropy)
                            put("name", name)
                        }

                        val prefs = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
                        val existingData = prefs.getString("wallet_data", "{}") ?: "{}"
                        val dataJson = JSONObject(existingData)

                        dataJson.put(name, walletObject)
                        dataJson.put("current_wallet", name)

                        prefs.edit {
                            putString("wallet_data", dataJson.toString())
                        }

                        withContext(Dispatchers.Main) {
                            viewModel.setSelectedWallet(name, context)
                            viewModel.loadWalletIds(context)
                            navController.navigate("home") {
                                popUpTo("restore_wallet") { inclusive = true }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("RestoreWalletScreen", "Error: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Restore Wallet")
        }
    }
}
