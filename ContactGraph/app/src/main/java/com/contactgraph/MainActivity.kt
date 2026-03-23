package com.contactgraph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.contactgraph.ui.MainViewModel
import com.contactgraph.ui.UiState
import com.contactgraph.ui.screens.GraphScreen
import com.contactgraph.ui.screens.ListScreen
import com.contactgraph.ui.theme.ContactGraphTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactGraphTheme {
                ContactGraphApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactGraphApp(viewModel: MainViewModel) {
    val uiState by viewModel.state.collectAsState()

    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.READ_CALL_LOG,
        )
    )

    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) viewModel.load()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !permissions.allPermissionsGranted -> PermissionScreen(
                    onRequest = { permissions.launchMultiplePermissionRequest() }
                )
                uiState is UiState.Idle || uiState is UiState.Loading -> LoadingScreen()
                uiState is UiState.Error -> ErrorScreen((uiState as UiState.Error).message) {
                    viewModel.load()
                }
                uiState is UiState.Success -> {
                    val contacts = (uiState as UiState.Success).contacts
                    MainContent(contacts)
                }
            }
        }
    }
}

@Composable
private fun MainContent(contacts: List<com.contactgraph.data.model.ContactNode>) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        // Tab bar
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.AccountTree, contentDescription = "Graph") },
                text = { Text("Graph") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.List, contentDescription = "List") },
                text = { Text("Ranked List") },
            )
        }

        Box(Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> GraphScreen(contacts)
                1 -> ListScreen(contacts)
            }
        }
    }
}

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📊", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(24.dp))
        Text(
            "Contact Graph needs access to your Contacts, SMS and Call Log to map your connections.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
            Text("Grant Permissions")
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Analysing your connections…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
