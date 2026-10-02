package com.example.data.model

data class EdamFlashcard(
    val id: String,
    val category: String,
    val difficulty: String,
    val frontPrompt: String,
    val frontHint: String,
    val backAnswer: String,
    val keyFormulaOrMove: String,
    val xpReward: Int = 15
)

data class ChessSquarePiece(
    val row: Int,
    val col: Int,
    val symbol: String,
    val isWhite: Boolean
)

data class ChessTacticalPuzzle(
    val id: String,
    val title: String,
    val eloTier: String,
    val theme: String,
    val prompt: String,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val winningMoveNotation: String,
    val gmExplanation: String,
    val xpReward: Int = 35,
    val pieces: List<ChessSquarePiece>
)

enum class FinancialInstrumentType(
    val id: String,
    val displayName: String,
    val tickerCode: String,
    val assetClass: String,
    val keyMetricLabel: String,
    val keyMetricValue: String,
    val riskProfile: String,
    val summary: String,
    val seriesPoints: List<Float>,
    val secondaryOverlayPoints: List<Float>,
    val volumeBars: List<Float>
) {
    EQUITIES(
        id = "equities",
        displayName = "Equities (Growth Stocks)",
        tickerCode = "NVDA / RELIANCE",
        assetClass = "Common Stock",
        keyMetricLabel = "Forward P/E · ROE",
        keyMetricValue = "28.4x · 31.2%",
        riskProfile = "Medium-High Growth",
        summary = "Fractional ownership in public companies. Driven by earnings growth, free cash flow, and institutional accumulation.",
        seriesPoints = listOf(112f, 115f, 114f, 119f, 123f, 121f, 128f, 134f, 131f, 139f, 145f, 152f),
        secondaryOverlayPoints = listOf(110f, 112f, 114f, 116f, 119f, 121f, 124f, 127f, 130f, 134f, 138f, 143f),
        volumeBars = listOf(0.45f, 0.62f, 0.38f, 0.78f, 0.85f, 0.51f, 0.92f, 0.88f, 0.49f, 0.95f, 0.84f, 1.0f)
    ),
    INDICES(
        id = "indices",
        displayName = "Market Indices (S&P 500 / NIFTY)",
        tickerCode = "SPX / NIFTY50",
        assetClass = "Benchmark Index",
        keyMetricLabel = "1Y Return · VIX",
        keyMetricValue = "+18.6% · 13.8",
        riskProfile = "Diversified Core",
        summary = "Market-cap weighted baskets tracking the broader economy. Ideal benchmark for measuring alpha and macro trend health.",
        seriesPoints = listOf(480f, 485f, 483f, 491f, 496f, 502f, 499f, 508f, 514f, 519f, 524f, 531f),
        secondaryOverlayPoints = listOf(478f, 481f, 484f, 487f, 490f, 494f, 497f, 501f, 506f, 511f, 516f, 522f),
        volumeBars = listOf(0.55f, 0.58f, 0.52f, 0.66f, 0.71f, 0.69f, 0.60f, 0.75f, 0.79f, 0.82f, 0.80f, 0.88f)
    ),
    OPTIONS(
        id = "options",
        displayName = "Options & Derivatives (Call/Put)",
        tickerCode = "LONG CALL PAYOFF",
        assetClass = "Derivative Contract",
        keyMetricLabel = "Delta (Δ) · Theta (Θ)",
        keyMetricValue = "+0.62 · -0.08/d",
        riskProfile = "Defined Risk / Asymmetric",
        summary = "Asymmetric contracts granting the right to buy (Call) or sell (Put) at a strike price. Loss is capped at premium paid.",
        seriesPoints = listOf(-20f, -20f, -20f, -20f, -20f, -15f, 0f, 20f, 45f, 70f, 95f, 120f),
        secondaryOverlayPoints = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        volumeBars = listOf(0.30f, 0.32f, 0.35f, 0.42f, 0.65f, 0.88f, 1.0f, 0.91f, 0.76f, 0.68f, 0.60f, 0.55f)
    ),
    ETFS(
        id = "etfs",
        displayName = "ETFs & Factor Baskets",
        tickerCode = "QQQ / GOLDBEES",
        assetClass = "Exchange-Traded Fund",
        keyMetricLabel = "Expense Ratio · Yield",
        keyMetricValue = "0.09% · 1.42%",
        riskProfile = "Low-Cost Compounder",
        summary = "Intraday tradable funds combining mutual-fund diversification with low expense ratios and tax efficiency.",
        seriesPoints = listOf(210f, 213f, 216f, 215f, 220f, 224f, 228f, 231f, 236f, 241f, 246f, 252f),
        secondaryOverlayPoints = listOf(208f, 211f, 213f, 215f, 218f, 221f, 224f, 227f, 231f, 235f, 240f, 245f),
        volumeBars = listOf(0.50f, 0.54f, 0.48f, 0.62f, 0.67f, 0.72f, 0.69f, 0.74f, 0.78f, 0.81f, 0.85f, 0.90f)
    ),
    BONDS(
        id = "bonds",
        displayName = "Bonds & Treasury Yield Curve",
        tickerCode = "US10Y / G-SEC",
        assetClass = "Fixed Income",
        keyMetricLabel = "Yield to Maturity · Duration",
        keyMetricValue = "4.28% · 7.4 yrs",
        riskProfile = "Capital Preservation",
        summary = "Sovereign and corporate debt instruments. Bond prices move inversely to interest rate changes according to modified duration.",
        seriesPoints = listOf(3.65f, 3.82f, 3.95f, 4.05f, 4.12f, 4.18f, 4.22f, 4.25f, 4.26f, 4.27f, 4.28f, 4.30f),
        secondaryOverlayPoints = listOf(4.10f, 4.08f, 4.06f, 4.05f, 4.07f, 4.10f, 4.13f, 4.16f, 4.18f, 4.20f, 4.22f, 4.24f),
        volumeBars = listOf(0.60f, 0.64f, 0.70f, 0.68f, 0.62f, 0.58f, 0.55f, 0.59f, 0.63f, 0.67f, 0.65f, 0.72f)
    ),
    COMMODITIES(
        id = "commodities",
        displayName = "Commodities & Forex (Gold / FX)",
        tickerCode = "XAU/USD · EUR/USD",
        assetClass = "Hard Asset & FX",
        keyMetricLabel = "Inflation Beta · Spread",
        keyMetricValue = "+0.84 · 0.8 pips",
        riskProfile = "Macro Hedge",
        summary = "Physical commodities and currency pairs used by institutional desks to hedge inflation, geopolitical risk, and real rates.",
        seriesPoints = listOf(2310f, 2335f, 2320f, 2360f, 2395f, 2410f, 2390f, 2445f, 2480f, 2515f, 2540f, 2585f),
        secondaryOverlayPoints = listOf(2295f, 2310f, 2322f, 2340f, 2362f, 2385f, 2395f, 2418f, 2445f, 2475f, 2505f, 2535f),
        volumeBars = listOf(0.48f, 0.61f, 0.53f, 0.77f, 0.84f, 0.79f, 0.62f, 0.89f, 0.93f, 0.87f, 0.91f, 0.96f)
    )
}

