package com.example.data.model

enum class UserRole {
    USER,
    PUBLISHER,
    MODERATOR,
    ADMIN
}

enum class UserPlan(val displayName: String, val priceFct: Double, val dailyUsageMinutes: Int, val maxDailyWithdraw: Double, val maxMonthlyWithdraw: Double, val durationDays: Int) {
    TRIAL("3-Day Trial", 0.0, 1440, 5000.0, 25000.0, 3),
    LIGHT("Light", 0.0, 30, 0.0, 0.0, 3650),
    BASIC("Basic", 99.0, 359, 500.0, 5000.0, 45),
    STANDARD("Standard", 499.0, 480, 1999.0, 10000.0, 45),
    PREMIUM("Premium", 999.0, 1440, 5000.0, 25000.0, 45)
}

enum class PublisherPlan(val displayName: String, val priceFct: Double) {
    SPARK("Spark", 0.0),
    BLAZE("Blaze (Lifetime)", 99.0)
}

data class UserProfile(
    val uid: String,
    val pinHash: String,
    val role: UserRole = UserRole.USER,
    val name: String,
    val mobile: String,
    val age: Int = 20,
    val gender: String = "Male",
    val country: String = "Bangladesh",
    val server: String = "South Asia / Bangladesh",
    val teamName: String = "",
    val referralCode: String = "",
    val profilePhotoUrl: String = "",
    val organizationName: String = "",
    val organizationLogoUrl: String = "",
    val secAnswer1: String = "",
    val secAnswer2: String = "",
    val secAnswer3: String = "",
    val balance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val activeStreak: Int = 1,
    val plan: UserPlan = UserPlan.TRIAL,
    val publisherPlan: PublisherPlan = PublisherPlan.SPARK,
    val planStartDateMillis: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val isSuspended: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val email: String = ""
)

