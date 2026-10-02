package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class LeagueTier(
    val tierId: String,
    val displayName: String,
    val emoji: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val minXpRequired: Int,
    val rankOrder: Int
) {
    BRONZE("league_bronze", "Bronze League", "🥉", 0xFFCD7F32, 0xFF8D5524, 0, 1),
    SILVER("league_silver", "Silver League", "🥈", 0xFFC0C0C0, 0xFF78909C, 250, 2),
    GOLD("league_gold", "Gold League", "🥇", 0xFFFFD700, 0xFFFFA000, 600, 3),
    SAPPHIRE("league_sapphire", "Sapphire League", "💎", 0xFF1E88E5, 0xFF0D47A1, 1100, 4),
    RUBY("league_ruby", "Ruby League", "🔴", 0xFFE53935, 0xFFB71C1C, 1800, 5),
    EMERALD("league_emerald", "Emerald League", "🟢", 0xFF43A047, 0xFF1B5E20, 2700, 6),
    AMETHYST("league_amethyst", "Amethyst League", "🟣", 0xFF8E24AA, 0xFF4A148C, 3800, 7),
    PEARL("league_pearl", "Pearl League", "🦪", 0xFFEC407A, 0xFF880E4F, 5100, 8),
    OBSIDIAN("league_obsidian", "Obsidian League", "⬛", 0xFF455A64, 0xFF212121, 6600, 9),
    DIAMOND("league_diamond", "Diamond League", "👑💎", 0xFF00E5FF, 0xFF0091EA, 8500, 10);

    fun nextLeague(): LeagueTier? {
        val nextRank = rankOrder + 1
        return entries.firstOrNull { it.rankOrder == nextRank }
    }

    fun previousLeague(): LeagueTier? {
        val prevRank = rankOrder - 1
        return entries.firstOrNull { it.rankOrder == prevRank }
    }
}

enum class LeaderboardZone {
    PROMOTION, // Top 5
    SAFE,      // Ranks 6 to 25
    DEMOTION   // Ranks 26 to 30
}

data class LeaderboardCompetitor(
    val id: String,
    val name: String,
    val avatarColor: Color,
    val avatarInitial: String,
    val weeklyXp: Int,
    val streakDays: Int,
    val rank: Int,
    val isCurrentUser: Boolean = false,
    val zone: LeaderboardZone = when {
        rank <= 5 -> LeaderboardZone.PROMOTION
        rank >= 26 -> LeaderboardZone.DEMOTION
        else -> LeaderboardZone.SAFE
    },
    val titleBadge: String = when {
        rank == 1 -> "🏆 Champion"
        rank == 2 -> "🥈 Silver Star"
        rank == 3 -> "🥉 Bronze Star"
        rank in 4..5 -> "⚡ Promotion Contender"
        else -> "📚 Active Scholar"
    }
)

data class SpeedDrillQuestion(
    val id: String,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val xpReward: Int = 30
)

object LeaderboardDrillCatalog {
    val questions = listOf(
        SpeedDrillQuestion(
            id = "drill_1",
            category = "Financial Markets",
            question = "What does a high Price-to-Earnings (P/E) ratio typically indicate about a stock?",
            options = listOf(
                "Investors expect higher future earnings growth",
                "The company is close to insolvency",
                "Dividends are guaranteed to double",
                "The company has zero debt"
            ),
            correctIndex = 0,
            explanation = "A high P/E ratio indicates that investors anticipate significant future growth and are willing to pay a premium per dollar of earnings."
        ),
        SpeedDrillQuestion(
            id = "drill_2",
            category = "Technical Analysis",
            question = "In candlestick charting, what does a long lower wick (hammer) signal after a downtrend?",
            options = listOf(
                "Persistent aggressive selling pressure",
                "Bullish rejection of lower prices and buying interest",
                "Immediate market halt by the exchange",
                "A guarantee that volatility is zero"
            ),
            correctIndex = 1,
            explanation = "A long lower wick shows buyers stepped in forcefully to push prices back up, signaling potential bullish reversal."
        ),
        SpeedDrillQuestion(
            id = "drill_3",
            category = "Macroeconomics",
            question = "When central banks raise interest rates, what is the usual intended effect?",
            options = listOf(
                "To accelerate consumer borrowing and inflation",
                "To cool economic overheating and curb rising inflation",
                "To weaken domestic currency exchange rates",
                "To force commercial banks to liquidate all reserves"
            ),
            correctIndex = 1,
            explanation = "Higher borrowing costs dampen demand and consumer spending, which helps slow down inflationary pressures."
        ),
        SpeedDrillQuestion(
            id = "drill_4",
            category = "Portfolio Risk",
            question = "What is the primary benefit of portfolio diversification across uncorrelated asset classes?",
            options = listOf(
                "Eliminating all systematic market risk",
                "Reducing unsystematic variance while maintaining expected return",
                "Guaranteed 50% monthly profit",
                "Eliminating the need to pay any transaction fees"
            ),
            correctIndex = 1,
            explanation = "Diversification smooths returns by ensuring that downturns in specific individual assets don't cripple the whole portfolio."
        ),
        SpeedDrillQuestion(
            id = "drill_5",
            category = "Options & Derivatives",
            question = "What is a 'Call Option'?",
            options = listOf(
                "The obligation to sell an asset at the strike price",
                "The right, but not obligation, to buy an asset at strike price",
                "A loan taken from a central bank",
                "A penalty issued for violating securities regulations"
            ),
            correctIndex = 1,
            explanation = "A Call Option gives the holder the right to purchase the underlying asset at a predetermined strike price prior to expiration."
        ),
        SpeedDrillQuestion(
            id = "drill_6",
            category = "AI Architecture",
            question = "What core mechanism enables Transformer models like Gemini to process long context relationships?",
            options = listOf(
                "Recurrent feedback loops with memory gates",
                "Multi-Head Self-Attention mechanisms",
                "Sequential character parsing",
                "Fixed lookup hash maps"
            ),
            correctIndex = 1,
            explanation = "Self-attention computes pairwise token affinities in parallel, letting the model connect related context regardless of distance."
        )
    )

    fun getRandomDrill(): SpeedDrillQuestion {
        return questions.random()
    }
}
