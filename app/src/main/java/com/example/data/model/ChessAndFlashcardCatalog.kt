package com.example.data.model

/**
 * Catalog for Chess Grandmaster course and daily flashcard drills.
 * Integrated with the badge system to track milestones.
 */
object ChessAndFlashcardCatalog {
    const val CHESS_GM_COURSE_ID = "chess_gm_complete"
    const val FLASHCARD_DAILY_COURSE_ID = "flashcard_daily"

    data class ChessPuzzle(
        val id: String,
        val fen: String,
        val solution: String,
        val difficulty: Int,
        val category: String
    )

    data class FlashcardDeck(
        val id: String,
        val title: String,
        val cards: List<FlashcardItem>
    )

    data class FlashcardItem(
        val id: String,
        val front: String,
        val back: String,
        val difficulty: Int
    )

    val SAMPLE_CHESS_PUZZLES = listOf(
        ChessPuzzle(
            id = "chess_1",
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            solution = "e2e4",
            difficulty = 1,
            category = "Opening"
        )
    )

    val SAMPLE_FLASHCARD_DECK = FlashcardDeck(
        id = "daily_1",
        title = "Daily Learning Streaks",
        cards = listOf(
            FlashcardItem(
                id = "card_1",
                front = "What is the Spanish Opening?",
                back = "An open chess opening where White plays 1.e4 and Black plays 1...e5.",
                difficulty = 3
            )
        )
    )
}