enum class StockGraphMode(val label: String) {
    PRICE_AND_SMA("Price + SMA/EMA"),
    CANDLESTICK("OHLC Candles"),
    RSI_MOMENTUM("RSI (14) Oscillator"),
    PAYOFF_RISK("Payoff / Volume Profile")
}

object ChessAndFlashcardCatalog {

    const val CHESS_GM_COURSE_ID = "edam_chess_novice_to_gm_v1"

    val heroFlashcards: List<EdamFlashcard> = listOf(
        EdamFlashcard(
            id = "fc_mkt_1",
            category = "Stock Market",
            difficulty = "Foundation",
            frontPrompt = "What does a P/E Ratio vs. PEG Ratio tell an equity investor?",
            frontHint = "Think about earnings multiple adjusted for annual EPS growth rate.",
            backAnswer = "P/E divides Stock Price by EPS. PEG divides the P/E ratio by the expected annual EPS growth rate — a PEG below 1.0 often signals growth at a reasonable price (GARP).",
            keyFormulaOrMove = "PEG = (Price / EPS) ÷ Annual EPS Growth %",
            xpReward = 15
        ),
        EdamFlashcard(
            id = "fc_chess_1",
            category = "Chess: Novice to GM",
            difficulty = "Tactical (1500 ELO)",
            frontPrompt = "What is the 'Greek Gift Sacrifice' (Bxh7+) in chess middlegames?",
            frontHint = "White sacrifices the light-squared Bishop on h7 against a castled Black King.",
            backAnswer = "White plays Bxh7+! Kxh7, followed by Ng5+ and Qh5+, ripping open the h-file for a decisive mating net unless Black can defend h7 with a Knight on f6 or f8.",
            keyFormulaOrMove = "1. Bxh7+! Kxh7 2. Ng5+ Kg8 3. Qh5",
            xpReward = 20
        ),
        EdamFlashcard(
            id = "fc_mkt_2",
            category = "Options & Instruments",
            difficulty = "Intermediate",
            frontPrompt = "How do Option Delta (Δ) and Theta (Θ) interact for a Long Call buyer?",
            frontHint = "Direction sensitivity vs. daily time decay.",
            backAnswer = "Delta (Δ) measures how much the option price gains per $1 rise in the stock. Theta (Θ) is the daily time-decay cost subtracted from the option's extrinsic value.",
            keyFormulaOrMove = "Breakeven at Expiry = Strike Price + Call Premium",
            xpReward = 15
        ),
        EdamFlashcard(
            id = "fc_chess_2",
            category = "Chess: Novice to GM",
            difficulty = "Grandmaster (2400 ELO)",
            frontPrompt = "Lucena Position vs. Philidor Position in Rook Endgames: which one wins?",
            frontHint = "Building a bridge vs. third-rank defense.",
            backAnswer = "The Lucena Position is a forced win using the 'Building a Bridge' technique (Rook on 4th/5th rank shielding King from checks). The Philidor Position is a textbook draw via 3rd-rank defense.",
            keyFormulaOrMove = "Lucena = Win (Bridge) · Philidor = Draw (3rd Rank)",
            xpReward = 25
        ),
        EdamFlashcard(
            id = "fc_mkt_3",
            category = "Bonds & Macro",
            difficulty = "Intermediate",
            frontPrompt = "Why do existing Bond prices fall when Central Bank interest rates rise?",
            frontHint = "Compare fixed coupon payments against newly issued higher-yielding bonds.",
            backAnswer = "Existing bonds pay a fixed coupon. When new bonds offer higher yields, older bonds must trade at a discount so their Yield-to-Maturity (YTM) matches the current market rate.",
            keyFormulaOrMove = "ΔPrice ≈ -Modified Duration × ΔYield",
            xpReward = 15
        ),
        EdamFlashcard(
            id = "fc_chess_3",
            category = "Chess: Novice to GM",
            difficulty = "Endgame (1800 ELO)",
            frontPrompt = "What is 'Direct Opposition' in King & Pawn Endgames?",
            frontHint = "Two Kings facing each other with 1 square between them.",
            backAnswer = "When both Kings stand on the same file or rank separated by one square, the side NOT having to move holds the opposition — forcing the opponent's King to give way.",
            keyFormulaOrMove = "Odd squares between Kings = Side to move loses opposition",
            xpReward = 20
        )
    )

