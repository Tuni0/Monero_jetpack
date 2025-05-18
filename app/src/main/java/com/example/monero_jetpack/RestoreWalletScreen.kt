package com.example.monero_jetpack
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


// Ekran przywracania portfela
@Composable
fun RestoreWalletScreen(navController: NavController, viewModel: WalletViewModel) {
    val context = LocalContext.current

    // Pola tekstowe do wpisania danych
    val mnemonicInput = remember { mutableStateOf("") }
    val walletName = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nagłówek
        Text("Restore Wallet", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Nazwa portfela
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

        // Przycisk przywracania portfela
        Button(
            onClick = {
                val mnemonic = mnemonicInput.value.trim()
                val name = walletName.value.trim()

                if (mnemonic.isBlank() || name.isBlank()) {
                    Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Wywołanie funkcji przywracającej w ViewModelu
                viewModel.restoreWallet(
                    context = context,
                    mnemonic = mnemonic,
                    walletName = name,
                    onSuccess = {
                        // Jeśli się uda – przechodzimy do ekranu głównego
                        navController.navigate("home") {
                            popUpTo("restore_wallet") { inclusive = true }
                        }
                    },
                    onError = { errorMsg ->
                        // Jeśli się nie uda – pokazujemy komunikat
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
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
