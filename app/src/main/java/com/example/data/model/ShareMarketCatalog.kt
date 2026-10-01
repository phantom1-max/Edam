package com.example.data.model

object ShareMarketCatalog {

    const val SHARE_MARKET_COURSE_ID = "course_share_market_mastery"

    fun createShareMarketCourse(completedIds: List<String> = emptyList()): Course {
        return Course(
            id = SHARE_MARKET_COURSE_ID,
            title = "Share Market & Equity Investing Mastery",
            level = "Intermediate",
            goal = "Understand stock exchanges, fundamental & technical analysis, valuation multiples, and portfolio risk management",
            description = "A complete, offline-ready curriculum covering how stock markets operate, how to analyze company financial statements, read price charts, and build a resilient long-term investment portfolio.",
            outcomes = listOf(
                "Understand how primary IPOs, secondary stock exchanges, and market indices operate",
                "Analyze Balance Sheets, Income Statements, Cash Flows, P/E, ROE, and Free Cash Flow",
                "Interpret candlestick patterns, support/resistance zones, moving averages, and RSI",
                "Manage portfolio risk with position sizing, stop-loss discipline, and asset allocation"
            ),
            units = listOf(
                CourseUnit(
                    id = "sm_unit_1",
                    title = "Foundations of the Share Market",
                    description = "How equities, exchanges, order books, and benchmark indices work in the real economy.",
                    lessons = listOf(
                        LessonSummary(
                            id = "sm_u1_l1",
                            title = "How Stock Exchanges & Order Books Work",
                            summary = "Explore bid-ask spreads, market vs. limit orders, liquidity, and clearing houses."
                        ),
                        LessonSummary(
                            id = "sm_u1_l2",
                            title = "IPOs, Market Cap & Benchmark Indices",
                            summary = "Learn how companies go public and how indices like S&P 500 and NIFTY 50 track the market."
                        ),
                        LessonSummary(
                            id = "sm_u1_l3",
                            title = "Bull vs. Bear Cycles & Market Participants",
                            summary = "Distinguish retail investors, institutional funds, market makers, and economic cycles."
                        )
                    )
                ),
                CourseUnit(
                    id = "sm_unit_2",
                    title = "Fundamental Analysis & Valuation",
                    description = "Evaluate a business's intrinsic value using financial statements and valuation ratios.",
                    lessons = listOf(
                        LessonSummary(
                            id = "sm_u2_l1",
                            title = "Reading the Income Statement & Balance Sheet",
                            summary = "Revenue, operating margins, debt-to-equity, and working capital explained clearly."
                        ),
                        LessonSummary(
                            id = "sm_u2_l2",
                            title = "Valuation Multiples: P/E, PEG, P/B & EV/EBITDA",
                            summary = "Compare stock prices against earnings growth and enterprise value across sectors."
                        ),
                        LessonSummary(
                            id = "sm_u2_l3",
                            title = "Free Cash Flow, ROE & Economic Moats",
                            summary = "Why cash flow matters more than paper profits and how durable competitive advantages compound."
                        )
                    )
                ),
                CourseUnit(
                    id = "sm_unit_3",
                    title = "Technical Analysis & Price Action",
                    description = "Read market supply and demand through price structure, volume, and momentum indicators.",
                    lessons = listOf(
                        LessonSummary(
                            id = "sm_u3_l1",
                            title = "Candlestick Anatomy & Volume Confirmation",
                            summary = "Open, high, low, close (OHLC) bars, engulfing patterns, and institutional volume footprints."
                        ),
                        LessonSummary(
                            id = "sm_u3_l2",
                            title = "Support, Resistance & Moving Averages",
                            summary = "Identify key demand/supply zones and trend structure with 50-day and 200-day EMAs."
                        ),
                        LessonSummary(
                            id = "sm_u3_l3",
                            title = "Momentum Indicators: RSI & MACD",
                            summary = "Spot overbought/oversold conditions, momentum divergences, and trend crossovers."
                        )
                    )
                ),
                CourseUnit(
                    id = "sm_unit_4",
                    title = "Risk Management & Portfolio Construction",
                    description = "Protect capital against drawdowns and compound wealth systematically.",
                    lessons = listOf(
                        LessonSummary(
                            id = "sm_u4_l1",
                            title = "Position Sizing & The 1–2% Risk Rule",
                            summary = "Calculate exact share quantities based on stop-loss distance and total account equity."
                        ),
                        LessonSummary(
                            id = "sm_u4_l2",
                            title = "Diversification, Index ETFs & Rebalancing",
                            summary = "Balance core index ETFs with high-conviction equities and periodic portfolio rebalancing."
                        ),
                        LessonSummary(
                            id = "sm_u4_l3",
                            title = "Behavioral Finance & Investor Psychology",
                            summary = "Overcome FOMO, loss aversion, confirmation bias, and panic selling during volatility."
                        )
                    )
                )
            ),
            completed = completedIds
        )
    }

