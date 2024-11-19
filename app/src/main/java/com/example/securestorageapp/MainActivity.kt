package com.example.securestorageapp

import com.google.crypto.tink.config.TinkConfig
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.securestorageapp.ui.theme.SecureStorageAppTheme
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (TinkHelper.aead == null) {
            TinkHelper.initialize(getApplicationContext());
        }
        setContent {
            SecureStorageAppTheme {
                SecureStorageScreen()
            }
        }
    }
}

@Composable
fun SecureStorageScreen() {
    // Retrieve the current context
    val context = LocalContext.current

    // State variables
    var inputText by remember { mutableStateOf("") }
    var retrievedText by remember { mutableStateOf("Stored data will appear here.") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input field for entering data (e.g., credit card or PIN)
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter Credit Card or PIN") },
            modifier = Modifier.fillMaxWidth()
        )

        // Save button to store encrypted data
        Button(
            onClick = {
                try {
                    // Save the encrypted data
                    SecureStorage.saveEncryptedData(context, "mock_data", inputText)

                    val encryptedData = SecureStorage.retrieveEncryptedData(context, "mock_data")
//                    Log.d("EncryptedData", "Encrypted Data: $encryptedData")

                    inputText = "" // Clear the input field after saving
                    errorMessage = "" // Reset error message
                } catch (e: Exception) {
                    errorMessage = "Error saving data: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        // Retrieve button to fetch and decrypt data
        Button(
            onClick = {
                try {
                    // Retrieve the encrypted data
                    val retrieved = SecureStorage.retrieveEncryptedData(context, "mock_data")
                    retrievedText = retrieved ?: "No data found."
                    errorMessage = "" // Reset error message
                } catch (e: Exception) {
                    errorMessage = "Error retrieving data: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retrieve")
        }

        // Display the retrieved text or any errors
        Text(
            text = retrievedText,
            modifier = Modifier.fillMaxWidth()
        )

        // Display error message (if any)
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SecureStorageScreenPreview() {
    SecureStorageAppTheme {
        SecureStorageScreen()
    }
}
