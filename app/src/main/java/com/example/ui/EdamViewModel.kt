package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.DailyStreakManager
import com.example.data.local.DailyStreakState
import com.example.data.local.EarnedBadgeEntity
import com.example.data.local.EdamDatabase
import com.example.data.model.BadgeCatalog
import com.example.data.model.BadgeDisplayItem
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.model.MarketChartType
import com.example.data.model.MarketFilterTab
import com.example.data.model.MarketGlobalSummary
import com.example.data.model.MarketTimeframe
import com.example.data.model.PlanTier
import com.example.data.model.ShareMarketCatalog
import com.example.data.model.TrendingCourseTicker
import com.example.data.model.TrendingMarketEngine
import com.example.data.remote.EdamCloudRepository
import com.example.data.repository.EdamRepository
import com.example.notification.DailyGoalReminderScheduler
import com.example.notification.EdamNotificationHelper
import com.example.ui.theme.EdamThemeMode
import androidx.compose.ui.graphics.Color
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.ScreenEntity
import com.example.data.model.CourseOutline
import com.example.data.model.CourseOutlinePresets
import com.example.data.model.LeaderboardCompetitor
import com.example.data.model.LeaderboardDrillCatalog
import com.example.data.model.LeaderboardZone
import com.example.data.model.LeagueTier
import com.example.data.model.QuickTopicPreset
import com.example.data.model.SpeedDrillQuestion
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal val Context.edamDataStore by preferencesDataStore(name = "edam_settings")

object EdamDailyGoalDataStoreKeys {
    val DAILY_LESSON_GOAL = intPreferencesKey("daily_lesson_goal")
    val DAILY_LESSONS_COMPLETED_TODAY = intPreferencesKey("daily_lessons_completed_today")
    val DAILY_LESSONS_EPOCH_DAY = longPreferencesKey("daily_lessons_epoch_day")
    val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_goal_reminder_enabled")
    val DAILY_REMINDER_HOUR = intPreferencesKey("daily_goal_reminder_hour")
    val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_goal_reminder_minute")
}

data class LessonModalState(
    val courseId: String,
    val unit: CourseUnit,
    val lessonSummary: LessonSummary,
    val isLoading: Boolean = true,
    val lessonContent: LessonContent? = null,
    val errorMessage: String? = null,
    val selectedAnswers: Map<Int, Int> = emptyMap()
)

data class MarketDashboardState(
    val tickers: List<TrendingCourseTicker> = TrendingMarketEngine.createInitialTickers(),
    val selectedTickerId: String = "market_shmkt",
    val timeframe: MarketTimeframe = MarketTimeframe.ONE_DAY,
    val chartType: MarketChartType = MarketChartType.AREA_LINE,
    val filterTab: MarketFilterTab = MarketFilterTab.ALL,
    val searchQuery: String = "",
    val bookmarkedTickerIds: Set<String> = setOf("market_shmkt"),
    val isLiveFeedActive: Boolean = true
)

data class EdamFormState(
    val courseName: String = "",
    val level: String = "",
    val goal: String = "",
    val isGeneratingCourse: Boolean = false,
    val courseCreationStageIndex: Int = 0,
    val courseCreationProgress: Float = 0f,
    val statusMessage: String = "",
    val showSavedCoursesSheet: Boolean = false,
    val showSettingsModal: Boolean = false,
    val showProfileModal: Boolean = false,
    val promoCodeInput: String = "",
    val promoStatusMessage: String = "",
    val lessonModalState: LessonModalState? = null
)

data class LocalPrefsState(
    val themeMode: EdamThemeMode = EdamThemeMode.SYSTEM_DEFAULT,
    val planTier: PlanTier = PlanTier.BASIC,
    val promoCodeUsed: String = "",
    val lastCourseId: String = "",
    val lastUnitId: String = "",
    val lastLessonId: String = "",
    val quizCheckpointsJson: String = "{}",
    val pushNotificationsEnabled: Boolean = true,
    val dailyLessonGoal: Int = 3,
    val dailyLessonsCompletedToday: Int = 1,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0
)

enum class AppScreenDestination(
    val route: String,
    val title: String,
    val screenEntityId: String
) {
    HOME("home", "Course Dashboard", "screen_home"),
    COURSE_STUDIO("course_studio", "AI Course Studio", "screen_course_studio"),
    LEADERBOARD("leaderboard", "Duolingo Leagues", "screen_leaderboard"),
    MARKET("market", "Trending Market", "screen_market"),
    PROFILE("profile", "Profile & Badges", "screen_profile")
}

data class OutlineStudioState(
    val topic: String = "",
    val level: String = "Intermediate",
    val goal: String = "",
    val focusArea: String = "Applied Strategy",
    val unitCount: Int = 5,
    val isGenerating: Boolean = false,
    val statusMessage: String = "",
    val generationStep: String = "",
    val generatedOutline: CourseOutline? = null,
    val errorMessage: String? = null
)

data class DuolingoLeaderboardState(
    val selectedLeague: LeagueTier = LeagueTier.SAPPHIRE,
    val isDrillModalVisible: Boolean = false,
    val currentDrillQuestion: SpeedDrillQuestion? = null,
    val drillSelectedOption: Int? = null,
    val drillFeedbackMessage: String? = null,
    val isDrillAnswerCorrect: Boolean? = null,
    val streakBonusClaimedToday: Boolean = false
)

data class LeaderboardComposite(
    val selectedLeague: LeagueTier,
    val competitors: List<LeaderboardCompetitor>,
    val currentUserRank: Int,
    val currentUserXp: Int,
    val drillState: DuolingoLeaderboardState
)

data class ResumeCheckpointInfo(
    val courseId: String,
    val courseTitle: String,
    val unitId: String,
    val unitTitle: String,
    val lessonId: String,
    val lessonTitle: String,
    val lessonSummary: String,
    val savedAnswersCount: Int,
    val isCompleted: Boolean
)

