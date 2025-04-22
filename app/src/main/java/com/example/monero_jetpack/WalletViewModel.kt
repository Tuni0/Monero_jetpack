package com.example.monero_jetpack

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import org.json.*  // w razie czego, ale ten wyżej powinien wystarczyć

data class Transaction(val amount: Double, val fee: Double, val type: String)

class WalletViewModel : ViewModel() {

    private val fetchMutex = Mutex()

    private val _userName = MutableStateFlow("Name")
    val userName: StateFlow<String> = _userName

    private val _walletDisplayName = MutableStateFlow("Loading...")
    val walletDisplayName: StateFlow<String> = _walletDisplayName

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

    private val _walletList = MutableStateFlow<List<String>>(emptyList())
    val walletList: StateFlow<List<String>> = _walletList

    private val _selectedWalletId = MutableStateFlow<String?>(null)
    val selectedWalletId: StateFlow<String?> = _selectedWalletId

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    }

    fun getEntropyForWallet(context: Context, walletId: String): String? {
        val prefs = getPrefs(context)
        val walletJson = prefs.getString("wallet_data", null) ?: return null
        val obj = JSONObject(walletJson)
        val walletObj = obj.optJSONObject(walletId) ?: return null
        return walletObj.optString("entropy", null)
    }

    fun setSelectedWallet(walletId: String, context: Context) {
        val prefs = getPrefs(context)
        val walletJson = JSONObject(prefs.getString("wallet_data", null) ?: return)
        walletJson.put("current_wallet", walletId)
        prefs.edit { putString("wallet_data", walletJson.toString()) }
        _selectedWalletId.value = walletId

        val walletObj = walletJson.optJSONObject(walletId)
        val name = walletObj?.optString("name", walletId) ?: walletId
        _walletDisplayName.value = name

        // ✅ fetchujemy od razu
        fetchWalletData(context)
    }

    fun loadSelectedWallet(context: Context) {
        val prefs = getPrefs(context)
        val walletJson = JSONObject(prefs.getString("wallet_data", null) ?: return)

        // 1. próbujemy domyślny
        var currentWallet = walletJson.optString("current_wallet", null)

        // 2. jeśli brak, weź pierwszy z dostępnych
        if (currentWallet.isNullOrEmpty()) {
            val keys = walletJson.keys().asSequence().filter { it != "current_wallet" }.toList()
            currentWallet = keys.firstOrNull()
            if (currentWallet == null) return  // brak jakichkolwiek portfeli
            walletJson.put("current_wallet", currentWallet)
            prefs.edit { putString("wallet_data", walletJson.toString()) }
        }

        _selectedWalletId.value = currentWallet
        val walletObj = walletJson.optJSONObject(currentWallet)
        val name = walletObj?.optString("name", currentWallet) ?: currentWallet
        _walletDisplayName.value = name

        fetchWalletData(context) // <-- ✅ automatyczny fetch
    }


    fun loadWalletIds(context: Context) {
        val prefs = getPrefs(context)
        val walletJson = prefs.getString("wallet_data", null) ?: return
        val obj = JSONObject(walletJson)
        val keys = obj.keys().asSequence().filter { it != "current_wallet" }.toList()
        _walletList.value = keys
    }

    fun deleteWallet(walletId: String, context: Context) {
        val prefs = getPrefs(context)
        val walletJson = JSONObject(prefs.getString("wallet_data", null) ?: return)
        walletJson.remove(walletId)
        if (walletJson.optString("current_wallet") == walletId) {
            walletJson.remove("current_wallet")
            _selectedWalletId.value = null
        }
        prefs.edit { putString("wallet_data", walletJson.toString()) }
        loadWalletIds(context)
    }

    fun getWalletDisplayName(context: Context, walletId: String): String {
        val prefs = getPrefs(context)
        val walletJson = prefs.getString("wallet_data", null) ?: return walletId
        val obj = JSONObject(walletJson)
        val walletObj = obj.optJSONObject(walletId) ?: return walletId
        return walletObj.optString("name", walletId)
    }

    fun fetchWalletData(context: Context) {
        val walletId = _selectedWalletId.value ?: return
        val prefs = getPrefs(context)
        val walletJson = JSONObject(prefs.getString("wallet_data", null) ?: return)
        val walletObj = walletJson.optJSONObject(walletId) ?: return
        val token = walletObj.optString("entropy", null)

        if (token.isNullOrEmpty() || token == "token") {
            Log.e("WALLET_VM", "Invalid token for wallet: $walletId")
            _error.value = "Invalid wallet token"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("WALLET_VM", "fetchWalletData called from: ${Throwable().stackTraceToString().split('\n')[1]}")

            fetchMutex.withLock {
                Log.d("WALLET_VM", "Fetching wallet data for $walletId")

                withContext(Dispatchers.Main) {
                    _isLoading.value = true
                    _error.value = null
                    _accountAddress.value = "Loading..."
                }

                try {


                    val prefs = getPrefs(context)
                    val walletJson = JSONObject(prefs.getString("wallet_data", null) ?: return@launch)
                    val walletObj = walletJson.optJSONObject(walletId) ?: return@launch

                    val token = walletObj.optString("entropy", null)
                    if (token.isNullOrEmpty() || token == "token") {
                        Log.e("WALLET_VM", "Invalid token for wallet: $walletId")
                        withContext(Dispatchers.Main) {
                            _error.value = "Invalid wallet token"
                        }
                        return@launch
                    }

                    val client = OkHttpClient()
                    val gson = Gson()
                    val user = FirebaseAuth.getInstance().currentUser
                    val firestore = FirebaseFirestore.getInstance()

                    val docSnapshot = user?.uid?.let {
                        firestore.collection("users").document(it).get().await()
                    }
                    val usernameFromFirestore = docSnapshot?.getString("username") ?: "Anonymous"

                    val existingAddress = walletObj.optString("address", null)
                    val addressString = if (existingAddress.isNullOrEmpty() || existingAddress.contains("Unauthorized")) {
                        val response = client.newCall(
                            Request.Builder()
                                .url("https://wallet.goodnodedemo.ovh/wallet/XMR/address")
                                .post(RequestBody.create("application/json".toMediaTypeOrNull(), ""))
                                .addHeader("Authorization", token)
                                .build()
                        ).execute()

                        val responseBody = response.body?.string()?.replace("\"", "") ?: ""
                        if (response.isSuccessful && responseBody.isNotEmpty() && !responseBody.contains("Unauthorized")) {
                            walletObj.put("address", responseBody)
                            walletJson.put(walletId, walletObj)
                            prefs.edit() { putString("wallet_data", walletJson.toString()) }
                            responseBody
                        } else {
                            Log.e("WALLET_VM", "Failed to get address: $responseBody")
                            withContext(Dispatchers.Main) {
                                _error.value = "Failed to get address: Unauthorized"
                            }
                            return@launch
                        }
                    } else {
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
                        withContext(Dispatchers.Main) {
                            _error.value = "Error fetching wallet data"
                        }
                        return@launch
                    }

                    val balanceJson = gson.fromJson(balanceResponse.body?.string(), JsonObject::class.java)
                    val transactionsJson = gson.fromJson(transactionsResponse.body?.string(), JsonObject::class.java)
                    val priceJson = gson.fromJson(priceResponse.body?.string(), JsonObject::class.java)

                    val confirmedBalance = balanceJson.get("confirmed")?.asDouble ?: 0.0
                    val xmrUsdRate = priceJson.getAsJsonObject("monero").get("usd").asDouble
                    val balanceUsd = confirmedBalance * xmrUsdRate

                    val inTransactions = transactionsJson.get("in")?.takeIf { it.isJsonArray }?.asJsonArray?.map {
                        val obj = it.asJsonObject
                        Transaction(
                            amount = obj.get("amount").asDouble,
                            fee = obj.get("fee").asDouble,
                            type = obj.get("type").asString
                        )
                    } ?: emptyList()

                    val outTransactions = transactionsJson.get("out")?.takeIf { it.isJsonArray }?.asJsonArray?.map {
                        val obj = it.asJsonObject
                        Transaction(
                            amount = obj.get("amount").asDouble,
                            fee = obj.get("fee").asDouble,
                            type = obj.get("type").asString
                        )
                    } ?: emptyList()

                    withContext(Dispatchers.Main) {
                        _userName.value = usernameFromFirestore
                        _accountBalance.value = "%.4f XMR".format(confirmedBalance)
                        _accountBalanceUsd.value = "$%.2f USD".format(balanceUsd)
                        _transactions.value = inTransactions + outTransactions
                        _accountAddress.value = addressString
                        _isLoading.value = false
                        Log.d("WALLET_VM", "StateFlows updated!")
                    }

                } catch (e: Exception) {
                    Log.e("WALLET_VM", "Exception in fetch: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _error.value = "Fetch error: ${e.localizedMessage}"
                        _isLoading.value = false
                    }
                }
            }
        }
    }
}
