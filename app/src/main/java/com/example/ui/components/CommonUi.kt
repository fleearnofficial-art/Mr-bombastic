package com.example.ui.components
import androidx.compose.material.icons.filled.ExitToApp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun VerifiedBadge(
    role: UserRole = UserRole.USER,
    modifier: Modifier = Modifier,
    size: Int = 16
) {
    val (badgeColor, icon) = when (role) {
        UserRole.ADMIN -> Color(0xFFFFD700) to Icons.Default.Security // Gold
        UserRole.MODERATOR -> Color(0xFFFF1744) to Icons.Default.Security // Red
        UserRole.PUBLISHER -> Color(0xFF00E5FF) to Icons.Default.Verified // Neon Cyan
        UserRole.USER -> Color(0xFF2979FF) to Icons.Default.Verified // Blue
    }

    Icon(
        imageVector = icon,
        contentDescription = "Verified Badge",
        tint = badgeColor,
        modifier = modifier.size(size.dp)
    )
}

@Composable
fun FctCurrencyPill(
    amount: Double,
    modifier: Modifier = Modifier,
    isGain: Boolean? = null
) {
    val formatted = String.format("%.2f", amount)
    val color = when (isGain) {
        true -> BrandGreen
        false -> BrandRed
        null -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$formatted FCT",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = color
        )
    }
}

@Composable
fun Success3DDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val scale = remember { Animatable(0.2f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .scale(scale.value),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandEmerald, BrandCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color(0xFF0A0B0E),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                ) {
                    Text(
                        text = "Continue",
                        color = Color(0xFF0A0B0E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    subtitle: String = "",
    actionText: String = "",
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted
                )
            }
        }
        if (actionText.isNotBlank()) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.heightIn(min = 40.dp)
            ) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandEmerald
                )
            }
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = com.example.ui.theme.BentoSurfaceCard
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(56.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(com.example.ui.theme.BrandRed.copy(alpha = 0.2f)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit",
                        tint = com.example.ui.theme.BrandRed,
                        modifier = androidx.compose.ui.Modifier.size(28.dp)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "Exit Fleearn?",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = com.example.ui.theme.BentoTextPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                androidx.compose.material3.Text(
                    text = "Are you sure you want to exit Fleearn?\nYour session will be saved locally.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = com.example.ui.theme.BentoTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                androidx.compose.material3.Button(
                    onClick = onConfirm,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(48.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.BrandRed
                    )
                ) {
                    androidx.compose.material3.Text("Exit Application", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(48.dp)
                ) {
                    androidx.compose.material3.Text("Cancel", color = com.example.ui.theme.BentoTextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}