    fun getPrebuiltShareMarketLessons(): Map<String, LessonContent> {
        val course = createShareMarketCourse()
        val map = mutableMapOf<String, LessonContent>()
        course.units.forEach { unit ->
            unit.lessons.forEach { lesson ->
                map[lesson.id] = buildOfflineLessonFor(course, unit, lesson)
            }
        }
        return map
    }

    /**
     * Builds a comprehensive, offline-ready LessonContent for any lesson in the Share Market course
     * or any custom course so that once a course is acquired, 100% of its lessons and quizzes work offline.
     */
    fun buildOfflineLessonFor(
        course: Course,
        unit: CourseUnit,
        lesson: LessonSummary
    ): LessonContent {
        return when (lesson.id) {
            "sm_u1_l1" -> LessonContent(
                title = lesson.title,
                objective = "Master how stock exchanges match buyers and sellers using electronic order books, bid-ask spreads, and order types.",
                sections = listOf(
                    LearningSection(
                        heading = "1. The Electronic Order Book & Bid-Ask Spread",
                        text = "Modern stock exchanges operate as continuous double auctions. Every stock has a live Order Book showing 'Bids' (buyers stating the highest price they are willing to pay) and 'Asks' or 'Offers' (sellers stating the lowest price they will accept). The gap between the highest bid and lowest ask is the Bid-Ask Spread—a direct measure of market liquidity.",
                        examples = listOf(
                            "Highly liquid stock: Highest Bid = $150.00, Lowest Ask = $150.02 -> Spread is just $0.02.",
                            "Thinly traded small-cap stock: Highest Bid = $12.10, Lowest Ask = $12.60 -> A $0.50 spread (4% implicit transaction cost)."
                        )
                    ),
                    LearningSection(
                        heading = "2. Market Orders vs. Limit Orders",
                        text = "A Market Order instructs your broker to execute immediately at the best available current price. It guarantees execution speed but not exact price. A Limit Order sets the maximum price you will pay (when buying) or minimum price you will accept (when selling), guaranteeing price control while waiting in the order queue.",
                        examples = listOf(
                            "Limit Buy Example: Stock trades at $205, but you place a Limit Buy at $200. Your order only fills if price drops to $200 or lower.",
                            "Slippage Warning: Placing a large Market Order during volatile earnings announcements can fill across multiple price levels higher than expected."
                        )
                    ),
                    LearningSection(
                        heading = "3. Trade Settlement & Clearing Houses",
                        text = "When your order matches on the exchange, the trade is executed immediately, but legal transfer of shares and cash happens through a Clearing House on a T+1 (Trade Date plus 1 business day) settlement cycle. This eliminates counterparty default risk.",
                        examples = listOf(
                            "T+1 Settlement: If you sell shares on Monday, settled cash is officially recorded in your depository account on Tuesday."
                        )
                    )
                ),
                practice = listOf(
                    PracticeQuestion(
                        question = "What does the 'Bid-Ask Spread' represent in a stock's order book?",
                        options = listOf(
                            "The difference between a stock's 52-week high and 52-week low",
                            "The difference between the highest price a buyer will pay and the lowest price a seller will accept",
                            "The brokerage commission charged per year",
                            "The dividend yield paid by the company"
                        ),
                        answerIndex = 1,
                        explanation = "The bid-ask spread is the gap between the highest bid (buyer offer) and lowest ask (seller offer) in the live order book."
                    ),
                    PracticeQuestion(
                        question = "Which order type guarantees that you will NOT pay more than your specified price when buying a stock?",
                        options = listOf(
                            "Market Order",
                            "Limit Buy Order",
                            "Day Market Sweep",
                            "Unpriced At-Open Order"
                        ),
                        answerIndex = 1,
                        explanation = "A Limit Buy Order specifies the strict maximum price you are willing to pay per share."
                    ),
                    PracticeQuestion(
                        question = "Why can a Market Order experience 'slippage' in a thinly traded stock?",
                        options = listOf(
                            "Because market orders expire after 5 seconds",
                            "Because it consumes available shares across progressively worse price levels in the order book",
                            "Because limit orders are prohibited on exchanges",
                            "Because clearing houses reject small-cap stocks"
                        ),
                        answerIndex = 1,
                        explanation = "When order book depth is thin, a market order fills against successively higher ask prices until the full quantity is completed."
                    ),
                    PracticeQuestion(
                        question = "What is the primary role of the Clearing House in a T+1 settlement system?",
                        options = listOf(
                            "To set the company's quarterly revenue targets",
                            "To guarantee trade completion and transfer shares and funds safely between buyer and seller",
                            "To recommend which stocks retail investors should buy",
                            "To prevent stock prices from falling"
                        ),
                        answerIndex = 1,
                        explanation = "Clearing houses act as the central counterparty to ensure shares and cash are transferred without counterparty default."
                    )
                ),
                summary = "Exchanges match buyers and sellers via the order book. Use Limit Orders to control execution price, monitor the Bid-Ask Spread to gauge liquidity, and rely on T+1 clearing for safe settlement."
            )

            "sm_u1_l2" -> LessonContent(
                title = lesson.title,
                objective = "Understand why companies issue shares in an IPO, how Market Capitalization is calculated, and how benchmark indices work.",
                sections = listOf(
                    LearningSection(
                        heading = "1. Primary Market (IPO) vs. Secondary Market",
                        text = "In an Initial Public Offering (IPO), a private company issues new shares to raise growth capital directly from investors (Primary Market). Once listed on an exchange, investors trade those shares among themselves in the Secondary Market without the company issuing new shares on each trade.",
                        examples = listOf(
                            "Primary Market: Company issues 10 million new shares at $25/share to raise $250M for R&D and factory expansion.",
                            "Secondary Market: You buy 50 shares on the NYSE or NSE from another investor at the current market price."
                        )
                    ),
                    LearningSection(
                        heading = "2. Share Price vs. Market Capitalization",
                        text = "A stock's price per share alone tells you nothing about how large or expensive a company is. Market Capitalization (Market Cap) equals Total Outstanding Shares multiplied by Current Share Price.",
                        examples = listOf(
                            "Company A: 10 million shares at $100 = $1 Billion Market Cap.",
                            "Company B: 1 billion shares at $20 = $20 Billion Market Cap (Company B is 20x larger despite a lower share price!)."
                        )
                    ),
                    LearningSection(
                        heading = "3. Free-Float Market Cap Indices",
                        text = "Benchmark indices like the S&P 500, NASDAQ-100, and NIFTY 50 weight companies by their Free-Float Market Cap (shares publicly available to trade, excluding locked-in promoter or government holdings).",
                        examples = listOf(
                            "If a $2 Trillion tech giant moves 3%, it impacts the index far more than a $15 Billion component moving 3%."
                        )
                    )
                ),
                practice = listOf(
                    PracticeQuestion(
                        question = "How is a company's Market Capitalization calculated?",
                        options = listOf(
                            "Share Price divided by Annual Dividend",
                            "Total Outstanding Shares multiplied by Current Share Price",
                            "Total Debt minus Cash Reserves",
                            "Share Price multiplied by Daily Trading Volume"
                        ),
                        answerIndex = 1,
                        explanation = "Market Cap = Total Outstanding Shares × Current Share Price, representing the total market value of a company's equity."
                    ),
                    PracticeQuestion(
                        question = "Company X trades at $500 with 1 million shares. Company Y trades at $50 with 100 million shares. Which company has the larger Market Cap?",
                        options = listOf(
                            "Company X ($500M) is larger than Company Y",
                            "Company Y ($5B) is 10 times larger than Company X ($500M)",
                            "Both have the exact same Market Cap",
                            "Cannot be determined without knowing the IPO date"
                        ),
                        answerIndex = 1,
                        explanation = "Company X is worth $500M ($500 × 1M), whereas Company Y is worth $5 Billion ($50 × 100M)."
                    ),
                    PracticeQuestion(
                        question = "When does the issuing company directly receive capital from share sales?",
                        options = listOf(
                            "Every time a retail trader buys shares on the secondary exchange",
                            "During the Primary Market IPO or a follow-on share issuance",
                            "Only when the stock price hits an all-time high",
                            "Whenever a short seller closes a position"
                        ),
                        answerIndex = 1,
                        explanation = "Companies raise capital in the primary market (IPOs / follow-on offerings); secondary market trades transfer ownership between investors."
                    ),
                    PracticeQuestion(
                        question = "What does 'Free-Float' mean in index weighting?",
                        options = listOf(
                            "Shares that pay zero brokerage fees",
                            "Only the shares readily available for public trading, excluding locked-in insider/government stakes",
                            "Companies with zero long-term debt",
                            "Stocks whose prices do not have daily circuit limits"
                        ),
                        answerIndex = 1,
                        explanation = "Free-float market cap counts publicly tradable shares and excludes locked-in founder or government holdings."
                    )
                ),
                summary = "Never judge a company's size by share price alone—always calculate Market Cap (Shares × Price). Benchmark indices track the broader economy using free-float market capitalization."
            )

            "sm_u2_l2" -> LessonContent(
                title = lesson.title,
                objective = "Learn how to value stocks using Price-to-Earnings (P/E), PEG Ratio, Price-to-Book (P/B), and Enterprise Value multiples.",
                sections = listOf(
                    LearningSection(
                        heading = "1. Price-to-Earnings (P/E) Ratio",
                        text = "The P/E ratio measures how much investors are willing to pay per dollar of a company's net earnings: P/E = Share Price ÷ Earnings Per Share (EPS). A high P/E often signals high expected future growth, while a low P/E may indicate a mature cyclical business or an undervalued stock.",
                        examples = listOf(
                            "Stock price = $120, Annual EPS = $6.00 -> P/E = 20x (investors pay $20 for every $1 of annual profit).",
                            "Always compare P/E within the same industry: software companies often trade at 30x+ P/E, while banks and utilities often trade at 10x–15x."
                        )
                    ),
                    LearningSection(
                        heading = "2. The PEG Ratio (Factoring in Growth)",
                        text = "The PEG Ratio divides the P/E ratio by the expected annual EPS growth rate: PEG = (P/E) ÷ Annual EPS Growth %. A PEG around 1.0 or lower often suggests a reasonable valuation relative to its growth speed.",
                        examples = listOf(
                            "Company A: P/E of 30 growing at 30% per year -> PEG = 1.0.",
                            "Company B: P/E of 18 growing at only 6% per year -> PEG = 3.0 (more expensive relative to growth!)."
                        )
                    ),
                    LearningSection(
                        heading = "3. Price-to-Book (P/B) & EV/EBITDA",
                        text = "Price-to-Book (P/B) compares market value to net assets (Book Value = Assets − Liabilities) and is especially useful for banks and asset-heavy firms. EV/EBITDA includes debt and cash (Enterprise Value = Market Cap + Debt − Cash), making it ideal for comparing companies with different debt levels.",
                        examples = listOf(
                            "If two telecom firms have the same P/E of 14x, but one carries $15B in debt and the other has $5B net cash, EV/EBITDA reveals the debt-free firm is much cheaper."
                        )
                    )
                ),
                practice = listOf(
                    PracticeQuestion(
                        question = "If a company's stock trades at $90 and its Earnings Per Share (EPS) over the last 12 months is $4.50, what is its P/E ratio?",
                        options = listOf(
                            "4.5x",
                            "15x",
                            "20x",
                            "40.5x"
                        ),
                        answerIndex = 2,
                        explanation = "P/E = Share Price ($90) ÷ EPS ($4.50) = 20x."
                    ),
                    PracticeQuestion(
                        question = "Why is the PEG ratio often more informative than the raw P/E ratio for growth stocks?",
                        options = listOf(
                            "It ignores earnings completely",
                            "It compares the P/E multiple directly against the company's earnings growth rate",
                            "It only looks at dividend payouts",
                            "It replaces market cap with daily trading volume"
                        ),
                        answerIndex = 1,
                        explanation = "PEG divides P/E by the annual EPS growth rate, showing whether a higher multiple is justified by faster growth."
                    ),
                    PracticeQuestion(
                        question = "Which valuation metric accounts for a company's debt and cash balance in addition to its equity market cap?",
                        options = listOf(
                            "Dividend Payout Ratio",
                            "EV/EBITDA (Enterprise Value to EBITDA)",
                            "Trailing P/E Ratio",
                            "Gross Profit Margin"
                        ),
                        answerIndex = 1,
                        explanation = "Enterprise Value (EV) = Market Cap + Total Debt − Cash, so EV/EBITDA neutralizes capital structure differences."
                    ),
                    PracticeQuestion(
                        question = "For which sector is Price-to-Book (P/B) most commonly used as a primary valuation benchmark?",
                        options = listOf(
                            "Banking, insurance, and financial institutions",
                            "Early-stage pre-revenue biotech startups",
                            "Cloud software subscriptions",
                            "Digital advertising agencies"
                        ),
                        answerIndex = 0,
                        explanation = "Banks and financial institutions hold financial assets and liabilities marked near book value, making P/B a core valuation metric."
                    )
                ),
                summary = "Combine P/E with growth (PEG), check balance sheet leverage via EV/EBITDA, and always compare valuation multiples against sector peers rather than in isolation."
            )

            else -> generateStructuredOfflineLesson(course, unit, lesson)
        }
    }

