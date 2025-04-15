package com.example.monero_jetpack

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.IOException
data class Transaction(val amount: Double, val fee: Double, val type: String)

class WalletViewModel : ViewModel() {
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

    init {
        fetchWalletData()
    }

    @SuppressLint("DefaultLocale")
    fun fetchWalletData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val client = OkHttpClient()
                val gson = Gson()

                val balanceRequest = Request.Builder()
                    .url("https://wallet.goodnodedemo.ovh/wallet/XMR/balance")
                    .addHeader("Authorization", "orzechySaZdrowe")
                    .build()

                val transactionsRequest = Request.Builder()
                    .url("https://wallet.goodnodedemo.ovh/wallet/XMR/transactions")
                    .addHeader("Authorization", "orzechySaZdrowe")
                    .build()

                val addressRequest = Request.Builder()
                    .url("https://wallet.goodnodedemo.ovh/wallet/XMR/address")
                    .addHeader("Authorization", "orzechySaZdrowe")
                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), ""))
                    .build()

                val priceRequest = Request.Builder()
                    .url("https://api.coingecko.com/api/v3/simple/price?ids=monero&vs_currencies=usd")
                    .build()

                val balanceResponse = withContext(Dispatchers.IO) { client.newCall(balanceRequest).execute() }
                val transactionsResponse = withContext(Dispatchers.IO) { client.newCall(transactionsRequest).execute() }
                val addressResponse = withContext(Dispatchers.IO) { client.newCall(addressRequest).execute() }
                val priceResponse = withContext(Dispatchers.IO) { client.newCall(priceRequest).execute() }

                if (!balanceResponse.isSuccessful || !transactionsResponse.isSuccessful || !addressResponse.isSuccessful || !priceResponse.isSuccessful) {
                    _error.value = "Error fetching wallet data."
                    return@launch
                }

                val balanceJson = gson.fromJson(balanceResponse.body?.string(), JsonObject::class.java)
                val transactionsJson = gson.fromJson(transactionsResponse.body?.string(), JsonObject::class.java)
                val priceJson = gson.fromJson(priceResponse.body?.string(), JsonObject::class.java)

                val confirmedBalance = balanceJson.get("confirmed").asDouble
                val xmrUsdRate = priceJson.getAsJsonObject("monero").get("usd").asDouble
                val balanceUsd = confirmedBalance * xmrUsdRate

                val inTransactions = transactionsJson.getAsJsonArray("in").map {
                    val obj = it.asJsonObject
                    Transaction(
                        amount = obj.get("amount").asDouble,
                        fee = obj.get("fee").asDouble,
                        type = obj.get("type").asString
                    )
                }

                val outTransactions = transactionsJson.getAsJsonArray("out").map {
                    val obj = it.asJsonObject
                    Transaction(
                        amount = obj.get("amount").asDouble,
                        fee = obj.get("fee").asDouble,
                        type = obj.get("type").asString
                    )
                }

                val allTransactions = inTransactions + outTransactions
                val addressString = addressResponse.body?.string()?.replace("\"", "") ?: ""

                withContext(Dispatchers.Main) {
                    _accountName.value = "My XMR Wallet"
                    _accountBalance.value = "%.4f XMR".format(confirmedBalance)
                    _accountBalanceUsd.value = "$%.2f USD".format(balanceUsd)
                    _transactions.value = allTransactions
                    _accountAddress.value = addressString
                }

            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}