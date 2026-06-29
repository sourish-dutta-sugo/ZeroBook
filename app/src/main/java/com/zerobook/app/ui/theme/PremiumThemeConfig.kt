package com.zerobook.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium Fintech Design System
 * 
 * A hybrid design system combining modern banking aesthetics with soft neumorphic details.
 * Color palette: Warm neutral backgrounds, professional dark text, soft elevated cards,
 * finance-focused accents, and clear status states.
 */
object PremiumThemeConfig {
    
    // ============================================================================
    // PREMIUM FINTECH THEME - Main Color Palette
    // ============================================================================
    
    val PREMIUM_FINTECH = AppTheme(
        name = "PREMIUM_FINTECH",
        // Warm neutral backgrounds for calm, trustworthy feel
        backgroundPrimary = Color(0xFFFAF8F5),      // Warm off-white (primary screen bg)
        backgroundSecondary = Color(0xFFFFFCFA),    // Slightly warmer white (cards)
        backgroundTertiary = Color(0xFFF5F2ED),     // Warm neutral (inputs, secondary areas)
        
        // Premium finance accent - deep teal/blue-green
        accentPrimary = Color(0xFF0F7D6F),          // Deep teal (primary actions, highlights)
        accentLight = Color(0xFFE0F4F0),            // Soft teal (backgrounds, highlights)
        
        // Professional dark text
        textPrimary = Color(0xFF1A1A1A),            // Near black (primary text)
        textSecondary = Color(0xFF4A4A4A),          // Dark gray (secondary text)
        textTertiary = Color(0xFF7A7A7A),           // Medium gray (tertiary text)
        
        // Status bar
        statusBarColor = Color(0xFFFAF8F5),
        statusBarDarkIcons = true
    )
    
    // ============================================================================
    // SEMANTIC TOKENS FOR PREMIUM FINTECH DESIGN
    // ============================================================================
    
    object Semantic {
        // Primary Actions & Highlights
        val primaryAction = Color(0xFF0F7D6F)       // Deep teal
        val primaryActionHover = Color(0xFF0A5D52)  // Darker teal
        val primaryActionLight = Color(0xFFE0F4F0)  // Soft teal background
        
        // Secondary Actions
        val secondaryAction = Color(0xFF6B7280)     // Gray
        val secondaryActionLight = Color(0xFFE5E7EB) // Light gray
        
        // Neumorphic Shadows (soft, subtle)
        val shadowLight = Color(0x0D000000)         // Very light shadow
        val shadowMedium = Color(0x1A000000)        // Medium shadow
        val shadowDark = Color(0x26000000)          // Darker shadow
        
        // Neumorphic Highlights (for raised effect)
        val highlightLight = Color(0xFFFFFFFF)      // White highlight
        val highlightMedium = Color(0xFFFFFBF8)     // Warm white
        
        // Card Backgrounds (elevated, soft)
        val cardElevated = Color(0xFFFFFCFA)        // Slightly raised white
        val cardElevatedAlt = Color(0xFFFAF8F5)     // Alternative elevated
        
        // Borders & Dividers
        val borderLight = Color(0xFFE8E4DE)         // Warm light border
        val borderMedium = Color(0xFFD4CFCA)        // Medium border
        val divider = Color(0xFFEBE7E2)             // Warm divider
        
        // Status Colors - Finance Focused
        val income = Color(0xFF059669)              // Green (income, credit, positive)
        val incomeBg = Color(0xFFDCFCE7)            // Light green background
        val expense = Color(0xFFDC2626)             // Red (expense, debit, negative)
        val expenseBg = Color(0xFFFEE2E2)           // Light red background
        val neutral = Color(0xFF6B7280)             // Gray (neutral transactions)
        val neutralBg = Color(0xFFF3F4F6)           // Light gray background
        
        // Warning & Info
        val warning = Color(0xFFEA580C)             // Orange
        val warningBg = Color(0xFFFEF3C7)           // Light orange
        val info = Color(0xFF0284C7)                // Blue
        val infoBg = Color(0xFFE0F2FE)              // Light blue
        
        // Success & Error
        val success = Color(0xFF059669)             // Green
        val successBg = Color(0xFFDCFCE7)           // Light green
        val error = Color(0xFFDC2626)               // Red
        val errorBg = Color(0xFFFEE2E2)             // Light red
        
        // Balance & Amount Display
        val balancePositive = Color(0xFF059669)     // Green for positive balance
        val balanceNegative = Color(0xFFDC2626)     // Red for negative balance
        val balanceNeutral = Color(0xFF1A1A1A)      // Black for neutral
        
        // Chart Colors (for analytics)
        val chartIncome = Color(0xFF059669)         // Green
        val chartExpense = Color(0xFFDC2626)        // Red
        val chartPending = Color(0xFFF59E0B)        // Amber
        val chartNeutral = Color(0xFF9CA3AF)        // Gray
    }
    
    // ============================================================================
    // TYPOGRAPHY TOKENS
    // ============================================================================
    
    object Typography {
        // Display sizes (for large headings)
        val displayLarge = 32f                      // 32sp
        val displayMedium = 28f                     // 28sp
        val displaySmall = 24f                      // 24sp
        
        // Headline sizes (for section headers)
        val headlineLarge = 22f                     // 22sp
        val headlineMedium = 20f                    // 20sp
        val headlineSmall = 18f                     // 18sp
        
        // Title sizes (for card titles, labels)
        val titleLarge = 16f                        // 16sp
        val titleMedium = 14f                       // 14sp
        val titleSmall = 12f                        // 12sp
        
        // Body sizes (for content)
        val bodyLarge = 16f                         // 16sp
        val bodyMedium = 14f                        // 14sp
        val bodySmall = 12f                         // 12sp
        
        // Label sizes (for buttons, badges)
        val labelLarge = 14f                        // 14sp
        val labelMedium = 12f                       // 12sp
        val labelSmall = 11f                        // 11sp
    }
    
    // ============================================================================
    // SPACING TOKENS (in dp)
    // ============================================================================
    
    object Spacing {
        val xs = 4f                                 // Extra small
        val sm = 8f                                 // Small
        val md = 12f                                // Medium
        val lg = 16f                                // Large
        val xl = 24f                                // Extra large
        val xxl = 32f                               // Double extra large
    }
    
    // ============================================================================
    // BORDER RADIUS TOKENS (in dp)
    // ============================================================================
    
    object BorderRadius {
        val xs = 4f                                 // Extra small (tight)
        val sm = 8f                                 // Small
        val md = 12f                                // Medium (default)
        val lg = 16f                                // Large
        val xl = 20f                                // Extra large
        val full = 999f                             // Full circle
    }
    
    // ============================================================================
    // SHADOW TOKENS (for neumorphic effects)
    // ============================================================================
    
    object Shadows {
        // Soft, subtle shadows for premium feel
        val elevationSmall = 2f                     // Small elevation
        val elevationMedium = 4f                    // Medium elevation
        val elevationLarge = 8f                     // Large elevation
        val elevationXLarge = 12f                   // Extra large elevation
    }
}
