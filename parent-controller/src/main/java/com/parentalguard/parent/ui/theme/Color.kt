package com.parentalguard.parent.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// AURA · 2026 — night-watch palette. Deep-space base, aurora accents.
// Dark is the primary voice; "Porcelain" is the light counterpart.
// ============================================================================

// --- Deep space (dark) ---
val AuraBgDeep = Color(0xFF05070D)
val AuraBgBase = Color(0xFF080C16)
val AuraBgRaise = Color(0xFF0C1220)
val AuraInk = Color(0xFFF1F5F9)
val AuraInkMid = Color(0xFF94A3B8)
val AuraInkDim = Color(0xFF5B6B84)

// --- Porcelain (light) ---
val AuraPaperBg = Color(0xFFF4F6FB)
val AuraPaperRaise = Color(0xFFFFFFFF)
val AuraPaperInk = Color(0xFF0B1220)
val AuraPaperInkMid = Color(0xFF475569)
val AuraPaperInkDim = Color(0xFF8A97AC)

// --- Aurora accents (shared across schemes) ---
val AuroraIndigo = Color(0xFF7C6CFF)
val AuroraViolet = Color(0xFFA78BFA)
val AuroraCyan = Color(0xFF22D3EE)
val AuroraSky = Color(0xFF38BDF8)

val AuroraSuccess = Color(0xFF34D399)
val AuroraWarning = Color(0xFFFBBF24)
val AuroraDanger = Color(0xFFFB7185)

// Accent variants tuned for porcelain backgrounds (deeper for contrast)
val PorcelainIndigo = Color(0xFF5F4BFF)
val PorcelainViolet = Color(0xFF7C3AED)
val PorcelainCyan = Color(0xFF0891B2)

// --- Category identity (muted jewel tones, readable on glass) ---
val CategorySocial = Color(0xFF38BDF8)
val CategoryGames = Color(0xFFA78BFA)
val CategoryEducation = Color(0xFF34D399)
val CategoryProductivity = Color(0xFFFBBF24)
val CategoryEntertainment = Color(0xFFFB7185)
val CategorySystem = Color(0xFF64748B)
val CategoryOther = Color(0xFF94A3B8)

// --- Legacy aliases kept so untouched files (NotificationHelper etc.) compile ---
val Success = AuroraSuccess
val Warning = AuroraWarning
val Error = AuroraDanger
val Info = AuroraSky
val Primary = AuroraIndigo
val Secondary = AuroraCyan
val OnlineGreen = AuroraSuccess
val OfflineRed = AuroraDanger
