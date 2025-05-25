package com.example.battleship

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.battleship.ui.theme.BattleshipTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BattleshipTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HttpRequestScreen()
                }
            }
        }
    }
}

@Composable
fun HttpRequestScreen() {
    var responseText by remember { mutableStateOf("Click the button to fetch response") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = {
            thread {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://httpbin.org/get")
                    .build()
                try {
                    val response = client.newCall(request).execute()
                    val body = response.body?.string() ?: "No Response"
                    responseText = body
                } catch (e: Exception) {
                    responseText = "Error: ${e.message}"
                    Log.e("HTTP_ERROR", "Exception: ${e.message}", e)
                }
            }
        }) {
            Text("Send HTTP GET")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = responseText)
    }
}
