package com.example.monero_jetpack
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monero_jetpack.ui.theme.Monero_jetpackTheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.zIndex
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import android.provider.Settings  // For Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            Monero_jetpackTheme {
                val navController = rememberNavController()

                // 👇 auto-login if already signed in
                val auth = FirebaseAuth.getInstance()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val user = auth.currentUser
                    if (user != null) {
                        try {
                            user.getIdToken(true).addOnSuccessListener {
                                // Token still valid → allow to stay signed in
                                startDestination = "home"
                            }.addOnFailureListener {
                                // Token invalid → sign out
                                auth.signOut()
                                startDestination = "startup"
                            }
                        } catch (e: Exception) {
                            auth.signOut()
                            startDestination = "startup"
                        }
                    } else {
                        startDestination = "startup"
                    }
                }


                startDestination?.let { start ->
                    NavHost(navController = navController, startDestination = start) {
                        composable("startup") { StartupScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("home") { MainScreen() }
                    }
                }

            }
        }
    }
}



data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel:WalletViewModel = viewModel()) {
    val accountName by viewModel.accountName.collectAsState()
    val accountBalance by viewModel.accountBalance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current
    var selectedItem by remember { mutableIntStateOf(0) }
    val items1 = listOf("Home", "Contacts", "Payments")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Person, Icons.Filled.Info)
    val unselectedIcons =
        listOf(Icons.Outlined.Home, Icons.Outlined.Person, Icons.Outlined.Info)
    var showQRCode by remember { mutableStateOf(false) } // <-- Lifted State
    var showPayment by remember { mutableStateOf(false) } // <-- Lifted State

    val navigationBarHeight = remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current


    var flag by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {


        Scaffold(
            Modifier.zIndex(0f),
            topBar = {
                TopAppBar(

                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically){
                        Image(
                            painter = painterResource(id = R.drawable.logo_1),
                            contentDescription = "Monero Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape) // Optional for circular background
                        )
                        Text(text = "Monero Wallet", fontSize = 25.sp)}
                    },
                    actions = {
                        IconButton(onClick = {

                        }) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Login"
                            )
                        }

                        IconButton(onClick = {
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    colors = TopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),

                )
            },
            bottomBar = {
                NavigationBar (
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        navigationBarHeight.value = with(density) { layoutCoordinates.size.height.toDp() }
                    }
                ){
                        items1.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                                        contentDescription = item
                                    )
                                },
                                label = { Text(item) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.onSurface,
                                    selectedIconColor = MaterialTheme.colorScheme.surface,

                                )
                            )

                        }

                }
            },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Dashboard(paddingValues = innerPadding,
                        flag = flag,
                        onToggleFlag = { flag = !flag },
                        accountName = accountName,
                        accountBalance = accountBalance,
                        isLoading = isLoading,
                        error = error,
                        transactions = transactions)


                }
            }
        )

        // Floating Action Button (FAB) - Placed Last to Ensure Visibility
        FloatingActionButton(
            onClick = { flag = !flag },
            containerColor = MaterialTheme.colorScheme.inversePrimary,
            contentColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            modifier = Modifier
                .align(Alignment.BottomEnd) // FAB Position
                .padding(end=20.dp, bottom = navigationBarHeight.value +20.dp)
                .zIndex(2f) // Ensure it's above everything, including the dim overlay
        ) {
            Icon(
                imageVector = if (flag) Icons.Filled.Clear else Icons.Filled.Add,
                contentDescription = "Menu Toggle"
            )
        }

        // Dimming overlay (covers EVERYTHING, including the AppBar and BottomBar)
        if(flag ||showQRCode||showPayment){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Dim effect
                    .clickable { flag = false } // Click outside to dismiss
                    .zIndex(1f) // Below FAB, above Scaffold
            )
        }
        if (flag ) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 30.dp, end = 28.dp)
                    .zIndex(2f) // Above the dim effect
            ) {
                FloatingMenu(onDismiss = {flag = false},
                    onShowQRCode = { showQRCode = true  },
                    onShowPayment = {showPayment = true},
                    navBarHeight = navigationBarHeight.value // Pass the height here
                )
            }

        }




    }
    // Show QR Code Fullscreen When Activated
    if (showQRCode) {
        QRCodeScreen("https://yourwebsite.com", onDismiss = { showQRCode = false })
    }
    if(showPayment){
        TransactionCard(onClose = { showPayment = false }, onSend = { qrCode, amount -> showPayment = false })
    }
}



