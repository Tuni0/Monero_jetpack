package com.example.monero_jetpack

import android.graphics.Color
import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun StartupScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate("home")
                } else {
                    Log.e("GoogleLogin", "Firebase login failed", task.exception)
                    Toast.makeText(context, "Google login failed", Toast.LENGTH_SHORT).show()

                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Google sign-in failed", e)
            Toast.makeText(context, "Google login error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("399010568004-a2tl0ct5kfgshg5u8529pqlbvc871265.apps.googleusercontent.com")
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            launcher.launch(googleSignInClient.signInIntent)
        }, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )

        ){ Image(
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

        Button(onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Log in with Email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("register") }, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )) {
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