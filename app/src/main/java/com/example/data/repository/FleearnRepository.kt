package com.example.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.data.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// A custom suspend extension to await Play Services tasks safely without extra dependency requirements
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: RuntimeException("Firebase task failed"))
        }
    }
}

object FleearnRepository {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _appConfig = MutableStateFlow(SystemAppConfig())
    val appConfig: StateFlow<SystemAppConfig> = _appConfig.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    private val _depositRequests = MutableStateFlow<List<DepositRequest>>(emptyList())
    val depositRequests: StateFlow<List<DepositRequest>> = _depositRequests.asStateFlow()

    private val _withdrawRequests = MutableStateFlow<List<WithdrawRequest>>(emptyList())
    val withdrawRequests: StateFlow<List<WithdrawRequest>> = _withdrawRequests.asStateFlow()

    private val _rechargeRequests = MutableStateFlow<List<MobileRechargeRequest>>(emptyList())
    val rechargeRequests: StateFlow<List<MobileRechargeRequest>> = _rechargeRequests.asStateFlow()

    private val _jobs = MutableStateFlow<List<JobItem>>(emptyList())
    val jobs: StateFlow<List<JobItem>> = _jobs.asStateFlow()

    private val _jobSubmissions = MutableStateFlow<List<JobSubmission>>(emptyList())
    val jobSubmissions: StateFlow<List<JobSubmission>> = _jobSubmissions.asStateFlow()

    private val _products = MutableStateFlow<List<ProductItem>>(emptyList())
    val products: StateFlow<List<ProductItem>> = _products.asStateFlow()

    private val _productOrders = MutableStateFlow<List<ProductOrder>>(emptyList())
    val productOrders: StateFlow<List<ProductOrder>> = _productOrders.asStateFlow()

    private val _services = MutableStateFlow<List<ServiceItem>>(emptyList())
    val services: StateFlow<List<ServiceItem>> = _services.asStateFlow()

    private val _serviceOrders = MutableStateFlow<List<ServiceOrder>>(emptyList())
    val serviceOrders: StateFlow<List<ServiceOrder>> = _serviceOrders.asStateFlow()

    private val _ads = MutableStateFlow<List<AdItem>>(emptyList())
    val ads: StateFlow<List<AdItem>> = _ads.asStateFlow()

    private val _tickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val tickets: StateFlow<List<SupportTicket>> = _tickets.asStateFlow()

    private val _reports = MutableStateFlow<List<UserReport>>(emptyList())
    val reports: StateFlow<List<UserReport>> = _reports.asStateFlow()

    private val _posts = MutableStateFlow<List<UserPost>>(emptyList())
    val posts: StateFlow<List<UserPost>> = _posts.asStateFlow()

    private val _events = MutableStateFlow<List<EventItem>>(emptyList())
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private val _shippingBanners = MutableStateFlow<List<ShippingBanner>>(emptyList())
    val shippingBanners: StateFlow<List<ShippingBanner>> = _shippingBanners.asStateFlow()

    private val _channels = MutableStateFlow<List<OfficialChannel>>(emptyList())
    val channels: StateFlow<List<OfficialChannel>> = _channels.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private val pendingMessages = java.util.Collections.synchronizedList(mutableListOf<ChatMessage>())

    private val _conversationMetas = MutableStateFlow<List<ConversationMeta>>(emptyList())
    val conversationMetas: StateFlow<List<ConversationMeta>> = _conversationMetas.asStateFlow()

    private val _callHistory = MutableStateFlow<List<CallRecord>>(emptyList())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()
    
    private val _blockedUsers = MutableStateFlow<Set<String>>(emptySet())
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    private val _moderatorInstructions = MutableStateFlow("1. Review user tickets within 15 minutes.\n2. Ensure all publisher campaigns comply with Fleearn safety.\n3. Escalate high priority financial inquiries to Administration.")
    val moderatorInstructions: StateFlow<String> = _moderatorInstructions.asStateFlow()

    private val _ratings = MutableStateFlow<List<Pair<Int, String>>>(listOf(5 to "Best 3D earning Fleearn!", 5 to "Super fast instant withdrawal."))
    val ratings: StateFlow<List<Pair<Int, String>>> = _ratings.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    private val _broadcastNotifications = MutableStateFlow<List<BroadcastNotification>>(emptyList())
    val broadcastNotifications: StateFlow<List<BroadcastNotification>> = _broadcastNotifications.asStateFlow()

    private val _salaryRecords = MutableStateFlow<List<ModeratorSalaryRecord>>(emptyList())
    val salaryRecords: StateFlow<List<ModeratorSalaryRecord>> = _salaryRecords.asStateFlow()

    private val globalListeners = mutableListOf<ListenerRegistration>()
    private val userListeners = mutableListOf<ListenerRegistration>()