@Composable
fun Dashboard(
    paddingValues: PaddingValues,
    flag: Boolean,
    onToggleFlag: () -> Unit,
    accountName: String,
    accountBalance: String,
    isLoading: Boolean,
    error: String?,
    transactions: List<Transaction>
) {
    var text by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val recentTransactions = transactions.takeLast(4).reversed()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // User Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {})
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceDim,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "User's Wallet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = accountName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            // Balance Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Total Balance",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$accountBalance",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Text(
                        text = "text",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { /* Send */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Filled.NorthEast, contentDescription = "Send")
                            Text("Send")
                        }

                        Button(
                            onClick = { /* Receive */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Filled.SouthWest, contentDescription = "Receive")
                            Text("Receive")
                        }
                    }
                }
            }
        }

        item {
            // Balance Card
            OutlinedCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.2.dp, MaterialTheme.colorScheme.onSurface),

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Wallet Address",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Enter text") },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Outlined.CopyAll, contentDescription = "Copy")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh

                        ),
                        shape = RoundedCornerShape(10), // Fully rounded corners

                    )

                }
            }
        }



        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                MonochromeLineChart(viewModel = WalletViewModel())
            }
        }

        item{
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recent Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    getExampleTransactions().forEach { tx ->
                        val isIncome = tx.type == "in"
                        val icon = if (isIncome) Icons.Filled.SouthWest else Icons.Filled.NorthEast
                        val bgColor = if (isIncome) Color(0xFFDFFFE3) else Color(0xFFFFE3E3)
                        val iconColor = if (isIncome) Color(0xFF0BAF3C) else Color(0xFFE53935)
                        val formattedAmount = (if (isIncome) "+" else "-") + "${tx.amount} XMR"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Jane Doe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("2 hours ago", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Text(
                                text = formattedAmount,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }




    }
}



fun List<Transaction>.toChartLine(): Line {
    val values = map { tx ->
        if (tx.type == "in") tx.amount else -tx.amount
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color.Green, Color.Red),
        startY = 0f,
        tileMode = TileMode.Clamp
    )

    return Line(
        label = "Balance Change",
        values = values + (-0.5)+ 0.5 + (-0.5)+0.5,
        color = gradientBrush,
        firstGradientFillColor = Color.Green.copy(alpha = 0.3f),
        secondGradientFillColor = Color.Red.copy(alpha = 0.3f),
        strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
        gradientAnimationDelay = 1000,
    )
}


@Composable
fun TransactionDeltaLineChart(viewModel: WalletViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    if (transactions.isNotEmpty()) {
        val line = remember(transactions) { listOf(transactions.toChartLine()) }

        LineChart(
            modifier = Modifier
                .fillMaxSize()
                .height(200.dp)
                .padding(horizontal = 22.dp),
            data = line,
            animationMode = AnimationMode.Together(delayBuilder = { it * 500L }),
            zeroLineProperties = ZeroLineProperties(
                enabled = true,
                color = SolidColor(Color.Red),
            ),
            minValue = -1.0,
            maxValue = 1.0

        )

    }
}

fun List<Transaction>.toBlackWhiteChart(color: Color): Line {
    val values = map { tx ->
        if (tx.type == "in") tx.amount else -tx.amount
    }
    return Line(
        label = "Monochrome",
        values = values + (-0.5)+ 0.5 + (-0.5)+0.5,
        color = SolidColor(color),
        drawStyle = ir.ehsannarmani.compose_charts.models.DrawStyle.Stroke(
            width = 3.dp,
            strokeStyle = StrokeStyle.Dashed(
                intervals = floatArrayOf(8f, 6f), // 8px dash, 6px space
                phase = 0f
            )
        ),

    )
}
fun getExampleTransactions(): List<Transaction> {
    return listOf(
        Transaction(type = "out", amount = 0.4, fee = 0.01),
        Transaction(type = "out", amount = 0.56, fee = 0.01),
        Transaction(type = "in", amount = 0.25, fee = 0.0),
        Transaction(type = "out", amount = 0.58, fee = 0.02),
        Transaction(type = "in", amount = 0.22, fee = 0.0),
        Transaction(type = "out", amount = 0.72, fee = 0.01),
        Transaction(type = "in", amount = 0.67, fee = 0.0),
        Transaction(type = "in", amount = 0.24, fee = 0.0),
        Transaction(type = "in", amount = 0.23, fee = 0.0),
        Transaction(type = "out", amount = 0.28, fee = 0.01),
        Transaction(type = "out", amount = 0.27, fee = 0.01),
        Transaction(type = "in", amount = 0.76, fee = 0.0),
        Transaction(type = "in", amount = 0.46, fee = 0.0),
        Transaction(type = "in", amount = 0.78, fee = 0.0),
        Transaction(type = "in", amount = 0.59, fee = 0.0)
    )
}

