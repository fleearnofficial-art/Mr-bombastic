#!/bin/bash
sed -i 's/androidx.compose.material.icons.Icons.Default.ExitToApp/Icons.Default.ExitToApp/g' app/src/main/java/com/example/ui/components/CommonUi.kt
sed -i '/package com.example.ui.components/a import androidx.compose.material.icons.filled.ExitToApp' app/src/main/java/com/example/ui/components/CommonUi.kt