    init {
        // Setup global snapshot listeners immediately on initialization
        listenToGlobalData()

        // Keep track of the currently logged in Firebase Auth state
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToUserSpecificData(user.uid)
            } else {
                _currentUser.value = null
                clearUserSpecificListeners()
            }
        }
    }

    private fun listenToGlobalData() {
        globalListeners.forEach { it.remove() }
        globalListeners.clear()

        // 1. Listen to system configurations
        val configL = firestore.collection("appConfig").document("config")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    repositoryScope.launch {
                        _appConfig.value = snapshot.toSystemAppConfig()
                    }
                }
            }
        globalListeners.add(configL)

        // 2. Listen to posts
        val postsL = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _posts.value = snapshot.documents.map { it.toUserPost() }
                    }
                }
            }
        globalListeners.add(postsL)

        // 3. Listen to notifications
        val notifL = firestore.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _broadcastNotifications.value = snapshot.documents.map { it.toBroadcastNotification() }
                    }
                }
            }
        globalListeners.add(notifL)

        // 4. Listen to jobs
        val jobsL = firestore.collection("jobs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _jobs.value = snapshot.documents.map { it.toJobItem() }
                    }
                }
            }
        globalListeners.add(jobsL)

        // 5. Listen to products
        val productsL = firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _products.value = snapshot.documents.map { it.toProductItem() }
                    }
                }
            }
        globalListeners.add(productsL)

        // 6. Listen to services
        val servicesL = firestore.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _services.value = snapshot.documents.map { it.toServiceItem() }
                    }
                }
            }
        globalListeners.add(servicesL)

        // 7. Listen to ads
        val adsL = firestore.collection("ads")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _ads.value = snapshot.documents.map { it.toAdItem() }
                    }
                }
            }
        globalListeners.add(adsL)

        // 8. Listen to events
        val eventsL = firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _events.value = snapshot.documents.map { it.toEventItem() }
                    }
                }
            }
        globalListeners.add(eventsL)

        // 9. Listen to shipping banners
        val bannersL = firestore.collection("shippingBanners")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _shippingBanners.value = snapshot.documents.map { it.toShippingBanner() }
                    }
                }
            }
        globalListeners.add(bannersL)

        // 10. Listen to official channels
        val channelsL = firestore.collection("channels")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _channels.value = snapshot.documents.map { it.toOfficialChannel() }
                    }
                }
            }
        globalListeners.add(channelsL)

        // 11. Listen to all users list (For admin operations)
        val usersL = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _users.value = snapshot.documents.mapNotNull { it.toUserProfile() }
                    }
                }
            }
        globalListeners.add(usersL)

        // 12. Listen to audit logs
        val auditL = firestore.collection("auditLogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _auditLogs.value = snapshot.documents.map { it.toAdminAuditLog() }
                    }
                }
            }
        globalListeners.add(auditL)
    }

    private fun listenToUserSpecificData(uid: String) {
        clearUserSpecificListeners()

        // 1. Listen to current user profile document
        val profileL = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    repositoryScope.launch {
                        _currentUser.value = snapshot.toUserProfile()
                    }
                }
            }
        userListeners.add(profileL)

        // 2. Listen to user transactions
        val txL = firestore.collection("transactions")
            .whereEqualTo("userUid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _transactions.value = snapshot.documents.map { it.toTransactionItem() }
                    }
                }
            }
        userListeners.add(txL)

        // 3. Listen to deposit requests
        val depL = firestore.collection("depositRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toDepositRequest() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _depositRequests.value = if (role == UserRole.ADMIN || role == UserRole.MODERATOR) {
                            all
                        } else {
                            all.filter { it.userUid == uid }
                        }
                    }
                }
            }
        userListeners.add(depL)

        // 4. Listen to withdraw requests
        val withdrawL = firestore.collection("withdrawRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toWithdrawRequest() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _withdrawRequests.value = if (role == UserRole.ADMIN || role == UserRole.MODERATOR) {
                            all
                        } else {
                            all.filter { it.userUid == uid }
                        }
                    }
                }
            }
        userListeners.add(withdrawL)

        // 5. Listen to mobile recharge requests
        val rechargeL = firestore.collection("rechargeRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toMobileRechargeRequest() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _rechargeRequests.value = if (role == UserRole.ADMIN) {
                            all
                        } else {
                            all.filter { it.userUid == uid }
                        }
                    }
                }
            }
        userListeners.add(rechargeL)

        // 6. Listen to job submissions
        val submissionL = firestore.collection("jobSubmissions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toJobSubmission() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _jobSubmissions.value = when (role) {
                            UserRole.ADMIN, UserRole.MODERATOR -> all
                            UserRole.PUBLISHER -> all.filter { it.publisherUid == uid }
                            else -> all.filter { it.userUid == uid }
                        }
                    }
                }
            }
        userListeners.add(submissionL)

        // 7. Listen to product orders
        val productOrderL = firestore.collection("productOrders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toProductOrder() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _productOrders.value = when (role) {
                            UserRole.ADMIN, UserRole.MODERATOR -> all
                            UserRole.PUBLISHER -> all.filter { it.publisherUid == uid }
                            else -> all.filter { it.buyerUid == uid }
                        }
                    }
                }
            }
        userListeners.add(productOrderL)

        // 8. Listen to service orders
        val serviceOrderL = firestore.collection("serviceOrders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toServiceOrder() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _serviceOrders.value = when (role) {
                            UserRole.ADMIN, UserRole.MODERATOR -> all
                            UserRole.PUBLISHER -> all.filter { it.publisherUid == uid }
                            else -> all.filter { it.buyerUid == uid }
                        }
                    }
                }
            }
        userListeners.add(serviceOrderL)

        // 9. Listen to support tickets
        val ticketL = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toSupportTicket() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _tickets.value = when (role) {
                            UserRole.ADMIN -> all
                            UserRole.MODERATOR -> all.filter { it.assignedModeratorUid == uid || it.assignedModeratorUid.isEmpty() }
                            else -> all.filter { it.creatorUid == uid }
                        }
                    }
                }
            }
        userListeners.add(ticketL)

        // 10. Listen to reports
        val reportL = firestore.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val all = snapshot.documents.map { it.toUserReport() }
                        val role = _currentUser.value?.role ?: UserRole.USER
                        _reports.value = if (role == UserRole.ADMIN || role == UserRole.MODERATOR) {
                            all
                        } else {
                            all.filter { it.reporterUid == uid }
                        }
                    }
                }
            }
        userListeners.add(reportL)

        // 11. Listen to direct messaging chat history
        val messagesL = firestore.collection("chatMessages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val remoteMsgs = snapshot.documents.map { it.toChatMessage() }
                            .filter { it.senderUid == uid || it.receiverUid == uid }
                        val merged = (remoteMsgs + pendingMessages.filter { pm -> remoteMsgs.none { rm -> rm.id == pm.id } }).sortedBy { it.timestamp }
                        _messages.value = merged
                    }
                }
            }
        userListeners.add(messagesL)

        // 12. Listen to user conversations metadata (pin, mute, unread, etc.)
        val conversationsL = firestore.collection("userConversations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _conversationMetas.value = snapshot.documents.map { it.toConversationMeta() }
                            .filter { it.userId == uid }
                    }
                }
            }
        userListeners.add(conversationsL)

        // 13. Listen to call history
        val callsL = firestore.collection("callHistory")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        _callHistory.value = snapshot.documents.map { it.toCallRecord() }
                            .filter { it.callerUid == uid || it.receiverUid == uid }
                    }
                }
            }
        userListeners.add(callsL)

        // 14. Listen to blocked users list
        val blockedL = firestore.collection("blockedUsers")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    repositoryScope.launch {
                        val set = snapshot.documents.mapNotNull { it.getString("blockedUserId") }.toSet()
                        _blockedUsers.value = set
                    }
                }
            }
        userListeners.add(blockedL)
    }

    private fun clearUserSpecificListeners() {
        userListeners.forEach { it.remove() }
        userListeners.clear()
    }

    // AUTH ACTIONS (Suspend functions executing safely on Dispatchers.IO)
    suspend fun login(uidInput: String, pin: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val input = uidInput.trim()
        val pinTrimmed = pin.trim()

        try {
            // 1. Resolve email address to sign in
            var resolvedEmail = ""
            if (input.contains("@")) {
                resolvedEmail = input
            } else {
                // First try static maps/patterns to avoid querying Firestore entirely if possible
                val defaultEmail = when (input) {
                    "1098765432" -> "tanvir@fleearn.com"
                    "8839201948" -> "publisher@fleearn.com"
                    "MX10293847" -> "moderator@fleearn.com"
                    "AX0123456789" -> "admin@fleearn.com"
                    else -> ""
                }
                
                if (defaultEmail.isNotEmpty()) {
                    resolvedEmail = defaultEmail
                } else if (input.startsWith("MX") && input.length >= 8) {
                    resolvedEmail = "mod_${input}@fleearn.com"
                } else {
                    // Try to clean input and check if it's a mobile number
                    val cleanInput = input.replace("+", "").replace("-", "").replace(" ", "").trim()
                    if (cleanInput.all { it.isDigit() } && cleanInput.length >= 8) {
                        resolvedEmail = "${cleanInput}@fleearn.com"
                    }
                }

                // If static resolution failed, try querying Firestore safely (by signing in anonymously first if needed)
                if (resolvedEmail.isEmpty()) {
                    var signedInAnonymously = false
                    try {
                        if (auth.currentUser == null) {
                            auth.signInAnonymously().await()
                            signedInAnonymously = true
                        }
                        
                        // Try direct document get first (if input is Firebase Auth UID)
                        val doc = firestore.collection("users").document(input).get().await()
                        if (doc.exists()) {
                            resolvedEmail = doc.getString("email") ?: ""
                        }
                        
                        if (resolvedEmail.isEmpty()) {
                            val userQuery = firestore.collection("users")
                                .whereEqualTo("uid", input)
                                .get()
                                .await()
                            if (!userQuery.isEmpty) {
                                resolvedEmail = userQuery.documents.first().getString("email") ?: ""
                            }
                        }
                        
                        if (resolvedEmail.isEmpty()) {
                            val mobileQuery = firestore.collection("users")
                                .whereEqualTo("mobile", input)
                                .get()
                                .await()
                            if (!mobileQuery.isEmpty) {
                                resolvedEmail = mobileQuery.documents.first().getString("email") ?: ""
                            }
                        }
                    } catch (e: Exception) {
                        // Query failed, but we want to fail gracefully with a descriptive message
                    } finally {
                        if (signedInAnonymously && auth.currentUser?.isAnonymous == true) {
                            auth.signOut()
                        }
                    }
                }
            }

            if (resolvedEmail.isEmpty()) {
                val defaultEmail = when (input) {
                    "1098765432" -> "tanvir@fleearn.com"
                    "8839201948" -> "publisher@fleearn.com"
                    "MX10293847" -> "moderator@fleearn.com"
                    "AX0123456789" -> "admin@fleearn.com"
                    else -> ""
                }
                if (defaultEmail.isNotEmpty()) {
                    resolvedEmail = defaultEmail
                }
            }

            if (resolvedEmail.isEmpty()) {
                return@withContext Pair(false, "No account found matching this UID or mobile number.")
            }

            // 2. Login via Firebase Auth (or auto-register demo/seed accounts if not in Firebase Auth yet)
            val firebaseUser = try {
                val authResult = auth.signInWithEmailAndPassword(resolvedEmail, pinTrimmed).await()
                authResult.user
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: ""
                if (errMsg.contains("operation is not allowed", ignoreCase = true) ||
                    e.javaClass.name.contains("OperationNotSupported", ignoreCase = true)
                ) {
                    return@withContext Pair(
                        false,
                        "Firebase Auth error: Email/Password প্রোভাইডার চালু নেই! দয়া করে Firebase Console > Authentication > Sign-in method-এ গিয়ে 'Email/Password' Enable করুন।"
                    )
                }

                // If user doesn't exist yet, attempt to create/register automatically (especially for demo accounts & first logins)
                val isDemo = input in listOf("1098765432", "8839201948", "MX10293847", "AX0123456789")
                if (isDemo || errMsg.contains("no user record", ignoreCase = true) || errMsg.contains("invalid-credential", ignoreCase = true)) {
                    try {
                        val createResult = auth.createUserWithEmailAndPassword(resolvedEmail, pinTrimmed).await()
                        createResult.user
                    } catch (createEx: Exception) {
                        null
                    }
                } else {
                    null
                }
            } ?: return@withContext Pair(false, "Authentication failed: Invalid credentials or user not found.")

            // 3. Retrieve details from Firestore or initialize profile
            var profile: UserProfile? = null
            var permissionDeniedOccurred = false

            try {
                val userProfileDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                if (userProfileDoc.exists()) {
                    profile = userProfileDoc.toUserProfile()
                }
            } catch (fsEx: Exception) {
                val fsMsg = fsEx.localizedMessage ?: ""
                if (fsMsg.contains("PERMISSION_DENIED", ignoreCase = true) || fsMsg.contains("permission", ignoreCase = true)) {
                    permissionDeniedOccurred = true
                }
            }

            // If profile does not exist in Firestore yet (e.g. freshly seeded user), build it and attempt to save
            if (profile == null) {
                val role = when {
                    input == "AX0123456789" -> UserRole.ADMIN
                    input == "MX10293847" || input.startsWith("MX") -> UserRole.MODERATOR
                    input == "8839201948" -> UserRole.PUBLISHER
                    else -> UserRole.USER
                }
                val defaultName = when (input) {
                    "1098765432" -> "Tanvir Ahmed"
                    "8839201948" -> "Nexus Media Group"
                    "MX10293847" -> "Senior Moderator (FleUra)"
                    "AX0123456789" -> "Super Admin (Global)"
                    else -> if (input.contains("@")) input.substringBefore("@") else "User $input"
                }
                profile = UserProfile(
                    uid = firebaseUser.uid,
                    pinHash = pinTrimmed,
                    role = role,
                    name = defaultName,
                    mobile = if (input.all { it.isDigit() || it == '+' }) input else "+880 1700-000000",
                    balance = if (role == UserRole.ADMIN) 50000.0 else if (role == UserRole.PUBLISHER) 1000.0 else 500.0,
                    email = resolvedEmail,
                    organizationName = if (role == UserRole.PUBLISHER) "Nexus Digital Corp" else ""
                )

                // Try to save to Firestore if permissions allow
                try {
                    firestore.collection("users").document(firebaseUser.uid).set(profile).await()
                } catch (saveEx: Exception) {
                    // Ignored if permissions are restricted
                }
            }

            if (profile.isSuspended) {
                auth.signOut()
                return@withContext Pair(false, "Your account has been suspended. Please contact Fleearn support.")
            }

            _currentUser.value = profile

            if (permissionDeniedOccurred) {
                Pair(true, "Login successful as ${profile.role.name} (Note: Firestore Rules update recommended: allow read, write: if request.auth != null;)")
            } else {
                Pair(true, "Login successful as ${profile.role.name}")
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown authentication error occurred."
            if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("permission", ignoreCase = true)) {
                Pair(false, "Firebase Firestore Permission Denied! দয়া করে Firebase Console-এ Cloud Firestore Rules আপডেট করুন।")
            } else {
                Pair(false, msg)
            }
        }
    }

    suspend fun registerUser(
        role: UserRole,
        name: String,
        mobile: String,
        age: Int,
        gender: String,
        country: String,
        server: String,
        teamName: String,
        referralCode: String,
        pin: String,
        q1: String,
        q2: String,
        q3: String,
        orgName: String = "",
        photoUrl: String = ""
    ): String = withContext(Dispatchers.IO) {
        val uniqueEmail = "${mobile.replace("+", "").trim()}@fleearn.com"

        // 1. Create account in Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(uniqueEmail, pin.trim()).await()
        val firebaseUser = authResult.user ?: throw RuntimeException("Failed to register Firebase User")

        val uid = firebaseUser.uid

        // 2. Build profile and store in Firestore
        val newUser = UserProfile(
            uid = uid,
            pinHash = pin.trim(),
            role = role,
            name = name,
            mobile = mobile,
            age = age,
            gender = gender,
            country = country,
            server = server,
            teamName = teamName,
            referralCode = referralCode,
            profilePhotoUrl = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150" },
            organizationName = orgName,
            secAnswer1 = q1,
            secAnswer2 = q2,
            secAnswer3 = q3,
            balance = if (role == UserRole.USER) 50.0 else 100.0, // Initial welcome reward bonus
            totalEarnings = if (role == UserRole.USER) 50.0 else 100.0,
            plan = UserPlan.TRIAL,
            isVerified = false,
            email = uniqueEmail
        )

        try {
            firestore.collection("users").document(uid).set(newUser).await()
        } catch (e: Exception) {
            // Allow registration to proceed even if Firestore write rules are pending
        }
        _currentUser.value = newUser

        // Audit log
        try {
            addAuditLog("USER_REGISTER", uid, "Successfully created profile with role ${role.name}")
        } catch (e: Exception) {
            // Ignore audit log failure
        }

        uid
    }

    suspend fun resetPinWithSecurity(uidInput: String, a1: String, a2: String, a3: String, newPin: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val input = uidInput.trim()
        var signedInAnonymously = false
        try {
            if (auth.currentUser == null) {
                try {
                    auth.signInAnonymously().await()
                    signedInAnonymously = true
                } catch (anonEx: Exception) {
                    // Ignore and try queries anyway
                }
            }

            // Find user document
            var userDoc: DocumentSnapshot? = null
            val query1 = firestore.collection("users").whereEqualTo("uid", input).get().await()
            if (!query1.isEmpty) {
                userDoc = query1.documents.first()
            } else {
                val query2 = firestore.collection("users").whereEqualTo("mobile", input).get().await()
                if (!query2.isEmpty) {
                    userDoc = query2.documents.first()
                }
            }

            if (userDoc == null || !userDoc.exists()) {
                return@withContext Pair(false, "User account not found.")
            }

            val profile = userDoc.toUserProfile() ?: return@withContext Pair(false, "Could not load user profile.")

            if (!profile.secAnswer1.equals(a1.trim(), ignoreCase = true) ||
                !profile.secAnswer2.equals(a2.trim(), ignoreCase = true) ||
                !profile.secAnswer3.equals(a3.trim(), ignoreCase = true)) {
                return@withContext Pair(false, "Security answers do not match our records.")
            }

            // Reset password in Firebase Auth if it's the logged-in user, otherwise update pin in Firestore
            val firebaseUser = auth.currentUser
            if (firebaseUser != null && firebaseUser.uid == profile.uid && !firebaseUser.isAnonymous) {
                firebaseUser.updatePassword(newPin.trim()).await()
            }

            firestore.collection("users").document(profile.uid)
                .update("pinHash", newPin.trim()).await()

            Pair(true, "PIN reset successfully! You can now login.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to reset PIN.")
        } finally {
            if (signedInAnonymously && auth.currentUser?.isAnonymous == true) {
                auth.signOut()
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        clearUserSpecificListeners()
    }

    // Modern Google Sign-In helper via Android Credential Manager
    suspend fun signInWithGoogle(context: Context, serverClientId: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user ?: return@withContext Pair(false, "Google Auth authentication failed.")

                val uid = firebaseUser.uid
                val userDoc = firestore.collection("users").document(uid).get().await()

                if (userDoc.exists()) {
                    val profile = userDoc.toUserProfile()
                    _currentUser.value = profile
                    return@withContext Pair(true, "SUCCESS")
                } else {
                    // Profile does not exist yet. Return special code to UI for completing profile
                    return@withContext Pair(true, "NEW_GOOGLE_USER:${uid}:${firebaseUser.email ?: ""}:${firebaseUser.displayName ?: ""}")
                }
            }
            return@withContext Pair(false, "Unsupported credential format.")
        } catch (e: Exception) {
            return@withContext Pair(false, e.localizedMessage ?: "Google Sign-In initialization failed.")
        }
    }

    // Finish registering a Google User
    suspend fun registerGoogleUser(
        uid: String,
        email: String,
        role: UserRole,
        name: String,
        mobile: String,
        age: Int,
        gender: String,
        country: String,
        server: String,
        pin: String,
        q1: String,
        q2: String,
        q3: String
    ) = withContext(Dispatchers.IO) {
        val newUser = UserProfile(
            uid = uid,
            pinHash = pin.trim(),
            role = role,
            name = name,
            mobile = mobile,
            age = age,
            gender = gender,
            country = country,
            server = server,
            secAnswer1 = q1,
            secAnswer2 = q2,
            secAnswer3 = q3,
            balance = 50.0,
            totalEarnings = 50.0,
            plan = UserPlan.TRIAL,
            email = email
        )
        firestore.collection("users").document(uid).set(newUser).await()
        _currentUser.value = newUser
    }

    // FINANCIAL OPERATIONS
    fun requestDeposit(amount: Double, method: String, senderNumber: String, txid: String): String {
        val user = _currentUser.value ?: return "Not logged in"
        val reqId = "DEP_${System.currentTimeMillis()}"
        val req = DepositRequest(
            id = reqId,
            userUid = user.uid,
            userName = user.name,
            amount = amount,
            method = method,
            senderNumber = senderNumber,
            transactionId = txid
        )
        firestore.collection("depositRequests").document(reqId).set(req)
        return reqId
    }

    fun approveDeposit(requestId: String) {
        repositoryScope.launch {
            try {
                val reqSnapshot = firestore.collection("depositRequests").document(requestId).get().await()
                if (!reqSnapshot.exists()) return@launch
                val req = reqSnapshot.toDepositRequest()

                firestore.runTransaction { transaction ->
                    val userRef = firestore.collection("users").document(req.userUid)
                    val userSnapshot = transaction.get(userRef)
                    if (userSnapshot.exists()) {
                        val currentBalance = userSnapshot.getDouble("balance") ?: 0.0
                        transaction.update(userRef, "balance", currentBalance + req.amount)
                    }

                    val reqRef = firestore.collection("depositRequests").document(requestId)
                    transaction.update(reqRef, "status", "APPROVED")

                    val txId = "TX_${System.currentTimeMillis()}"
                    val tx = TransactionItem(
                        id = txId,
                        userUid = req.userUid,
                        type = "DEPOSIT",
                        amount = req.amount,
                        details = "Deposit approved via ${req.method} (TXID: ${req.transactionId})"
                    )
                    transaction.set(firestore.collection("transactions").document(txId), tx)
                }.await()
            } catch (e: Exception) {
                // Handle or log error
            }
        }
    }

    fun rejectDeposit(requestId: String) {
        firestore.collection("depositRequests").document(requestId).update("status", "REJECTED")
    }

    fun requestWithdraw(amount: Double, method: String, walletNumber: String): Pair<Boolean, String> {
        val user = _currentUser.value ?: return Pair(false, "Not logged in")

        val fee = if (user.role == UserRole.MODERATOR) {
            50.0
        } else {
            val thousands = (amount / 1000).toInt()
            if (amount < 1000) 30.0 else (thousands + 1) * 30.0
        }

        val totalDeduction = amount + fee
        if (user.balance < totalDeduction) {
            return Pair(false, "Insufficient balance. Required $totalDeduction FCT including $fee FCT fee.")
        }

        repositoryScope.launch {
            try {
                firestore.runTransaction { transaction ->
                    val userRef = firestore.collection("users").document(user.uid)
                    val userSnap = transaction.get(userRef)
                    val currentBal = userSnap.getDouble("balance") ?: 0.0
                    val currentWithdrawn = userSnap.getDouble("totalWithdrawn") ?: 0.0

                    if (currentBal >= totalDeduction) {
                        transaction.update(userRef, "balance", currentBal - totalDeduction)
                        transaction.update(userRef, "totalWithdrawn", currentWithdrawn + amount)

                        val reqId = "WTH_${System.currentTimeMillis()}"
                        val req = WithdrawRequest(
                            id = reqId,
                            userUid = user.uid,
                            userName = user.name,
                            userRole = user.role,
                            amount = amount,
                            fee = fee,
                            netAmount = amount,
                            method = method,
                            walletNumber = walletNumber
                        )
                        transaction.set(firestore.collection("withdrawRequests").document(reqId), req)

                        val txId = "TX_${System.currentTimeMillis()}"
                        val tx = TransactionItem(
                            id = txId,
                            userUid = user.uid,
                            type = "WITHDRAW",
                            amount = amount,
                            fee = fee,
                            details = "Withdrawal request to $method ($walletNumber)"
                        )
                        transaction.set(firestore.collection("transactions").document(txId), tx)
                    }
                }.await()
            } catch (e: Exception) {
                // handle error
            }
        }
        return Pair(true, "Withdrawal request submitted! Net: $amount FCT (Fee: $fee FCT)")
    }

    fun approveWithdraw(requestId: String) {
        firestore.collection("withdrawRequests").document(requestId).update("status", "APPROVED")
    }

    fun rejectWithdraw(requestId: String) {
        repositoryScope.launch {
            try {
                val reqSnap = firestore.collection("withdrawRequests").document(requestId).get().await()
                if (!reqSnap.exists()) return@launch
                val req = reqSnap.toWithdrawRequest()

                firestore.runTransaction { transaction ->
                    val userRef = firestore.collection("users").document(req.userUid)
                    val userSnap = transaction.get(userRef)
                    if (userSnap.exists()) {
                        val currentBal = userSnap.getDouble("balance") ?: 0.0
                        val currentWithdrawn = userSnap.getDouble("totalWithdrawn") ?: 0.0
                        val refundAmount = req.amount + req.fee

                        transaction.update(userRef, "balance", currentBal + refundAmount)
                        transaction.update(userRef, "totalWithdrawn", (currentWithdrawn - req.amount).coerceAtLeast(0.0))
                    }
                    transaction.update(firestore.collection("withdrawRequests").document(requestId), "status", "REJECTED")
                }.await()
            } catch (e: Exception) {
                // log
            }
        }
    }

    suspend fun sendMoney(receiverUid: String, amount: Double, pin: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val sender = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        if (sender.pinHash != pin.trim()) {
            return@withContext Pair(false, "Incorrect 8-digit PIN")
        }
        val rUid = receiverUid.trim()
        if (sender.uid == rUid) {
            return@withContext Pair(false, "Cannot send money to your own UID")
        }

        val fee = 9.0
        val total = amount + fee

        try {
            val receiverSnap = firestore.collection("users").document(rUid).get().await()
            if (!receiverSnap.exists()) {
                return@withContext Pair(false, "Receiver UID not found in Fleearn system.")
            }
            val receiverName = receiverSnap.getString("name") ?: "Receiver"

            firestore.runTransaction { transaction ->
                val senderRef = firestore.collection("users").document(sender.uid)
                val receiverRef = firestore.collection("users").document(rUid)

                val senderSnap = transaction.get(senderRef)
                val senderBal = senderSnap.getDouble("balance") ?: 0.0

                if (senderBal >= total) {
                    val receiverBal = receiverSnap.getDouble("balance") ?: 0.0
                    val receiverTotalEarnings = receiverSnap.getDouble("totalEarnings") ?: 0.0

                    transaction.update(senderRef, "balance", senderBal - total)
                    transaction.update(receiverRef, "balance", receiverBal + amount)
                    transaction.update(receiverRef, "totalEarnings", receiverTotalEarnings + amount)

                    val txSId = "TX_${System.currentTimeMillis()}_S"
                    val txS = TransactionItem(
                        id = txSId,
                        userUid = sender.uid,
                        type = "SEND_MONEY",
                        amount = amount,
                        fee = fee,
                        details = "Sent money to $receiverName ($rUid)"
                    )
                    transaction.set(firestore.collection("transactions").document(txSId), txS)

                    val txRId = "TX_${System.currentTimeMillis()}_R"
                    val txR = TransactionItem(
                        id = txRId,
                        userUid = rUid,
                        type = "RECEIVE_MONEY",
                        amount = amount,
                        details = "Received money from ${sender.name} (${sender.uid})"
                    )
                    transaction.set(firestore.collection("transactions").document(txRId), txR)
                }
            }.await()

            Pair(true, "Sent $amount FCT successfully to $receiverName! (9 FCT fee deducted)")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to perform transfer.")
        }
    }

    suspend fun requestMobileRecharge(mobile: String, amount: Double): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        val fee = 9.0
        val total = amount + fee

        if (user.balance < total) {
            return@withContext Pair(false, "Insufficient balance. Required $total FCT ($amount FCT + 9 FCT fee)")
        }

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(user.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= total) {
                    transaction.update(userRef, "balance", bal - total)

                    val reqId = "RCH_${System.currentTimeMillis()}"
                    val req = MobileRechargeRequest(
                        id = reqId,
                        userUid = user.uid,
                        mobileNumber = mobile.trim(),
                        amount = amount,
                        fee = fee
                    )
                    transaction.set(firestore.collection("rechargeRequests").document(reqId), req)
                }
            }.await()
            Pair(true, "Recharge request of $amount FCT submitted for $mobile (Fee: 9 FCT)")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to submit recharge request.")
        }
    }

    fun approveRecharge(requestId: String) {
        firestore.collection("rechargeRequests").document(requestId).update("status", "APPROVED")
    }

    // PLANS & UPGRADES
    suspend fun upgradeUserPlan(newPlan: UserPlan): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        if (user.balance < newPlan.priceFct) {
            return@withContext Pair(false, "Insufficient balance. ${newPlan.displayName} costs ${newPlan.priceFct} FCT")
        }

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(user.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= newPlan.priceFct) {
                    transaction.update(userRef, "balance", bal - newPlan.priceFct)
                    transaction.update(userRef, "plan", newPlan.name)
                    transaction.update(userRef, "planStartDateMillis", System.currentTimeMillis())
                    transaction.update(userRef, "isVerified", (newPlan == UserPlan.STANDARD || newPlan == UserPlan.PREMIUM))
                }
            }.await()
            Pair(true, "Successfully activated ${newPlan.displayName} plan for 45 days!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to upgrade plan.")
        }
    }

    suspend fun upgradePublisherBlaze(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val pub = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        val cost = PublisherPlan.BLAZE.priceFct
        if (pub.balance < cost) {
            return@withContext Pair(false, "Insufficient balance. Blaze costs $cost FCT")
        }

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(pub.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= cost) {
                    transaction.update(userRef, "balance", bal - cost)
                    transaction.update(userRef, "publisherPlan", PublisherPlan.BLAZE.name)
                    transaction.update(userRef, "isVerified", true)
                }
            }.await()
            Pair(true, "Successfully upgraded to Blaze Lifetime plan!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to upgrade publisher plan.")
        }
    }

    suspend fun applyPartnerProgram(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        val fee = 44.0

        val isAgeEligible = user.age >= 15
        val isWithdrawEligible = user.totalWithdrawn >= 500.0
        val isEarningEligible = user.totalEarnings >= 1000.0
        val isStreakEligible = user.activeStreak >= 30

        if (!isAgeEligible) return@withContext Pair(false, "Age requirement not met (Minimum 15 years old).")
        if (!isWithdrawEligible) return@withContext Pair(false, "Withdrawal history requirement not met (Min 500 FCT).")
        if (!isEarningEligible) return@withContext Pair(false, "Earning requirement not met (Min 1,000 FCT total earnings).")
        if (!isStreakEligible) return@withContext Pair(false, "Active streak requirement not met (Min 30-day continuous streak).")

        if (user.balance < fee) {
            return@withContext Pair(false, "Insufficient balance for 44 FCT continuation fee.")
        }

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(user.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= fee) {
                    transaction.update(userRef, "balance", bal - fee)
                    transaction.update(userRef, "isVerified", true)
                }
            }.await()
            Pair(true, "Partner application verified & accepted! Opening Partner Portal.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to process partner application.")
        }
    }

    // JOB CREATION & SUBMISSION
    suspend fun createJob(
        title: String,
        description: String,
        link: String,
        category: String,
        thumbnailUrl: String,
        slots: Int,
        rewardPerSlot: Double
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val creator = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        if (slots < 25) return@withContext Pair(false, "Minimum 25 slots required")
        if (rewardPerSlot < 0.50) return@withContext Pair(false, "Minimum 0.50 FCT reward per slot required")

        val postingFee = 25.0
        val totalCost = (slots * rewardPerSlot) + postingFee

        if (creator.balance < totalCost) {
            return@withContext Pair(false, "Insufficient balance. Required $totalCost FCT ($slots x $rewardPerSlot + 25 FCT fee)")
        }

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(creator.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= totalCost) {
                    transaction.update(userRef, "balance", bal - totalCost)

                    val jobId = "JOB_${System.currentTimeMillis()}"
                    val newJob = JobItem(
                        id = jobId,
                        publisherUid = creator.uid,
                        publisherName = if (creator.organizationName.isNotBlank()) creator.organizationName else creator.name,
                        title = title,
                        description = description,
                        jobLink = link,
                        thumbnailUrl = thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=300" },
                        category = category,
                        slots = slots,
                        remainingSlots = slots,
                        rewardPerSlot = rewardPerSlot
                    )
                    transaction.set(firestore.collection("jobs").document(jobId), newJob)
                }
            }.await()
            Pair(true, "Job created successfully! $totalCost FCT deducted.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to create job.")
        }
    }

    suspend fun submitJobProof(jobId: String, note: String, proofCount: Int, proofUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Pair(false, "Not logged in")

        try {
            val jobDoc = firestore.collection("jobs").document(jobId).get().await()
            if (!jobDoc.exists()) return@withContext Pair(false, "Job not found")
            val job = jobDoc.toJobItem()

            if (job.remainingSlots <= 0) {
                return@withContext Pair(false, "Sorry, all slots for this job have been filled.")
            }

            firestore.runTransaction { transaction ->
                val jobRef = firestore.collection("jobs").document(jobId)
                val jobSnap = transaction.get(jobRef)
                val remaining = jobSnap.getLong("remainingSlots") ?: 0

                if (remaining > 0) {
                    transaction.update(jobRef, "remainingSlots", remaining - 1)

                    val subId = "SUB_${System.currentTimeMillis()}"
                    val submission = JobSubmission(
                        id = subId,
                        jobId = job.id,
                        jobTitle = job.title,
                        publisherUid = job.publisherUid,
                        userUid = user.uid,
                        userName = user.name,
                        rewardAmount = job.rewardPerSlot,
                        proofNote = note,
                        proofImagesCount = proofCount,
                        proofUrl = proofUrl
                    )
                    transaction.set(firestore.collection("jobSubmissions").document(subId), submission)
                }
            }.await()
            Pair(true, "Job proof submitted! Publisher will review and release your reward.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to submit proof.")
        }
    }

    fun approveJobSubmission(submissionId: String) {
        repositoryScope.launch {
            try {
                val subDoc = firestore.collection("jobSubmissions").document(submissionId).get().await()
                if (!subDoc.exists()) return@launch
                val sub = subDoc.toJobSubmission()

                firestore.runTransaction { transaction ->
                    val userRef = firestore.collection("users").document(sub.userUid)
                    val userSnap = transaction.get(userRef)

                    if (userSnap.exists()) {
                        val currentBal = userSnap.getDouble("balance") ?: 0.0
                        val currentEarn = userSnap.getDouble("totalEarnings") ?: 0.0

                        transaction.update(userRef, "balance", currentBal + sub.rewardAmount)
                        transaction.update(userRef, "totalEarnings", currentEarn + sub.rewardAmount)

                        transaction.update(firestore.collection("jobSubmissions").document(submissionId), "status", "APPROVED")

                        val txId = "TX_${System.currentTimeMillis()}"
                        val tx = TransactionItem(
                            id = txId,
                            userUid = sub.userUid,
                            type = "JOB_REWARD",
                            amount = sub.rewardAmount,
                            details = "Reward approved for job: ${sub.jobTitle}"
                        )
                        transaction.set(firestore.collection("transactions").document(txId), tx)
                    }
                }.await()
            } catch (e: Exception) {
                // log
            }
        }
    }

    fun rejectJobSubmission(submissionId: String) {
        repositoryScope.launch {
            try {
                val subDoc = firestore.collection("jobSubmissions").document(submissionId).get().await()
                if (!subDoc.exists()) return@launch
                val sub = subDoc.toJobSubmission()

                firestore.runTransaction { transaction ->
                    val half = sub.rewardAmount / 2.0
                    val pubRef = firestore.collection("users").document(sub.publisherUid)
                    val pubSnap = transaction.get(pubRef)

                    if (pubSnap.exists()) {
                        val pubBal = pubSnap.getDouble("balance") ?: 0.0
                        transaction.update(pubRef, "balance", pubBal + half)
                    }

                    transaction.update(firestore.collection("jobSubmissions").document(submissionId), "status", "REJECTED")
                }.await()
            } catch (e: Exception) {
                // log
            }
        }
    }

    // DROP SHIPPING & RESELLING
    suspend fun placeResellOrder(
        productId: String,
        receiverName: String,
        receiverMobile: String,
        receiverAddress: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Pair(false, "Not logged in")

        try {
            val prodDoc = firestore.collection("products").document(productId).get().await()
            if (!prodDoc.exists()) return@withContext Pair(false, "Product not found")
            val prod = prodDoc.toProductItem()

            val deliveryCharge = 200.0
            val profit = prod.resellPrice - prod.adminPrice

            val orderId = "ORD_${System.currentTimeMillis()}"
            val order = ProductOrder(
                id = orderId,
                productId = prod.id,
                productTitle = prod.title,
                publisherUid = prod.publisherUid,
                buyerUid = user.uid,
                receiverName = receiverName,
                receiverMobile = receiverMobile,
                receiverAddress = receiverAddress,
                adminPrice = prod.adminPrice,
                resellPrice = prod.resellPrice,
                profit = profit,
                deliveryCharge = deliveryCharge
            )

            firestore.collection("productOrders").document(orderId).set(order).await()
            Pair(true, "Resell order placed! You will earn $profit FCT profit upon delivery.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to place order.")
        }
    }

    fun toggleSaveProduct(productId: String) {
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(isSaved = !it.isSaved) else it
        }
    }

    suspend fun postProduct(
        title: String,
        description: String,
        category: String,
        adminPrice: Double,
        resellPrice: Double,
        stock: Int,
        thumbnails: List<String>
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val pub = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        val fee = 15.0
        if (pub.balance < fee) return@withContext Pair(false, "Insufficient balance for 15 FCT product posting fee")

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(pub.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= fee) {
                    transaction.update(userRef, "balance", bal - fee)

                    val prodId = "PROD_${System.currentTimeMillis()}"
                    val newProd = ProductItem(
                        id = prodId,
                        publisherUid = pub.uid,
                        publisherOrgName = if (pub.organizationName.isNotBlank()) pub.organizationName else pub.name,
                        publisherOrgLogo = pub.organizationLogoUrl.ifEmpty { pub.profilePhotoUrl },
                        publisherMobile = pub.mobile,
                        title = title,
                        description = description,
                        category = category,
                        adminPrice = adminPrice,
                        resellPrice = resellPrice,
                        stock = stock,
                        thumbnails = thumbnails.ifEmpty { listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500") }
                    )
                    transaction.set(firestore.collection("products").document(prodId), newProd)
                }
            }.await()
            Pair(true, "Product posted successfully to Resell Marketplace!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to post product.")
        }
    }

    // SERVICES & MESSAGES
    suspend fun postService(
        title: String,
        description: String,
        price: Double
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val pub = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        val fee = 20.0
        if (pub.balance < fee) return@withContext Pair(false, "Insufficient balance for 20 FCT service posting fee")

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(pub.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= fee) {
                    transaction.update(userRef, "balance", bal - fee)

                    val srvId = "SRV_${System.currentTimeMillis()}"
                    val newService = ServiceItem(
                        id = srvId,
                        publisherUid = pub.uid,
                        publisherName = if (pub.organizationName.isNotBlank()) pub.organizationName else pub.name,
                        title = title,
                        description = description,
                        price = price
                    )
                    transaction.set(firestore.collection("services").document(srvId), newService)
                }
            }.await()
            Pair(true, "Service posted successfully to Services Hub!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to post service.")
        }
    }

    fun updateProductOrderStatus(orderId: String, newStatus: String) {
        firestore.collection("productOrders").document(orderId).update("status", newStatus)
    }

    fun updateServiceOrderStatus(orderId: String, newStatus: String) {
        firestore.collection("serviceOrders").document(orderId).update("status", newStatus)
    }

    fun updatePublisherProfile(
        name: String,
        orgName: String,
        orgLogoUrl: String,
        mobile: String,
        country: String
    ): Pair<Boolean, String> {
        val current = _currentUser.value ?: return Pair(false, "Not logged in")
        firestore.collection("users").document(current.uid).update(
            mapOf(
                "name" to name.trim().ifEmpty { current.name },
                "organizationName" to orgName.trim().ifEmpty { current.organizationName },
                "organizationLogoUrl" to orgLogoUrl.trim().ifEmpty { current.organizationLogoUrl },
                "mobile" to mobile.trim().ifEmpty { current.mobile },
                "country" to country.trim().ifEmpty { current.country }
            )
        )
        return Pair(true, "Profile & Organization updated successfully!")
    }

    suspend fun changeUserPin(oldPin: String, newPin: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        if (current.pinHash != oldPin.trim()) {
            return@withContext Pair(false, "Current PIN is incorrect.")
        }
        if (newPin.trim().length != 8 || !newPin.trim().all { it.isDigit() }) {
            return@withContext Pair(false, "New PIN must be exactly 8 digits.")
        }

        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                firebaseUser.updatePassword(newPin.trim()).await()
            }
            firestore.collection("users").document(current.uid).update("pinHash", newPin.trim()).await()
            Pair(true, "8-Digit PIN changed successfully!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to update PIN.")
        }
    }

    fun updateSecurityQuestions(q1: String, q2: String, q3: String): Pair<Boolean, String> {
        val current = _currentUser.value ?: return Pair(false, "Not logged in")
        firestore.collection("users").document(current.uid).update(
            mapOf(
                "secAnswer1" to q1.trim().ifEmpty { current.secAnswer1 },
                "secAnswer2" to q2.trim().ifEmpty { current.secAnswer2 },
                "secAnswer3" to q3.trim().ifEmpty { current.secAnswer3 }
            )
        )
        return Pair(true, "Security questions updated successfully!")
    }

    suspend fun createAd(imageUrl: String, destUrl: String, views: Int, durationDays: Int, cost: Double): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val pub = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        if (pub.balance < cost) return@withContext Pair(false, "Insufficient balance ($cost FCT required)")

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(pub.uid)
                val userSnap = transaction.get(userRef)
                val bal = userSnap.getDouble("balance") ?: 0.0

                if (bal >= cost) {
                    transaction.update(userRef, "balance", bal - cost)

                    val adId = "AD_${System.currentTimeMillis()}"
                    val newAd = AdItem(
                        id = adId,
                        publisherUid = pub.uid,
                        publisherName = if (pub.organizationName.isNotBlank()) pub.organizationName else pub.name,
                        imageUrl = imageUrl,
                        destinationUrl = destUrl,
                        targetViews = views,
                        durationDays = durationDays,
                        costFct = cost
                    )
                    transaction.set(firestore.collection("ads").document(adId), newAd)
                }
            }.await()
            Pair(true, "Ad campaign launched successfully!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to launch ad campaign.")
        }
    }

    fun recordAdImpression(adId: String) {
        firestore.collection("ads").document(adId)
            .update("currentViews", com.google.firebase.firestore.FieldValue.increment(1))
    }

    fun recordAdClick(adId: String) {
        firestore.collection("ads").document(adId)
            .update("clicks", com.google.firebase.firestore.FieldValue.increment(1))
    }

    // SUPPORT TICKETS & REPORTS
    fun submitSupportTicket(title: String, details: String, whatsApp: String): String {
        val user = _currentUser.value ?: return ""
        val tktId = "TKT_${System.currentTimeMillis()}"
        val tkt = SupportTicket(
            id = tktId,
            creatorUid = user.uid,
            creatorName = user.name,
            problemTitle = title,
            problemDetails = details,
            whatsAppNumber = whatsApp
        )
        firestore.collection("tickets").document(tktId).set(tkt)
        return tktId
    }

    fun updateTicketStatus(ticketId: String, status: String) {
        firestore.collection("tickets").document(ticketId).update("status", status)
    }

    fun submitReport(title: String, description: String): String {
        val user = _currentUser.value ?: return ""
        val repId = "REP_${System.currentTimeMillis()}"
        val rep = UserReport(
            id = repId,
            reporterUid = user.uid,
            title = title,
            description = description
        )
        firestore.collection("reports").document(repId).set(rep)
        return repId
    }

    fun resolveReport(reportId: String) {
        firestore.collection("reports").document(reportId).update("status", "RESOLVED")
    }

    fun blockUser(blockedUserId: String) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${blockedUserId}"
        firestore.collection("blockedUsers").document(docId).set(
            mapOf(
                "userId" to user.uid,
                "blockedUserId" to blockedUserId,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun unblockUser(blockedUserId: String) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${blockedUserId}"
        firestore.collection("blockedUsers").document(docId).delete()
    }

    fun submitRating(stars: Int, note: String) {
        val id = "RATE_${System.currentTimeMillis()}"
        firestore.collection("ratings").document(id).set(mapOf("stars" to stars, "note" to note))
    }

    // ADMIN OPERATIONS
    fun generateModerator(name: String, mobile: String, pin: String): String {
        val modUid = "MX" + (10000000..99999999).random().toString()
        val defaultEmail = "mod_$modUid@fleearn.com"

        repositoryScope.launch {
            try {
                // Register via Auth
                auth.createUserWithEmailAndPassword(defaultEmail, pin.trim()).await()
                val uid = auth.currentUser?.uid ?: modUid

                val mod = UserProfile(
                    uid = uid,
                    pinHash = pin.trim(),
                    role = UserRole.MODERATOR,
                    name = name,
                    mobile = mobile,
                    balance = 0.0,
                    isVerified = true,
                    email = defaultEmail
                )
                firestore.collection("users").document(uid).set(mod).await()
                addAuditLog("GENERATE_MODERATOR", uid, "Created moderator account $name")
            } catch (e: Exception) {
                // log
            }
        }
        return modUid
    }

    suspend fun sendModeratorSalary(modUid: String, amount: Double): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cfg = _appConfig.value
        if (cfg.platformProfit < amount) {
            return@withContext Pair(false, "Insufficient platform profit balance ($amount FCT)")
        }

        try {
            val modSnap = firestore.collection("users").document(modUid).get().await()
            if (!modSnap.exists()) return@withContext Pair(false, "Moderator profile not found.")
            val modName = modSnap.getString("name") ?: "Moderator"

            firestore.runTransaction { transaction ->
                val modRef = firestore.collection("users").document(modUid)
                val configRef = firestore.collection("appConfig").document("config")

                val currentBal = modSnap.getDouble("balance") ?: 0.0
                val currentEarn = modSnap.getDouble("totalEarnings") ?: 0.0

                transaction.update(modRef, "balance", currentBal + amount)
                transaction.update(modRef, "totalEarnings", currentEarn + amount)
                transaction.update(configRef, "platformProfit", cfg.platformProfit - amount)

                val txId = "SAL_${System.currentTimeMillis()}"
                val tx = TransactionItem(
                    id = txId,
                    userUid = modUid,
                    type = "SALARY",
                    amount = amount,
                    details = "Monthly Moderator Salary from Administration"
                )
                transaction.set(firestore.collection("transactions").document(txId), tx)
            }.await()

            Pair(true, "Sent $amount FCT salary to Moderator $modName successfully!")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to process salary.")
        }
    }

    fun updateAppConfig(config: SystemAppConfig) {
        firestore.collection("appConfig").document("config").set(config)
    }

    fun updateModeratorInstructions(instructions: String) {
        firestore.collection("appConfig").document("moderatorInstructions").set(mapOf("content" to instructions))
    }

    fun createEvent(title: String, description: String, dateString: String, url: String) {
        val evtId = "EVT_${System.currentTimeMillis()}"
        val evt = EventItem(
            id = evtId,
            title = title,
            description = description,
            dateString = dateString,
            destinationUrl = url
        )
        firestore.collection("events").document(evtId).set(evt)
    }

    // SOCIAL FEED & MESSAGES
    fun createPost(content: String, type: String = "POST") {
        val user = _currentUser.value ?: return
        val postId = "POST_${System.currentTimeMillis()}"
        val newPost = UserPost(
            id = postId,
            authorUid = user.uid,
            authorName = user.name,
            authorPhoto = user.profilePhotoUrl,
            type = type,
            content = content,
            simulatedLikes = (10..250).random()
        )
        firestore.collection("posts").document(postId).set(newPost)
    }

    fun sendMessage(
        receiverUid: String,
        text: String,
        isAudio: Boolean = false,
        isImage: Boolean = false,
        isVideo: Boolean = false,
        isFile: Boolean = false,
        mediaUrl: String = "",
        fileName: String = "",
        fileSize: String = "",
        audioDurationSec: Int = 0,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null,
        customMsgId: String? = null
    ) {
        val user = _currentUser.value ?: return
        val msgId = customMsgId ?: "MSG_${System.currentTimeMillis()}"
        
        // Encrypt message content with symmetric E2EE
        val encryptedText = if (text.isNotBlank()) {
            com.example.util.MessageCrypt.encrypt(text, user.uid, receiverUid)
        } else ""

        val msg = ChatMessage(
            id = msgId,
            senderUid = user.uid,
            receiverUid = receiverUid,
            messageText = encryptedText,
            isAudio = isAudio,
            timestamp = System.currentTimeMillis(),
            isImage = isImage,
            isVideo = isVideo,
            isFile = isFile,
            mediaUrl = mediaUrl,
            fileName = fileName,
            fileSize = fileSize,
            audioDurationSec = audioDurationSec,
            replyToId = replyToId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            reactionsJson = "{}",
            isEdited = false,
            deletedForEveryone = false,
            deletedForUidsJson = "[]",
            isEncrypted = true,
            deliveryStatus = "SENDING"
        )
        
        // Ensure pendingMessages has it
        pendingMessages.removeAll { it.id == msgId }
        pendingMessages.add(msg)
        
        // Push local update instantly
        val currentMsgs = _messages.value.filter { it.id != msgId }
        _messages.value = (currentMsgs + msg).sortedBy { it.timestamp }

        // Save to Firestore
        firestore.collection("chatMessages").document(msgId).set(msg)
            .addOnSuccessListener {
                pendingMessages.removeAll { it.id == msgId }
                val finalMsg = msg.copy(deliveryStatus = "SENT")
                val updatedMsgs = _messages.value.map { if (it.id == msgId) finalMsg else it }
                _messages.value = updatedMsgs
                
                // Increment unread count for partner
                incrementConversationUnread(receiverUid, user.uid)
            }
            .addOnFailureListener { e ->
                val failedMsg = msg.copy(deliveryStatus = "FAILED")
                pendingMessages.removeAll { it.id == msgId }
                pendingMessages.add(failedMsg)
                val updatedMsgs = _messages.value.map { if (it.id == msgId) failedMsg else it }
                _messages.value = updatedMsgs
            }
    }

    private fun incrementConversationUnread(userId: String, partnerId: String) {
        val docId = "${userId}_${partnerId}"
        firestore.collection("userConversations").document(docId).get().addOnSuccessListener { snapshot ->
            val count = snapshot.getLong("unreadCount") ?: 0
            firestore.collection("userConversations").document(docId).set(
                mapOf(
                    "id" to docId,
                    "userId" to userId,
                    "partnerId" to partnerId,
                    "unreadCount" to (count + 1)
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    fun markConversationAsRead(partnerId: String) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${partnerId}"
        firestore.collection("userConversations").document(docId).set(
            mapOf("unreadCount" to 0),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun updateConversationPin(partnerId: String, isPinned: Boolean) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${partnerId}"
        firestore.collection("userConversations").document(docId).set(
            mapOf(
                "id" to docId,
                "userId" to user.uid,
                "partnerId" to partnerId,
                "isPinned" to isPinned
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun updateConversationMute(partnerId: String, isMuted: Boolean) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${partnerId}"
        firestore.collection("userConversations").document(docId).set(
            mapOf(
                "id" to docId,
                "userId" to user.uid,
                "partnerId" to partnerId,
                "isMuted" to isMuted
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun updateConversationArchive(partnerId: String, isArchived: Boolean) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${partnerId}"
        firestore.collection("userConversations").document(docId).set(
            mapOf(
                "id" to docId,
                "userId" to user.uid,
                "partnerId" to partnerId,
                "isArchived" to isArchived
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun deleteConversation(partnerId: String) {
        val user = _currentUser.value ?: return
        val docId = "${user.uid}_${partnerId}"
        // Delete metadata
        firestore.collection("userConversations").document(docId).delete()
        // Also delete matching chat messages locally for me (we can add user to deletedForUids on all matching messages)
        firestore.collection("chatMessages")
            .whereEqualTo("senderUid", user.uid)
            .whereEqualTo("receiverUid", partnerId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    deleteMessageForMe(doc.id)
                }
            }
        firestore.collection("chatMessages")
            .whereEqualTo("senderUid", partnerId)
            .whereEqualTo("receiverUid", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    deleteMessageForMe(doc.id)
                }
            }
    }

    fun deleteMessageForMe(messageId: String) {
        val user = _currentUser.value ?: return
        firestore.collection("chatMessages").document(messageId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val currentDeletedJson = snapshot.getString("deletedForUidsJson") ?: "[]"
                try {
                    val array = org.json.JSONArray(currentDeletedJson)
                    var found = false
                    for (i in 0 until array.length()) {
                        if (array.getString(i) == user.uid) found = true
                    }
                    if (!found) {
                        array.put(user.uid)
                        firestore.collection("chatMessages").document(messageId).update("deletedForUidsJson", array.toString())
                    }
                } catch (e: Exception) {
                    firestore.collection("chatMessages").document(messageId).update("deletedForUidsJson", "[\"${user.uid}\"]")
                }
            }
        }
    }

    fun deleteMessageForEveryone(messageId: String) {
        firestore.collection("chatMessages").document(messageId).update("deletedForEveryone", true)
    }

    fun editMessage(messageId: String, newText: String) {
        val user = _currentUser.value ?: return
        firestore.collection("chatMessages").document(messageId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val receiverUid = snapshot.getString("receiverUid") ?: ""
                val encryptedText = com.example.util.MessageCrypt.encrypt(newText, user.uid, receiverUid)
                firestore.collection("chatMessages").document(messageId).update(
                    mapOf(
                        "messageText" to encryptedText,
                        "isEdited" to true
                    )
                )
            }
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        val user = _currentUser.value ?: return
        firestore.collection("chatMessages").document(messageId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val currentReactionsJson = snapshot.getString("reactionsJson") ?: "{}"
                try {
                    val json = org.json.JSONObject(currentReactionsJson)
                    json.put(user.uid, emoji)
                    firestore.collection("chatMessages").document(messageId).update("reactionsJson", json.toString())
                } catch (e: Exception) {
                    firestore.collection("chatMessages").document(messageId).update("reactionsJson", "{\"${user.uid}\":\"$emoji\"}")
                }
            }
        }
    }

    fun registerCall(callerUid: String, receiverUid: String, callerName: String, receiverName: String, isVideo: Boolean, status: String, durationSec: Int = 0): String {
        val callId = "CALL_${System.currentTimeMillis()}"
        val record = CallRecord(
            id = callId,
            callerUid = callerUid,
            receiverUid = receiverUid,
            callerName = callerName,
            receiverName = receiverName,
            isVideo = isVideo,
            status = status,
            timestamp = System.currentTimeMillis(),
            durationSec = durationSec
        )
        firestore.collection("callHistory").document(callId).set(record)
        return callId
    }

    fun updateCallStatus(callId: String, status: String, durationSec: Int) {
        firestore.collection("callHistory").document(callId).update(
            mapOf(
                "status" to status,
                "durationSec" to durationSec
            )
        )
    }

    suspend fun deleteAccount(uid: String, pin: String, answer1: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Pair(false, "Not logged in")
        if (user.pinHash != pin.trim()) return@withContext Pair(false, "Incorrect PIN")
        if (!user.secAnswer1.equals(answer1.trim(), ignoreCase = true)) return@withContext Pair(false, "Security answer does not match")

        try {
            firestore.collection("users").document(uid).delete().await()
            auth.currentUser?.delete()?.await()
            logout()
            Pair(true, "Account permanently deleted.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to delete account.")
        }
    }

    // ADMIN EXTENDED ACTIONS
    fun suspendUser(uid: String) {
        firestore.collection("users").document(uid).update("isSuspended", true)
        addAuditLog("SUSPEND_USER", uid, "Account status set to suspended")
    }

    fun reactivateUser(uid: String) {
        firestore.collection("users").document(uid).update("isSuspended", false)
        addAuditLog("REACTIVATE_USER", uid, "Account status restored to active")
    }

    suspend fun adminDeleteUser(uid: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(uid).delete().await()
            addAuditLog("DELETE_USER", uid, "User account permanently removed by Administrator")
            Pair(true, "Account $uid deleted successfully.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to delete user.")
        }
    }

    suspend fun adjustUserBalance(uid: String, deltaAmount: Double, reason: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val userRef = firestore.collection("users").document(uid)
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) return@withContext Pair(false, "User not found")

            firestore.runTransaction { transaction ->
                val currentBal = userSnap.getDouble("balance") ?: 0.0
                val newBal = (currentBal + deltaAmount).coerceAtLeast(0.0)
                transaction.update(userRef, "balance", newBal)

                val txId = "ADJ_${System.currentTimeMillis()}"
                val tx = TransactionItem(
                    id = txId,
                    userUid = uid,
                    type = if (deltaAmount >= 0) "CREDIT_ADJUSTMENT" else "DEBIT_ADJUSTMENT",
                    amount = Math.abs(deltaAmount),
                    details = "Admin Balance Adjustment: $reason"
                )
                transaction.set(firestore.collection("transactions").document(txId), tx)
            }.await()

            addAuditLog("BALANCE_ADJUSTMENT", uid, "Adjusted by $deltaAmount FCT ($reason)")
            Pair(true, "Balance adjusted successfully.")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to adjust balance.")
        }
    }

    fun toggleUserVerification(uid: String) {
        repositoryScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val current = doc.getBoolean("isVerified") ?: false
                    firestore.collection("users").document(uid).update("isVerified", !current).await()
                    addAuditLog("TOGGLE_VERIFICATION", uid, "Verification status toggled")
                }
            } catch (e: Exception) {
                // log
            }
        }
    }

    fun setUserPlan(uid: String, plan: UserPlan) {
        firestore.collection("users").document(uid).update("plan", plan.name)
        addAuditLog("CHANGE_USER_PLAN", uid, "User plan set to ${plan.displayName}")
    }

    fun setPublisherPlan(uid: String, plan: PublisherPlan) {
        firestore.collection("users").document(uid).update("publisherPlan", plan.name)
        addAuditLog("CHANGE_PUB_PLAN", uid, "Publisher plan set to ${plan.displayName}")
    }

    fun approveJob(jobId: String) {
        firestore.collection("jobs").document(jobId).update("isApproved", true)
        addAuditLog("APPROVE_JOB", jobId, "Job approved for public listing")
    }

    fun rejectJob(jobId: String) {
        firestore.collection("jobs").document(jobId).delete()
        addAuditLog("REJECT_JOB", jobId, "Job rejected and removed")
    }

    fun approveProduct(productId: String) {
        firestore.collection("products").document(productId).update("isApproved", true)
        addAuditLog("APPROVE_PRODUCT", productId, "Product approved for marketplace")
    }

    fun rejectProduct(productId: String) {
        firestore.collection("products").document(productId).delete()
        addAuditLog("REJECT_PRODUCT", productId, "Product rejected and removed")
    }

    fun approveService(serviceId: String) {
        firestore.collection("services").document(serviceId).update("isApproved", true)
        addAuditLog("APPROVE_SERVICE", serviceId, "Service listing approved")
    }

    fun rejectService(serviceId: String) {
        firestore.collection("services").document(serviceId).delete()
        addAuditLog("REJECT_SERVICE", serviceId, "Service listing rejected")
    }

    fun approveAd(adId: String) {
        firestore.collection("ads").document(adId).update(mapOf("isApproved" to true, "isActive" to true))
        addAuditLog("APPROVE_AD", adId, "Ad campaign approved and activated")
    }

    fun suspendAd(adId: String) {
        firestore.collection("ads").document(adId).update("isActive", false)
        addAuditLog("SUSPEND_AD", adId, "Ad campaign suspended")
    }

    suspend fun addShippingBanner(imageUrl: String, destUrl: String, title: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val current = _shippingBanners.value
        val config = _appConfig.value
        if (current.size >= config.maxShippingBanners) {
            return@withContext Pair(false, "Maximum limit of ${config.maxShippingBanners} banners reached.")
        }
        val bannerId = "banner_${System.currentTimeMillis()}"
        val banner = ShippingBanner(
            id = bannerId,
            imageUrl = imageUrl,
            destinationUrl = destUrl,
            title = title
        )
        firestore.collection("shippingBanners").document(bannerId).set(banner).await()
        addAuditLog("ADD_BANNER", bannerId, "Added shipping banner: $title")
        Pair(true, "Shipping banner added successfully.")
    }

    fun removeShippingBanner(bannerId: String) {
        firestore.collection("shippingBanners").document(bannerId).delete()
        addAuditLog("REMOVE_BANNER", bannerId, "Removed shipping banner")
    }

    fun sendBroadcastNotification(title: String, message: String, targetAudience: String) {
        val notifId = "notif_${System.currentTimeMillis()}"
        val notif = BroadcastNotification(
            id = notifId,
            title = title,
            message = message,
            targetAudience = targetAudience
        )
        firestore.collection("notifications").document(notifId).set(notif)
        addAuditLog("BROADCAST_NOTIFICATION", targetAudience, "Sent alert: $title")
    }

    fun addAuditLog(action: String, target: String, details: String) {
        val currentAdmin = _currentUser.value?.uid ?: "AX_ROOT"
        val logId = "log_${System.currentTimeMillis()}"
        val log = AdminAuditLog(
            id = logId,
            adminUid = currentAdmin,
            action = action,
            target = target,
            details = details
        )
        firestore.collection("auditLogs").document(logId).set(log)
    }
}

