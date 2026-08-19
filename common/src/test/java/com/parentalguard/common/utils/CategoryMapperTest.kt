package com.parentalguard.common.utils

import com.parentalguard.common.model.AppCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun getCategoryForPackage_knownApp_mapsToCategory() {
        assertEquals(AppCategory.SOCIAL, CategoryMapper.getCategoryForPackage("com.whatsapp"))
        assertEquals(AppCategory.GAMES, CategoryMapper.getCategoryForPackage("com.roblox.client"))
        assertEquals(AppCategory.EDUCATION, CategoryMapper.getCategoryForPackage("com.duolingo"))
        assertEquals(AppCategory.ENTERTAINMENT, CategoryMapper.getCategoryForPackage("com.netflix.mediaclient"))
    }

    @Test
    fun getCategoryForPackage_unknownApp_defaultsToOther() {
        assertEquals(AppCategory.OTHER, CategoryMapper.getCategoryForPackage("com.example.unknown.app"))
    }

    @Test
    fun getCategoryForPackage_usesAppLabelForUnknownPackages() {
        assertEquals(
            AppCategory.GAMES,
            CategoryMapper.getCategoryForPackage("com.example.childgame", "My Game")
        )
        assertEquals(
            AppCategory.PRODUCTIVITY,
            CategoryMapper.getCategoryForPackage("com.example.work", "Work Calendar")
        )
    }

    @Test
    fun isWhitelisted_systemApp_true() {
        assertTrue(CategoryMapper.isWhitelisted("com.android.phone"))
        assertTrue(CategoryMapper.isWhitelisted("com.android.settings"))
    }

    @Test
    fun isWhitelisted_socialApp_false() {
        assertFalse(CategoryMapper.isWhitelisted("com.whatsapp"))
    }

    @Test
    fun getPackagesInCategory_returnsAllMembers() {
        val games = CategoryMapper.getPackagesInCategory(AppCategory.GAMES)
        assertTrue(games.contains("com.roblox.client"))
        assertTrue(games.contains("com.supercell.clashofclans"))
        assertFalse(games.contains("com.whatsapp"))
    }
}