    private fun generateStructuredOfflineLesson(
        course: Course,
        unit: CourseUnit,
        lesson: LessonSummary
    ): LessonContent {
        val topicTitle = lesson.title
        val summaryHint = lesson.summary.ifBlank { "Core principles and practical application of $topicTitle." }
        return LessonContent(
            title = topicTitle,
            objective = "Master the core concepts of $topicTitle within ${unit.title} (${course.level} level) and apply them to real-world scenarios.",
            sections = listOf(
                LearningSection(
                    heading = "1. Core Principles of $topicTitle",
                    text = "$summaryHint Understanding this topic builds a strong foundation for ${course.goal}. In ${unit.title}, we focus on clear cause-and-effect relationships, structured frameworks, and measurable signals rather than guesswork.",
                    examples = listOf(
                        "Practical Scenario: Break down $topicTitle into observable metrics before making a decision.",
                        "Benchmark Rule: Compare baseline performance against historical norms to filter out short-term noise."
                    )
                ),
                LearningSection(
                    heading = "2. Step-by-Step Analytical Framework",
                    text = "To apply $topicTitle effectively at the ${course.level} level, follow a repeatable three-step workflow: (1) Identify the primary objective and constraints, (2) Evaluate key indicators using objective data, and (3) Verify risk-reward balance before taking action.",
                    examples = listOf(
                        "Step 1 — Context Check: Align your analysis of $topicTitle with the broader structure of ${unit.title}.",
                        "Step 2 — Risk Filter: Always define what evidence would invalidate your hypothesis before committing resources."
                    )
                ),
                LearningSection(
                    heading = "3. Common Pitfalls & Best Practices",
                    text = "Even experienced practitioners make mistakes when they rely on a single indicator in isolation. Combining structural understanding of $topicTitle with disciplined execution ensures consistent long-term results.",
                    examples = listOf(
                        "Avoid Confirmation Bias: Look at both supporting and contradictory signals when reviewing $topicTitle.",
                        "Consistency over Impulse: Document your checklist criteria in advance."
                    )
                )
            ),
            practice = listOf(
                PracticeQuestion(
                    question = "What is the primary goal of studying '$topicTitle' in ${unit.title}?",
                    options = listOf(
                        "To memorize isolated terms without real-world context",
                        "To apply structured principles of $topicTitle toward ${course.goal}",
                        "To rely on random speculation without checking fundamentals",
                        "To skip risk evaluation altogether"
                    ),
                    answerIndex = 1,
                    explanation = "Every lesson in ${course.title} is designed to build practical mastery toward ${course.goal}."
                ),
                PracticeQuestion(
                    question = "In the three-step analytical framework for '$topicTitle', what should you do before taking action?",
                    options = listOf(
                        "Verify the risk-reward balance and define what evidence would invalidate your thesis",
                        "Ignore conflicting data points",
                        "Follow emotional impulses during high volatility",
                        "Skip baseline comparisons"
                    ),
                    answerIndex = 0,
                    explanation = "Defining invalidation criteria and checking risk-reward protects against avoidable errors."
                ),
                PracticeQuestion(
                    question = "Why is it important NOT to rely on a single metric in isolation when working with '$topicTitle'?",
                    options = listOf(
                        "Because single metrics are always illegal",
                        "Because combining multiple objective signals reduces false positives and confirmation bias",
                        "Because data analysis has no value in ${unit.title}",
                        "Because checklists slow down learning"
                    ),
                    answerIndex = 1,
                    explanation = "Cross-checking complementary indicators filters out noise and prevents confirmation bias."
                ),
                PracticeQuestion(
                    question = "Which habit best supports long-term mastery of '${unit.title}'?",
                    options = listOf(
                        "Using a written, repeatable checklist and reviewing outcomes objectively",
                        "Changing your strategy after every single outcome",
                        "Avoiding practice questions",
                        "Making decisions without reviewing ${lesson.title}"
                    ),
                    answerIndex = 0,
                    explanation = "A repeatable checklist keeps execution disciplined and measurable over time."
                )
            ),
            summary = "In this lesson on $topicTitle, you learned the core principles, the 3-step analytical workflow, and how to avoid common pitfalls while working toward ${course.goal}."
        )
    }
}
