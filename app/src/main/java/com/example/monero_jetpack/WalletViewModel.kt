package com.example.monero_jetpack

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException


data class Transaction(val amount: Double, val fee: Double, val type: String)

class WalletViewModel : ViewModel() {

    init {
        Log.d("WALLET_VM", "WalletViewModel initialized")
    }
    private val _userName = MutableStateFlow("Name")
    val userName: StateFlow<String> = _userName

    private val _accountName = MutableStateFlow("Loading...")
    val accountName: StateFlow<String> = _accountName

    private val _accountBalance = MutableStateFlow("0.00")
    val accountBalance: StateFlow<String> = _accountBalance

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _accountAddress = MutableStateFlow("Address")
    val accountAddress: StateFlow<String> = _accountAddress

    private val _accountBalanceUsd = MutableStateFlow("")
    val accountBalanceUsd: StateFlow<String> = _accountBalanceUsd

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private fun getAuthTokenFromEncryptedPrefs(context: Context): String? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                "secure_wallet_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val json = prefs.getString("wallet_data", null)
            val jsonObj = JSONObject(json ?: return null)
            val entropy = jsonObj.optString("entropy")
            Log.d("WALLET_VM", "Entropy loaded: $entropy")
            entropy
        } catch (e: Exception) {
            Log.e("WALLET_VM", "Failed to load auth token: ${e.message}")
            null
        }
    }

    fun fetchWalletData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("WALLET_VM", "Fetching wallet data...")
            try {
                _isLoading.value = true
                _error.value = null

                val token = getAuthTokenFromEncryptedPrefs(context) ?: ""
                Log.d("WALLET_VM", "Using token: $token")

                val client = OkHttpClient()
                val gson = Gson()
                val user = FirebaseAuth.getInstance().currentUser
                val firestore = FirebaseFirestore.getInstance()

                val docSnapshot = user?.uid?.let {
                    firestore.collection("users").document(it).get().await()
                }

                val existingAddress = docSnapshot?.getString("address_token")
                val usernameFromFirestore = docSnapshot?.getString("username") ?: "Anonymous"

                val addressString = if (existingAddress.isNullOrEmpty()) {
                    val request = Request.Builder()
                        .url("https://wallet.goodnodedemo.ovh/wallet/XMR/address")
                        .post(RequestBody.create("application/json".toMediaTypeOrNull(), ""))
                        .addHeader("Authorization", token)
                        .build()
                    val addressResponse = client.newCall(request).execute()
                    val addressBody = addressResponse.body?.string() ?: ""
                    val newAddress = addressBody.replace("\"", "")
                    Log.d("WALLET_VM", "New address: $newAddress")
                    if (user != null && newAddress.isNotEmpty()) {
                        firestore.collection("users").document(user.uid)
                            .update("address_token", newAddress)
                    }
                    newAddress
                } else {
                    Log.d("WALLET_VM", "Using existing address: $existingAddress")
                    existingAddress
                }

                val balanceResponse = client.newCall(
                    Request.Builder()
                        .url("https://wallet.goodnodedemo.ovh/wallet/XMR/balance")
                        .addHeader("Authorization", token)
                        .build()
                ).execute()

                val transactionsResponse = client.newCall(
                    Request.Builder()
                        .url("https://wallet.goodnodedemo.ovh/wallet/XMR/transactions")
                        .addHeader("Authorization", token)
                        .build()
                ).execute()

                val priceResponse = client.newCall(
                    Request.Builder()
                        .url("https://api.coingecko.com/api/v3/simple/price?ids=monero&vs_currencies=usd")
                        .build()
                ).execute()

                if (!balanceResponse.isSuccessful || !transactionsResponse.isSuccessful || !priceResponse.isSuccessful) {
                    Log.e("WALLET_VM", "One or more responses failed.")
                    _error.value = "Error fetching wallet data."
                    return@launch
                }

                val balanceJson = gson.fromJson(balanceResponse.body?.string(), JsonObject::class.java)
                val transactionsJson = gson.fromJson(transactionsResponse.body?.string(), JsonObject::class.java)
                val priceJson = gson.fromJson(priceResponse.body?.string(), JsonObject::class.java)

                val confirmedBalance = balanceJson.get("confirmed")?.asDouble ?: 0.0
                val xmrUsdRate = priceJson.getAsJsonObject("monero").get("usd").asDouble
                val balanceUsd = confirmedBalance * xmrUsdRate

                Log.d("WALLET_VM", "Confirmed balance: $confirmedBalance XMR = $balanceUsd USD")

                val inTransactions = if (
                    transactionsJson.has("in") &&
                    transactionsJson["in"] != null &&
                    transactionsJson["in"].isJsonArray
                ) {
                    transactionsJson.getAsJsonArray("in").map {
                        val obj = it.asJsonObject
                        Transaction(
                            amount = obj.get("amount").asDouble,
                            fee = obj.get("fee").asDouble,
                            type = obj.get("type").asString
                        )
                    }
                } else emptyList()

                val outTransactions = if (
                    transactionsJson.has("out") &&
                    transactionsJson["out"] != null &&
                    transactionsJson["out"].isJsonArray
                ) {
                    transactionsJson.getAsJsonArray("out").map {
                        val obj = it.asJsonObject
                        Transaction(
                            amount = obj.get("amount").asDouble,
                            fee = obj.get("fee").asDouble,
                            type = obj.get("type").asString
                        )
                    }
                } else emptyList()


                val allTransactions = inTransactions + outTransactions
                Log.d("WALLET_VM", "Fetched ${allTransactions.size} transactions")

                withContext(Dispatchers.Main) {
                    _userName.value = usernameFromFirestore
                    _accountName.value = "My XMR Wallet"
                    _accountBalance.value = "%.4f XMR".format(confirmedBalance)
                    _accountBalanceUsd.value = "$%.2f USD".format(balanceUsd)
                    _transactions.value = allTransactions
                    _accountAddress.value = addressString
                    Log.d("WALLET_VM", "StateFlows updated!")
                }

            } catch (e: IOException) {
                Log.e("WALLET_VM", "Network error: ${e.message}")
                _error.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                Log.e("WALLET_VM", "Unexpected error: ${e.message}")
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
