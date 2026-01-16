package com.parentalguard.common.network

object CloudConfig {
    // Replace with your Render app URL after deployment
    // Example: "https://family-guard.onrender.com"
    const val BASE_URL = "https://your-render-app-name.onrender.com"
    
    // Derived URLs
    const val WS_URL = "wss://your-render-app-name.onrender.com"
    
    // Endpoints
    const val ENDPOINT_REGISTER = "/register"
    const val ENDPOINT_HEALTH = "/health"
}
