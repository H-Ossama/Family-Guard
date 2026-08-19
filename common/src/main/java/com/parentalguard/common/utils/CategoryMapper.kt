package com.parentalguard.common.utils

import com.parentalguard.common.model.AppCategory

object CategoryMapper {
    
    // Predefined mappings for popular apps
    private val categoryMap = mapOf(
        // Social Media
        "com.facebook.katana" to AppCategory.SOCIAL,
        "com.facebook.orca" to AppCategory.SOCIAL,
        "com.instagram.android" to AppCategory.SOCIAL,
        "com.whatsapp" to AppCategory.SOCIAL,
        "com.snapchat.android" to AppCategory.SOCIAL,
        "com.twitter.android" to AppCategory.SOCIAL,
        "com.zhiliaoapp.musically" to AppCategory.SOCIAL, // TikTok
        "com.discord" to AppCategory.SOCIAL,
        "com.linkedin.android" to AppCategory.SOCIAL,
        "com.reddit.frontpage" to AppCategory.SOCIAL,
        "com.telegram.messenger" to AppCategory.SOCIAL,
        
        // Games
        "com.mojang.minecraftpe" to AppCategory.GAMES,
        "com.supercell.clashofclans" to AppCategory.GAMES,
        "com.supercell.brawlstars" to AppCategory.GAMES,
        "com.roblox.client" to AppCategory.GAMES,
        "com.ea.gp.fifamobile" to AppCategory.GAMES,
        "com.pubg.krmobile" to AppCategory.GAMES,
        "com.garena.game.freefire" to AppCategory.GAMES,
        "com.epicgames.fortnite" to AppCategory.GAMES,
        "com.activision.callofduty.shooter" to AppCategory.GAMES,
        "com.kiloo.subwaysurf" to AppCategory.GAMES,
        
        // Education
        "com.duolingo" to AppCategory.EDUCATION,
        "com.khanacademy.android" to AppCategory.EDUCATION,
        "com.google.android.apps.classroom" to AppCategory.EDUCATION,
        "com.photomath.app" to AppCategory.EDUCATION,
        "org.wikipedia" to AppCategory.EDUCATION,
        "com.quizlet.quizletandroid" to AppCategory.EDUCATION,
        "com.google.android.apps.docs" to AppCategory.EDUCATION,
        "com.microsoft.office.word" to AppCategory.EDUCATION,
        "com.adobe.reader" to AppCategory.EDUCATION,
        
        // Entertainment
        "com.google.android.youtube" to AppCategory.ENTERTAINMENT,
        "com.netflix.mediaclient" to AppCategory.ENTERTAINMENT,
        "com.spotify.music" to AppCategory.ENTERTAINMENT,
        "com.disney.disneyplus" to AppCategory.ENTERTAINMENT,
        "tv.twitch.android.app" to AppCategory.ENTERTAINMENT,
        "com.amazon.avod.thirdpartyclient" to AppCategory.ENTERTAINMENT, // Prime Video
        
        // Productivity
        "com.microsoft.office.excel" to AppCategory.PRODUCTIVITY,
        "com.microsoft.office.powerpoint" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.docs.editors.sheets" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.docs.editors.docs" to AppCategory.PRODUCTIVITY,
        "com.evernote" to AppCategory.PRODUCTIVITY,
        "com.todoist" to AppCategory.PRODUCTIVITY,
        "com.google.android.keep" to AppCategory.PRODUCTIVITY,
        "com.trello" to AppCategory.PRODUCTIVITY,
        "com.notion.id" to AppCategory.PRODUCTIVITY
    )
    
    // System apps that should be whitelisted by default
    private val whitelistedPackages = setOf(
        "com.android.phone",           // Phone/Dialer
        "com.android.contacts",        // Contacts
        "com.android.mms",             // SMS/MMS
        "com.google.android.dialer",   // Google Dialer
        "com.google.android.contacts", // Google Contacts
        "com.android.calculator2",     // Calculator
        "com.google.android.calculator", // Google Calculator
        "com.android.settings",        // Settings (for emergencies)
        "com.android.systemui",        // System UI
        "com.android.launcher",        // Launcher
        "com.google.android.apps.maps" // Maps (for safety/navigation)
    )
    
    /**
     * Get the category for a given package name
     */
    fun getCategoryForPackage(packageName: String): AppCategory {
        return getCategoryForPackage(packageName, null)
    }

    /**
     * Classifies known apps first, then uses package/label keywords for apps
     * that are not in the static catalog yet.
     */
    fun getCategoryForPackage(packageName: String, appLabel: String?): AppCategory {
        categoryMap[packageName]?.let { return it }

        val value = "$packageName ${appLabel.orEmpty()}".lowercase()
        return when {
            containsAny(value, "facebook", "instagram", "whatsapp", "messenger", "snapchat", "tiktok", "twitter", "telegram", "discord", "reddit", "linkedin", "pinterest", "social") -> AppCategory.SOCIAL
            containsAny(value, "game", "games", "roblox", "minecraft", "clash", "brawl", "pubg", "freefire", "fortnite", "subway", "candy", "chess") -> AppCategory.GAMES
            containsAny(value, "school", "education", "learning", "learn", "classroom", "duolingo", "khan", "quiz", "course", "udemy", "coursera", "wikipedia", "dictionary") -> AppCategory.EDUCATION
            containsAny(value, "youtube", "netflix", "spotify", "music", "video", "twitch", "disney", "prime video", "hulu", "movie", "movies", "podcast", "stream") -> AppCategory.ENTERTAINMENT
            containsAny(value, "office", "docs", "sheets", "word", "excel", "powerpoint", "drive", "notes", "calendar", "gmail", "mail", "outlook", "notion", "todo", "task", "slack", "teams", "zoom", "browser", "chrome", "firefox", "edge") -> AppCategory.PRODUCTIVITY
            else -> AppCategory.OTHER
        }
    }

    private fun containsAny(value: String, vararg terms: String): Boolean = terms.any(value::contains)
    
    /**
     * Check if a package should be whitelisted (never blocked)
     */
    fun isWhitelisted(packageName: String): Boolean {
        return whitelistedPackages.contains(packageName)
    }
    
    /**
     * Get all packages that belong to a specific category
     */
    fun getPackagesInCategory(category: AppCategory): List<String> {
        return categoryMap.filterValues { it == category }.keys.toList()
    }
}
