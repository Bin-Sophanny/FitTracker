package com.example.fittrack.data.model

/**
 * Data class representing user's token balance
 * Matches backend blockchain-service model
 */
data class TokenBalance(
    val balance: Int,               // Current token balance
    val totalEarned: Int,           // Total tokens earned
    val transactions: List<TokenTransaction>
)

/**
 * Data class representing a blockchain transaction
 */
data class TokenTransaction(
    val id: String,                 // Transaction ID
    val amount: Int,                // Token amount
    val reason: String,             // Reason for reward (e.g., "Completed 10000 steps")
    val date: String,               // Transaction date "yyyy-MM-dd"
    val transactionHash: String? = null  // Blockchain transaction hash
)

/**
 * Response after awarding tokens
 */
data class RewardResponse(
    val message: String,
    val tokensAwarded: Int,
    val newBalance: Int,
    val transactionHash: String? = null
)

