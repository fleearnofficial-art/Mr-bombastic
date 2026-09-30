package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    EN("en", "English", "English"),
    BN("bn", "Bengali", "বাংলা"),
    HI("hi", "Hindi", "हिन्दी"),
    AR("ar", "Arabic", "العربية"),
    ES("es", "Spanish", "Español"),
    FR("fr", "French", "Français"),
    DE("de", "German", "Deutsch"),
    JA("ja", "Japanese", "日本語"),
    ZH("zh", "Chinese", "中文"),
    PT("pt", "Portuguese", "Português"),
    RU("ru", "Russian", "Русский"),
    TR("tr", "Turkish", "Türkçe"),
    ID("id", "Indonesian", "Bahasa Indonesia"),
    UR("ur", "Urdu", "اردو"),
    KO("ko", "Korean", "한국어")
}

class LanguageManager {
    var currentLanguage by mutableStateOf(AppLanguage.EN)

    fun getString(key: String): String {
        return translations[currentLanguage]?.get(key)
            ?: translations[AppLanguage.EN]?.get(key)
            ?: key
    }

    companion object {
        val LocalLanguage = compositionLocalOf { LanguageManager() }

        private val enMap = mapOf(
            "app_name" to "Fleearn",
            "slogan" to "Earn your way",
            "version" to "Version 5.0 (Beta)",
            "login" to "Login",
            "signup" to "Sign Up",
            "logout" to "Log Out",
            "balance" to "FCT Balance",
            "total_earnings" to "Total Earnings",
            "total_withdrawn" to "Total Withdrawn",
            "active_streak" to "Active Streak",
            "days" to "Days",
            "deposit" to "Deposit",
            "withdraw" to "Withdraw",
            "send_money" to "Send Money",
            "recharge" to "Mobile Recharge",
            "feed" to "Feed",
            "notice" to "Notice",
            "leaderboard" to "Leaderboard",
            "services" to "Services",
            "courses" to "Courses",
            "post_job" to "Post Job",
            "submissions" to "Submissions",
            "referral" to "Referral",
            "website" to "Website",
            "settings" to "Settings",
            "jobs" to "Jobs",
            "home" to "Home",
            "shipping" to "Shipping",
            "connections" to "Connections",
            "download_center" to "Download Center",
            "dark_mode" to "Dark Theme",
            "light_mode" to "Light Theme",
            "language" to "Language",
            "profile" to "Profile",
            "edit_profile" to "Edit Profile",
            "events" to "Events (2026-2050)",
            "support" to "Support & Ticket",
            "more_from_us" to "More From Us",
            "partner_program" to "Partner Program",
            "plans" to "Plan Upgrade / Renew",
            "rate_us" to "Rate Us",
            "report" to "Report an Issue",
            "delete_account" to "Delete Account",
            "submit" to "Submit",
            "cancel" to "Cancel",
            "confirm" to "Confirm",
            "apply" to "Apply",
            "fct" to "FCT",
            "uid" to "UID",
            "pin" to "8-Digit PIN",
            "role" to "Panel Role",
            "user" to "User (Fleearn)",
            "publisher" to "Publisher (FleConsole)",
            "admin" to "Admin (Fleministration)",
            "moderator" to "Moderator (FleUra)",
            "congratulations" to "Congratulations!",
            "trial_notice" to "3-Day Premium Trial active! Enjoy all features.",
            "success" to "Success",
            "error" to "Error"
        )

        private val bnMap = mapOf(
            "app_name" to "Fleearn",
            "slogan" to "Earn your way",
            "version" to "ভার্সন ৫.০ (বেটা)",
            "login" to "লগইন",
            "signup" to "রেজিস্ট্রেশন",
            "logout" to "লগ আউট",
            "balance" to "FCT ব্যালেন্স",
            "total_earnings" to "মোট আয়",
            "total_withdrawn" to "মোট উত্তোলন",
            "active_streak" to "সক্রিয় স্ট্রিক",
            "days" to "দিন",
            "deposit" to "ডিপোজিট",
            "withdraw" to "উত্তোলন",
            "send_money" to "টাকা পাঠান",
            "recharge" to "মোবাইল রিচার্জ",
            "feed" to "ফিড",
            "notice" to "নোটিশ",
            "leaderboard" to "লিডারবোর্ড",
            "services" to "সার্ভিস সমূহ",
            "courses" to "কোর্স সমূহ",
            "post_job" to "কাজ দিন",
            "submissions" to "জমা কাজ",
            "referral" to "রেফারাল",
            "website" to "ওয়েবসাইট",
            "settings" to "সেটিংস",
            "jobs" to "মাইক্রো জব",
            "home" to "হোম",
            "shipping" to "রিসেল শপ",
            "connections" to "কানেকশন",
            "download_center" to "ডাউনলোড সেন্টার",
            "dark_mode" to "ডার্ক মোড",
            "light_mode" to "লাইট মোড",
            "language" to "ভাষা",
            "profile" to "প্রোফাইল",
            "edit_profile" to "প্রোফাইল এডিট",
            "events" to "ইভেন্টস (২০২৬-২০৫০)",
            "support" to "সাপোর্ট ও টিকিট",
            "more_from_us" to "আমাদের অন্যান্য",
            "partner_program" to "পার্টনার প্রোগ্রাম",
            "plans" to "প্ল্যান আপগ্রেড / রিনিউ",
            "rate_us" to "রেটিং দিন",
            "report" to "রিপোর্ট করুন",
            "delete_account" to "অ্যাকাউন্ট মুছুন",
            "submit" to "জমা দিন",
            "cancel" to "বাতিল",
            "confirm" to "নিশ্চিত করুন",
            "apply" to "আবেদন করুন",
            "fct" to "FCT",
            "uid" to "ইউজার আইডি",
            "pin" to "৮ সংখ্যার পিন",
            "role" to "প্যানেল সিলেক্ট",
            "user" to "ইউজার (Fleearn)",
            "publisher" to "পাবলিশার (FleConsole)",
            "admin" to "অ্যাডমিন (Fleministration)",
            "moderator" to "মডারেটর (FleUra)",
            "congratulations" to "অভিনন্দন!",
            "trial_notice" to "৩ দিনের প্রিমিয়াম ট্রায়াল সক্রিয় রয়েছে!",
            "success" to "সফল হয়েছে",
            "error" to "ত্রুটি"
        )

        private val hiMap = mapOf(
            "app_name" to "Fleearn",
            "slogan" to "Earn your way",
            "login" to "लॉग इन करें",
            "signup" to "साइन अप करें",
            "balance" to "FCT शेष राशि",
            "deposit" to "जमा करें",
            "withdraw" to "निकासी",
            "send_money" to "पैसे भेजें",
            "recharge" to "मोबाइल रिचार्ज",
            "settings" to "सेटिंग्स",
            "jobs" to "नौकरियां",
            "home" to "होम",
            "shipping" to "रीसेलिंग",
            "connections" to "कनेक्शन"
        )

        private val arMap = mapOf(
            "app_name" to "Fleearn",
            "slogan" to "Earn your way",
            "login" to "تسجيل الدخول",
            "signup" to "إنشاء حساب",
            "balance" to "رصيد FCT",
            "deposit" to "إيداع",
            "withdraw" to "سحب",
            "settings" to "الإعدادات",
            "jobs" to "المهام",
            "home" to "الرئيسية",
            "shipping" to "إعادة البيع",
            "connections" to "الرسائل"
        )

        private val esMap = mapOf(
            "app_name" to "Fleearn",
            "slogan" to "Earn your way",
            "login" to "Iniciar Sesión",
            "signup" to "Registrarse",
            "balance" to "Saldo FCT",
            "deposit" to "Depositar",
            "withdraw" to "Retirar",
            "settings" to "Ajustes",
            "jobs" to "Trabajos",
            "home" to "Inicio",
            "shipping" to "Comercio",
            "connections" to "Conexiones"
        )

        private val translations: Map<AppLanguage, Map<String, String>> = mapOf(
            AppLanguage.EN to enMap,
            AppLanguage.BN to bnMap,
            AppLanguage.HI to hiMap,
            AppLanguage.AR to arMap,
            AppLanguage.ES to esMap
        )
    }
}

@Composable
fun stringResApp(key: String): String {
    val langManager = LanguageManager.LocalLanguage.current
    return langManager.getString(key)
}