@Composable
fun MonochromeLineChart(viewModel: WalletViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val dataToShow = if (transactions.isNotEmpty()) transactions else getExampleTransactions()
    if (true) {
        val themeColor = MaterialTheme.colorScheme.onSurface

        val line = remember(dataToShow, themeColor) {
            listOf(dataToShow.toBlackWhiteChart(themeColor))
        }


        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 22.dp),
            data = line,
            animationMode = AnimationMode.Together(delayBuilder = { it * 300L }),
            zeroLineProperties = ZeroLineProperties(
                enabled = true,
                color = SolidColor(Color.Gray),
            ),
            minValue = -1.0,
            maxValue = 1.0,
            gridProperties = GridProperties(
                enabled = false,


            ),
            dotsProperties = DotProperties(
                enabled = true,
                color = SolidColor(MaterialTheme.colorScheme.onSurface),
                strokeWidth = 4.dp,
                radius = 7.dp,
                strokeColor = SolidColor(MaterialTheme.colorScheme.surface),
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            ),

        )
    }
}


@Composable
fun FloatingMenu(onDismiss: () -> Unit, onShowQRCode: () -> Unit,onShowPayment: ()->Unit,navBarHeight: Dp){
    // Floating Menu
    var showQRCode by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(bottom = navBarHeight+50.dp).zIndex(4f),
        horizontalAlignment = Alignment.End
    ) {

        Row(modifier= Modifier.padding(bottom = 30.dp)) {
            Text(
                "Send", modifier = Modifier
                    .clickable { onShowPayment(); onDismiss() }
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp),
                color = Color.White
            )
            FloatingActionButton(
                onClick = {onShowPayment(); onDismiss()  },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier = Modifier.size(40.dp)

            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Localized description"
                )
            }
        }
        Row(modifier= Modifier.padding(bottom = 30.dp)) {
            Text(
                "Receive", modifier = Modifier
                    .clickable { onShowQRCode(); onDismiss() }
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp),
                color = Color.White
            )
            FloatingActionButton(
                onClick = { onShowQRCode(); onDismiss() },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(40.dp)

            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Localized description"
                )
            }

        }
    }
}


fun generateQRCode(text: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun QRCodeScreen(inputText: String, onDismiss: () -> Unit) {
    val qrBitmap = remember(inputText) { generateQRCode(inputText) }
    var isVisible by remember { mutableStateOf(false) }

    // Delay animation until composable is fully loaded
    LaunchedEffect(Unit) {
        isVisible = true
    }
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it }, // Start from bottom of screen
                animationSpec = tween(durationMillis = 500) // Smooth animation
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, // Exit to bottom
                animationSpec = tween(durationMillis = 300)
            )
        ) {
    Box( // Ensure full screen coverage
        modifier = Modifier
            .fillMaxSize()
            .zIndex(3f)
            .clickable {
                isVisible = false // Trigger exit animation
                onDismiss()
            }

    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(0.dp) // Ensure no extra padding
                .align(alignment = Alignment.BottomCenter)
                .clickable(enabled = false) {} // Prevents clicks on the card from closing it

        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = inputText, color = Color.White)
            }
        }
    }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TransactionCard(
    onClose: () -> Unit,
    onSend: (String, String) -> Unit,
) {
    val context = LocalContext.current
    var qrCodeText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    // Camera permission state - using the new API
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Result launcher for QR scanner
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            qrCodeText = result.data?.getStringExtra("SCAN_RESULT") ?: ""
        }
    }

    // Check permission when launched
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // QR Code Input with one button for scanning
                OutlinedTextField(
                    value = qrCodeText,
                    onValueChange = { qrCodeText = it },
                    label = { Text("QR Code") },
                    trailingIcon = {
                        // Only one button to scan QR code, checking for permission before launching
                        when {
                            permissionState.status.isGranted -> {
                                IconButton(onClick = {
                                    val intent = Intent(context, QrScannerActivity::class.java)
                                    scanLauncher.launch(intent)
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.qr_code_24px),
                                        contentDescription = "Scan QR Code"
                                    )
                                }
                            }
                            permissionState.status.shouldShowRationale -> {
                                // If permission is denied and should show rationale, display rationale button
                                IconButton(onClick = { permissionState.launchPermissionRequest() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Grant Camera Permission"
                                    )
                                }
                            }
                            else -> {
                                // If permission is denied and we can't show rationale, show button to open settings
                                IconButton(onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Open Settings"
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onSend(qrCodeText, amountText) }
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen() }