    val chessPuzzles: List<ChessTacticalPuzzle> = listOf(
        ChessTacticalPuzzle(
            id = "puzzle_novice_fork",
            title = "Royal Knight Fork on f7",
            eloTier = "Novice → Club (1100 ELO)",
            theme = "Double Attack / Fork",
            prompt = "White to move: Tap the White Knight on g5, then tap f7 to fork the Black King and Queen!",
            fromRow = 3,
            fromCol = 6,
            toRow = 1,
            toCol = 5,
            winningMoveNotation = "Nxf7+! (Forks King on e8 & Queen on d8)",
            gmExplanation = "Vex's Note: The f7 square is the weakest point in Black's opening camp because only the King defends it. Nxf7+ wins decisive material immediately!",
            xpReward = 35,
            pieces = listOf(
                ChessSquarePiece(0, 3, "♛", false),
                ChessSquarePiece(0, 4, "♚", false),
                ChessSquarePiece(0, 7, "♜", false),
                ChessSquarePiece(1, 4, "♟", false),
                ChessSquarePiece(1, 5, "♟", false),
                ChessSquarePiece(1, 6, "♟", false),
                ChessSquarePiece(3, 6, "♘", true),
                ChessSquarePiece(4, 2, "♗", true),
                ChessSquarePiece(7, 4, "♔", true),
                ChessSquarePiece(7, 3, "♕", true)
            )
        ),
        ChessTacticalPuzzle(
            id = "puzzle_master_backrank",
            title = "Opera House Back-Rank Combination",
            eloTier = "Candidate Master (2050 ELO)",
            theme = "Back-Rank Checkmate",
            prompt = "White to move: Tap the White Rook on d1 and deliver checkmate on d8!",
            fromRow = 7,
            fromCol = 3,
            toRow = 0,
            toCol = 3,
            winningMoveNotation = "Rd8# (Morphy's Opera Game Mate)",
            gmExplanation = "Vex's Note: Inspired by Paul Morphy's 1858 Opera Game! Because the Bishop on g5 protects d8 and Black's pawns trap their own King, Rd8# is checkmate.",
            xpReward = 45,
            pieces = listOf(
                ChessSquarePiece(0, 4, "♚", false),
                ChessSquarePiece(1, 5, "♟", false),
                ChessSquarePiece(1, 6, "♟", false),
                ChessSquarePiece(1, 7, "♟", false),
                ChessSquarePiece(3, 6, "♗", true),
                ChessSquarePiece(7, 3, "♖", true),
                ChessSquarePiece(7, 6, "♔", true)
            )
        ),
        ChessTacticalPuzzle(
            id = "puzzle_gm_opposition",
            title = "Grandmaster Endgame: Seizing Opposition",
            eloTier = "Grandmaster (2550 ELO)",
            theme = "King Opposition & Key Squares",
            prompt = "White to move: Step the White King from e5 to e6 to seize direct opposition and escort the d-pawn to promotion!",
            fromRow = 3,
            fromCol = 4,
            toRow = 2,
            toCol = 4,
            winningMoveNotation = "Ke6! (Direct Opposition — Zougzwang)",
            gmExplanation = "Vex's Note: With Ke6!, Black is in Zugzwang. Whether Black plays Kd8 or Kf8, White's d-pawn marches to d7 and d8=Q unstoppable!",
            xpReward = 50,
            pieces = listOf(
                ChessSquarePiece(0, 4, "♚", false),
                ChessSquarePiece(3, 3, "♙", true),
                ChessSquarePiece(3, 4, "♔", true)
            )
        )
    )