data class TransactionItem(
    val id: String,
    val userUid: String,
    val type: String, // "DEPOSIT", "WITHDRAW", "SEND_MONEY", "RECEIVE_MONEY", "JOB_REWARD", "COMMISSION", "FEE", "SALARY"
    val amount: Double,
    val fee: Double = 0.0,
    val details: String,
    val status: String = "APPROVED", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

data class DepositRequest(
    val id: String,
    val userUid: String,
    val userName: String,
    val amount: Double,
    val method: String, // "bKash", "Nagad", "Upay"
    val senderNumber: String,
    val transactionId: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

data class WithdrawRequest(
    val id: String,
    val userUid: String,
    val userName: String,
    val userRole: UserRole,
    val amount: Double,
    val fee: Double,
    val netAmount: Double,
    val method: String, // "bKash", "Nagad"
    val walletNumber: String,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

data class MobileRechargeRequest(
    val id: String,
    val userUid: String,
    val mobileNumber: String,
    val amount: Double,
    val fee: Double = 9.0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

data class JobItem(
    val id: String,
    val publisherUid: String,
    val publisherName: String,
    val title: String,
    val description: String,
    val jobLink: String,
    val thumbnailUrl: String,
    val category: String,
    val slots: Int,
    val remainingSlots: Int,
    val rewardPerSlot: Double,
    val requiredScreenshots: Int = 1,
    val requiredNotes: Int = 1,
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class JobSubmission(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val publisherUid: String,
    val userUid: String,
    val userName: String,
    val rewardAmount: Double,
    val proofNote: String,
    val proofImagesCount: Int,
    val proofUrl: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

data class ProductItem(
    val id: String,
    val publisherUid: String,
    val publisherOrgName: String,
    val publisherOrgLogo: String,
    val publisherMobile: String,
    val title: String,
    val description: String,
    val category: String,
    val adminPrice: Double,
    val resellPrice: Double,
    val stock: Int = 50,
    val thumbnails: List<String> = emptyList(),
    val isSaved: Boolean = false,
    val isApproved: Boolean = true
)

data class ProductOrder(
    val id: String,
    val productId: String,
    val productTitle: String,
    val publisherUid: String,
    val buyerUid: String,
    val receiverName: String,
    val receiverMobile: String,
    val receiverAddress: String,
    val adminPrice: Double,
    val resellPrice: Double,
    val profit: Double,
    val deliveryCharge: Double = 200.0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

data class ServiceItem(
    val id: String,
    val publisherUid: String,
    val publisherName: String,
    val title: String,
    val description: String,
    val price: Double,
    val isApproved: Boolean = true
)

data class ServiceOrder(
    val id: String,
    val serviceId: String,
    val serviceTitle: String,
    val publisherUid: String,
    val buyerUid: String,
    val buyerWhatsApp: String,
    val feePaid: Double = 50.0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

data class AdItem(
    val id: String,
    val publisherUid: String,
    val publisherName: String,
    val imageUrl: String,
    val destinationUrl: String,
    val targetViews: Int,
    val currentViews: Int = 0,
    val clicks: Int = 0,
    val durationDays: Int = 7,
    val costFct: Double = 100.0,
    val isApproved: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String,
    val creatorUid: String,
    val creatorName: String,
    val problemTitle: String,
    val problemDetails: String,
    val whatsAppNumber: String,
    val assignedModeratorUid: String = "",
    val status: String = "RUNNING", // "RUNNING", "CLOSED"
    val timestamp: Long = System.currentTimeMillis()
)

data class UserReport(
    val id: String,
    val reporterUid: String,
    val title: String,
    val description: String,
    val status: String = "PENDING", // "PENDING", "RESOLVED"
    val timestamp: Long = System.currentTimeMillis()
)

data class UserPost(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val authorPhoto: String,
    val type: String = "POST", // "POST", "LONG_VIDEO", "SHORTS", "LIVE"
    val content: String,
    val mediaUrl: String = "",
    val simulatedLikes: Int = 124,
    val timestamp: Long = System.currentTimeMillis()
)

data class EventItem(
    val id: String,
    val title: String,
    val description: String,
    val destinationUrl: String,
    val dateString: String = "2026-10-15",
    val timestamp: Long = System.currentTimeMillis()
)

data class ShippingBanner(
    val id: String,
    val imageUrl: String,
    val destinationUrl: String,
    val title: String
)

data class ChatMessage(
    val id: String,
    val senderUid: String,
    val receiverUid: String, // or channelId
    val messageText: String,
    val isAudio: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isImage: Boolean = false,
    val isVideo: Boolean = false,
    val isFile: Boolean = false,
    val mediaUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val audioDurationSec: Int = 0,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val reactionsJson: String = "{}",
    val isEdited: Boolean = false,
    val deletedForEveryone: Boolean = false,
    val deletedForUidsJson: String = "[]",
    val isEncrypted: Boolean = true,
    val deliveryStatus: String = "SENT" // SENDING, SENT, FAILED
)

data class ConversationMeta(
    val id: String = "",
    val userId: String = "",
    val partnerId: String = "",
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val lastReadTimestamp: Long = 0
)

data class CallRecord(
    val id: String = "",
    val callerUid: String = "",
    val receiverUid: String = "",
    val callerName: String = "",
    val receiverName: String = "",
    val isVideo: Boolean = false,
    val status: String = "MISSED", // MISSED, COMPLETED, REJECTED, ONGOING
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int = 0
)

data class OfficialChannel(
    val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String = "",
    val isPinned: Boolean = true,
    val isVerified: Boolean = true,
    val memberCount: Int = 15420
)

data class SystemAppConfig(
    val appName: String = "Fleearn",
    val slogan: String = "Earn your way",
    val description: String = "The premier 3D Earning Fleearn connecting users, publishers, moderators, and administration.",
    val appLogoUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
    val appVersion: String = "5.0.0-Beta",
    val bKashNumber: String = "+880 1711-234567 (Merchant)",
    val nagadNumber: String = "+880 1811-987654 (Merchant)",
    val upayNumber: String = "+880 1911-554433 (Agent)",
    val aiSupportUrl: String = "https://ai.studio/support/fleearn",
    val moreFromUsUrl: String = "https://fleearn.example.com/Fleearn",
    val websiteUrl: String = "https://fleearn.example.com",
    val termsUrl: String = "https://fleearn.example.com/terms",
    val privacyUrl: String = "https://fleearn.example.com/privacy",
    val communityGuidelinesUrl: String = "https://fleearn.example.com/guidelines",
    val publisherPolicyUrl: String = "https://fleearn.example.com/publisher-policy",
    val publisherAssociationUrl: String = "https://fleearn.example.com/association",
    val partnerProgramUrl: String = "https://fleearn.example.com/partner",
    val gamingTopupUrl: String = "https://fleearn.example.com/gaming",
    val subscriptionUrl: String = "https://fleearn.example.com/subscriptions",
    val courseWebsiteUrl: String = "https://fleearn.example.com/academy",
    val courseDailyPin: String = "FLE7894",
    val noticePdfUrl: String = "https://fleearn.example.com/notices/latest.pdf",
    val companyInfo: String = "Fleearn Technologies Inc. - Registered Financial Fleearn & Reselling Platform.",
    val platformProfit: Double = 84520.0,
    val sendMoneyFee: Double = 5.0,
    val mobileRechargeFee: Double = 9.0,
    val partnerProgramFee: Double = 500.0,
    val userWithdrawalFeePercent: Double = 2.0,
    val moderatorWithdrawalFeePercent: Double = 1.0,
    val jobPostingFee: Double = 25.0,
    val productPostingFee: Double = 15.0,
    val servicePostingFee: Double = 20.0,
    val productDeliveryFee: Double = 200.0,
    val publisherSparkPlanPrice: Double = 0.0,
    val publisherBlazePlanPrice: Double = 99.0,
    val minDepositAmount: Double = 50.0,
    val minWithdrawAmount: Double = 100.0,
    val maxDailyWithdrawLimit: Double = 5000.0,
    val maintenanceMode: Boolean = false,
    val forceUpdateEnabled: Boolean = false,
    val twoFactorAuthEnabled: Boolean = true,
    val adDisplayDurationSeconds: Int = 7,
    val shippingBannerRotationSeconds: Int = 3,
    val maxShippingBanners: Int = 10,
    val defaultLanguage: String = "English",
    val isDarkTheme: Boolean = true
)

data class AdminAuditLog(
    val id: String,
    val adminUid: String,
    val action: String,
    val target: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BroadcastNotification(
    val id: String,
    val title: String,
    val message: String,
    val targetAudience: String, // "ALL", "USERS", "PUBLISHERS", "MODERATORS"
    val timestamp: Long = System.currentTimeMillis()
)

data class ModeratorSalaryRecord(
    val id: String,
    val moderatorUid: String,
    val moderatorName: String,
    val amount: Double,
    val status: String = "SENT",
    val timestamp: Long = System.currentTimeMillis()
)
