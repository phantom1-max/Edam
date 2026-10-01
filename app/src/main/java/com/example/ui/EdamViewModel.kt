package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.EdamDatabase
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
import com.example.ui.theme.EdamThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val Context.edamDataStore by preferencesDataStore(name = "edam_settings")

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
    val isLiveFeedActive: Boolean = true
)

data class EdamFormState(
    val courseName: String = "",
    val level: String = "",
    val goal: String = "",
    val isGeneratingCourse: Boolean = false,
    val statusMessage: String = "",
    val showSavedCoursesSheet: Boolean = false,
    val showSettingsModal: Boolean = false,
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
    val quizCheckpointsJson: String = "{}"
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
    val courseName: String = "",
    val level: String = "",
    val goal: String = "",
    val isGeneratingCourse: Boolean = false,
    val statusMessage: String = "",
    val activeCourse: Course? = null,
    val cachedLessonsCount: Int = 0,
    val savedCourses: List<Course> = emptyList(),
    val showSavedCoursesSheet: Boolean = false,
    val showSettingsModal: Boolean = false,
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
    val isMarketLiveFeedActive: Boolean = true,
    val marketGlobalSummary: MarketGlobalSummary = MarketGlobalSummary()
)

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

    private val databaseId: String = application.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
    private val cloudRepository = EdamCloudRepository(firestore)

    private val repository: EdamRepository = EdamRepository(
        dao = EdamDatabase.getInstance(application).edamDao(),
        cloudRepository = cloudRepository
    )

    private val formState = MutableStateFlow(EdamFormState())
    private val marketState = MutableStateFlow(MarketDashboardState())
    private var tickCounter = 1L

    private val localPrefsFlow = appContext.edamDataStore.data.map { prefs ->
        LocalPrefsState(
            themeMode = EdamThemeMode.fromName(prefs[themePrefKey]),
            planTier = PlanTier.fromId(prefs[planPrefKey]),
            promoCodeUsed = prefs[promoPrefKey] ?: "",
            lastCourseId = prefs[lastCoursePrefKey] ?: "",
            lastUnitId = prefs[lastUnitPrefKey] ?: "",
            lastLessonId = prefs[lastLessonPrefKey] ?: "",
            quizCheckpointsJson = prefs[quizCheckpointsPrefKey] ?: "{}"
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

    val uiState: StateFlow<EdamUiState> = combine(
        formState,
        activeCourseWithOfflineCountFlow,
        localPrefsFlow,
        marketState
    ) { form, (activeCourse, cachedCount, allCourses), prefs, market ->
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

        EdamUiState(
            courseName = form.courseName,
            level = form.level,
            goal = form.goal,
            isGeneratingCourse = form.isGeneratingCourse,
            statusMessage = form.statusMessage,
            activeCourse = activeCourse,
            cachedLessonsCount = cachedCount,
            savedCourses = allCourses,
            showSavedCoursesSheet = form.showSavedCoursesSheet,
            showSettingsModal = form.showSettingsModal,
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
            isMarketLiveFeedActive = market.isLiveFeedActive,
            marketGlobalSummary = globalSummary
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
        openLesson(
            unitId = checkpoint.unitId,
            lessonId = checkpoint.lessonId,
            forceRefresh = false
        )
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
            repository.ensureShareMarketCourseSeeded(makeActiveIfEmpty = true)
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
                    statusMessage = "Loaded ${ticker.symbol} (${ticker.title}) from Trending Market — tap 'Create with Gemini' to build!"
                )
            }
            onScrollToBuilder()
        }
    }

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
            } catch (e: Exception) {
                Log.w("EdamViewModel", "Failed to sync user account with Firestore", e)
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
            repository.activateShareMarketCourse()
            formState.update {
                it.copy(
                    statusMessage = "Share Market & Equity Investing Mastery loaded (100% Offline Ready)."
                )
            }
            onReady()
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
                statusMessage = "Gemini is designing your learning path & bundling offline lessons..."
            )
        }

        viewModelScope.launch {
            try {
                val created = repository.createAndSaveCourse(
                    courseName = name,
                    level = level,
                    goal = goal
                )
                marketState.update {
                    it.copy(selectedTickerId = "user_course_${created.id}")
                }
                formState.update {
                    it.copy(
                        isGeneratingCourse = false,
                        statusMessage = "Course created & saved for offline study."
                    )
                }
                onCourseCreated()
            } catch (error: Exception) {
                formState.update {
                    it.copy(
                        isGeneratingCourse = false,
                        statusMessage = "Something went wrong: ${error.message ?: "AI request failed."}"
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
            val prefsSnap = appContext.edamDataStore.data.map { prefs ->
                prefs[quizCheckpointsPrefKey] ?: "{}"
            }
            var rawCheckpoints = "{}"
            try {
                appContext.edamDataStore.edit { prefs ->
                    rawCheckpoints = prefs[quizCheckpointsPrefKey] ?: "{}"
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
            repository.markLessonComplete(
                courseId = modal.courseId,
                lessonId = modal.lessonSummary.id
            )
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
}
