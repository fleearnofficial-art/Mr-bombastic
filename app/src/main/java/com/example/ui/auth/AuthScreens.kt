package com.example.ui.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import kotlinx.coroutines.launch
import com.example.ui.components.Geometric3DCanvas
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val appConfig by FleearnRepository.appConfig.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 3D Geometric shapes canvas background
        Geometric3DCanvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            primaryColor = BrandCyan,
            secondaryColor = BrandGold,
            accentColor = BrandPurple
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 300f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding badge
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version 5.0 (Beta)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandCyan
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Main Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = appConfig.appLogoUrl,
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandCyan, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = appConfig.appName,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )

                Text(
                    text = appConfig.slogan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = appConfig.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("welcome_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Login to Fleearn", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("welcome_signup_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Create New Account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (UserProfile) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var selectedRoleIndex by remember { mutableStateOf(0) }
    val roles = listOf(UserRole.USER, UserRole.PUBLISHER, UserRole.MODERATOR, UserRole.ADMIN)

    var uidText by remember { mutableStateOf("") }
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    val roleTitles = listOf("User (Fleearn)", "Publisher", "Moderator", "Admin")

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Login Access",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Role selection tabs
            TabRow(
                selectedTabIndex = selectedRoleIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                roleTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedRoleIndex == index,
                        onClick = {
                            selectedRoleIndex = index
                            errorMessage = ""
                            // Autofill demo for convenient reviewer testing
                            when (index) {
                                0 -> { uidText = "1098765432"; pinText = "12345678" }
                                1 -> { uidText = "8839201948"; pinText = "12345678" }
                                2 -> { uidText = "MX10293847"; pinText = "12345678" }
                                3 -> { uidText = "AX0123456789"; pinText = "25066013" }
                            }
                        },
                        text = {
                            Text(
                                text = title.split(" ").first(),
                                fontSize = 12.sp,
                                fontWeight = if (selectedRoleIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Role Description card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (selectedRoleIndex) {
                        3 -> BrandGold.copy(alpha = 0.1f)
                        2 -> BrandRed.copy(alpha = 0.1f)
                        1 -> BrandCyan.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerifiedBadge(role = roles[selectedRoleIndex], size = 24)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = roleTitles[selectedRoleIndex],
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when (selectedRoleIndex) {
                                3 -> "Fixed Admin UID (AX prefix) & PIN access"
                                2 -> "Moderator UID (MX prefix) provided by Admin"
                                1 -> "Publisher Console for Ads, Jobs, Products"
                                else -> "Standard Earning, Reselling, Social"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uidText,
                onValueChange = { uidText = it; errorMessage = "" },
                label = { Text("Account UID (${if (selectedRoleIndex == 3) "AX..." else if (selectedRoleIndex == 2) "MX..." else "10-digit"})") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_uid_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = pinText,
                onValueChange = { if (it.length <= 8) pinText = it; errorMessage = "" },
                label = { Text("8-Digit Secret PIN") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_pin_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showForgotPinDialog = true },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Forgot PIN?", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            var isLoading by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = BrandRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (uidText.isBlank()) {
                        errorMessage = "Please enter your UID"
                        return@Button
                    }
                    if (pinText.length < 8) {
                        errorMessage = "PIN must be 8 digits"
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        val result = FleearnRepository.login(uidText, pinText)
                        isLoading = false
                        if (result.first) {
                            FleearnRepository.currentUser.value?.let { onLoginSuccess(it) }
                        } else {
                            errorMessage = result.second
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val serverClientId = "801896661862-s9v8j1pmp12r08v4ltan7j124hpe4cvk.apps.googleusercontent.com"
                        val result = FleearnRepository.signInWithGoogle(context, serverClientId)
                        isLoading = false
                        if (result.first) {
                            if (result.second == "SUCCESS") {
                                FleearnRepository.currentUser.value?.let { onLoginSuccess(it) }
                            } else if (result.second.startsWith("NEW_GOOGLE_USER:")) {
                                val parts = result.second.split(":")
                                val googleUid = parts[1]
                                val googleEmail = parts[2]
                                val googleName = parts[3]
                                Toast.makeText(context, "Google authenticated! Completing registration...", Toast.LENGTH_LONG).show()
                                onNavigateToSignUp()
                            }
                        } else {
                            errorMessage = result.second
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_login_button"),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Sign Up Now", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showForgotPinDialog) {
        ForgotPinDialog(onDismiss = { showForgotPinDialog = false })
    }
}

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onRegistrationSuccess: (String) -> Unit
) {
    var isPublisherMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var orgName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("22") }
    var gender by remember { mutableStateOf("Male") }
    var country by remember { mutableStateOf("Bangladesh") }
    var server by remember { mutableStateOf("South Asia / Bangladesh") }
    var teamName by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    // 3 Security questions
    var q1Answer by remember { mutableStateOf("") }
    var q2Answer by remember { mutableStateOf("") }
    var q3Answer by remember { mutableStateOf("") }

    // Anti-bot challenge (e.g. 12 - 2 = ?)
    var botAnswer by remember { mutableStateOf("") }
    val botNum1 = 12
    val botNum2 = 2
    val botCorrect = botNum1 - botNum2

    var termsAgreed by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var generatedUidDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Create Fleearn Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Role switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isPublisherMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isPublisherMode = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "User (Fleearn)",
                        fontWeight = FontWeight.Bold,
                        color = if (!isPublisherMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPublisherMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isPublisherMode = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Publisher (FleConsole)",
                        fontWeight = FontWeight.Bold,
                        color = if (isPublisherMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = "" },
                label = { Text(if (isPublisherMode) "Publisher Representative Name" else "Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            if (isPublisherMode) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it; errorMessage = "" },
                    label = { Text("Company / Organization Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it; errorMessage = "" },
                label = { Text("Mobile Number (+880...)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it; errorMessage = "" },
                    label = { Text("Age (13-100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name (Optional)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = referralCode,
                    onValueChange = { referralCode = it },
                    label = { Text("Referral UID (Optional)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8) pin = it; errorMessage = "" },
                label = { Text("Set 8-Digit Secret PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 8) confirmPin = it; errorMessage = "" },
                label = { Text("Confirm 8-Digit PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Security Questions Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3 Mandatory Security Questions (For PIN Recovery)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = q1Answer,
                        onValueChange = { q1Answer = it; errorMessage = "" },
                        label = { Text("1. What memory do you remember most?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = q2Answer,
                        onValueChange = { q2Answer = it; errorMessage = "" },
                        label = { Text("2. Write any six numbers.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = q3Answer,
                        onValueChange = { q3Answer = it; errorMessage = "" },
                        label = { Text("3. Write the name of any person.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anti-bot challenge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Anti-Bot Verification Challenge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrandGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Solve: $botNum1 - $botNum2 = ?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = botAnswer,
                        onValueChange = { botAnswer = it; errorMessage = "" },
                        label = { Text("Your Answer") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAgreed,
                    onCheckedChange = { termsAgreed = it }
                )
                Text(
                    text = "I agree to the Terms of Service & Privacy Policy",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            var isLoading by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = BrandRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter your name"
                        return@Button
                    }
                    if (mobile.length < 10) {
                        errorMessage = "Please enter a valid mobile number"
                        return@Button
                    }
                    val age = ageText.toIntOrNull()
                    if (age == null || age < 13 || age > 100) {
                        errorMessage = "Age must be between 13 and 100"
                        return@Button
                    }
                    if (pin.length != 8) {
                        errorMessage = "PIN must be exactly 8 digits"
                        return@Button
                    }
                    if (pin != confirmPin) {
                        errorMessage = "PINs do not match"
                        return@Button
                    }
                    if (q1Answer.isBlank() || q2Answer.isBlank() || q3Answer.isBlank()) {
                        errorMessage = "Please answer all 3 security questions"
                        return@Button
                    }
                    if (botAnswer.trim() != botCorrect.toString()) {
                        errorMessage = "Anti-bot challenge answer is incorrect"
                        return@Button
                    }
                    if (!termsAgreed) {
                        errorMessage = "Please accept the Terms of Service"
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val role = if (isPublisherMode) UserRole.PUBLISHER else UserRole.USER
                            val newUid = FleearnRepository.registerUser(
                                role = role,
                                name = name,
                                mobile = mobile,
                                age = age,
                                gender = gender,
                                country = country,
                                server = server,
                                teamName = teamName,
                                referralCode = referralCode,
                                pin = pin,
                                q1 = q1Answer,
                                q2 = q2Answer,
                                q3 = q3Answer,
                                orgName = orgName
                            )
                            generatedUidDialog = newUid
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Registration failed."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Complete Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    generatedUidDialog?.let { uid ->
        val context = LocalContext.current
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(BrandGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BrandGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Registration Successful!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your unique 10-digit Fleearn account UID has been generated. Please save this UID and your 8-digit PIN securely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Your Account UID:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(uid, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Fleearn UID", uid)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UID copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy UID", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            generatedUidDialog = null
                            onRegistrationSuccess(uid)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Go to Login Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPinDialog(onDismiss: () -> Unit) {
    var uid by remember { mutableStateOf("") }
    var a1 by remember { mutableStateOf("") }
    var a2 by remember { mutableStateOf("") }
    var a3 by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reset 8-Digit PIN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Verify your account identity with your 3 security question answers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uid,
                    onValueChange = { uid = it },
                    label = { Text("Account UID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = a1,
                    onValueChange = { a1 = it },
                    label = { Text("1. Memory you remember most?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = a2,
                    onValueChange = { a2 = it },
                    label = { Text("2. Six numbers wrote during signup?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = a3,
                    onValueChange = { a3 = it },
                    label = { Text("3. Name of person wrote during signup?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 8) newPin = it },
                    label = { Text("New 8-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                var isLoading by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                if (msg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = msg,
                        color = if (isSuccess) BrandGreen else BrandRed,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (uid.isBlank() || a1.isBlank() || a2.isBlank() || a3.isBlank()) {
                            msg = "Please fill in all fields"
                            isSuccess = false
                            return@Button
                        }
                        if (newPin.length != 8) {
                            msg = "New PIN must be 8 digits"
                            isSuccess = false
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            val res = FleearnRepository.resetPinWithSecurity(uid, a1, a2, a3, newPin)
                            isLoading = false
                            msg = res.second
                            isSuccess = res.first
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Verify & Set New PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
