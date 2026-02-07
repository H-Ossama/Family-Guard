package com.parentalguard.common.network

object CloudConfig {
    // Render app URL
    const val BASE_URL = "https://family-guard-relay.onrender.com"
    
    // Derived URLs
    const val WS_URL = "wss://family-guard-relay.onrender.com"
    
    // Endpoints
    const val ENDPOINT_REGISTER = "/register"
    const val ENDPOINT_HEALTH = "/health"
}