// EXTENSION MAPPING FUNCTIONS FOR EXCELLENT RUNTIME SAFETY
fun DocumentSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null
    val uid = getString("uid") ?: id
    val pinHash = getString("pinHash") ?: ""
    val roleStr = getString("role") ?: "USER"
    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
    val name = getString("name") ?: ""
    val mobile = getString("mobile") ?: ""
    val age = getLong("age")?.toInt() ?: 20
    val gender = getString("gender") ?: "Male"
    val country = getString("country") ?: "Bangladesh"
    val server = getString("server") ?: "South Asia / Bangladesh"
    val teamName = getString("teamName") ?: ""
    val referralCode = getString("referralCode") ?: ""
    val profilePhotoUrl = getString("profilePhotoUrl") ?: ""
    val organizationName = getString("organizationName") ?: ""
    val organizationLogoUrl = getString("organizationLogoUrl") ?: ""
    val secAnswer1 = getString("secAnswer1") ?: ""
    val secAnswer2 = getString("secAnswer2") ?: ""
    val secAnswer3 = getString("secAnswer3") ?: ""
    val balance = getDouble("balance") ?: 0.0
    val totalEarnings = getDouble("totalEarnings") ?: 0.0
    val totalWithdrawn = getDouble("totalWithdrawn") ?: 0.0
    val activeStreak = getLong("activeStreak")?.toInt() ?: 1
    val planStr = getString("plan") ?: "TRIAL"
    val plan = try { UserPlan.valueOf(planStr) } catch (e: Exception) { UserPlan.TRIAL }
    val pubPlanStr = getString("publisherPlan") ?: "SPARK"
    val publisherPlan = try { PublisherPlan.valueOf(pubPlanStr) } catch (e: Exception) { PublisherPlan.SPARK }
    val planStartDateMillis = getLong("planStartDateMillis") ?: System.currentTimeMillis()
    val isVerified = getBoolean("isVerified") ?: false
    val isSuspended = getBoolean("isSuspended") ?: false
    val createdAtMillis = getLong("createdAtMillis") ?: System.currentTimeMillis()
    val email = getString("email") ?: ""

    return UserProfile(
        uid = uid,
        pinHash = pinHash,
        role = role,
        name = name,
        mobile = mobile,
        age = age,
        gender = gender,
        country = country,
        server = server,
        teamName = teamName,
        referralCode = referralCode,
        profilePhotoUrl = profilePhotoUrl,
        organizationName = organizationName,
        organizationLogoUrl = organizationLogoUrl,
        secAnswer1 = secAnswer1,
        secAnswer2 = secAnswer2,
        secAnswer3 = secAnswer3,
        balance = balance,
        totalEarnings = totalEarnings,
        totalWithdrawn = totalWithdrawn,
        activeStreak = activeStreak,
        plan = plan,
        publisherPlan = publisherPlan,
        planStartDateMillis = planStartDateMillis,
        isVerified = isVerified,
        isSuspended = isSuspended,
        createdAtMillis = createdAtMillis,
        email = email
    )
}

