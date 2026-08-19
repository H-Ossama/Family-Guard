package com.parentalguard.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class BlockingScreenStyle {
    CURRENT,
    BLACKOUT,
    QUIET_FOCUS
}