    fun createNoviceToGmChessCourse(): Course {
        val units = listOf(
            CourseUnit(
                id = "chess_u1",
                title = "Unit 1: Novice Foundations & Opening Principles (600–1200 ELO)",
                description = "Master board geometry, algebraic notation, rapid piece development, and early King safety.",
                lessons = listOf(
                    LessonSummary(
                        id = "chess_l1_1",
                        title = "1.1 Center Control, Tempo & Piece Activity",
                        summary = "Why e4/d4/e5/d5 control the board and how every move in the opening fights for tempo."
                    ),
                    LessonSummary(
                        id = "chess_l1_2",
                        title = "1.2 King Safety, Castling & Punishing f7/f2 Weaknesses",
                        summary = "Recognizing premature Queen sorties, castling timing, and classic tactical strikes on f7."
                    ),
                    LessonSummary(
                        id = "chess_l1_3",
                        title = "1.3 Relative Piece Value & Active vs. Passive Bishops",
                        summary = "Understanding dynamic piece value beyond 1-3-3-5-9 based on open diagonals and outposts."
                    )
                )
            ),
            CourseUnit(
                id = "chess_u2",
                title = "Unit 2: Tactical Combinatorics & Pattern Recognition (1200–1700 ELO)",
                description = "Train automatic recognition of forks, pins, skewers, discovered attacks, and mating nets.",
                lessons = listOf(
                    LessonSummary(
                        id = "chess_l2_1",
                        title = "2.1 Knight Forks, Royal Forks & Overloaded Defenders",
                        summary = "Calculating 2- and 3-move forcing sequences that overload enemy defenders."
                    ),
                    LessonSummary(
                        id = "chess_l2_2",
                        title = "2.2 Absolute vs. Relative Pins, Skewers & X-Ray Attacks",
                        summary = "Exploiting aligned pieces on files and diagonals with " +
                            "high-pressure pile-ups."
                    ),
                    LessonSummary(
                        id = "chess_l2_3",
                        title = "2.3 Deflection, Decoy & The Greek Gift Sacrifice (Bxh7+)",
                        summary = "Ripping open the castled King's fortress with calculated piece sacrifices."
                    )
                )
            ),
            CourseUnit(
                id = "chess_u3",
                title = "Unit 3: Positional Mastery & Pawn Structures (1700–2100 ELO)",
                description = "Transition from tactics to deep strategic planning using pawn chains, outposts, and open files.",
                lessons = listOf(
                    LessonSummary(
                        id = "chess_l3_1",
                        title = "3.1 Isolated Queen's Pawn (IQP) & Hanging Pawns",
                        summary = "Playing both sides of the d4/d5 IQP: dynamic piece activity vs. endgame blockade."
                    ),
                    LessonSummary(
                        id = "chess_l3_2",
                        title = "3.2 Carlsbad Structure & The Minority Attack",
                        summary = "Advancing b4-b5 to create permanent queenside weaknesses in Queen's Gambit structures."
                    ),
                    LessonSummary(
                        id = "chess_l3_3",
                        title = "3.3 Good Knight vs. Bad Bishop & Outpost Domination",
                        summary = "Planting unassailable Knights on d5/e5/f5 and restricting the opponent's minor pieces."
                    )
                )
            ),
            CourseUnit(
                id = "chess_u4",
                title = "Unit 4: Master Endgame Technique (2100–2400 ELO)",
                description = "Convert small advantages with surgical precision in King & Pawn and Rook endgames.",
                lessons = listOf(
                    LessonSummary(
                        id = "chess_l4_1",
                        title = "4.1 King & Pawn Endgames: Opposition, Key Squares & Triangulation",
                        summary = "Calculating distant opposition, the Rule of the Square, and losing a move via triangulation."
                    ),
                    LessonSummary(
                        id = "chess_l4_2",
                        title = "4.2 Essential Rook Endgames: Lucena Bridge & Philidor Defense",
                        summary = "The two most important theoretical positions every Master knows by heart."
                    ),
                    LessonSummary(
                        id = "chess_l4_3",
                        title = "4.3 Opposite-Colored Bishops & Fortress Mechanisms",
                        summary = "When 2 extra pawns still draw — and how to break through an endgame fortress."
                    )
                )
            ),
            CourseUnit(
                id = "chess_u5",
                title = "Unit 5: Grandmaster Calculation & Prophylaxis (2400–2700+ GM)",
                description = "Think like a Grandmaster: Kotov's candidate moves, Petrosian/Karpov prophylaxis, and Carlsen squeeze.",
                lessons = listOf(
                    LessonSummary(
                        id = "chess_l5_1",
                        title = "5.1 Kotov's Tree of Analysis & Candidate Move Discipline",
                        summary = "Structuring deep calculation without re-checking lines: checks, captures, and threats first."
                    ),
                    LessonSummary(
                        id = "chess_l5_2",
                        title = "5.2 Prophylactic Thinking & The Positional Exchange Sacrifice (Rxc3!)",
                        summary = "Neutralizing the opponent's plan before it begins and sacrificing a Rook for a minor piece + pawn structure."
                    ),
                    LessonSummary(
                        id = "chess_l5_3",
                        title = "5.3 Grandmaster Practical Conversion & Time-Trouble Psychology",
                        summary = "Maximizing practical winning chances, maintaining tension, and converting advantages cleanly."
                    )
                )
            )
        )

        return Course(
            id = CHESS_GM_COURSE_ID,
            title = "Novice to Grandmaster (GM) Chess Mastery",
            level = "Novice to GM (600–2600+ ELO)",
            goal = "Master tactical calculation, positional pawn structures, theoretical endgames, and Grandmaster strategy",
            description = "Guided by Vex and Edam, this structured 5-unit academy takes you from fundamental opening principles and tactical forks all the way to Grandmaster prophylaxis, exchange sacrifices, and Lucena/Philidor endgame mastery.",
            outcomes = listOf(
                "Spot 3- to 5-move tactical combinations (Forks, Pins, Greek Gift, Back-Rank Mates) under time pressure",
                "Formulate clear middlegame plans based on pawn structures (IQP, Carlsbad minority attack, closed chains)",
                "Convert theoretical King & Pawn and Rook endgames (Opposition, Triangulation, Lucena Bridge, Philidor Defense)",
                "Apply Grandmaster candidate-move discipline and prophylactic thinking in every position"
            ),
            units = units,
            completed = emptyList()
        )
    }