fun DocumentSnapshot.toTransactionItem(): TransactionItem {
    return TransactionItem(
        id = getString("id") ?: id,
        userUid = getString("userUid") ?: "",
        type = getString("type") ?: "DEPOSIT",
        amount = getDouble("amount") ?: 0.0,
        fee = getDouble("fee") ?: 0.0,
        details = getString("details") ?: "",
        status = getString("status") ?: "APPROVED",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toDepositRequest(): DepositRequest {
    return DepositRequest(
        id = getString("id") ?: id,
        userUid = getString("userUid") ?: "",
        userName = getString("userName") ?: "",
        amount = getDouble("amount") ?: 0.0,
        method = getString("method") ?: "bKash",
        senderNumber = getString("senderNumber") ?: "",
        transactionId = getString("transactionId") ?: "",
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toWithdrawRequest(): WithdrawRequest {
    val roleStr = getString("userRole") ?: "USER"
    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
    return WithdrawRequest(
        id = getString("id") ?: id,
        userUid = getString("userUid") ?: "",
        userName = getString("userName") ?: "",
        userRole = role,
        amount = getDouble("amount") ?: 0.0,
        fee = getDouble("fee") ?: 0.0,
        netAmount = getDouble("netAmount") ?: 0.0,
        method = getString("method") ?: "bKash",
        walletNumber = getString("walletNumber") ?: "",
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toMobileRechargeRequest(): MobileRechargeRequest {
    return MobileRechargeRequest(
        id = getString("id") ?: id,
        userUid = getString("userUid") ?: "",
        mobileNumber = getString("mobileNumber") ?: "",
        amount = getDouble("amount") ?: 0.0,
        fee = getDouble("fee") ?: 9.0,
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toJobItem(): JobItem {
    return JobItem(
        id = getString("id") ?: id,
        publisherUid = getString("publisherUid") ?: "",
        publisherName = getString("publisherName") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        jobLink = getString("jobLink") ?: "",
        thumbnailUrl = getString("thumbnailUrl") ?: "",
        category = getString("category") ?: "",
        slots = getLong("slots")?.toInt() ?: 100,
        remainingSlots = getLong("remainingSlots")?.toInt() ?: 100,
        rewardPerSlot = getDouble("rewardPerSlot") ?: 1.0,
        requiredScreenshots = getLong("requiredScreenshots")?.toInt() ?: 1,
        requiredNotes = getLong("requiredNotes")?.toInt() ?: 1,
        isApproved = getBoolean("isApproved") ?: true,
        createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toJobSubmission(): JobSubmission {
    return JobSubmission(
        id = getString("id") ?: id,
        jobId = getString("jobId") ?: "",
        jobTitle = getString("jobTitle") ?: "",
        publisherUid = getString("publisherUid") ?: "",
        userUid = getString("userUid") ?: "",
        userName = getString("userName") ?: "",
        rewardAmount = getDouble("rewardAmount") ?: 0.0,
        proofNote = getString("proofNote") ?: "",
        proofImagesCount = getLong("proofImagesCount")?.toInt() ?: 1,
        proofUrl = getString("proofUrl") ?: "",
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toProductItem(): ProductItem {
    @Suppress("UNCHECKED_CAST")
    val thumbnailsList = get("thumbnails") as? List<String> ?: emptyList()
    return ProductItem(
        id = getString("id") ?: id,
        publisherUid = getString("publisherUid") ?: "",
        publisherOrgName = getString("publisherOrgName") ?: "",
        publisherOrgLogo = getString("publisherOrgLogo") ?: "",
        publisherMobile = getString("publisherMobile") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        category = getString("category") ?: "",
        adminPrice = getDouble("adminPrice") ?: 0.0,
        resellPrice = getDouble("resellPrice") ?: 0.0,
        stock = getLong("stock")?.toInt() ?: 50,
        thumbnails = thumbnailsList,
        isSaved = getBoolean("isSaved") ?: false,
        isApproved = getBoolean("isApproved") ?: true
    )
}

fun DocumentSnapshot.toProductOrder(): ProductOrder {
    return ProductOrder(
        id = getString("id") ?: id,
        productId = getString("productId") ?: "",
        productTitle = getString("productTitle") ?: "",
        publisherUid = getString("publisherUid") ?: "",
        buyerUid = getString("buyerUid") ?: "",
        receiverName = getString("receiverName") ?: "",
        receiverMobile = getString("receiverMobile") ?: "",
        receiverAddress = getString("receiverAddress") ?: "",
        adminPrice = getDouble("adminPrice") ?: 0.0,
        resellPrice = getDouble("resellPrice") ?: 0.0,
        profit = getDouble("profit") ?: 0.0,
        deliveryCharge = getDouble("deliveryCharge") ?: 200.0,
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toServiceItem(): ServiceItem {
    return ServiceItem(
        id = getString("id") ?: id,
        publisherUid = getString("publisherUid") ?: "",
        publisherName = getString("publisherName") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        price = getDouble("price") ?: 0.0,
        isApproved = getBoolean("isApproved") ?: true
    )
}

fun DocumentSnapshot.toServiceOrder(): ServiceOrder {
    return ServiceOrder(
        id = getString("id") ?: id,
        serviceId = getString("serviceId") ?: "",
        serviceTitle = getString("serviceTitle") ?: "",
        publisherUid = getString("publisherUid") ?: "",
        buyerUid = getString("buyerUid") ?: "",
        buyerWhatsApp = getString("buyerWhatsApp") ?: "",
        feePaid = getDouble("feePaid") ?: 50.0,
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toAdItem(): AdItem {
    return AdItem(
        id = getString("id") ?: id,
        publisherUid = getString("publisherUid") ?: "",
        publisherName = getString("publisherName") ?: "",
        imageUrl = getString("imageUrl") ?: "",
        destinationUrl = getString("destinationUrl") ?: "",
        targetViews = getLong("targetViews")?.toInt() ?: 1000,
        currentViews = getLong("currentViews")?.toInt() ?: 0,
        clicks = getLong("clicks")?.toInt() ?: 0,
        durationDays = getLong("durationDays")?.toInt() ?: 7,
        costFct = getDouble("costFct") ?: 100.0,
        isApproved = getBoolean("isApproved") ?: true,
        isActive = getBoolean("isActive") ?: true,
        createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toSupportTicket(): SupportTicket {
    return SupportTicket(
        id = getString("id") ?: id,
        creatorUid = getString("creatorUid") ?: "",
        creatorName = getString("creatorName") ?: "",
        problemTitle = getString("problemTitle") ?: "",
        problemDetails = getString("problemDetails") ?: "",
        whatsAppNumber = getString("whatsAppNumber") ?: "",
        assignedModeratorUid = getString("assignedModeratorUid") ?: "",
        status = getString("status") ?: "RUNNING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toUserReport(): UserReport {
    return UserReport(
        id = getString("id") ?: id,
        reporterUid = getString("reporterUid") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        status = getString("status") ?: "PENDING",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toUserPost(): UserPost {
    return UserPost(
        id = getString("id") ?: id,
        authorUid = getString("authorUid") ?: "",
        authorName = getString("authorName") ?: "",
        authorPhoto = getString("authorPhoto") ?: "",
        type = getString("type") ?: "POST",
        content = getString("content") ?: "",
        mediaUrl = getString("mediaUrl") ?: "",
        simulatedLikes = getLong("simulatedLikes")?.toInt() ?: 0,
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toEventItem(): EventItem {
    return EventItem(
        id = getString("id") ?: id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        destinationUrl = getString("destinationUrl") ?: "",
        dateString = getString("dateString") ?: "2026-10-15",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toShippingBanner(): ShippingBanner {
    return ShippingBanner(
        id = getString("id") ?: id,
        imageUrl = getString("imageUrl") ?: "",
        destinationUrl = getString("destinationUrl") ?: "",
        title = getString("title") ?: ""
    )
}

fun DocumentSnapshot.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = getString("id") ?: id,
        senderUid = getString("senderUid") ?: "",
        receiverUid = getString("receiverUid") ?: "",
        messageText = getString("messageText") ?: "",
        isAudio = getBoolean("isAudio") ?: false,
        timestamp = getLong("timestamp") ?: System.currentTimeMillis(),
        isImage = getBoolean("isImage") ?: false,
        isVideo = getBoolean("isVideo") ?: false,
        isFile = getBoolean("isFile") ?: false,
        mediaUrl = getString("mediaUrl") ?: "",
        fileName = getString("fileName") ?: "",
        fileSize = getString("fileSize") ?: "",
        audioDurationSec = getLong("audioDurationSec")?.toInt() ?: 0,
        replyToId = getString("replyToId"),
        replyToText = getString("replyToText"),
        replyToSenderName = getString("replyToSenderName"),
        reactionsJson = getString("reactionsJson") ?: "{}",
        isEdited = getBoolean("isEdited") ?: false,
        deletedForEveryone = getBoolean("deletedForEveryone") ?: false,
        deletedForUidsJson = getString("deletedForUidsJson") ?: "[]",
        isEncrypted = getBoolean("isEncrypted") ?: true,
        deliveryStatus = getString("deliveryStatus") ?: "SENT"
    )
}

fun DocumentSnapshot.toConversationMeta(): ConversationMeta {
    return ConversationMeta(
        id = getString("id") ?: id,
        userId = getString("userId") ?: "",
        partnerId = getString("partnerId") ?: "",
        isPinned = getBoolean("isPinned") ?: false,
        isMuted = getBoolean("isMuted") ?: false,
        isArchived = getBoolean("isArchived") ?: false,
        unreadCount = getLong("unreadCount")?.toInt() ?: 0,
        isTyping = getBoolean("isTyping") ?: false,
        lastReadTimestamp = getLong("lastReadTimestamp") ?: 0L
    )
}

fun DocumentSnapshot.toCallRecord(): CallRecord {
    return CallRecord(
        id = getString("id") ?: id,
        callerUid = getString("callerUid") ?: "",
        receiverUid = getString("receiverUid") ?: "",
        callerName = getString("callerName") ?: "",
        receiverName = getString("receiverName") ?: "",
        isVideo = getBoolean("isVideo") ?: false,
        status = getString("status") ?: "MISSED",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis(),
        durationSec = getLong("durationSec")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toOfficialChannel(): OfficialChannel {
    return OfficialChannel(
        id = getString("id") ?: id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        avatarUrl = getString("avatarUrl") ?: "",
        isPinned = getBoolean("isPinned") ?: true,
        isVerified = getBoolean("isVerified") ?: true,
        memberCount = getLong("memberCount")?.toInt() ?: 15420
    )
}

fun DocumentSnapshot.toSystemAppConfig(): SystemAppConfig {
    return SystemAppConfig(
        appName = getString("appName") ?: "Fleearn",
        slogan = getString("slogan") ?: "Earn your way",
        description = getString("description") ?: "The premier 3D Earning Fleearn connecting users, publishers, moderators, and administration.",
        appLogoUrl = getString("appLogoUrl") ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
        appVersion = getString("appVersion") ?: "5.0.0-Beta",
        bKashNumber = getString("bKashNumber") ?: "+880 1711-234567 (Merchant)",
        nagadNumber = getString("nagadNumber") ?: "+880 1811-987654 (Merchant)",
        upayNumber = getString("upayNumber") ?: "+880 1911-554433 (Agent)",
        aiSupportUrl = getString("aiSupportUrl") ?: "https://ai.studio/support/fleearn",
        moreFromUsUrl = getString("moreFromUsUrl") ?: "https://fleearn.example.com/Fleearn",
        websiteUrl = getString("websiteUrl") ?: "https://fleearn.example.com",
        termsUrl = getString("termsUrl") ?: "https://fleearn.example.com/terms",
        privacyUrl = getString("privacyUrl") ?: "https://fleearn.example.com/privacy",
        communityGuidelinesUrl = getString("communityGuidelinesUrl") ?: "https://fleearn.example.com/guidelines",
        publisherPolicyUrl = getString("publisherPolicyUrl") ?: "https://fleearn.example.com/publisher-policy",
        publisherAssociationUrl = getString("publisherAssociationUrl") ?: "https://fleearn.example.com/association",
        partnerProgramUrl = getString("partnerProgramUrl") ?: "https://fleearn.example.com/partner",
        gamingTopupUrl = getString("gamingTopupUrl") ?: "https://fleearn.example.com/gaming",
        subscriptionUrl = getString("subscriptionUrl") ?: "https://fleearn.example.com/subscriptions",
        courseWebsiteUrl = getString("courseWebsiteUrl") ?: "https://fleearn.example.com/academy",
        courseDailyPin = getString("courseDailyPin") ?: "FLE7894",
        noticePdfUrl = getString("noticePdfUrl") ?: "https://fleearn.example.com/notices/latest.pdf",
        companyInfo = getString("companyInfo") ?: "Fleearn Technologies Inc. - Registered Financial Fleearn & Reselling Platform.",
        platformProfit = getDouble("platformProfit") ?: 84520.0,
        sendMoneyFee = getDouble("sendMoneyFee") ?: 5.0,
        mobileRechargeFee = getDouble("mobileRechargeFee") ?: 9.0,
        partnerProgramFee = getDouble("partnerProgramFee") ?: 500.0,
        userWithdrawalFeePercent = getDouble("userWithdrawalFeePercent") ?: 2.0,
        moderatorWithdrawalFeePercent = getDouble("moderatorWithdrawalFeePercent") ?: 1.0,
        jobPostingFee = getDouble("jobPostingFee") ?: 25.0,
        productPostingFee = getDouble("productPostingFee") ?: 15.0,
        servicePostingFee = getDouble("servicePostingFee") ?: 20.0,
        productDeliveryFee = getDouble("productDeliveryFee") ?: 200.0,
        publisherSparkPlanPrice = getDouble("publisherSparkPlanPrice") ?: 0.0,
        publisherBlazePlanPrice = getDouble("publisherBlazePlanPrice") ?: 99.0,
        minDepositAmount = getDouble("minDepositAmount") ?: 50.0,
        minWithdrawAmount = getDouble("minWithdrawAmount") ?: 100.0,
        maxDailyWithdrawLimit = getDouble("maxDailyWithdrawLimit") ?: 5000.0,
        maintenanceMode = getBoolean("maintenanceMode") ?: false,
        forceUpdateEnabled = getBoolean("forceUpdateEnabled") ?: false,
        twoFactorAuthEnabled = getBoolean("twoFactorAuthEnabled") ?: true,
        adDisplayDurationSeconds = getLong("adDisplayDurationSeconds")?.toInt() ?: 7,
        shippingBannerRotationSeconds = getLong("shippingBannerRotationSeconds")?.toInt() ?: 3,
        maxShippingBanners = getLong("maxShippingBanners")?.toInt() ?: 10,
        defaultLanguage = getString("defaultLanguage") ?: "English",
        isDarkTheme = getBoolean("isDarkTheme") ?: true
    )
}

fun DocumentSnapshot.toAdminAuditLog(): AdminAuditLog {
    return AdminAuditLog(
        id = getString("id") ?: id,
        adminUid = getString("adminUid") ?: "SYSTEM",
        action = getString("action") ?: "LOGIN",
        target = getString("target") ?: "SYSTEM",
        details = getString("details") ?: "",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}

fun DocumentSnapshot.toBroadcastNotification(): BroadcastNotification {
    return BroadcastNotification(
        id = getString("id") ?: id,
        title = getString("title") ?: "",
        message = getString("message") ?: "",
        targetAudience = getString("targetAudience") ?: "ALL",
        timestamp = getLong("timestamp") ?: System.currentTimeMillis()
    )
}
