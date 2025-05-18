package com.example.monero_jetpack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monero_jetpack.ui.theme.Monero_jetpackTheme
import com.google.firebase.auth.FirebaseAuth

// Aktywność ustawień (uruchamia ekran SettingsScreen)
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Ustawienia stylu systemowego
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = MaterialTheme.colorScheme.surface.toArgb(),
                    darkScrim = MaterialTheme.colorScheme.surface.toArgb()
                )
            )
            // Wczytanie motywu i ekranu
            Monero_jetpackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    SettingsScreen(WalletViewModel())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WalletViewModel) {
    val context = LocalContext.current
    // Dialogi do różnych akcji
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteWalletDialog by remember { mutableStateOf(false) }

    // Załaduj dostępne portfele
    LaunchedEffect(Unit) {
        viewModel.loadWalletIds(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                // Pasek górny z logo i tytułem
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_1),
                                contentDescription = "Monero Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Settings", fontSize = 25.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Lista opcji ustawień
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text("Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

                        SectionHeader("Account")
                        SettingRow(title = "Account info", onClick = {})

                        SectionHeader("Appearance")
                        SettingRow(title = "Color Scheme", onClick = {})
                        SettingRow(title = "Visible categories", onClick = {})

                        SectionHeader("Notifications")
                        SettingRow(title = "Notifications", onClick = {})
                        SettingRow(title = "Additional popups", onClick = {})
                    }

                    // Dolna część – przyciski
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        // Wylogowanie
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text("Log out")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Usuwanie portfela
                        OutlinedButton(
                            onClick = { showDeleteWalletDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text("Delete wallet")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Usuwanie konta
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Delete account")
                        }

                        // Dialog potwierdzenia usunięcia konta
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Are you sure?") },
                                text = { Text("This will permanently delete your account and cannot be undone.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val user = FirebaseAuth.getInstance().currentUser
                                            user?.delete()?.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val intent = Intent(context, MainActivity::class.java).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "Failed to delete account", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            showDeleteDialog = false
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp),
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDeleteDialog = false },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )
                        }

                        // Dialog wylogowania
                        if (showLogoutDialog) {
                            AlertDialog(
                                onDismissRequest = { showLogoutDialog = false },
                                title = { Text("Are you sure?") },
                                text = { Text("Do you want to log out?") },
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                textContentColor = MaterialTheme.colorScheme.onErrorContainer,
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            FirebaseAuth.getInstance().signOut()
                                            val intent = Intent(context, MainActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            }
                                            context.startActivity(intent)
                                            showLogoutDialog = false
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp),
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showLogoutDialog = false },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        // Dialog usuwania portfela
                        if (showDeleteWalletDialog) {
                            WalletDeleteDialog(
                                walletList = viewModel.walletList.collectAsState().value,
                                onDelete = { walletId ->
                                    viewModel.deleteWallet(walletId, context)
                                    Toast.makeText(context, "Deleted $walletId", Toast.LENGTH_SHORT).show()
                                    showDeleteWalletDialog = false
                                },
                                onDismiss = { showDeleteWalletDialog = false }
                            )
                        }
                    }
                }
            }
        )
    }
}

// Okno do usuwania portfela
@Composable
fun WalletDeleteDialog(
    walletList: List<String>,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Wallet") },
        text = {
            Column {
                if (walletList.isEmpty()) {
                    Text("No wallets available.")
                } else {
                    walletList.forEach { walletId ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onDelete(walletId) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(walletId)
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Nagłówek sekcji w ustawieniach
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Pojedynczy wiersz w ustawieniach
@Composable
fun SettingRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.SemiBold)
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(WalletViewModel())
}