    fun getOfflineChessLessonContent(lessonId: String, lessonTitle: String): LessonContent {
        return LessonContent(
            title = lessonTitle,
            objective = "Guided by Vex (Edam's Grandmaster Chess Tactician), this lesson builds concrete board vision, forcing-move calculation, and strategic pattern recognition.",
            sections = listOf(
                LearningSection(
                    heading = "1. Core Grandmaster Principle",
                    text = "Every strong move in chess serves dual purposes: improving your least active piece while restricting your opponent's best counterplay. In $lessonTitle, focus on identifying undefended pieces, weak color complexes, and forcing continuations before committing your move.",
                    examples = listOf(
                        "Step 1: Scan all checks, captures, and direct tactical threats for both sides.",
                        "Step 2: Identify the opponent's idea ('If it were their move right now, what would they play?').",
                        "Step 3: Select candidate moves and calculate each branch once with full concentration."
                    )
                ),
                LearningSection(
                    heading = "2. Model Board Pattern & Annotated Continuation",
                    text = "Consider the classic central setup: White controls e4 and d5 with active minor pieces while Black lags in development. By opening files before the enemy King castles (or sacrificing on h7/f7 when defenders are absent), temporary material investment converts into an unstoppable mating attack or decisive endgame transition.",
                    examples = listOf(
                        "Open Center = Favor Bishops & Rooks; Closed Center = Maneuver Knights to outposts.",
                        "Rook Endgames: Always place your Rook behind passed pawns (Tarrasch Rule).",
                        "Prophylaxis: Stop your opponent's only active plan before launching your own assault."
                    )
                )
            ),
            practice = listOf(
                PracticeQuestion(
                    question = "In Grandmaster calculation discipline (Kotov's method), what is the correct order for evaluating forcing moves?",
                    options = listOf(
                        "Checks first, then Captures, then Threats (CCT)",
                        "Passive pawn moves first, then King retreats",
                        "Only calculate moves on the queenside",
                        "Move the Queen on every turn"
                    ),
                    answerIndex = 0,
                    explanation = "Calculating Checks, Captures, and Threats (CCT) first narrows the opponent's replies and reveals forcing tactical wins immediately."
                ),
                PracticeQuestion(
                    question = "According to the Tarrasch Rule in Rook endgames, where does your Rook belong relative to a passed pawn?",
                    options = listOf(
                        "Directly in front of your own passed pawn",
                        "Behind the passed pawn (whether it is yours or the opponent's)",
                        "On the a1 corner square passively",
                        "Traded off immediately regardless of pawn structure"
                    ),
                    answerIndex = 1,
                    explanation = "A Rook behind a passed pawn gains scope as the pawn advances, whereas a Rook in front of it becomes increasingly cramped."
                ),
                PracticeQuestion(
                    question = "What is 'Prophylaxis' in high-level positional chess?",
                    options = listOf(
                        "Sacrificing the Queen on move 2",
                        "Anticipating and neutralizing the opponent's plan before executing your own",
                        "Moving only pawns for the first 12 moves",
                        "Refusing to castle in open games"
                    ),
                    answerIndex = 1,
                    explanation = "Pioneered by Nimzowitsch, Petrosian, and Karpov, prophylaxis prevents the opponent's counterplay at its root."
                )
            ),
            summary = "Always evaluate forcing moves first (Checks, Captures, Threats), keep your pieces coordinated on active squares, and activate your King immediately in the endgame."
        )
    }
}
