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
fun AddWalletScreen(navController: NavController, viewModel: WalletViewModel) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Pomoce do wpisywania nazwy i pokazywania mnemonic
    val showMnemonicDialog = remember { mutableStateOf(false) }
    val mnemonicValue = remember { mutableStateOf("") }
    val walletName = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nagłówek i ikonka dodawania
        Text("Monero Wallet", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.inverseSurface)
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.inverseSurface),
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Opis i instrukcja
        Text("Create a new wallet", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.inverseSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We will generate a new wallet for you with a recovery phrase",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Pole na nazwę portfela
        OutlinedTextField(
            value = walletName.value,
            onValueChange = { walletName.value = it },
            label = { Text("Wallet Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Przycisk tworzenia portfela
        Button(
            onClick = {
                if (walletName.value.isBlank()) {
                    Toast.makeText(context, "Please enter a wallet name", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                user?.let {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://wallet.goodnodedemo.ovh/account/new")
                        .post(RequestBody.create("application/json".toMediaTypeOrNull(), ""))
                        .build()

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = client.newCall(request).execute()
                            if (!response.isSuccessful) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Wallet creation failed: ${response.code}", Toast.LENGTH_LONG).show()
                                }
                                return@launch
                            }

                            val walletJson = response.body?.string() ?: ""
                            val jsonObject = JSONObject(walletJson)
                            val mnemonic = jsonObject.optString("mnemonic", "")
                            val entropy = jsonObject.optString("entropy", "")

                            if (mnemonic.isBlank() || entropy.isBlank()) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Invalid wallet data from server", Toast.LENGTH_LONG).show()
                                }
                                return@launch
                            }

                            // Zapis portfela do local storage
                            val walletObject = JSONObject().apply {
                                put("mnemonic", mnemonic)
                                put("entropy", entropy)
                                put("name", walletName.value)
                            }

                            val prefs = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
                            val existingData = prefs.getString("wallet_data", "{}") ?: "{}"
                            val dataJson = JSONObject(existingData)

                            dataJson.put(walletName.value, walletObject)
                            dataJson.put("current_wallet", walletName.value)

                            prefs.edit {
                                putString("wallet_data", dataJson.toString())
                            }

                            // Zaznaczenie że użytkownik ma portfel (w Firebase)
                            try {
                                firestore.collection("users").document(it.uid)
                                    .update("hasWallet", true)
                            } catch (e: Exception) {
                                Log.e("AddWalletScreen", "Firestore update failed: ${e.message}")
                            }

                            // Ustawienie portfela w ViewModelu i pokazanie mnemonic
                            withContext(Dispatchers.Main) {
                                viewModel.setSelectedWallet(walletName.value, context)
                                viewModel.loadWalletIds(context)
                                mnemonicValue.value = mnemonic
                                showMnemonicDialog.value = true
                            }

                        } catch (e: Exception) {
                            Log.e("AddWalletScreen", "Network error: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Create")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Oddzielenie przycisków
        OrDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk do przywrócenia istniejącego portfela
        Button(
            onClick = {
                navController.navigate("restore_wallet")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Restore existing wallet")
        }
    }
    }

    // Pokazuje okno z mnemonic po utworzeniu portfela
    if (showMnemonicDialog.value) {
        MnemonicPopup(
            mnemonic = mnemonicValue.value,
            onClose = {
                showMnemonicDialog.value = false
                navController.navigate("home") {
                    popUpTo("add_wallet") { inclusive = true }
                }
            }
        )
    }
}

// Popup z hasłem odzyskiwania
@Composable
fun MnemonicPopup(mnemonic: String, onClose: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Your Recovery Phrase") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mnemonic,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(mnemonic))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface
                    )
                ) {
                    Text("Copy to Clipboard")
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Continue")
            }
        }
    )
}

// Customowy przerywnik
@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = Color.Gray
        )
        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray
        )
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = Color.Gray
        )
    }
}