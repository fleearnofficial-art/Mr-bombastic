package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object InAppBrowserManager {
    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl = _currentUrl.asStateFlow()

    private val _initialTitle = MutableStateFlow<String?>("Web View")
    val initialTitle = _initialTitle.asStateFlow()

    fun openUrl(url: String, title: String? = null) {
        if (url.isBlank()) return
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("intent://") && !url.startsWith("market://")) {
            "https://$url"
        } else {
            url
        }
        _initialTitle.value = title ?: "Browser"
        _currentUrl.value = validUrl
    }

    fun close() {
        _currentUrl.value = null
    }
}

@Composable
fun InAppBrowserContainer() {
    val activeUrl by InAppBrowserManager.currentUrl.collectAsState()
    val defaultTitle by InAppBrowserManager.initialTitle.collectAsState()

    activeUrl?.let { url ->
        InAppBrowserModal(
            initialUrl = url,
            defaultTitle = defaultTitle ?: "Browser",
            onClose = { InAppBrowserManager.close() }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBrowserModal(
    initialUrl: String,
    defaultTitle: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrlDisplay by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf(defaultTitle) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("in_app_mini_browser"),
            color = BentoBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BentoBackground)
            ) {
                // Mini Browser Header Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BentoSurface,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorderSubtle, androidx.compose.ui.graphics.RectangleShape)
                    ) {
                        // Action Bar Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Close Button
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .testTag("browser_close_button")
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close Browser",
                                    tint = BentoTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Navigation Back / Forward Buttons
                            IconButton(
                                onClick = { webViewInstance?.goBack() },
                                enabled = canGoBack,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (canGoBack) BentoTextPrimary else BentoTextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                enabled = canGoForward,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = if (canGoForward) BentoTextPrimary else BentoTextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Center Address Bar / Info Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoSurfaceCard)
                                    .border(1.dp, BentoBorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Secure",
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Column {
                                        Text(
                                            text = pageTitle.ifBlank { "In-App Browser" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = BentoTextPrimary
                                        )
                                        Text(
                                            text = currentUrlDisplay.replace("https://", "").replace("http://", ""),
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = BentoTextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Copy URL Action
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("URL", currentUrlDisplay)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "URL Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp),
                                content = {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy URL",
                                        tint = BrandCyan,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            )

                            // Refresh Action
                            IconButton(
                                onClick = {
                                    hasError = false
                                    webViewInstance?.reload()
                                },
                                modifier = Modifier.size(34.dp),
                                content = {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh Page",
                                        tint = BentoTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            // Open in External App / Browser Action
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrlDisplay))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open external app: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(34.dp),
                                content = {
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = "Open in External App",
                                        tint = BrandGold,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            )
                        }

                        // Progress Indicator
                        if (isLoading && progress < 100) {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp),
                                color = BrandEmerald,
                                trackColor = Color.Transparent
                            )
                        }
                    }
                }

                // Browser Body Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (hasError) {
                        // Error & Fallback UI
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(BrandRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = BrandRed,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Failed to load webpage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BentoTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage.ifBlank { "Check your internet connection or try again." },
                                fontSize = 12.sp,
                                color = BentoTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        hasError = false
                                        webViewInstance?.reload()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrlDisplay))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Text("Open in App", color = BrandCyan)
                                }
                            }
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = settings.userAgentString + " FleearnApp/5.0"
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        progress = newProgress
                                        isLoading = newProgress < 100
                                    }

                                    override fun onReceivedTitle(view: WebView?, title: String?) {
                                        if (!title.isNullOrBlank()) {
                                            pageTitle = title
                                        }
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                        hasError = false
                                        url?.let {
                                            currentUrlDisplay = it
                                        }
                                        canGoBack = view?.canGoBack() ?: false
                                        canGoForward = view?.canGoForward() ?: false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        url?.let {
                                            currentUrlDisplay = it
                                        }
                                        canGoBack = view?.canGoBack() ?: false
                                        canGoForward = view?.canGoForward() ?: false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            hasError = true
                                            errorMessage = error?.description?.toString() ?: "Network error"
                                        }
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        
                                        // Check for standard web protocols
                                        if (url.startsWith("http://") || url.startsWith("https://")) {
                                            // Handle specific app intent domains if requested (e.g., play store, youtube app links)
                                            if (url.startsWith("https://play.google.com/store/apps/") || url.startsWith("market://")) {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    ctx.startActivity(intent)
                                                    return true
                                                } catch (_: Exception) {
                                                    return false
                                                }
                                            }
                                            return false // Let WebView load internally
                                        }

                                        // Custom Scheme / App Intent Handling (e.g., intent://, market://, whatsapp://, tg://, tel:, mailto:)
                                        try {
                                            val intent: Intent = if (url.startsWith("intent://")) {
                                                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                            } else {
                                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            }

                                            if (intent.resolveActivity(ctx.packageManager) != null) {
                                                ctx.startActivity(intent)
                                                return true
                                            } else {
                                                // Fallback to browser fallback url if provided in intent
                                                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                                if (!fallbackUrl.isNullOrBlank()) {
                                                    view?.loadUrl(fallbackUrl)
                                                    return true
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Could not resolve intent
                                        }
                                        return true
                                    }
                                }

                                loadUrl(initialUrl)
                                webViewInstance = this
                            }
                        },
                        update = { view ->
                            webViewInstance = view
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
