package com.example.ui.moderator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun ModeratorInboxTab(moderator: UserProfile) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Inbox", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = BentoTextPrimary)
                IconButton(onClick = { /* Search */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = BentoTextPrimary)
                }
            }
        }
        
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search users or publishers...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.large
            )
        }
        
        item {
            Text("Recent Messages", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoTextSecondary, modifier = Modifier.padding(bottom = 12.dp))
        }

        items(3) { idx ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = BentoTextMuted)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("User ${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Hello, need help with my ticket.", fontSize = 12.sp, color = BentoTextSecondary)
                    }
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = BrandRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