data class EdamUiState(
    val currentDestination: AppScreenDestination = AppScreenDestination.HOME,
    val allScreens: List<ScreenEntity> = emptyList(),
    val courseName: String = "",
    val level: String = "",
    val goal: String = "",
    val isGeneratingCourse: Boolean = false,
    val courseCreationStageIndex: Int = 0,
    val courseCreationProgress: Float = 0f,
    val statusMessage: String = "",
    val activeCourse: Course? = null,
    val cachedLessonsCount: Int = 0,
    val savedCourses: List<Course> = emptyList(),
    val showSavedCoursesSheet: Boolean = false,
    val showSettingsModal: Boolean = false,
    val showProfileModal: Boolean = false,
    val themeMode: EdamThemeMode = EdamThemeMode.SYSTEM_DEFAULT,
    val planTier: PlanTier = PlanTier.BASIC,
    val promoCodeUsed: String = "",
    val promoCodeInput: String = "",
    val promoStatusMessage: String = "",
    val userDisplayName: String = "",
    val userEmail: String = "",
    val lessonModalState: LessonModalState? = null,
    val resumeCheckpoint: ResumeCheckpointInfo? = null,
    val marketTickers: List<TrendingCourseTicker> = TrendingMarketEngine.createInitialTickers(),
    val selectedTickerId: String = "market_shmkt",
    val marketTimeframe: MarketTimeframe = MarketTimeframe.ONE_DAY,
    val marketChartType: MarketChartType = MarketChartType.AREA_LINE,
    val marketFilterTab: MarketFilterTab = MarketFilterTab.ALL,
    val marketSearchQuery: String = "",
    val bookmarkedTickerIds: Set<String> = setOf("market_shmkt"),
    val isMarketLiveFeedActive: Boolean = true,
    val marketGlobalSummary: MarketGlobalSummary = MarketGlobalSummary(),
    val earnedBadges: List<EarnedBadgeEntity> = emptyList(),
    val badgeDisplayItems: List<BadgeDisplayItem> = emptyList(),
    val pushNotificationsEnabled: Boolean = true,
    val totalCompletedLessonsCount: Int = 0,
    val averageMasteryPercentage: Int = 0,
    // Course Outline Studio state
    val outlineTopic: String = "",
    val outlineLevel: String = "Intermediate",
    val outlineGoal: String = "",
    val outlineFocusArea: String = "Applied Strategy",
    val outlineUnitCount: Int = 5,
    val isGeneratingOutline: Boolean = false,
    val outlineStatusMessage: String = "",
    val outlineGenerationStep: String = "",
    val generatedOutline: CourseOutline? = null,
    val outlineErrorMessage: String? = null,
    // Duolingo Leaderboard state
    val selectedLeague: LeagueTier = LeagueTier.SAPPHIRE,
    val leaderboardEntries: List<LeaderboardCompetitor> = emptyList(),
    val currentUserRank: Int = 4,
    val currentUserWeeklyXp: Int = 1420,
    val isDrillModalVisible: Boolean = false,
    val currentDrillQuestion: SpeedDrillQuestion? = null,
    val drillSelectedOption: Int? = null,
    val drillFeedbackMessage: String? = null,
    val isDrillAnswerCorrect: Boolean? = null,
    val streakBonusClaimedToday: Boolean = false,
    val dailyStreak: DailyStreakState = DailyStreakState(),
    val selectedCompanion: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    val dailyLessonGoal: Int = 3,
    val dailyLessonsCompletedToday: Int = 1,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0
) {
    val dailyLessonGoalProgress: Float
        get() = (dailyLessonsCompletedToday.toFloat() / dailyLessonGoal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

@OptIn(ExperimentalCoroutinesApi::class)
class EdamViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val themePrefKey = stringPreferencesKey("theme_mode")
    private val planPrefKey = stringPreferencesKey("plan_tier")
    private val promoPrefKey = stringPreferencesKey("promo_code_used")
    private val lastCoursePrefKey = stringPreferencesKey("last_course_id")
    private val lastUnitPrefKey = stringPreferencesKey("last_unit_id")
    private val lastLessonPrefKey = stringPreferencesKey("last_lesson_id")
    private val quizCheckpointsPrefKey = stringPreferencesKey("quiz_checkpoints_json")
    private val pushNotificationsPrefKey = booleanPreferencesKey("push_notifications_enabled")
    private val dailyLessonGoalPrefKey = EdamDailyGoalDataStoreKeys.DAILY_LESSON_GOAL
    private val dailyLessonsCompletedTodayPrefKey = EdamDailyGoalDataStoreKeys.DAILY_LESSONS_COMPLETED_TODAY
    private val dailyLessonsEpochDayPrefKey = EdamDailyGoalDataStoreKeys.DAILY_LESSONS_EPOCH_DAY
    private val dailyReminderEnabledPrefKey = EdamDailyGoalDataStoreKeys.DAILY_REMINDER_ENABLED
    private val dailyReminderHourPrefKey = EdamDailyGoalDataStoreKeys.DAILY_REMINDER_HOUR
    private val dailyReminderMinutePrefKey = EdamDailyGoalDataStoreKeys.DAILY_REMINDER_MINUTE

    private val databaseId: String = application.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
    private val cloudRepository = EdamCloudRepository(firestore)

    private val repository: EdamRepository = EdamRepository(
        dao = EdamDatabase.getInstance(application).edamDao(),
        cloudRepository = cloudRepository
    )

    private val formState = MutableStateFlow(EdamFormState())
    private val marketState = MutableStateFlow(MarketDashboardState())
    private val dailyStreakManager = DailyStreakManager(appContext)
    private val selectedCompanionFlow = MutableStateFlow(EdamCompanionCharacter.EDAM)
    private var tickCounter = 1L

    private val localPrefsFlow = appContext.edamDataStore.data.map { prefs ->
        val todayEpoch = DailyStreakManager.currentEpochDay()
        val storedEpoch = prefs[dailyLessonsEpochDayPrefKey] ?: todayEpoch
        val completedToday = if (storedEpoch == todayEpoch) {
            (prefs[dailyLessonsCompletedTodayPrefKey] ?: 1).coerceAtLeast(0)
        } else {
            0
        }
        LocalPrefsState(
            themeMode = EdamThemeMode.fromName(prefs[themePrefKey]),
            planTier = PlanTier.fromId(prefs[planPrefKey]),
            promoCodeUsed = prefs[promoPrefKey] ?: "",
            lastCourseId = prefs[lastCoursePrefKey] ?: "",
            lastUnitId = prefs[lastUnitPrefKey] ?: "",
            lastLessonId = prefs[lastLessonPrefKey] ?: "",
            quizCheckpointsJson = prefs[quizCheckpointsPrefKey] ?: "{}",
            pushNotificationsEnabled = prefs[pushNotificationsPrefKey] ?: true,
            dailyLessonGoal = (prefs[dailyLessonGoalPrefKey] ?: 3).coerceIn(1, 10),
            dailyLessonsCompletedToday = completedToday,
            dailyReminderEnabled = prefs[dailyReminderEnabledPrefKey] ?: true,
            dailyReminderHour = (prefs[dailyReminderHourPrefKey] ?: 20).coerceIn(0, 23),
            dailyReminderMinute = (prefs[dailyReminderMinutePrefKey] ?: 0).coerceIn(0, 59)
        )
    }

    private val activeCourseWithOfflineCountFlow = combine(
        repository.activeCourseFlow,
        repository.allCoursesFlow
    ) { active, all ->
        val resolved = active ?: all.firstOrNull()
        Pair(resolved, all)
    }.flatMapLatest { (resolvedCourse, allCourses) ->
        if (resolvedCourse == null) {
            flowOf(Triple<Course?, Int, List<Course>>(null, 0, allCourses))
        } else {
            repository.observeCachedLessonCount(resolvedCourse.id).map { count ->
                Triple(resolvedCourse, count, allCourses)
            }
        }
    }

    private val currentDestinationFlow = MutableStateFlow(AppScreenDestination.HOME)
    private val outlineStudioStateFlow = MutableStateFlow(OutlineStudioState())
    private val selectedLeagueFlow = MutableStateFlow(LeagueTier.SAPPHIRE)
    private val duolingoDrillStateFlow = MutableStateFlow(DuolingoLeaderboardState())

    private val leaderboardCompositeFlow = combine(
        selectedLeagueFlow,
        duolingoDrillStateFlow
    ) { league, drill ->
        Pair(league, drill)
    }.flatMapLatest { (league, drill) ->
        repository.leaderboardFlow(league.tierId).map { entities ->
            val competitors = entities.mapIndexed { index, entity ->
                val rank = index + 1
                val zone = when {
                    rank <= 5 -> LeaderboardZone.PROMOTION
                    rank >= (entities.size - 4).coerceAtLeast(6) -> LeaderboardZone.DEMOTION
                    else -> LeaderboardZone.SAFE
                }
                LeaderboardCompetitor(
                    id = entity.userId,
                    name = entity.displayName,
                    avatarColor = Color(entity.avatarColorHex),
                    avatarInitial = entity.avatarInitial,
                    weeklyXp = entity.weeklyXp,
                    streakDays = entity.streakDays,
                    rank = rank,
                    isCurrentUser = entity.isCurrentUser,
                    zone = zone
                )
            }
            val userEntry = competitors.find { it.isCurrentUser }
            LeaderboardComposite(
                selectedLeague = league,
                competitors = competitors,
                currentUserRank = userEntry?.rank ?: 4,
                currentUserXp = userEntry?.weeklyXp ?: 1420,
                drillState = drill
            )
        }
    }

    private val baseUiStateFlow: Flow<EdamUiState> = combine(
        formState,
        activeCourseWithOfflineCountFlow,
        localPrefsFlow,
        marketState,
        repository.earnedBadgesFlow
    ) { form, (activeCourse, cachedCount, allCourses), prefs, market, earnedBadges ->
        val currentUser = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        }
        val mergedTickers = TrendingMarketEngine.syncWithUserCourses(
            currentTickers = market.tickers,
            userCourses = allCourses
        )
        val globalSummary = TrendingMarketEngine.computeGlobalSummary(mergedTickers)
        val resumeInfo = resolveResumeCheckpoint(activeCourse, prefs)

        val totalCompletedLessons = allCourses.sumOf { it.completedLessonsCount }
        val avgMastery = if (allCourses.isEmpty()) 0 else {
            (allCourses.sumOf { it.progressPercentage } / allCourses.size)
        }
        val streak = dailyStreakManager.loadStateFromPrefs()

        val badgeItems = BadgeCatalog.MILESTONE_BADGES.map { def ->
            val unlockedMatch = earnedBadges.find { it.badgeId == def.id }
            if (unlockedMatch != null) {
                BadgeDisplayItem(
                    definition = def,
                    isUnlocked = true,
                    currentProgressPct = 100,
                    unlockedAt = unlockedMatch.unlockedAt,
                    courseTitle = unlockedMatch.courseTitle,
                    isCloudSynced = true
                )
            } else {
                val (progress, autoUnlocked) = when (def.category) {
                    "STREAK" -> {
                        val currentStreak = streak.currentStreak
                        val target = if (def.id == "badge_streak_7") 7 else 3
                        val pct = ((currentStreak * 100) / target).coerceIn(0, 100)
                        Pair(pct, pct >= 100)
                    }
                    "CHESS" -> {
                        val count = streak.chessPuzzlesSolvedCount
                        val pct = if (count >= 1) 100 else 0
                        Pair(pct, count >= 1)
                    }
                    "MARKET" -> {
                        val shmktCourse = allCourses.find { it.id == ShareMarketCatalog.SHARE_MARKET_COURSE_ID }
                        val pct = shmktCourse?.progressPercentage ?: (if (def.id == "badge_market_analyst" && (activeCourse?.id == ShareMarketCatalog.SHARE_MARKET_COURSE_ID || mergedTickers.isNotEmpty())) 100 else 0)
                        Pair(pct, pct >= def.percentageRequired)
                    }
                    "CODING" -> {
                        val codingCourse = allCourses.find { it.title.contains("Code", ignoreCase = true) || it.title.contains("Python", ignoreCase = true) || it.title.contains("Virus", ignoreCase = true) }
                        val pct = codingCourse?.progressPercentage ?: 0
                        Pair(pct, pct >= def.percentageRequired)
                    }
                    "POLYMATH" -> {
                        val hasCustom = allCourses.any { it.id != ShareMarketCatalog.SHARE_MARKET_COURSE_ID && it.id != ChessAndFlashcardCatalog.CHESS_GM_COURSE_ID }
                        val pct = if (hasCustom) 100 else 0
                        Pair(pct, hasCustom)
                    }
                    else -> {
                        val highestProgress = if (def.specificCourseId != null) {
                            allCourses.find { it.id == def.specificCourseId }?.progressPercentage ?: 0
                        } else {
                            allCourses.maxOfOrNull { it.progressPercentage } ?: (activeCourse?.progressPercentage ?: 0)
                        }
                        Pair(highestProgress, highestProgress >= def.percentageRequired)
                    }
                }
                BadgeDisplayItem(
                    definition = def,
                    isUnlocked = autoUnlocked,
                    currentProgressPct = progress,
                    isCloudSynced = autoUnlocked
                )
            }
        }

        EdamUiState(
            courseName = form.courseName,
            level = form.level,
            goal = form.goal,
            isGeneratingCourse = form.isGeneratingCourse,
            courseCreationStageIndex = form.courseCreationStageIndex,
            courseCreationProgress = form.courseCreationProgress,
            statusMessage = form.statusMessage,
            activeCourse = activeCourse,
            cachedLessonsCount = cachedCount,
            savedCourses = allCourses,
            showSavedCoursesSheet = form.showSavedCoursesSheet,
            showSettingsModal = form.showSettingsModal,
            showProfileModal = form.showProfileModal,
            themeMode = prefs.themeMode,
            planTier = prefs.planTier,
            promoCodeUsed = prefs.promoCodeUsed,
            promoCodeInput = form.promoCodeInput,
            promoStatusMessage = form.promoStatusMessage,
            userDisplayName = currentUser?.displayName?.ifBlank { "Edam Learner" } ?: "Edam Learner",
            userEmail = currentUser?.email ?: "",
            lessonModalState = form.lessonModalState,
            resumeCheckpoint = resumeInfo,
            marketTickers = mergedTickers,
            selectedTickerId = market.selectedTickerId,
            marketTimeframe = market.timeframe,
            marketChartType = market.chartType,
            marketFilterTab = market.filterTab,
            marketSearchQuery = market.searchQuery,
            bookmarkedTickerIds = market.bookmarkedTickerIds,
            isMarketLiveFeedActive = market.isLiveFeedActive,
            marketGlobalSummary = globalSummary,
            earnedBadges = earnedBadges,
            badgeDisplayItems = badgeItems,
            pushNotificationsEnabled = prefs.pushNotificationsEnabled,
            totalCompletedLessonsCount = totalCompletedLessons,
            averageMasteryPercentage = avgMastery,
            dailyLessonGoal = prefs.dailyLessonGoal,
            dailyLessonsCompletedToday = prefs.dailyLessonsCompletedToday,
            dailyReminderEnabled = prefs.dailyReminderEnabled,
            dailyReminderHour = prefs.dailyReminderHour,
            dailyReminderMinute = prefs.dailyReminderMinute
        )
    }

    private val companionAndStreakFlow = combine(
        dailyStreakManager.streakState,
        selectedCompanionFlow
    ) { streak, companion ->
        Pair(streak, companion)
    }

    val uiState: StateFlow<EdamUiState> = combine(
        baseUiStateFlow,
        repository.screensFlow,
        currentDestinationFlow,
        outlineStudioStateFlow,
        leaderboardCompositeFlow
    ) { base, screens, destination, outline, lb ->
        base.copy(
            currentDestination = destination,
            allScreens = screens,
            outlineTopic = outline.topic,
            outlineLevel = outline.level,
            outlineGoal = outline.goal,
            outlineFocusArea = outline.focusArea,
            outlineUnitCount = outline.unitCount,
            isGeneratingOutline = outline.isGenerating,
            outlineStatusMessage = outline.statusMessage,
            outlineGenerationStep = outline.generationStep,
            generatedOutline = outline.generatedOutline,
            outlineErrorMessage = outline.errorMessage,
            selectedLeague = lb.selectedLeague,
            leaderboardEntries = lb.competitors,
            currentUserRank = lb.currentUserRank,
            currentUserWeeklyXp = lb.currentUserXp,
            isDrillModalVisible = lb.drillState.isDrillModalVisible,
            currentDrillQuestion = lb.drillState.currentDrillQuestion,
            drillSelectedOption = lb.drillState.drillSelectedOption,
            drillFeedbackMessage = lb.drillState.drillFeedbackMessage,
            isDrillAnswerCorrect = lb.drillState.isDrillAnswerCorrect,
            streakBonusClaimedToday = lb.drillState.streakBonusClaimedToday
        )
    }.combine(companionAndStreakFlow) { state, (streak, companion) ->
        state.copy(
            dailyStreak = streak,
            selectedCompanion = companion
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EdamUiState()
    )

    private fun resolveResumeCheckpoint(
        course: Course?,
        prefs: LocalPrefsState
    ): ResumeCheckpointInfo? {
        if (course == null || course.units.isEmpty()) return null

        // 1. Check if the saved checkpoint belongs to the active course
        if (prefs.lastCourseId == course.id && prefs.lastUnitId.isNotBlank() && prefs.lastLessonId.isNotBlank()) {
            val savedUnit = course.units.find { it.id == prefs.lastUnitId }
            val savedLesson = savedUnit?.lessons?.find { it.id == prefs.lastLessonId }
            if (savedUnit != null && savedLesson != null) {
                val savedAnswers = parseSavedQuizAnswers(prefs.quizCheckpointsJson, course.id, savedLesson.id)
                return ResumeCheckpointInfo(
                    courseId = course.id,
                    courseTitle = course.title,
                    unitId = savedUnit.id,
                    unitTitle = savedUnit.title,
                    lessonId = savedLesson.id,
                    lessonTitle = savedLesson.title,
                    lessonSummary = savedLesson.summary,
                    savedAnswersCount = savedAnswers.size,
                    isCompleted = course.completed.contains(savedLesson.id)
                )
            }
        }

        // 2. Otherwise pick the first incomplete lesson in the active course (or very first lesson if all complete)
        for (unit in course.units) {
            for (lesson in unit.lessons) {
                if (!course.completed.contains(lesson.id)) {
                    val savedAnswers = parseSavedQuizAnswers(prefs.quizCheckpointsJson, course.id, lesson.id)
                    return ResumeCheckpointInfo(
                        courseId = course.id,
                        courseTitle = course.title,
                        unitId = unit.id,
                        unitTitle = unit.title,
                        lessonId = lesson.id,
                        lessonTitle = lesson.title,
                        lessonSummary = lesson.summary,
                        savedAnswersCount = savedAnswers.size,
                        isCompleted = false
                    )
                }
            }
        }

        val firstUnit = course.units.first()
        val firstLesson = firstUnit.lessons.firstOrNull() ?: return null
        val savedAnswers = parseSavedQuizAnswers(prefs.quizCheckpointsJson, course.id, firstLesson.id)
        return ResumeCheckpointInfo(
            courseId = course.id,
            courseTitle = course.title,
            unitId = firstUnit.id,
            unitTitle = firstUnit.title,
            lessonId = firstLesson.id,
            lessonTitle = firstLesson.title,
            lessonSummary = firstLesson.summary,
            savedAnswersCount = savedAnswers.size,
            isCompleted = course.completed.contains(firstLesson.id)
        )
    }

    private fun parseSavedQuizAnswers(
        rawJson: String,
        courseId: String,
        lessonId: String
    ): Map<Int, Int> {
        return try {
            val root = org.json.JSONObject(rawJson)
            val key = "$courseId:$lessonId"
            val lessonObj = root.optJSONObject(key) ?: return emptyMap()
            val result = mutableMapOf<Int, Int>()
            val keys = lessonObj.keys()
            while (keys.hasNext()) {
                val qKey = keys.next()
                val qIdx = qKey.toIntOrNull()
                if (qIdx != null) {
                    result[qIdx] = lessonObj.optInt(qKey, 0)
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private suspend fun persistQuizCheckpoint(
        courseId: String,
        unitId: String,
        lessonId: String,
        selectedAnswers: Map<Int, Int>
    ) {
        appContext.edamDataStore.edit { prefs ->
            prefs[lastCoursePrefKey] = courseId
            prefs[lastUnitPrefKey] = unitId
            prefs[lastLessonPrefKey] = lessonId
            val currentRaw = prefs[quizCheckpointsPrefKey] ?: "{}"
            val root = try {
                org.json.JSONObject(currentRaw)
            } catch (_: Exception) {
                org.json.JSONObject()
            }
            val lessonObj = org.json.JSONObject()
            selectedAnswers.forEach { (qIdx, optIdx) ->
                lessonObj.put(qIdx.toString(), optIdx)
            }
            root.put("$courseId:$lessonId", lessonObj)
            prefs[quizCheckpointsPrefKey] = root.toString()
        }
    }

    fun resumeFromLastCheckpoint() {
        val checkpoint = uiState.value.resumeCheckpoint ?: return
        val currentActive = uiState.value.activeCourse
        if (currentActive == null || currentActive.id != checkpoint.courseId) {
            viewModelScope.launch {
                repository.selectCourse(checkpoint.courseId)
                openLesson(
                    unitId = checkpoint.unitId,
                    lessonId = checkpoint.lessonId,
                    forceRefresh = false
                )
            }
        } else {
            openLesson(
                unitId = checkpoint.unitId,
                lessonId = checkpoint.lessonId,
                forceRefresh = false
            )
        }
    }

    val availableLevels = listOf(
        "Beginner",
        "Elementary",
        "Intermediate",
        "Advanced",
        "University",
        "Professional"
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultScreensSeeded()
            val shareCourse = repository.ensureShareMarketCourseSeeded(makeActiveIfEmpty = true)
            repository.ensureChessGmCourseSeeded()
            repository.evaluateAndUnlockCourseBadges(shareCourse, context = null, pushEnabled = false)
            val currentActive = repository.activeCourseFlow.first()
            if (currentActive != null) {
                repository.evaluateAndUnlockCourseBadges(currentActive, context = null, pushEnabled = false)
            }
            val initialXp = (currentActive?.completedLessonsCount ?: 0) * 50 + 1420
            val userName = Firebase.auth.currentUser?.displayName ?: "You"
            repository.ensureLeaderboardSeeded(
                leagueId = LeagueTier.SAPPHIRE.tierId,
                currentUserName = userName,
                initialUserXp = initialXp
            )
            syncSignedInUserWithCloud()
            // Ensure WorkManager daily learning goal reminder is scheduled according to DataStore prefs
            try {
                val snap = appContext.edamDataStore.data.first()
                val pushEnabled = snap[pushNotificationsPrefKey] ?: true
                val reminderEnabled = snap[dailyReminderEnabledPrefKey] ?: true
                val hour = (snap[dailyReminderHourPrefKey] ?: 20).coerceIn(0, 23)
                val minute = (snap[dailyReminderMinutePrefKey] ?: 0).coerceIn(0, 59)
                val goal = (snap[dailyLessonGoalPrefKey] ?: 3).coerceIn(1, 10)
                if (pushEnabled && reminderEnabled) {
                    DailyGoalReminderScheduler.scheduleDailyGoalReminder(
                        context = appContext,
                        hour24 = hour,
                        minute = minute,
                        dailyGoal = goal
                    )
                }
            } catch (e: Exception) {
                Log.w("EdamViewModel", "WorkManager schedule init warning", e)
            }
        }
        startRealTimeMarketTickerLoop()
    }

    private fun startRealTimeMarketTickerLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(2200L)
                if (marketState.value.isLiveFeedActive) {
                    tickCounter++
                    marketState.update { current ->
                        current.copy(
                            tickers = TrendingMarketEngine.stepRealTimeTick(
                                tickers = current.tickers,
                                tickCounter = tickCounter
                            )
                        )
                    }
                }
            }
        }
    }

    fun selectMarketTicker(tickerId: String) {
        marketState.update { it.copy(selectedTickerId = tickerId) }
    }

    fun selectMarketTimeframe(timeframe: MarketTimeframe) {
        marketState.update { it.copy(timeframe = timeframe) }
    }

    fun selectMarketChartType(chartType: MarketChartType) {
        marketState.update { it.copy(chartType = chartType) }
    }

    fun selectMarketFilterTab(filterTab: MarketFilterTab) {
        marketState.update { it.copy(filterTab = filterTab) }
    }

    fun onMarketSearchQueryChange(query: String) {
        marketState.update { it.copy(searchQuery = query) }
    }

    fun toggleBookmarkTicker(tickerId: String) {
        marketState.update { current ->
            val updated = if (current.bookmarkedTickerIds.contains(tickerId)) {
                current.bookmarkedTickerIds - tickerId
            } else {
                current.bookmarkedTickerIds + tickerId
            }
            current.copy(bookmarkedTickerIds = updated)
        }
    }

    fun toggleMarketLiveFeed() {
        marketState.update { it.copy(isLiveFeedActive = !it.isLiveFeedActive) }
    }

    fun launchOrSelectTickerCourse(
        ticker: TrendingCourseTicker,
        onScrollToCourse: () -> Unit,
        onScrollToBuilder: () -> Unit
    ) {
        val linkedId = ticker.linkedCourseId
        if (linkedId == ShareMarketCatalog.SHARE_MARKET_COURSE_ID) {
            openShareMarketCourse(onReady = onScrollToCourse)
        } else if (linkedId != null) {
            viewModelScope.launch {
                repository.selectCourse(linkedId)
                onScrollToCourse()
            }
        } else {
            applyQuickPreset(
                courseName = ticker.title,
                level = ticker.level,
                goal = ticker.goal
            )
            formState.update {
                it.copy(
                    statusMessage = "Loaded ${ticker.symbol} (${ticker.title}) from Trending Market — tap 'Create with Edam' to build!"
                )
            }
            onScrollToBuilder()
        }
    }

    private var streakObserverJob: kotlinx.coroutines.Job? = null

    fun syncSignedInUserWithCloud() {
        val user = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        } ?: return

        viewModelScope.launch {
            try {
                val existingCloud = cloudRepository.getUserAccount(user.uid).getOrNull()
                val currentPrefs = uiState.value
                if (existingCloud != null) {
                    appContext.edamDataStore.edit { prefs ->
                        prefs[planPrefKey] = existingCloud.planTier
                        prefs[themePrefKey] = existingCloud.themeMode
                        if (!existingCloud.promoCodeUsed.isNullOrBlank()) {
                            prefs[promoPrefKey] = existingCloud.promoCodeUsed
                        }
                    }
                } else {
                    cloudRepository.upsertUserAccount(
                        displayName = user.displayName ?: "Edam Learner",
                        email = user.email,
                        planTier = currentPrefs.planTier.id,
                        promoCodeUsed = currentPrefs.promoCodeUsed.ifBlank { null },
                        themeMode = currentPrefs.themeMode.name
                    )
                }

                // Sync consecutive-day learning streak with Firestore
                val existingCloudStreak = cloudRepository.getDailyStreak(user.uid).getOrNull()
                val localStreak = dailyStreakManager.loadStateFromPrefs()
                if (existingCloudStreak != null) {
                    val merged = dailyStreakManager.applyCloudStreakDoc(existingCloudStreak)
                    cloudRepository.upsertDailyStreak(
                        currentStreak = merged.currentStreak,
                        longestStreak = merged.longestStreak,
                        lastStudyEpochDay = merged.lastStudyEpochDay,
                        totalXp = merged.totalXp,
                        weeklyStudyDaysCsv = merged.weeklyStudyDaysCsv,
                        streakFreezeActive = merged.streakFreezeActive,
                        streakFreezesAvailable = merged.streakFreezesAvailable
                    )
                } else {
                    val created = cloudRepository.upsertDailyStreak(
                        currentStreak = localStreak.currentStreak,
                        longestStreak = localStreak.longestStreak,
                        lastStudyEpochDay = localStreak.lastStudyEpochDay,
                        totalXp = localStreak.totalXp,
                        weeklyStudyDaysCsv = localStreak.weeklyStudyDaysCsv,
                        streakFreezeActive = localStreak.streakFreezeActive,
                        streakFreezesAvailable = localStreak.streakFreezesAvailable
                    ).getOrNull()
                    if (created != null) {
                        dailyStreakManager.applyCloudStreakDoc(created)
                    }
                }

                streakObserverJob?.cancel()
                streakObserverJob = viewModelScope.launch {
                    try {
                        cloudRepository.observeUserDailyStreak(user.uid).collect { cloudStreak ->
                            if (cloudStreak != null) {
                                dailyStreakManager.applyCloudStreakDoc(cloudStreak)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("EdamViewModel", "Firestore streak listener stopped", e)
                    }
                }

                // Sync Badges with Firestore
                try {
                    val cloudBadges = cloudRepository.getUserBadges(user.uid).getOrDefault(emptyList())
                    for (cb in cloudBadges) {
                        if (cb.isUnlocked) {
                            repository.recordBadgeUnlocked(
                                badgeId = cb.badgeKey.ifBlank { cb.id },
                                badgeTitle = cb.title,
                                percentageRequired = cb.progressPct,
                                courseId = "cloud",
                                courseTitle = cb.category
                            )
                        }
                    }
                    syncBadgesToFirestore()
                } catch (e: Exception) {
                    Log.w("EdamViewModel", "Failed to sync badges with Firestore", e)
                }
            } catch (e: Exception) {
                Log.w("EdamViewModel", "Failed to sync user account with Firestore", e)
            }
        }
    }

    fun syncBadgesToFirestore() {
        val user = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        } ?: return

        viewModelScope.launch {
            try {
                val currentBadges = uiState.value.badgeDisplayItems
                for (item in currentBadges) {
                    if (item.isUnlocked || item.currentProgressPct > 0) {
                        cloudRepository.upsertUserBadge(
                            badgeId = item.definition.id,
                            badgeKey = item.definition.id,
                            title = item.definition.title,
                            description = item.definition.description,
                            category = item.definition.category,
                            iconKey = item.definition.iconKey,
                            progressPct = item.currentProgressPct,
                            isUnlocked = item.isUnlocked,
                            unlockedAt = item.unlockedAt ?: System.currentTimeMillis()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("EdamViewModel", "Failed to sync badges to Firestore", e)
            }
        }
    }

    private suspend fun checkAndAwardMilestoneBadges(streak: DailyStreakState) {
        if (streak.currentStreak >= 3) {
            repository.recordBadgeUnlocked(
                badgeId = "badge_streak_3",
                badgeTitle = "Flame Awakened",
                percentageRequired = 100,
                courseId = "streak",
                courseTitle = "Daily Habit"
            )
        }
        if (streak.currentStreak >= 7) {
            repository.recordBadgeUnlocked(
                badgeId = "badge_streak_7",
                badgeTitle = "Unstoppable Week",
                percentageRequired = 100,
                courseId = "streak",
                courseTitle = "Daily Habit"
            )
        }
        syncBadgesToFirestore()
    }

    fun syncDailyStreakToFirestore(streak: DailyStreakState = dailyStreakManager.loadStateFromPrefs()) {
        val user = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        } ?: return

        viewModelScope.launch {
            try {
                val result = cloudRepository.upsertDailyStreak(
                    currentStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak,
                    lastStudyEpochDay = streak.lastStudyEpochDay,
                    totalXp = streak.totalXp,
                    weeklyStudyDaysCsv = streak.weeklyStudyDaysCsv,
                    streakFreezeActive = streak.streakFreezeActive,
                    streakFreezesAvailable = streak.streakFreezesAvailable
                )
                result.getOrNull()?.let { cloudDoc ->
                    dailyStreakManager.applyCloudStreakDoc(cloudDoc)
                }
            } catch (e: Exception) {
                Log.w("EdamViewModel", "Failed to sync daily streak to Firestore", e)
            }
        }
    }

    fun onCourseNameChange(newValue: String) {
        formState.update { it.copy(courseName = newValue) }
    }

    fun onLevelChange(newLevel: String) {
        formState.update { it.copy(level = newLevel) }
    }

    fun onGoalChange(newGoal: String) {
        formState.update { it.copy(goal = newGoal) }
    }

    fun applyQuickPreset(courseName: String, level: String, goal: String) {
        formState.update {
            it.copy(
                courseName = courseName,
                level = level,
                goal = goal,
                statusMessage = ""
            )
        }
    }

    fun openShareMarketCourse(onReady: () -> Unit = {}) {
        viewModelScope.launch {
            selectedCompanionFlow.value = EdamCompanionCharacter.ROBO_BROKER
            formState.update {
                it.copy(
                    isGeneratingCourse = true,
                    courseCreationStageIndex = 1,
                    courseCreationProgress = 0.45f,
                    statusMessage = "RoboBroker & Edam are unpacking your Stock Market & Business pack..."
                )
            }
            delay(280L)
            formState.update {
                it.copy(
                    courseCreationStageIndex = 3,
                    courseCreationProgress = 0.92f,
                    statusMessage = "Synchronizing candlestick charts, instruments & offline quizzes..."
                )
            }
            delay(240L)
            repository.activateShareMarketCourse()
            repository.recordBadgeUnlocked(
                badgeId = "badge_market_analyst",
                badgeTitle = "Bull Market Analyst",
                percentageRequired = 100,
                courseId = ShareMarketCatalog.SHARE_MARKET_COURSE_ID,
                courseTitle = "Stock Market Lab"
            )
            syncBadgesToFirestore()
            formState.update {
                it.copy(
                    isGeneratingCourse = false,
                    courseCreationStageIndex = 4,
                    courseCreationProgress = 1f,
                    statusMessage = "Stock Market & Equity Investing Mastery loaded with RoboBroker (100% Offline Ready)."
                )
            }
            onReady()
        }
    }

    fun openChessGmCourse(onReady: () -> Unit = {}) {
        viewModelScope.launch {
            selectedCompanionFlow.value = EdamCompanionCharacter.VEX
            formState.update {
                it.copy(
                    isGeneratingCourse = true,
                    courseCreationStageIndex = 1,
                    courseCreationProgress = 0.45f,
                    statusMessage = "Vex is setting up the 5-Unit Novice to Grandmaster Chess Academy..."
                )
            }
            delay(280L)
            formState.update {
                it.copy(
                    courseCreationStageIndex = 3,
                    courseCreationProgress = 0.92f,
                    statusMessage = "Loading tactical combinations, positional structures & GM endgames..."
                )
            }
            delay(240L)
            repository.activateChessGmCourse()
            repository.recordBadgeUnlocked(
                badgeId = "badge_chess_tactician",
                badgeTitle = "Tactical Prodigy",
                percentageRequired = 100,
                courseId = ChessAndFlashcardCatalog.CHESS_GM_COURSE_ID,
                courseTitle = "Novice to GM Chess"
            )
            syncBadgesToFirestore()
            formState.update {
                it.copy(
                    isGeneratingCourse = false,
                    courseCreationStageIndex = 4,
                    courseCreationProgress = 1f,
                    statusMessage = "♟️ Novice to Grandmaster (GM) Chess Mastery loaded with Vex (100% Offline Ready)."
                )
            }
            onReady()
        }
    }

    fun selectCompanionCharacter(companion: EdamCompanionCharacter) {
        selectedCompanionFlow.value = companion
    }

    fun recordFlashcardMastered(xpReward: Int = 15) {
        val updated = dailyStreakManager.recordStudyActivity(xpEarned = xpReward, isFlashcard = true)
        syncDailyStreakToFirestore(updated)
        viewModelScope.launch {
            repository.awardLeaderboardXp(xpReward)
            checkAndAwardMilestoneBadges(updated)
        }
    }

    fun recordChessPuzzleSolved(xpReward: Int = 25) {
        val updated = dailyStreakManager.recordStudyActivity(xpEarned = xpReward, isChessPuzzle = true)
        syncDailyStreakToFirestore(updated)
        viewModelScope.launch {
            repository.awardLeaderboardXp(xpReward)
            repository.recordBadgeUnlocked(
                badgeId = "badge_chess_tactician",
                badgeTitle = "Tactical Prodigy",
                percentageRequired = 100,
                courseId = ChessAndFlashcardCatalog.CHESS_GM_COURSE_ID,
                courseTitle = "Novice to GM Chess"
            )
            checkAndAwardMilestoneBadges(updated)
        }
    }

    fun checkInDailyStreak(xpReward: Int = 20) {
        val updated = dailyStreakManager.recordStudyActivity(xpEarned = xpReward)
        syncDailyStreakToFirestore(updated)
        viewModelScope.launch {
            repository.awardLeaderboardXp(xpReward)
            checkAndAwardMilestoneBadges(updated)
        }
    }

    fun toggleStreakFreeze() {
        val updated = dailyStreakManager.toggleStreakFreeze()
        syncDailyStreakToFirestore(updated)
    }

    fun setDailyLessonGoal(targetLessons: Int) {
        val clamped = targetLessons.coerceIn(1, 10)
        viewModelScope.launch {
            appContext.edamDataStore.edit { prefs ->
                prefs[dailyLessonGoalPrefKey] = clamped
                val todayEpoch = DailyStreakManager.currentEpochDay()
                if (prefs[dailyLessonsEpochDayPrefKey] == null) {
                    prefs[dailyLessonsEpochDayPrefKey] = todayEpoch
                }
            }
            val current = uiState.value
            if (current.pushNotificationsEnabled && current.dailyReminderEnabled) {
                try {
                    DailyGoalReminderScheduler.scheduleDailyGoalReminder(
                        context = appContext,
                        hour24 = current.dailyReminderHour,
                        minute = current.dailyReminderMinute,
                        dailyGoal = clamped
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appContext.edamDataStore.edit { prefs ->
                prefs[dailyReminderEnabledPrefKey] = enabled
                if (enabled) {
                    prefs[pushNotificationsPrefKey] = true
                }
            }
            val current = uiState.value
            try {
                if (enabled) {
                    DailyGoalReminderScheduler.scheduleDailyGoalReminder(
                        context = appContext,
                        hour24 = current.dailyReminderHour,
                        minute = current.dailyReminderMinute,
                        dailyGoal = current.dailyLessonGoal
                    )
                } else {
                    DailyGoalReminderScheduler.cancelDailyGoalReminder(appContext)
                }
            } catch (e: Exception) {
                Log.w("EdamViewModel", "Failed to toggle WorkManager daily reminder", e)
            }
        }
    }

    fun updateDailyReminderTime(hour24: Int, minute: Int) {
        val safeHour = hour24.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        viewModelScope.launch {
            appContext.edamDataStore.edit { prefs ->
                prefs[dailyReminderHourPrefKey] = safeHour
                prefs[dailyReminderMinutePrefKey] = safeMinute
                prefs[dailyReminderEnabledPrefKey] = true
                prefs[pushNotificationsPrefKey] = true
            }
            try {
                DailyGoalReminderScheduler.scheduleDailyGoalReminder(
                    context = appContext,
                    hour24 = safeHour,
                    minute = safeMinute,
                    dailyGoal = uiState.value.dailyLessonGoal
                )
            } catch (e: Exception) {
                Log.w("EdamViewModel", "Failed to schedule WorkManager daily reminder time", e)
            }
        }
    }

    fun triggerImmediateDailyGoalReminder() {
        val current = uiState.value
        val formattedTime = DailyGoalReminderScheduler.formatTime12Hour(
            current.dailyReminderHour,
            current.dailyReminderMinute
        )
        EdamNotificationHelper.sendDailyGoalReminderNotification(
            context = appContext,
            completedToday = current.dailyLessonsCompletedToday,
            dailyGoal = current.dailyLessonGoal,
            currentStreak = current.dailyStreak.currentStreak,
            scheduledTimeLabel = formattedTime
        )
        try {
            DailyGoalReminderScheduler.triggerImmediateGoalReminderWork(
                context = appContext,
                hour24 = current.dailyReminderHour,
                minute = current.dailyReminderMinute,
                dailyGoal = current.dailyLessonGoal
            )
        } catch (e: Exception) {
            Log.w("EdamViewModel", "Failed to enqueue immediate WorkManager reminder", e)
        }
    }

    fun recordDailyLessonGoalProgress(increment: Int = 1) {
        viewModelScope.launch {
            val todayEpoch = DailyStreakManager.currentEpochDay()
            appContext.edamDataStore.edit { prefs ->
                val storedEpoch = prefs[dailyLessonsEpochDayPrefKey] ?: todayEpoch
                val baseCount = if (storedEpoch == todayEpoch) {
                    prefs[dailyLessonsCompletedTodayPrefKey] ?: 1
                } else {
                    0
                }
                prefs[dailyLessonsEpochDayPrefKey] = todayEpoch
                prefs[dailyLessonsCompletedTodayPrefKey] = (baseCount + increment).coerceAtLeast(0)
            }
            val updatedStreak = dailyStreakManager.recordStudyActivity(xpEarned = 25 * increment.coerceAtLeast(1))
            syncDailyStreakToFirestore(updatedStreak)
            repository.awardLeaderboardXp(25 * increment.coerceAtLeast(1))
        }
    }

    fun createCourse(onCourseCreated: () -> Unit = {}) {
        val current = formState.value
        val name = current.courseName.trim()
        val level = current.level.trim()
        val goal = current.goal.trim()

        if (name.isEmpty() || level.isEmpty() || goal.isEmpty()) {
            formState.update {
                it.copy(statusMessage = "Fill in the course, level and goal.")
            }
            return
        }

        formState.update {
            it.copy(
                isGeneratingCourse = true,
                courseCreationStageIndex = 0,
                courseCreationProgress = 0.22f,
                statusMessage = "Stage 1/4: Edam is analyzing \"$name\" ($level) & calibrating your learning path..."
            )
        }

        viewModelScope.launch {
            try {
                delay(320L)
                formState.update {
                    it.copy(
                        courseCreationStageIndex = 1,
                        courseCreationProgress = 0.52f,
                        statusMessage = "Stage 2/4: Structuring modular units, outcomes & progressive milestones..."
                    )
                }
                delay(320L)
                formState.update {
                    it.copy(
                        courseCreationStageIndex = 2,
                        courseCreationProgress = 0.82f,
                        statusMessage = "Stage 3/4: Crafting interactive lessons, quizzes & bundling offline study pack..."
                    )
                }
                val created = repository.createAndSaveCourse(
                    courseName = name,
                    level = level,
                    goal = goal
                )
                repository.recordBadgeUnlocked(
                    badgeId = "badge_polymath",
                    badgeTitle = "Universal Polymath",
                    percentageRequired = 100,
                    courseId = created.id,
                    courseTitle = created.title
                )
                if (name.contains("Code", ignoreCase = true) ||
                    name.contains("Python", ignoreCase = true) ||
                    name.contains("Virus", ignoreCase = true) ||
                    name.contains("Program", ignoreCase = true)
                ) {
                    selectedCompanionFlow.value = EdamCompanionCharacter.BIT_VIRUS
                    repository.recordBadgeUnlocked(
                        badgeId = "badge_coding_virus",
                        badgeTitle = "Byte Glitch Overlord",
                        percentageRequired = 100,
                        courseId = created.id,
                        courseTitle = created.title
                    )
                }
                syncBadgesToFirestore()
                formState.update {
                    it.copy(
                        courseCreationStageIndex = 3,
                        courseCreationProgress = 0.96f,
                        statusMessage = "Stage 4/4: Finalizing course syllabus & unlocking +35 XP..."
                    )
                }
                delay(220L)
                dailyStreakManager.recordStudyActivity(xpEarned = 35)
                repository.awardLeaderboardXp(35)
                marketState.update {
                    it.copy(selectedTickerId = "user_course_${created.id}")
                }
                formState.update {
                    it.copy(
                        isGeneratingCourse = false,
                        courseCreationStageIndex = 4,
                        courseCreationProgress = 1.0f,
                        statusMessage = "Course created & saved for offline study."
                    )
                }
                onCourseCreated()
            } catch (error: Exception) {
                formState.update {
                    it.copy(
                        isGeneratingCourse = false,
                        courseCreationStageIndex = 0,
                        courseCreationProgress = 0f,
                        statusMessage = "Something went wrong: ${error.message ?: "Edam request failed."}"
                    )
                }
            }
        }
    }

    fun ensureActiveCourseOfflineReady() {
        val course = uiState.value.activeCourse ?: return
        viewModelScope.launch {
            repository.bundleCourseForOfflineUse(course)
        }
    }

    fun openLesson(
        unitId: String,
        lessonId: String,
        forceRefresh: Boolean = false
    ) {
        val course = uiState.value.activeCourse ?: return
        val unit = course.units.find { it.id == unitId } ?: return
        val lessonSummary = unit.lessons.find { it.id == lessonId } ?: return

        viewModelScope.launch {
            val rawCheckpoints = try {
                val snap = appContext.edamDataStore.data.first()
                snap[quizCheckpointsPrefKey] ?: "{}"
            } catch (_: Exception) {
                "{}"
            }
            try {
                appContext.edamDataStore.edit { prefs ->
                    prefs[lastCoursePrefKey] = course.id
                    prefs[lastUnitPrefKey] = unit.id
                    prefs[lastLessonPrefKey] = lessonSummary.id
                }
            } catch (_: Exception) {
            }

            val restoredAnswers = if (forceRefresh) {
                emptyMap()
            } else {
                parseSavedQuizAnswers(rawCheckpoints, course.id, lessonSummary.id)
            }

            formState.update {
                it.copy(
                    lessonModalState = LessonModalState(
                        courseId = course.id,
                        unit = unit,
                        lessonSummary = lessonSummary,
                        isLoading = true,
                        lessonContent = null,
                        errorMessage = null,
                        selectedAnswers = restoredAnswers
                    )
                )
            }

            try {
                val content = repository.getOrGenerateLesson(
                    course = course,
                    unit = unit,
                    lesson = lessonSummary,
                    forceRefresh = forceRefresh
                )
                formState.update { state ->
                    val currentModal = state.lessonModalState
                    if (currentModal != null && currentModal.lessonSummary.id == lessonId) {
                        state.copy(
                            lessonModalState = currentModal.copy(
                                isLoading = false,
                                lessonContent = content,
                                errorMessage = null
                            )
                        )
                    } else {
                        state
                    }
                }
            } catch (error: Exception) {
                formState.update { state ->
                    val currentModal = state.lessonModalState
                    if (currentModal != null && currentModal.lessonSummary.id == lessonId) {
                        state.copy(
                            lessonModalState = currentModal.copy(
                                isLoading = false,
                                lessonContent = null,
                                errorMessage = error.message ?: "AI request failed."
                            )
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun answerQuestion(questionIndex: Int, optionIndex: Int) {
        var updatedModal: LessonModalState? = null
        formState.update { state ->
            val modal = state.lessonModalState ?: return@update state
            if (modal.selectedAnswers.containsKey(questionIndex)) {
                return@update state
            }
            val nextModal = modal.copy(
                selectedAnswers = modal.selectedAnswers + (questionIndex to optionIndex)
            )
            updatedModal = nextModal
            state.copy(lessonModalState = nextModal)
        }
        val target = updatedModal ?: return
        viewModelScope.launch {
            persistQuizCheckpoint(
                courseId = target.courseId,
                unitId = target.unit.id,
                lessonId = target.lessonSummary.id,
                selectedAnswers = target.selectedAnswers
            )
        }
    }

    fun completeCurrentLesson() {
        val modal = formState.value.lessonModalState ?: return
        viewModelScope.launch {
            val newlyEarned = repository.markLessonComplete(
                courseId = modal.courseId,
                lessonId = modal.lessonSummary.id,
                context = appContext,
                pushEnabled = uiState.value.pushNotificationsEnabled
            )
            val updatedStreak = dailyStreakManager.recordStudyActivity(xpEarned = 45)
            syncDailyStreakToFirestore(updatedStreak)
            repository.awardLeaderboardXp(45)
            val todayEpoch = DailyStreakManager.currentEpochDay()
            appContext.edamDataStore.edit { prefs ->
                val storedEpoch = prefs[dailyLessonsEpochDayPrefKey] ?: todayEpoch
                val baseCount = if (storedEpoch == todayEpoch) {
                    prefs[dailyLessonsCompletedTodayPrefKey] ?: 1
                } else {
                    0
                }
                prefs[dailyLessonsEpochDayPrefKey] = todayEpoch
                prefs[dailyLessonsCompletedTodayPrefKey] = baseCount + 1
            }
            if (newlyEarned.isNotEmpty()) {
                val latest = newlyEarned.last()
                formState.update {
                    it.copy(
                        statusMessage = "🎖️ Achievement Unlocked: \"${latest.badgeTitle}\" (${latest.percentageRequired}%)!"
                    )
                }
            }

            // Automatically advance last checkpoint to the next incomplete lesson in the course
            val course = uiState.value.activeCourse
            if (course != null) {
                val allPairs = course.units.flatMap { u -> u.lessons.map { l -> Pair(u.id, l.id) } }
                val currentIdx = allPairs.indexOfFirst { it.second == modal.lessonSummary.id }
                val nextPair = if (currentIdx >= 0 && currentIdx + 1 < allPairs.size) {
                    allPairs[currentIdx + 1]
                } else {
                    allPairs.firstOrNull { !course.completed.contains(it.second) && it.second != modal.lessonSummary.id }
                }
                if (nextPair != null) {
                    appContext.edamDataStore.edit { prefs ->
                        prefs[lastCoursePrefKey] = course.id
                        prefs[lastUnitPrefKey] = nextPair.first
                        prefs[lastLessonPrefKey] = nextPair.second
                    }
                }
            }
        }
    }

    fun closeLesson() {
        formState.update { it.copy(lessonModalState = null) }
    }

    fun toggleSavedCoursesSheet(show: Boolean) {
        formState.update { it.copy(showSavedCoursesSheet = show) }
    }

    fun toggleSettingsModal(show: Boolean) {
        formState.update { it.copy(showSettingsModal = show, promoStatusMessage = "") }
    }

    fun toggleProfileModal(show: Boolean) {
        formState.update { it.copy(showProfileModal = show) }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appContext.edamDataStore.edit { prefs ->
                prefs[pushNotificationsPrefKey] = enabled
            }
            val current = uiState.value
            try {
                if (enabled && current.dailyReminderEnabled) {
                    DailyGoalReminderScheduler.scheduleDailyGoalReminder(
                        context = appContext,
                        hour24 = current.dailyReminderHour,
                        minute = current.dailyReminderMinute,
                        dailyGoal = current.dailyLessonGoal
                    )
                } else if (!enabled) {
                    DailyGoalReminderScheduler.cancelDailyGoalReminder(appContext)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun sendTestNotification() {
        EdamNotificationHelper.sendTestMilestoneNotification(appContext)
    }

    fun onPromoCodeInputChange(newCode: String) {
        formState.update { it.copy(promoCodeInput = newCode, promoStatusMessage = "") }
    }

    fun applyPromoCode() {
        val code = formState.value.promoCodeInput.trim()
        if (PlanTier.isValidDeveloperCode(code)) {
            viewModelScope.launch {
                appContext.edamDataStore.edit { prefs ->
                    prefs[planPrefKey] = PlanTier.MAX.id
                    prefs[promoPrefKey] = PlanTier.DEVELOPER_MAX_CODE
                }
                formState.update {
                    it.copy(
                        promoCodeInput = "",
                        promoStatusMessage = "100% OFF Developer Code Applied! You now have MAX Tier for $0.00."
                    )
                }
                syncPreferencesToCloud(
                    planTier = PlanTier.MAX,
                    promoCode = PlanTier.DEVELOPER_MAX_CODE,
                    themeMode = uiState.value.themeMode
                )
            }
        } else {
            formState.update {
                it.copy(
                    promoStatusMessage = "Invalid promo code. Check the code and try again."
                )
            }
        }
    }

    fun selectPlanTier(tier: PlanTier) {
        viewModelScope.launch {
            appContext.edamDataStore.edit { prefs ->
                prefs[planPrefKey] = tier.id
            }
            formState.update {
                it.copy(promoStatusMessage = "Switched to ${tier.displayName} plan.")
            }
            syncPreferencesToCloud(
                planTier = tier,
                promoCode = uiState.value.promoCodeUsed.ifBlank { null },
                themeMode = uiState.value.themeMode
            )
        }
    }

    fun selectThemeMode(mode: EdamThemeMode) {
        viewModelScope.launch {
            appContext.edamDataStore.edit { prefs ->
                prefs[themePrefKey] = mode.name
            }
            syncPreferencesToCloud(
                planTier = uiState.value.planTier,
                promoCode = uiState.value.promoCodeUsed.ifBlank { null },
                themeMode = mode
            )
        }
    }

    private suspend fun syncPreferencesToCloud(
        planTier: PlanTier,
        promoCode: String?,
        themeMode: EdamThemeMode
    ) {
        val user = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        } ?: return

        cloudRepository.upsertUserAccount(
            displayName = user.displayName ?: "Edam Learner",
            email = user.email,
            planTier = planTier.id,
            promoCodeUsed = promoCode,
            themeMode = themeMode.name
        )
    }

    fun selectSavedCourse(courseId: String) {
        viewModelScope.launch {
            repository.selectCourse(courseId)
            formState.update { it.copy(showSavedCoursesSheet = false) }
        }
    }

    fun deleteSavedCourse(courseId: String) {
        viewModelScope.launch {
            repository.deleteCourse(courseId)
        }
    }

    // Screen Navigation
    fun navigateToDestination(destination: AppScreenDestination) {
        currentDestinationFlow.value = destination
        viewModelScope.launch {
            repository.recordScreenVisit(destination.screenEntityId)
        }
    }

    fun navigateBackToHome() {
        navigateToDestination(AppScreenDestination.HOME)
    }

    // AI Course Studio / Outline Generator Actions
    fun onOutlineTopicChange(topic: String) {
        outlineStudioStateFlow.update { it.copy(topic = topic, errorMessage = null) }
    }

    fun onOutlineLevelChange(level: String) {
        outlineStudioStateFlow.update { it.copy(level = level) }
    }

    fun onOutlineGoalChange(goal: String) {
        outlineStudioStateFlow.update { it.copy(goal = goal) }
    }

    fun onOutlineFocusAreaChange(focus: String) {
        outlineStudioStateFlow.update { it.copy(focusArea = focus) }
    }

    fun onOutlineUnitCountChange(count: Int) {
        outlineStudioStateFlow.update { it.copy(unitCount = count.coerceIn(3, 8)) }
    }

    fun applyOutlinePreset(preset: QuickTopicPreset) {
        outlineStudioStateFlow.update {
            it.copy(
                topic = preset.topic,
                level = preset.level,
                goal = preset.goal,
                focusArea = preset.focusArea,
                errorMessage = null
            )
        }
    }

    fun generateCourseOutline(onSuccess: (() -> Unit)? = null) {
        val current = outlineStudioStateFlow.value
        val topic = current.topic.trim()
        if (topic.isBlank()) {
            outlineStudioStateFlow.update {
                it.copy(errorMessage = "Please enter a topic to generate a structured outline.")
            }
            return
        }

        viewModelScope.launch {
            outlineStudioStateFlow.update {
                it.copy(
                    isGenerating = true,
                    statusMessage = "Edam is structuring your course syllabus...",
                    generationStep = "Edam is preparing your curriculum architecture...",
                    errorMessage = null
                )
            }
            delay(350L)
            outlineStudioStateFlow.update {
                it.copy(generationStep = "Edam is analyzing learning prerequisites & modular outcomes...")
            }
            delay(400L)
            outlineStudioStateFlow.update {
                it.copy(generationStep = "Edam is building units, lessons, and XP checkpoints...")
            }

            try {
                val outline = repository.generateCourseOutline(
                    topic = topic,
                    level = current.level,
                    goal = current.goal.ifBlank { "Master $topic through structured lessons and practice" },
                    focusArea = current.focusArea,
                    unitCount = current.unitCount
                )
                outlineStudioStateFlow.update {
                    it.copy(
                        isGenerating = false,
                        generatedOutline = outline,
                        statusMessage = "Course outline generated successfully with Edam!",
                        generationStep = "Complete ✓"
                    )
                }
                onSuccess?.invoke()
            } catch (e: Exception) {
                outlineStudioStateFlow.update {
                    it.copy(
                        isGenerating = false,
                        statusMessage = "Could not generate outline. Please try again.",
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun enrollInGeneratedOutline(onEnrolled: (() -> Unit)? = null) {
        val outline = outlineStudioStateFlow.value.generatedOutline ?: return
        viewModelScope.launch {
            val course = repository.saveAndActivateCourseOutline(outline)
            repository.evaluateAndUnlockCourseBadges(course, context = appContext, pushEnabled = true)
            awardUserXp(50)
            currentDestinationFlow.value = AppScreenDestination.HOME
            onEnrolled?.invoke()
        }
    }

    fun clearGeneratedOutline() {
        outlineStudioStateFlow.update {
            it.copy(generatedOutline = null, statusMessage = "", errorMessage = null)
        }
    }

    // Duolingo Competitive Leaderboard Actions
    fun selectLeague(tier: LeagueTier) {
        selectedLeagueFlow.value = tier
        viewModelScope.launch {
            repository.ensureLeaderboardSeeded(
                leagueId = tier.tierId,
                currentUserName = uiState.value.userDisplayName,
                initialUserXp = uiState.value.currentUserWeeklyXp
            )
        }
    }

    fun startSpeedDrill() {
        val drill = LeaderboardDrillCatalog.getRandomDrill()
        duolingoDrillStateFlow.update {
            it.copy(
                isDrillModalVisible = true,
                currentDrillQuestion = drill,
                drillSelectedOption = null,
                drillFeedbackMessage = null,
                isDrillAnswerCorrect = null
            )
        }
    }

    fun submitDrillAnswer(optionIndex: Int) {
        val currentDrill = duolingoDrillStateFlow.value.currentDrillQuestion ?: return
        val isCorrect = optionIndex == currentDrill.correctIndex
        duolingoDrillStateFlow.update {
            it.copy(
                drillSelectedOption = optionIndex,
                isDrillAnswerCorrect = isCorrect,
                drillFeedbackMessage = if (isCorrect) {
                    "🎉 Correct! +${currentDrill.xpReward} XP earned! You climbed the leaderboard!"
                } else {
                    "Incorrect. ${currentDrill.explanation}"
                }
            )
        }
        if (isCorrect) {
            dailyStreakManager.recordStudyActivity(xpEarned = currentDrill.xpReward)
            viewModelScope.launch {
                repository.awardLeaderboardXp(currentDrill.xpReward)
                if (uiState.value.pushNotificationsEnabled) {
                    EdamNotificationHelper.sendBadgeUnlockedNotification(
                        context = appContext,
                        badgeTitle = "⚡ Speed Drill Victor (+${currentDrill.xpReward} XP)",
                        courseTitle = "Duolingo Leagues Competition",
                        percentage = 100
                    )
                }
            }
        }
    }

    fun closeSpeedDrill() {
        duolingoDrillStateFlow.update {
            it.copy(
                isDrillModalVisible = false,
                currentDrillQuestion = null,
                drillSelectedOption = null,
                drillFeedbackMessage = null,
                isDrillAnswerCorrect = null
            )
        }
    }

    fun claimStreakBonus() {
        if (duolingoDrillStateFlow.value.streakBonusClaimedToday) return
        duolingoDrillStateFlow.update { it.copy(streakBonusClaimedToday = true) }
        dailyStreakManager.recordStudyActivity(xpEarned = 25)
        viewModelScope.launch {
            repository.awardLeaderboardXp(25)
            if (uiState.value.pushNotificationsEnabled) {
                EdamNotificationHelper.sendBadgeUnlockedNotification(
                    context = appContext,
                    badgeTitle = "🔥 Daily Streak Reward (+25 XP)",
                    courseTitle = "Duolingo Daily Challenge",
                    percentage = 100
                )
            }
        }
    }

    fun awardUserXp(xp: Int) {
        val updated = dailyStreakManager.recordStudyActivity(xpEarned = xp)
        syncDailyStreakToFirestore(updated)
        viewModelScope.launch {
            repository.awardLeaderboardXp(xp)
        }
    }
}
