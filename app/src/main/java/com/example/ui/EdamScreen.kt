package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.DailyStreakState
import com.example.data.local.ScreenEntity
import com.example.data.model.ChessAndFlashcardCatalog
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.DiscoverCatalog
import com.example.data.model.DiscoverSubject
import com.example.data.model.LeagueTier
import com.example.data.model.LearningSection
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.model.PlanTier
import com.example.data.model.PracticeQuestion
import com.example.data.model.ShareMarketCatalog
import com.example.ui.theme.EdamThemeMode
import com.example.ui.theme.EdamThemeQuickToggleButton
import com.example.ui.theme.LocalEdamThemeSpec
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdamApp(
    viewModel: EdamViewModel,
    onSignOutComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val themeSpec = LocalEdamThemeSpec.current
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val glowAlpha = themeSpec.radialGlowAlpha
    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
    val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha)
    val bgColor = MaterialTheme.colorScheme.background

    BackHandler(enabled = uiState.currentDestination != AppScreenDestination.HOME) {
        viewModel.navigateBackToHome()
    }
    BackHandler(enabled = uiState.lessonModalState != null) {
        viewModel.closeLesson()
    }
    BackHandler(enabled = uiState.showSettingsModal) {
        viewModel.toggleSettingsModal(false)
    }
    BackHandler(enabled = uiState.showSavedCoursesSheet) {
        viewModel.toggleSavedCoursesSheet(false)
    }
    BackHandler(enabled = uiState.showProfileModal) {
        viewModel.toggleProfileModal(false)
    }

    var rootOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    var globalCursorPosition by remember { mutableStateOf<Offset?>(null) }

    CompositionLocalProvider(LocalCursorPosition provides globalCursorPosition) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootOriginInWindow = coords.localToRoot(Offset.Zero)
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull()
                        if (change != null) {
                            globalCursorPosition = rootOriginInWindow + change.position
                        }
                    }
                }
            }
            .drawBehind {
                drawRect(color = bgColor)
                if (glowAlpha > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryGlow, bgColor.copy(alpha = 0f)),
                            center = Offset(size.width * 0.15f, size.height * 0.10f),
                            radius = size.maxDimension * 0.38f
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.10f),
                        radius = size.maxDimension * 0.38f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondaryGlow, bgColor.copy(alpha = 0f)),
                            center = Offset(size.width * 0.85f, size.height * 0.15f),
                            radius = size.maxDimension * 0.40f
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = size.maxDimension * 0.40f
                    )
                }
            },
        containerColor = bgColor.copy(alpha = 0f),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isExpanded = maxWidth >= 700.dp
            val horizontalMargin = if (isExpanded) 28.dp else 14.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1100.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Sticky Glass Navigation Bar
                EdamNavigationBar(
                    planTier = uiState.planTier,
                    currentDestination = uiState.currentDestination,
                    savedCoursesCount = uiState.savedCourses.size,
                    unlockedBadgesCount = uiState.earnedBadges.size,
                    currentUserRank = uiState.currentUserRank,
                    selectedLeagueEmoji = uiState.selectedLeague.emoji,
                    currentStreak = uiState.dailyStreak.currentStreak,
                    selectedCompanion = uiState.selectedCompanion,
                    themeMode = uiState.themeMode,
                    onToggleTheme = {
                        val current = uiState.themeMode
                        val nextMode = when (current) {
                            EdamThemeMode.DARK,
                            EdamThemeMode.EXTRA_DARK,
                            EdamThemeMode.HIGH_CONTRAST_BLACK -> EdamThemeMode.LIGHT
                            else -> EdamThemeMode.DARK
                        }
                        viewModel.selectThemeMode(nextMode)
                    },
                    onGoHome = {
                        viewModel.navigateToDestination(AppScreenDestination.HOME)
                    },
                    onGoToStudio = {
                        viewModel.navigateToDestination(AppScreenDestination.COURSE_STUDIO)
                    },
                    onGoToLeaderboard = {
                        viewModel.navigateToDestination(AppScreenDestination.LEADERBOARD)
                    },
                    onGoToMarket = {
                        viewModel.navigateToDestination(AppScreenDestination.HOME)
                        coroutineScope.launch {
                            listState.animateScrollToItem(1)
                        }
                    },
                    onGoToCreate = {
                        viewModel.navigateToDestination(AppScreenDestination.HOME)
                        coroutineScope.launch {
                            listState.animateScrollToItem(2)
                        }
                    },
                    onOpenSavedCourses = {
                        viewModel.toggleSavedCoursesSheet(true)
                    },
                    onOpenProfile = {
                        viewModel.toggleProfileModal(true)
                    },
                    onOpenSettings = {
                        viewModel.toggleSettingsModal(true)
                    },
                    modifier = Modifier
                        .padding(horizontal = horizontalMargin, vertical = 8.dp)
                )

                // ScreenEntity Quick Navigation & Telemetry Bar
                if (uiState.allScreens.isNotEmpty()) {
                    ScreenRegistryQuickBar(
                        screens = uiState.allScreens,
                        currentDestination = uiState.currentDestination,
                        onSelectScreen = { screen ->
                            when (screen.route) {
                                "home" -> viewModel.navigateToDestination(AppScreenDestination.HOME)
                                "course_studio" -> viewModel.navigateToDestination(AppScreenDestination.COURSE_STUDIO)
                                "leaderboard" -> viewModel.navigateToDestination(AppScreenDestination.LEADERBOARD)
                                "market" -> {
                                    viewModel.navigateToDestination(AppScreenDestination.HOME)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(1)
                                    }
                                }
                                "profile" -> {
                                    viewModel.navigateToDestination(AppScreenDestination.HOME)
                                    viewModel.toggleProfileModal(true)
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = horizontalMargin, vertical = 4.dp)
                    )
                }

                when (uiState.currentDestination) {
                    AppScreenDestination.COURSE_STUDIO -> {
                        CourseOutlineGeneratorScreen(
                            topic = uiState.outlineTopic,
                            level = uiState.outlineLevel,
                            goal = uiState.outlineGoal,
                            focusArea = uiState.outlineFocusArea,
                            unitCount = uiState.outlineUnitCount,
                            isGenerating = uiState.isGeneratingOutline,
                            statusMessage = uiState.outlineStatusMessage,
                            generationStep = uiState.outlineGenerationStep,
                            generatedOutline = uiState.generatedOutline,
                            errorMessage = uiState.outlineErrorMessage,
                            availableLevels = viewModel.availableLevels,
                            onTopicChange = viewModel::onOutlineTopicChange,
                            onLevelChange = viewModel::onOutlineLevelChange,
                            onGoalChange = viewModel::onOutlineGoalChange,
                            onFocusAreaChange = viewModel::onOutlineFocusAreaChange,
                            onUnitCountChange = viewModel::onOutlineUnitCountChange,
                            onApplyPreset = viewModel::applyOutlinePreset,
                            onGenerateOutline = { viewModel.generateCourseOutline() },
                            onEnrollOutline = {
                                viewModel.enrollInGeneratedOutline(
                                    onEnrolled = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(3)
                                        }
                                    }
                                )
                            },
                            onClearOutline = viewModel::clearGeneratedOutline,
                            onNavigateBack = viewModel::navigateBackToHome,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = horizontalMargin)
                        )
                    }

                    AppScreenDestination.LEADERBOARD -> {
                        DuolingoLeaderboardScreen(
                            selectedLeague = uiState.selectedLeague,
                            competitors = uiState.leaderboardEntries,
                            currentUserRank = uiState.currentUserRank,
                            currentUserWeeklyXp = uiState.currentUserWeeklyXp,
                            isDrillModalVisible = uiState.isDrillModalVisible,
                            currentDrillQuestion = uiState.currentDrillQuestion,
                            drillSelectedOption = uiState.drillSelectedOption,
                            drillFeedbackMessage = uiState.drillFeedbackMessage,
                            isDrillAnswerCorrect = uiState.isDrillAnswerCorrect,
                            streakBonusClaimedToday = uiState.streakBonusClaimedToday,
                            currentStreakDays = uiState.dailyStreak.currentStreak,
                            onSelectLeague = viewModel::selectLeague,
                            onStartSpeedDrill = viewModel::startSpeedDrill,
                            onSubmitDrillAnswer = viewModel::submitDrillAnswer,
                            onCloseSpeedDrill = viewModel::closeSpeedDrill,
                            onClaimStreakBonus = viewModel::claimStreakBonus,
                            onNavigateBack = viewModel::navigateBackToHome,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = horizontalMargin)
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("main_scroll_list"),
                            contentPadding = PaddingValues(
                                start = horizontalMargin,
                                end = horizontalMargin,
                                bottom = 36.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 0: Hero section with Animated Edam Mascot, Flashcard Deck, Companion Roster & Quick Launch Cards
                            item(key = "hero_section") {
                                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                    EdamHeroSection(
                                        isExpanded = isExpanded,
                                        dailyStreak = uiState.dailyStreak,
                                        selectedCompanion = uiState.selectedCompanion,
                                        onMasterFlashcard = { viewModel.recordFlashcardMastered() },
                                        onOpenProfile = { viewModel.toggleProfileModal(true) },
                                        onOpenChess = { viewModel.openChessGmCourse() },
                                        onOpenMarket = { viewModel.openShareMarketCourse() },
                                        onOpenStreakDashboard = { viewModel.toggleProfileModal(true) },
                                        onLogStudyActivity = { viewModel.checkInDailyStreak(xpReward = 20) },
                                        onLaunchCustomTopic = { topic ->
                                            viewModel.onOutlineTopicChange(topic)
                                            viewModel.navigateToDestination(AppScreenDestination.COURSE_STUDIO)
                                        },
                                        onTestPushReminder = { viewModel.triggerDailyStreakReminderPush(context) }
                                    )
                                    HomeDailyStreakDashboardCard(
                                        dailyStreak = uiState.dailyStreak,
                                        isSignedIn = uiState.userEmail.isNotBlank(),
                                        onLogActivityToday = { viewModel.checkInDailyStreak(xpReward = 20) },
                                        onSyncCloudStreak = { viewModel.syncDailyStreakToFirestore() },
                                        onOpenProfile = { viewModel.toggleProfileModal(true) }
                                    )
                                    EdamCompanionRosterCard(
                                        selectedCharacter = uiState.selectedCompanion,
                                        onSelectCharacter = viewModel::selectCompanionCharacter
                                    )
                                    QuickFeatureLaunchRow(
                                        selectedLeague = uiState.selectedLeague,
                                        currentUserRank = uiState.currentUserRank,
                                        currentUserWeeklyXp = uiState.currentUserWeeklyXp,
                                        onOpenCourseStudio = {
                                            viewModel.navigateToDestination(AppScreenDestination.COURSE_STUDIO)
                                        },
                                        onOpenLeaderboard = {
                                            viewModel.navigateToDestination(AppScreenDestination.LEADERBOARD)
                                        }
                                    )
                                    val checkpoint = uiState.resumeCheckpoint
                                    if (checkpoint != null) {
                                        ResumeLearningBanner(
                                            checkpoint = checkpoint,
                                            selectedCompanion = uiState.selectedCompanion,
                                            onResumeClick = viewModel::resumeFromLastCheckpoint
                                        )
                                    }
                                }
                            }

                            // Discover Section: Search & Explore Learning Subjects, Add New Custom Subjects with Firestore Cloud Sync
                            item(key = "discover_subjects_section") {
                                DiscoverSubjectsSection(
                                    searchQuery = uiState.discoverSearchQuery,
                                    selectedCategory = uiState.discoverSelectedCategory,
                                    userAddedSubjects = uiState.userAddedSubjects,
                                    activeCourseId = uiState.activeCourse?.id,
                                    isExpanded = isExpanded,
                                    selectedCompanion = uiState.selectedCompanion,
                                    onSearchQueryChange = viewModel::onDiscoverSearchQueryChange,
                                    onSelectCategory = viewModel::onDiscoverSelectedCategoryChange,
                                    onSelectSubject = { subject ->
                                        viewModel.launchOrSelectDiscoverSubject(
                                            subject = subject,
                                            onReady = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(5)
                                                }
                                            }
                                        )
                                    },
                                    onAddNewSubject = { title, category, level, goal ->
                                        viewModel.addNewDiscoverSubject(
                                            title = title,
                                            category = category,
                                            level = level,
                                            goal = goal,
                                            onSuccess = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(5)
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                            // 1: Trending Courses Financial Market Dashboard + Multi-Instrument & Multi-Graph Learning Lab
                            item(key = "trending_market_section") {
                                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                    TrendingCoursesMarketDashboard(
                                        tickers = uiState.marketTickers,
                                        selectedTickerId = uiState.selectedTickerId,
                                        timeframe = uiState.marketTimeframe,
                                        chartType = uiState.marketChartType,
                                        filterTab = uiState.marketFilterTab,
                                        searchQuery = uiState.marketSearchQuery,
                                        bookmarkedTickerIds = uiState.bookmarkedTickerIds,
                                        isLiveFeedActive = uiState.isMarketLiveFeedActive,
                                        globalSummary = uiState.marketGlobalSummary,
                                        isExpanded = isExpanded,
                                        onSelectTicker = viewModel::selectMarketTicker,
                                        onSelectTimeframe = viewModel::selectMarketTimeframe,
                                        onSelectChartType = viewModel::selectMarketChartType,
                                        onSelectFilterTab = viewModel::selectMarketFilterTab,
                                        onSearchQueryChange = viewModel::onMarketSearchQueryChange,
                                        onToggleBookmarkTicker = viewModel::toggleBookmarkTicker,
                                        onToggleLiveFeed = viewModel::toggleMarketLiveFeed,
                                        onLaunchTickerCourse = { ticker ->
                                            viewModel.launchOrSelectTickerCourse(
                                                ticker = ticker,
                                                onScrollToCourse = {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(4)
                                                    }
                                                },
                                                onScrollToBuilder = {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(3)
                                                    }
                                                }
                                            )
                                        }
                                    )
                                    StockInstrumentsAndGraphsLearningLab()
                                }
                            }

                            // 2: Novice to Grandmaster (GM) Chess Training Interactive Board & Curriculum Launcher
                            item(key = "chess_gm_training_section") {
                                NoviceToGmChessTrainingCard(
                                    chessPuzzlesSolvedCount = uiState.dailyStreak.chessPuzzlesSolvedCount,
                                    onPuzzleSolved = { viewModel.recordChessPuzzleSolved() },
                                    onLaunchFullChessCourse = {
                                        viewModel.openChessGmCourse(
                                            onReady = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(4)
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                            // 3: Create a Course section + Share Market & Chess GM Course Launchers + Edam Studio Launcher
                            item(key = "create_section") {
                                CreateCourseSection(
                                    courseName = uiState.courseName,
                                    selectedLevel = uiState.level,
                                    goal = uiState.goal,
                                    availableLevels = viewModel.availableLevels,
                                    isGenerating = uiState.isGeneratingCourse,
                                    courseCreationStageIndex = uiState.courseCreationStageIndex,
                                    courseCreationProgress = uiState.courseCreationProgress,
                                    statusMessage = uiState.statusMessage,
                                    selectedCompanion = uiState.selectedCompanion,
                                    onCourseNameChange = viewModel::onCourseNameChange,
                                    onLevelChange = viewModel::onLevelChange,
                                    onGoalChange = viewModel::onGoalChange,
                                    onPresetClick = viewModel::applyQuickPreset,
                                    onOpenCourseStudio = {
                                        viewModel.navigateToDestination(AppScreenDestination.COURSE_STUDIO)
                                    },
                                    onOpenShareMarketCourse = {
                                        viewModel.openShareMarketCourse(
                                            onReady = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(4)
                                                }
                                            }
                                        )
                                    },
                                    onOpenChessGmCourse = {
                                        viewModel.openChessGmCourse(
                                            onReady = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(4)
                                                }
                                            }
                                        )
                                    },
                                    onCreateCourse = {
                                        viewModel.createCourse(
                                            onCourseCreated = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(4)
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                            // 4: Active Course section with Offline Ready indicator & Animated Entrance (or Mascot Empty State)
                            val course = uiState.activeCourse
                            if (course != null) {
                                item(key = "course_header_animated_section") {
                                    AnimatedContent(
                                        targetState = course,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(380)) + expandVertically(animationSpec = tween(380)))
                                                .togetherWith(fadeOut(animationSpec = tween(220)))
                                        },
                                        label = "active_course_transition"
                                    ) { animatedCourse ->
                                        CourseOverviewCard(
                                            course = animatedCourse,
                                            cachedLessonsCount = uiState.cachedLessonsCount,
                                            selectedCompanion = uiState.selectedCompanion,
                                            onEnsureOffline = viewModel::ensureActiveCourseOfflineReady,
                                            onOpenLesson = { unitId, lessonId ->
                                                viewModel.openLesson(unitId = unitId, lessonId = lessonId)
                                            }
                                        )
                                    }
                                }
                            } else {
                                item(key = "empty_course_state") {
                                    EmptyCoursePlaceholderCard(
                                        selectedCompanion = uiState.selectedCompanion,
                                        onLoadShareMarket = { viewModel.openShareMarketCourse() },
                                        onLoadChessGm = { viewModel.openChessGmCourse() },
                                        onOpenStudio = {
                                            viewModel.navigateToDestination(AppScreenDestination.COURSE_STUDIO)
                                        }
                                    )
                                }
                            }

                            // Prototype security notice footer
                            item(key = "prototype_notice") {
                                Text(
                                    text = stringResource(R.string.security_prototype_notice),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Lesson Modal with Visual Quiz Progress Tracker & Animated Mascot
            val modalState = uiState.lessonModalState
            if (modalState != null) {
                val isCompleted = uiState.activeCourse?.completed?.contains(modalState.lessonSummary.id) == true
                LessonModalDialog(
                    courseTitle = uiState.activeCourse?.title.orEmpty(),
                    modalState = modalState,
                    isLessonCompleted = isCompleted,
                    selectedCompanion = uiState.selectedCompanion,
                    chessPuzzlesSolvedCount = uiState.dailyStreak.chessPuzzlesSolvedCount,
                    onChessPuzzleSolved = { viewModel.recordChessPuzzleSolved() },
                    onClose = viewModel::closeLesson,
                    onRetry = {
                        viewModel.openLesson(
                            unitId = modalState.unit.id,
                            lessonId = modalState.lessonSummary.id,
                            forceRefresh = true
                        )
                    },
                    onAnswerQuestion = viewModel::answerQuestion,
                    onMarkComplete = viewModel::completeCurrentLesson
                )
            }

            // Comprehensive Settings Modal (Account, Basic/Pro/Max Plans + Developer Code, Appearance & Theme)
            if (uiState.showSettingsModal) {
                SettingsModalDialog(
                    uiState = uiState,
                    onClose = { viewModel.toggleSettingsModal(false) },
                    onSelectPlan = viewModel::selectPlanTier,
                    onPromoCodeChange = viewModel::onPromoCodeInputChange,
                    onApplyPromoCode = viewModel::applyPromoCode,
                    onSelectTheme = viewModel::selectThemeMode,
                    onSignOut = {
                        viewModel.toggleSettingsModal(false)
                        signOutUser(
                            credentialManager = credentialManager,
                            onSignOutComplete = onSignOutComplete,
                            scope = coroutineScope
                        )
                    }
                )
            }

            // User Profile Screen with earned badges, SharedPreferences daily streak, DataStore daily lesson goal & companions
            if (uiState.showProfileModal) {
                UserProfileDialog(
                    userDisplayName = uiState.userDisplayName,
                    userEmail = uiState.userEmail,
                    planTier = uiState.planTier,
                    totalCoursesCount = uiState.savedCourses.size,
                    totalCompletedLessons = uiState.totalCompletedLessonsCount,
                    averageMasteryPct = uiState.averageMasteryPercentage,
                    badgeDisplayItems = uiState.badgeDisplayItems,
                    pushNotificationsEnabled = uiState.pushNotificationsEnabled,
                    dailyStreak = uiState.dailyStreak,
                    selectedCompanion = uiState.selectedCompanion,
                    onSelectCompanion = viewModel::selectCompanionCharacter,
                    themeMode = uiState.themeMode,
                    onSelectTheme = viewModel::selectThemeMode,
                    onCheckInStreak = { viewModel.checkInDailyStreak() },
                    onToggleStreakFreeze = viewModel::toggleStreakFreeze,
                    dailyLessonGoal = uiState.dailyLessonGoal,
                    dailyLessonsCompletedToday = uiState.dailyLessonsCompletedToday,
                    onSetDailyLessonGoal = viewModel::setDailyLessonGoal,
                    onRecordDailyLessonProgress = { viewModel.recordDailyLessonGoalProgress() },
                    dailyReminderEnabled = uiState.dailyReminderEnabled,
                    dailyReminderHour = uiState.dailyReminderHour,
                    dailyReminderMinute = uiState.dailyReminderMinute,
                    onToggleDailyReminder = viewModel::setDailyReminderEnabled,
                    onUpdateDailyReminderTime = viewModel::updateDailyReminderTime,
                    onTriggerImmediateGoalReminder = viewModel::triggerImmediateDailyGoalReminder,
                    onTogglePushNotifications = viewModel::setPushNotificationsEnabled,
                    onSendTestNotification = viewModel::sendTestNotification,
                    onClose = { viewModel.toggleProfileModal(false) }
                )
            }

            // Saved courses switcher bottom sheet
            if (uiState.showSavedCoursesSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleSavedCoursesSheet(false) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    SavedCoursesBottomSheet(
                        courses = uiState.savedCourses,
                        activeCourseId = uiState.activeCourse?.id,
                        onSelectCourse = { courseId ->
                            viewModel.selectSavedCourse(courseId)
                            coroutineScope.launch {
                                listState.animateScrollToItem(3)
                            }
                        },
                        onDeleteCourse = viewModel::deleteSavedCourse
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun EdamNavigationBar(
    planTier: PlanTier,
    currentDestination: AppScreenDestination,
    savedCoursesCount: Int,
    unlockedBadgesCount: Int,
    currentUserRank: Int,
    selectedLeagueEmoji: String,
    currentStreak: Int,
    selectedCompanion: EdamCompanionCharacter,
    themeMode: EdamThemeMode,
    onToggleTheme: () -> Unit,
    onGoHome: () -> Unit,
    onGoToStudio: () -> Unit,
    onGoToLeaderboard: () -> Unit,
    onGoToMarket: () -> Unit,
    onGoToCreate: () -> Unit,
    onOpenSavedCourses: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("edam_nav_bar"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 6.dp,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onGoHome)
                    .padding(vertical = 2.dp, horizontal = 4.dp)
                    .testTag("nav_home_logo_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF1F2937)),
                    contentAlignment = Alignment.Center
                ) {
                    EdamMascot(
                        expression = EdamExpression.HAPPY,
                        character = selectedCompanion,
                        size = 34.dp,
                        showTablet = false
                    )
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Active Plan Tier Badge
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (planTier == PlanTier.MAX) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = planTier.displayName.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = if (planTier == PlanTier.MAX) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("nav_plan_badge")
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // AI Course Outline Studio Navigation Button
                TextButton(
                    onClick = onGoToStudio,
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (currentDestination == AppScreenDestination.COURSE_STUDIO) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_ai_studio_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = stringResource(R.string.nav_ai_studio),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.nav_ai_studio),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Duolingo Competitive Leagues Navigation Button
                TextButton(
                    onClick = onGoToLeaderboard,
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (currentDestination == AppScreenDestination.LEADERBOARD) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_leagues_button")
                ) {
                    Text(
                        text = "$selectedLeagueEmoji #$currentUserRank",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }

                if (savedCoursesCount > 0) {
                    TextButton(
                        onClick = onOpenSavedCourses,
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("nav_saved_courses_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = stringResource(R.string.nav_saved_courses),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$savedCoursesCount",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                TextButton(
                    onClick = onGoToMarket,
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_market_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = stringResource(R.string.nav_trending_market),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Profile, Daily Streak & Badges navigation button
                TextButton(
                    onClick = onOpenProfile,
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = stringResource(R.string.nav_profile),
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier
                            .size(18.dp)
                            .testTag("nav_streak_flame_icon")
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${currentStreak}d",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("nav_streak_count_text")
                    )
                }

                // Theme Switcher Quick Toggle (☀️ Light / 🌙 Dark)
                EdamThemeQuickToggleButton(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.nav_settings_description),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScreenRegistryQuickBar(
    screens: List<ScreenEntity>,
    currentDestination: AppScreenDestination,
    onSelectScreen: (ScreenEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("screen_entity_registry_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        screens.forEach { screen ->
            val isCurrent = screen.screenId == currentDestination.screenEntityId
            Surface(
                onClick = { onSelectScreen(screen) },
                shape = RoundedCornerShape(999.dp),
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                },
                border = BorderStroke(
                    width = if (isCurrent) 1.5.dp else 1.dp,
                    color = if (isCurrent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .testTag("screen_entity_chip_${screen.route}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val icon = when (screen.route) {
                        "course_studio" -> Icons.Filled.AutoAwesome
                        "leaderboard" -> Icons.Filled.EmojiEvents
                        "market" -> Icons.AutoMirrored.Filled.ShowChart
                        "profile" -> Icons.Filled.Verified
                        else -> Icons.Filled.School
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${screen.visitCount}v",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickFeatureLaunchRow(
    selectedLeague: LeagueTier,
    currentUserRank: Int,
    currentUserWeeklyXp: Int,
    onOpenCourseStudio: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_feature_launch_row"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: AI Course Outline Studio (Gemini 3.5 Flash)
        Surface(
            onClick = onOpenCourseStudio,
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
            tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("home_launch_studio_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.course_studio_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Input any topic to generate a structured Edam course syllabus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Card 2: Edam Competitive Leaderboard
        Surface(
            onClick = onOpenLeaderboard,
            shape = RoundedCornerShape(22.dp),
            color = Color(selectedLeague.primaryColorHex).copy(alpha = 0.14f),
            border = BorderStroke(1.5.dp, Color(selectedLeague.primaryColorHex).copy(alpha = 0.75f)),
            tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("home_launch_leaderboard_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedLeague.emoji,
                        fontSize = 22.sp
                    )
                    Text(
                        text = selectedLeague.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Rank #$currentUserRank · $currentUserWeeklyXp XP · Rapid Drills",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EdamHeroSection(
    isExpanded: Boolean,
    dailyStreak: DailyStreakState,
    selectedCompanion: EdamCompanionCharacter,
    onMasterFlashcard: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenChess: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenStreakDashboard: () -> Unit,
    onLogStudyActivity: () -> Unit,
    onLaunchCustomTopic: (String) -> Unit,
    onTestPushReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    var customTopicInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = if (isExpanded) 32.dp else 18.dp,
                bottom = 8.dp,
                start = 4.dp,
                end = 4.dp
            )
            .testTag("hero_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Prominent In-App Toast / Banner Reminder when daily study streak is not completed today
        if (!dailyStreak.studiedToday) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 780.dp)
                    .padding(bottom = 12.dp)
                    .testTag("hero_streak_at_risk_banner"),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF7F1D1D),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF581C87), Color(0xFF991B1B), Color(0xFFB45309))
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔥 Daily Study Streak Reminder (${dailyStreak.currentStreak}-Day Streak)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "You haven't studied today yet! Complete a quick 2-minute lesson or flip flashcards now to keep your streak burning and claim +20 XP.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF3F4F6)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onLogStudyActivity,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color(0xFF1F2937)
                            ),
                            modifier = Modifier.weight(1f).testTag("streak_banner_study_button")
                        ) {
                            Text("⚡ Study Now (+20 XP)", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onTestPushReminder,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("streak_banner_push_button")
                        ) {
                            Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Alert", maxLines = 1)
                        }
                    }
                }
            }
        }

        // Live Streak + XP + Active Companion Status Pill
        Surface(
            onClick = onOpenProfile,
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
            modifier = Modifier.testTag("hero_streak_xp_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${dailyStreak.currentStreak} Day Streak",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⚡ ${dailyStreak.totalXp} XP · Lv.${dailyStreak.xpLevel}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.hero_title),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isExpanded) 60.sp else 42.sp,
                lineHeight = if (isExpanded) 62.sp else 44.sp,
                letterSpacing = if (isExpanded) (-2.5).sp else (-1.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.hero_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 25.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 650.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Animated Edam Mascot & Companion Character Stage (replaces AI-generated book banner)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 780.dp)
                .testTag("hero_mascot_stage_card"),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF1F2937),
            border = BorderStroke(
                width = if (themeSpec.isHighContrast) 2.dp else 1.5.dp,
                color = Color(0xFFF59E0B).copy(alpha = 0.7f)
            ),
            shadowElevation = if (themeSpec.isHighContrast) 0.dp else 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF111827),
                                Color(0xFF1F2937),
                                Color(0xFF273549)
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                EdamMascot(
                    expression = if (dailyStreak.studiedToday) EdamExpression.SUCCESS else EdamExpression.HAPPY,
                    character = selectedCompanion,
                    size = if (isExpanded) 118.dp else 96.dp,
                    showTablet = true
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(selectedCompanion.sproutPrimaryHex).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${selectedCompanion.displayName.uppercase()} · ${selectedCompanion.roleTitle.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFFFBBF24),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = if (dailyStreak.studiedToday) {
                            "Awesome momentum! Your ${dailyStreak.currentStreak}-day streak is active today."
                        } else {
                            "Welcome back! Flip a quick flashcard or complete a lesson to ignite today's streak."
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        ),
                        color = Color(0xFFEDE9E4)
                    )

                    Text(
                        text = selectedCompanion.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEDE9E4).copy(alpha = 0.78f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Flashcard Deck right in the First Hero Section
        EdamHeroFlashcardDeck(
            currentStreak = dailyStreak.currentStreak,
            totalXp = dailyStreak.totalXp,
            flashcardsReviewedCount = dailyStreak.flashcardsReviewedCount,
            onMasterFlashcard = { onMasterFlashcard() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Three Highlighted Core Disciplines: Chess, Stock Market, Daily Flashcard Streaks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 780.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1: Chess Tactics & GM Academy
            Surface(
                onClick = onOpenChess,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_chess_highlight_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("♟️", fontSize = 20.sp)
                        Text(
                            text = "Chess GM",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Tactical puzzles, 8x8 board trainer & 5-unit GM curriculum guided by Vex.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                    Text(
                        text = "Open Board →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF8B5CF6)
                    )
                }
            }

            // 2: Stock Market & Live Trading Simulator
            Surface(
                onClick = onOpenMarket,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_market_highlight_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📈", fontSize = 20.sp)
                        Text(
                            text = "Stock Market",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Equities, options, RSI, OHLC candlesticks & order books with RoboBroker.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                    Text(
                        text = "Trade Simulator →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }
            }

            // 3: Daily Flashcards & Consecutive Streaks
            Surface(
                onClick = onOpenStreakDashboard,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_streak_highlight_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚡", fontSize = 20.sp)
                        Text(
                            text = "Daily Streaks",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${dailyStreak.currentStreak} days active! Review formulas, earn XP and protect your streak.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                    Text(
                        text = "Streak Hub →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Learn Any Other Subject on Earth Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 780.dp)
                .testTag("hero_learn_any_subject_card"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Learn Any Other Subject on Earth",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Edam synthesizes personalized curricula, quizzes & flashcards for any discipline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Subject Topic Pills
                val quickSubjects = listOf(
                    "🐍 Python & Coding Viruses",
                    "⚛️ Quantum Computing",
                    "🧬 Genetics & DNA",
                    "🌍 World History",
                    "🇯🇵 Japanese Fluency",
                    "🎵 Music Theory & Harmony"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(quickSubjects) { subject ->
                        SuggestionChip(
                            onClick = {
                                val clean = subject.substringAfter(" ")
                                onLaunchCustomTopic(clean)
                            },
                            label = { Text(subject, style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Custom Topic Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customTopicInput,
                        onValueChange = { customTopicInput = it },
                        placeholder = { Text("e.g. Astrophysics, Organic Chemistry...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_topic_input")
                    )

                    Button(
                        onClick = {
                            val topic = customTopicInput.ifBlank { "Quantum Physics" }
                            onLaunchCustomTopic(topic)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("launch_custom_topic_button")
                    ) {
                        Icon(imageVector = Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Home Dashboard Daily Learning Streak Card with animated Flame Icon,
 * current consecutive day count, 7-day timeline, and Firestore Cloud Streak synchronization.
 */
@Composable
private fun HomeDailyStreakDashboardCard(
    dailyStreak: DailyStreakState,
    isSignedIn: Boolean,
    onLogActivityToday: () -> Unit,
    onSyncCloudStreak: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val infiniteTransition = rememberInfiniteTransition(label = "home_streak_flame_pulse")
    val flamePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home_flame_scale"
    )

    Surface(
        onClick = onOpenProfile,
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_dashboard_streak_card"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1A2230),
        border = BorderStroke(
            width = if (themeSpec.isHighContrast) 2.dp else 1.5.dp,
            color = Color(0xFFF59E0B).copy(alpha = 0.78f)
        ),
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF1F2937),
                            Color(0xFF273549)
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Flame Icon Badge + Consecutive Day Streak Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(flamePulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFBBF24).copy(alpha = 0.38f),
                                        Color(0xFFF59E0B).copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .testTag("home_streak_flame_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Daily Learning Streak Flame",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("home_streak_flame_icon")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${dailyStreak.currentStreak}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp,
                                    lineHeight = 32.sp
                                ),
                                color = Color(0xFFFBBF24),
                                modifier = Modifier.testTag("home_streak_count_text")
                            )
                            Text(
                                text = if (dailyStreak.currentStreak == 1) "Day Streak" else "Days Streak",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFEDE9E4),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = "${dailyStreak.streakTierTitle} · Best: ${dailyStreak.longestStreak}d · ${if (dailyStreak.studiedToday) "Active Today ✓" else "Complete 1 activity today"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEDE9E4).copy(alpha = 0.78f)
                        )
                    }
                }

                // Firestore Cloud Streak Sync Status Chip
                Surface(
                    onClick = onSyncCloudStreak,
                    shape = RoundedCornerShape(999.dp),
                    color = if (dailyStreak.isCloudSynced || isSignedIn) {
                        Color(0xFF10B981).copy(alpha = 0.18f)
                    } else {
                        Color(0xFFF59E0B).copy(alpha = 0.18f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (dailyStreak.isCloudSynced || isSignedIn) Color(0xFF10B981) else Color(0xFFF59E0B)
                    ),
                    modifier = Modifier.testTag("home_streak_firestore_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (dailyStreak.isCloudSynced || isSignedIn) {
                                Icons.Filled.CloudDone
                            } else {
                                Icons.Filled.CloudSync
                            },
                            contentDescription = "Firestore Streak Sync",
                            tint = if (dailyStreak.isCloudSynced || isSignedIn) {
                                Color(0xFF34D399)
                            } else {
                                Color(0xFFFBBF24)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (dailyStreak.isCloudSynced || isSignedIn) {
                                "Firestore Synced"
                            } else {
                                "Firestore Streak"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (dailyStreak.isCloudSynced || isSignedIn) {
                                Color(0xFF34D399)
                            } else {
                                Color(0xFFFBBF24)
                            }
                        )
                    }
                }
            }

            // 7-Day Monday–Sunday Consecutive Flame Chain + Quick Log Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dayLabels.forEachIndexed { idx, label ->
                        val active = dailyStreak.weeklyStudyDays.getOrElse(idx) { false }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (active) Color(0xFFF59E0B) else Color(0xFF374151)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "$label activity",
                                    tint = if (active) Color(0xFF111827) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (active) Color(0xFFFBBF24) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                Button(
                    onClick = onLogActivityToday,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dailyStreak.studiedToday) {
                            Color(0xFF10B981)
                        } else {
                            Color(0xFFF59E0B)
                        },
                        contentColor = Color(0xFF111827)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_streak_check_in_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (dailyStreak.studiedToday) "Logged ✓" else "Check In",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumeLearningBanner(
    checkpoint: ResumeCheckpointInfo,
    selectedCompanion: EdamCompanionCharacter,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("resume_learning_banner"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 3.dp,
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 8.dp,
        border = BorderStroke(
            width = if (themeSpec.isHighContrast) 2.dp else 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (themeSpec.isHighContrast) 1f else 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EdamMascot(
                expression = EdamExpression.CURIOUS,
                character = selectedCompanion,
                size = 56.dp,
                showTablet = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.resume_banner_eyebrow),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = checkpoint.lessonTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${checkpoint.courseTitle} · ${checkpoint.unitTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (checkpoint.savedAnswersCount > 0) {
                    Text(
                        text = stringResource(
                            R.string.resume_quiz_checkpoint_format,
                            checkpoint.savedAnswersCount
                        ),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Button(
                onClick = onResumeClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("resume_where_left_off_button")
            ) {
                Text(
                    text = stringResource(R.string.resume_banner_button),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverSubjectsSection(
    searchQuery: String,
    selectedCategory: String,
    userAddedSubjects: List<DiscoverSubject>,
    activeCourseId: String?,
    isExpanded: Boolean,
    selectedCompanion: EdamCompanionCharacter,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectSubject: (DiscoverSubject) -> Unit,
    onAddNewSubject: (title: String, category: String, level: String, goal: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val themeSpec = LocalEdamThemeSpec.current

    val allSubjects = remember(userAddedSubjects) {
        val presetIds = DiscoverCatalog.PRESET_SUBJECTS.map { it.id }.toSet()
        userAddedSubjects.filter { it.id !in presetIds } + DiscoverCatalog.PRESET_SUBJECTS
    }

    val filteredSubjects = remember(allSubjects, searchQuery, selectedCategory) {
        allSubjects.filter { subject ->
            val matchesCategory = selectedCategory == "All" ||
                subject.category.equals(selectedCategory, ignoreCase = true) ||
                subject.category.contains(selectedCategory.split(" ").first(), ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                subject.title.contains(searchQuery, ignoreCase = true) ||
                subject.description.contains(searchQuery, ignoreCase = true) ||
                subject.tagList.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("discover_subjects_section"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 8.dp,
        border = BorderStroke(
            width = if (themeSpec.isHighContrast) 2.dp else 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Title and "Add Subject" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Discover New Subjects",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Search catalog or add custom topics with real-time Firestore sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("btn_open_add_subject_dialog"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Subject", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discover_search_input"),
                placeholder = {
                    Text(
                        "Search subjects, topics, or fields (e.g. AI, Trading, Physics...)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DiscoverCatalog.CATEGORIES) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(999.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // Subjects List or Empty State
            if (filteredSubjects.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EdamMascot(
                            expression = EdamExpression.CURIOUS,
                            character = selectedCompanion,
                            size = 72.dp,
                            showTablet = false
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No existing subjects found for \"$searchQuery\""
                            } else {
                                "No subjects in this category yet"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Would you like Edam to synthesize a brand new curriculum for this topic and sync it with Firestore?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isNotBlank()) {
                            Button(
                                onClick = {
                                    onAddNewSubject(
                                        searchQuery.trim(),
                                        selectedCategory.takeIf { it != "All" } ?: "Technology & AI",
                                        "Beginner",
                                        "Comprehensive foundational and applied mastery in $searchQuery"
                                    )
                                },
                                modifier = Modifier.testTag("btn_generate_search_query_course"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate \"$searchQuery\" with Edam (+60 XP)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredSubjects.forEach { subject ->
                        val isActive = subject.id == activeCourseId
                        DiscoverSubjectCard(
                            subject = subject,
                            isActive = isActive,
                            onSelect = { onSelectSubject(subject) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDiscoverSubjectDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { title, cat, lvl, goal ->
                onAddNewSubject(title, cat, lvl, goal)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DiscoverSubjectCard(
    subject: DiscoverSubject,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discover_subject_card_${subject.id}")
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = subject.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (subject.isUserAdded) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Firestore Synced",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = subject.level,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = subject.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subject.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text("${subject.unitsCount} Units", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Text("+${subject.xpReward} XP", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF59E0B))
                    }
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Text("Active Subject", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF10B981))
                        }
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Start Learning", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDiscoverSubjectDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, level: String, goal: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technology & AI") }
    var level by remember { mutableStateOf("Intermediate") }
    var goal by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Technology & AI", "Finance & Markets", "Strategy & Logic", "Science & Math", "Humanities")
    val levels = listOf("Beginner", "Intermediate", "Advanced", "Mastery")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Learning Subject",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Curriculum will be saved & synced with Firestore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subject Title") },
                    placeholder = { Text("e.g. Quantum Cryptography, Biohacking...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_subject_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = levelExpanded,
                        onExpandedChange = { levelExpanded = !levelExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = level,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Level") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = levelExpanded,
                            onDismissRequest = { levelExpanded = false }
                        ) {
                            levels.forEach { lvl ->
                                DropdownMenuItem(
                                    text = { Text(lvl) },
                                    onClick = {
                                        level = lvl
                                        levelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Learning Goal / Focus") },
                    placeholder = { Text("What core competencies do you want to master?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_subject_goal_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSubmit(
                                    title.trim(),
                                    category,
                                    level,
                                    goal.ifBlank { "Master $title from foundations to advanced applications" }
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("add_subject_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add & Sync Subject", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateCourseSection(
    courseName: String,
    selectedLevel: String,
    goal: String,
    availableLevels: List<String>,
    isGenerating: Boolean,
    courseCreationStageIndex: Int = 0,
    courseCreationProgress: Float = 0f,
    statusMessage: String,
    selectedCompanion: EdamCompanionCharacter,
    onCourseNameChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onPresetClick: (String, String, String) -> Unit,
    onOpenCourseStudio: () -> Unit,
    onOpenShareMarketCourse: () -> Unit,
    onOpenChessGmCourse: () -> Unit,
    onCreateCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelDropdownExpanded by remember { mutableStateOf(false) }
    val themeSpec = LocalEdamThemeSpec.current
    val fieldSurfaceAlpha = if (themeSpec.isHighContrast) 1f else 0.85f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("create_section"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 8.dp,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EdamMascot(
                    expression = if (isGenerating) EdamExpression.WORKING else EdamExpression.HAPPY,
                    character = selectedCompanion,
                    size = 64.dp,
                    showTablet = true
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.create_section_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.create_section_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Edam Course Outline Architect Launcher
            Button(
                onClick = onOpenCourseStudio,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("open_course_studio_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Edam Course Outline Studio",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Featured Share Market Course Banner Button (Instant Offline Ready)
            OutlinedButton(
                onClick = onOpenShareMarketCourse,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    contentColor = if (themeSpec.isHighContrast) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("load_share_market_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_load_share_market),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Featured Novice to Grandmaster (GM) Chess Academy Course Button
            OutlinedButton(
                onClick = onOpenChessGmCourse,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(themeSpec.borderWidth, Color(EdamCompanionCharacter.VEX.sproutPrimaryHex)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("load_chess_gm_course_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Color(EdamCompanionCharacter.VEX.sproutPrimaryHex),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "♟️ Load Novice to Grandmaster (GM) Chess Course (Offline Ready)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick topic suggestions including Share Market & Novice to GM Chess
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf(
                    Triple(
                        "Share Market & Technical Analysis",
                        "Intermediate",
                        "Read candlestick charts, evaluate P/E & ROE fundamentals, and manage portfolio risk"
                    ),
                    Triple(
                        "Chess: Novice to Grandmaster (GM) Mastery",
                        "Beginner to Advanced",
                        "Master opening theory, tactical combinations, positional structures, and GM endgames"
                    ),
                    Triple(
                        "Python & Data Analysis",
                        "Beginner",
                        "Write clean Python scripts and analyze real-world datasets from scratch"
                    )
                )
                presets.forEachIndexed { index, (presetTitle, presetLevel, presetGoal) ->
                    SuggestionChip(
                        onClick = { onPresetClick(presetTitle, presetLevel, presetGoal) },
                        label = {
                            Text(
                                text = presetTitle,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = fieldSurfaceAlpha),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (themeSpec.isHighContrast) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            },
                            borderWidth = themeSpec.borderWidth
                        ),
                        modifier = Modifier.testTag("preset_chip_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Course Name Input (#courseName)
            OutlinedTextField(
                value = courseName,
                onValueChange = onCourseNameChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.placeholder_course_name),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = fieldSurfaceAlpha),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("course_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Level Selector (#level)
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { levelDropdownExpanded = true }
                        .testTag("level_selector"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = fieldSurfaceAlpha),
                    border = BorderStroke(
                        themeSpec.borderWidth,
                        if (levelDropdownExpanded) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedLevel.ifBlank { stringResource(R.string.placeholder_level) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedLevel.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = stringResource(R.string.placeholder_level),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = levelDropdownExpanded,
                    onDismissRequest = { levelDropdownExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .widthIn(min = 240.dp)
                ) {
                    availableLevels.forEach { levelOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = levelOption,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onLevelChange(levelOption)
                                levelDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("level_option_${levelOption.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Goal Textarea (#goal)
            OutlinedTextField(
                value = goal,
                onValueChange = onGoalChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.placeholder_goal),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                minLines = 4,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = fieldSurfaceAlpha),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .testTag("goal_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Primary Button (#generateBtn)
            Button(
                onClick = onCreateCourse,
                enabled = !isGenerating,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("generate_course_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.btn_building_course),
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_create_with_gemini),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // 5. Animated Course Creation Stage & Status Line (#status) with Edam Mascot feedback
            AnimatedVisibility(
                visible = isGenerating,
                enter = fadeIn(tween(260)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(240))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    NewCourseGenerationAnimationCard(
                        courseTitle = courseName.ifBlank { "Your New Edam Course" },
                        stageIndex = courseCreationStageIndex,
                        progress = courseCreationProgress,
                        statusMessage = statusMessage,
                        selectedCompanion = selectedCompanion
                    )
                }
            }

            if (isGenerating || statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                val isErrorStatus = statusMessage.startsWith("Something went wrong") ||
                    statusMessage.startsWith("Fill in")
                EdamMascotSpeechBanner(
                    title = when {
                        isGenerating -> "${selectedCompanion.displayName} is crafting your course..."
                        isErrorStatus -> "${selectedCompanion.displayName} noticed an issue"
                        else -> "${selectedCompanion.displayName} Course Update"
                    },
                    message = statusMessage.ifBlank { "Designing units, lessons, and interactive practice quizzes..." },
                    expression = when {
                        isGenerating -> EdamExpression.WORKING
                        isErrorStatus -> EdamExpression.ENCOURAGEMENT
                        else -> EdamExpression.SUCCESS
                    },
                    character = selectedCompanion,
                    modifier = Modifier.testTag("status_text")
                )
            }
        }
    }
}

@Composable
private fun NewCourseGenerationAnimationCard(
    courseTitle: String,
    stageIndex: Int,
    progress: Float,
    statusMessage: String,
    selectedCompanion: EdamCompanionCharacter,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "new_course_gen_transition")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "course_orbit_angle"
    )
    val mascotPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "course_mascot_pulse"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.15f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 220f),
        label = "course_gen_progress_bar"
    )

    val stages = remember {
        listOf(
            "Analyzing topic prerequisites & calibrating proficiency",
            "Structuring modular units & progressive learning milestones",
            "Crafting interactive lessons, explanations & practice quizzes",
            "Bundling 100% offline study pack & unlocking XP rewards"
        )
    }

    val primaryAccent = Color(selectedCompanion.sproutPrimaryHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("course_generation_animation_card"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.5.dp, primaryAccent.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1F2937),
                            Color(0xFF111827)
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Orbiting Energy Rings + Pulsing Edam Mascot Stage
            Box(
                modifier = Modifier.size(132.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 4.dp.toPx()
                    val outerRadius = (size.minDimension - strokePx * 2) / 2f
                    val innerRadius = outerRadius * 0.78f

                    // Outer ambient ring
                    drawCircle(
                        color = primaryAccent.copy(alpha = 0.18f),
                        radius = outerRadius,
                        style = Stroke(width = strokePx)
                    )

                    // Rotating outer energy arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                primaryAccent,
                                Color(0xFFFBBF24),
                                Color.Transparent
                            )
                        ),
                        startAngle = orbitAngle,
                        sweepAngle = 210f,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Counter-rotating inner arc
                    drawArc(
                        color = Color(0xFF10B981).copy(alpha = 0.75f),
                        startAngle = -orbitAngle * 1.3f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(
                            (size.width - innerRadius * 2) / 2f,
                            (size.height - innerRadius * 2) / 2f
                        ),
                        size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Box(modifier = Modifier.scale(mascotPulse)) {
                    EdamMascot(
                        expression = EdamExpression.WORKING,
                        character = selectedCompanion,
                        size = 84.dp,
                        showTablet = true
                    )
                }
            }

            Text(
                text = "Building \"$courseTitle\"",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFFEDE9E4),
                textAlign = TextAlign.Center
            )

            Text(
                text = statusMessage.ifBlank {
                    "${selectedCompanion.displayName} is assembling your custom curriculum..."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFBBF24),
                textAlign = TextAlign.Center
            )

            // Animated Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Course Synthesis Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEDE9E4).copy(alpha = 0.75f)
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = primaryAccent
                    )
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = primaryAccent,
                    trackColor = Color(0xFF374151),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .testTag("course_generation_progress_bar")
                )
            }

            // 4-Stage Animated Checklist
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stages.forEachIndexed { idx, stepLabel ->
                    val isDone = idx < stageIndex
                    val isCurrent = idx == stageIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isCurrent -> primaryAccent.copy(alpha = 0.16f)
                                    isDone -> Color(0xFF10B981).copy(alpha = 0.12f)
                                    else -> Color(0xFF1F2937).copy(alpha = 0.5f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isCurrent) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = primaryAccent
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF374151))
                            )
                        }
                        Text(
                            text = stepLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = when {
                                isDone -> Color(0xFF34D399)
                                isCurrent -> Color(0xFFEDE9E4)
                                else -> Color(0xFFEDE9E4).copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCoursePlaceholderCard(
    selectedCompanion: EdamCompanionCharacter,
    onLoadShareMarket: () -> Unit,
    onLoadChessGm: () -> Unit,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_course_state_card"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EdamMascot(
                expression = EdamExpression.SLEEP,
                character = selectedCompanion,
                size = 92.dp,
                showTablet = false
            )
            Text(
                text = "${selectedCompanion.displayName} is resting until you pick a course",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Launch our complete Share Market or Novice-to-GM Chess Academy below, or generate a custom syllabus with Edam.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onLoadShareMarket,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Share Market", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onLoadChessGm,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Chess GM", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = onOpenStudio,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edam Studio", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseOverviewCard(
    course: Course,
    cachedLessonsCount: Int,
    selectedCompanion: EdamCompanionCharacter,
    onEnsureOffline: () -> Unit,
    onOpenLesson: (unitId: String, lessonId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    val innerSurfaceAlpha = if (themeSpec.isHighContrast) 1f else 0.78f
    val totalLessons = course.totalLessons.coerceAtLeast(1)
    val savedOfflineCount = cachedLessonsCount.coerceAtMost(course.totalLessons)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_section"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 8.dp,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Course Header (.course-header)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = innerSurfaceAlpha),
                border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Level Badge (.badge)
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = if (themeSpec.isHighContrast) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            } else null
                        ) {
                            Text(
                                text = course.level,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (themeSpec.isHighContrast) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }

                        // Offline Ready Badge
                        Surface(
                            onClick = onEnsureOffline,
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("offline_ready_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OfflinePin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.offline_ready_badge,
                                        savedOfflineCount,
                                        course.totalLessons
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Course Title + Companion Mascot Guide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("course_title_text")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        EdamMascot(
                            expression = if (course.progressPercentage >= 100) {
                                EdamExpression.SUCCESS
                            } else {
                                EdamExpression.HAPPY
                            },
                            character = when {
                                course.title.contains("Chess", ignoreCase = true) -> EdamCompanionCharacter.VEX
                                course.title.contains("Market", ignoreCase = true) ||
                                    course.title.contains("Stock", ignoreCase = true) -> EdamCompanionCharacter.KORA
                                else -> selectedCompanion
                            },
                            size = 76.dp,
                            showTablet = true
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress Bar (.progress)
                    val animatedProgress by animateFloatAsState(
                        targetValue = (course.progressPercentage / 100f).coerceIn(0f, 1f),
                        animationSpec = spring(),
                        label = "course_progress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(99.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(
                            R.string.course_progress_format,
                            course.completedLessonsCount,
                            course.totalLessons,
                            course.progressPercentage
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("course_progress_text")
                    )

                    // Outcomes (.outcomes)
                    if (course.outcomes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            course.outcomes.forEach { outcome ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.background.copy(alpha = innerSurfaceAlpha),
                                    border = if (themeSpec.isHighContrast) {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    } else null
                                ) {
                                    Text(
                                        text = "✓ $outcome",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Units (.unit)
            course.units.forEachIndexed { unitIndex, unit ->
                UnitCard(
                    unitIndex = unitIndex,
                    unit = unit,
                    completedLessonIds = course.completed,
                    onOpenLesson = { lessonId ->
                        onOpenLesson(unit.id, lessonId)
                    }
                )
            }
        }
    }
}

@Composable
private fun UnitCard(
    unitIndex: Int,
    unit: CourseUnit,
    completedLessonIds: List<String>,
    onOpenLesson: (lessonId: String) -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    val unitAlpha = if (themeSpec.isHighContrast) 1f else 0.65f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unit_card_${unit.id}"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = unitAlpha),
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.unit_header_format, unitIndex + 1, unit.title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = unit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                unit.lessons.forEachIndexed { lessonIndex, lesson ->
                    val isComplete = completedLessonIds.contains(lesson.id)
                    LessonRowButton(
                        lessonNumber = lessonIndex + 1,
                        lesson = lesson,
                        isComplete = isComplete,
                        onClick = { onOpenLesson(lesson.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRowButton(
    lessonNumber: Int,
    lesson: LessonSummary,
    isComplete: Boolean,
    onClick: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    val rowAlpha = if (themeSpec.isHighContrast) 1f else 0.88f

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("lesson_button_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = rowAlpha),
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circle lesson number (.lesson-number)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$lessonNumber",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        color = if (themeSpec.isHighContrast) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (lesson.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = lesson.summary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isComplete) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.testTag("lesson_completed_check_${lesson.id}")
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LessonModalDialog(
    courseTitle: String,
    modalState: LessonModalState,
    isLessonCompleted: Boolean,
    selectedCompanion: EdamCompanionCharacter,
    chessPuzzlesSolvedCount: Int,
    onChessPuzzleSolved: () -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onAnswerQuestion: (questionIndex: Int, optionIndex: Int) -> Unit,
    onMarkComplete: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .testTag("lesson_modal"),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 850.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = if (themeSpec.isHighContrast) 0.dp else 24.dp,
                border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
            ) {
                val scrollState = rememberScrollState()
                val displayedTitle = modalState.lessonContent?.title ?: modalState.lessonSummary.title
                val displayedObjective = when {
                    modalState.isLoading -> stringResource(R.string.lesson_loading_objective)
                    modalState.lessonContent != null -> modalState.lessonContent.objective
                    else -> modalState.lessonSummary.summary
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                ) {
                    // Modal Top (.modal-top)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayedTitle,
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("lesson_modal_title")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = displayedObjective,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("lesson_modal_objective")
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                                .testTag("close_lesson_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close_lesson_content_description),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when {
                        modalState.isLoading -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                EdamMascot(
                                    expression = EdamExpression.THINKING,
                                    character = selectedCompanion,
                                    size = 96.dp,
                                    showTablet = true
                                )
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.lesson_loading_body),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        modalState.errorMessage != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EdamMascotSpeechBanner(
                                    title = stringResource(R.string.lesson_error_title),
                                    message = modalState.errorMessage,
                                    expression = EdamExpression.ENCOURAGEMENT,
                                    character = selectedCompanion
                                )
                                OutlinedButton(
                                    onClick = onRetry,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("retry_lesson_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = stringResource(R.string.retry_lesson_button))
                                }
                            }
                        }

                        modalState.lessonContent != null -> {
                            LessonLoadedContent(
                                courseTitle = courseTitle,
                                lesson = modalState.lessonContent,
                                selectedAnswers = modalState.selectedAnswers,
                                isLessonCompleted = isLessonCompleted,
                                selectedCompanion = selectedCompanion,
                                chessPuzzlesSolvedCount = chessPuzzlesSolvedCount,
                                onChessPuzzleSolved = onChessPuzzleSolved,
                                onAnswerQuestion = onAnswerQuestion,
                                onMarkComplete = onMarkComplete
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonLoadedContent(
    courseTitle: String,
    lesson: LessonContent,
    selectedAnswers: Map<Int, Int>,
    isLessonCompleted: Boolean,
    selectedCompanion: EdamCompanionCharacter,
    chessPuzzlesSolvedCount: Int,
    onChessPuzzleSolved: () -> Unit,
    onAnswerQuestion: (questionIndex: Int, optionIndex: Int) -> Unit,
    onMarkComplete: () -> Unit
) {
    val activeGuide = when {
        courseTitle.contains("Chess", ignoreCase = true) ||
            lesson.title.contains("Chess", ignoreCase = true) ||
            lesson.title.contains("Grandmaster", ignoreCase = true) -> EdamCompanionCharacter.VEX
        courseTitle.contains("Market", ignoreCase = true) ||
            courseTitle.contains("Stock", ignoreCase = true) ||
            lesson.title.contains("Candlestick", ignoreCase = true) -> EdamCompanionCharacter.KORA
        else -> selectedCompanion
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Companion Study Coach Tip Banner
        EdamMascotSpeechBanner(
            title = "${activeGuide.displayName}'s Study Insight",
            message = lesson.objective.ifBlank {
                "Read each concept carefully, explore the interactive visual lab, and test your recall in the quiz below to earn XP!"
            },
            expression = if (isLessonCompleted) EdamExpression.SUCCESS else EdamExpression.CURIOUS,
            character = activeGuide,
            badgeText = if (isLessonCompleted) "MASTERED ✓" else "+25 XP"
        )

        // Explanation Sections (.learning-section)
        lesson.sections.forEach { section ->
            LearningSectionBlock(section = section)
        }

        // Interactive Stock Instruments & Graphs Lab inside Stock/Market Lessons
        if (courseTitle.contains("Market", ignoreCase = true) ||
            courseTitle.contains("Stock", ignoreCase = true) ||
            lesson.title.contains("Candlestick", ignoreCase = true) ||
            lesson.title.contains("Indicator", ignoreCase = true) ||
            lesson.title.contains("Instrument", ignoreCase = true)
        ) {
            StockInstrumentsAndGraphsLearningLab()
        }

        // Interactive Chess Board Trainer inside Chess Novice-to-GM Lessons
        if (courseTitle.contains("Chess", ignoreCase = true) ||
            lesson.title.contains("Chess", ignoreCase = true) ||
            lesson.title.contains("Tactic", ignoreCase = true) ||
            lesson.title.contains("Endgame", ignoreCase = true) ||
            lesson.title.contains("Opening", ignoreCase = true)
        ) {
            NoviceToGmChessTrainingCard(
                chessPuzzlesSolvedCount = chessPuzzlesSolvedCount,
                onPuzzleSolved = onChessPuzzleSolved,
                onLaunchFullChessCourse = {}
            )
        }

        // Practice Section (.practice) with Visual Quiz Progress Tracker
        if (lesson.practice.isNotEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = stringResource(R.string.lesson_practice_heading),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Visual Progress Tracker for Quizzes
            QuizVisualProgressTracker(
                questions = lesson.practice,
                selectedAnswers = selectedAnswers
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                lesson.practice.forEachIndexed { qIndex, question ->
                    PracticeQuestionCard(
                        questionIndex = qIndex,
                        totalQuestions = lesson.practice.size,
                        question = question,
                        selectedOptionIndex = selectedAnswers[qIndex],
                        onSelectOption = { optIndex ->
                            onAnswerQuestion(qIndex, optIndex)
                        }
                    )
                }
            }
        }

        // Lesson Summary (.learning-section)
        if (lesson.summary.isNotBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.lesson_summary_heading),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = lesson.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Complete Lesson Button (#completeLesson)
        Button(
            onClick = onMarkComplete,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLessonCompleted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (isLessonCompleted) {
                    MaterialTheme.colorScheme.onTertiary
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("complete_lesson_button")
        ) {
            if (isLessonCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.lesson_completed_badge),
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = stringResource(R.string.lesson_mark_complete),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun QuizVisualProgressTracker(
    questions: List<PracticeQuestion>,
    selectedAnswers: Map<Int, Int>
) {
    val themeSpec = LocalEdamThemeSpec.current
    val totalQuestions = questions.size
    val answeredCount = selectedAnswers.size
    val remainingCount = (totalQuestions - answeredCount).coerceAtLeast(0)
    val correctCount = selectedAnswers.entries.count { (qIdx, optIdx) ->
        questions.getOrNull(qIdx)?.answerIndex == optIdx
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (totalQuestions == 0) 0f else (answeredCount.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "quiz_progress_tracker"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_progress_tracker_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.quiz_tracker_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (remainingCount == 0) {
                            stringResource(R.string.quiz_all_completed, totalQuestions)
                        } else {
                            stringResource(R.string.quiz_remaining_format, answeredCount, remainingCount)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (remainingCount == 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.testTag("quiz_remaining_text")
                    )
                }

                // Live Score Badge
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.quiz_score_format, correctCount, totalQuestions),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (themeSpec.isHighContrast) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("quiz_score_badge")
                    )
                }
            }

            // Animated Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    .testTag("quiz_progress_bar")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            if (remainingCount == 0) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }

            // Segmented Step Pills for each Question (Q1, Q2, Q3, Q4...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                questions.forEachIndexed { index, question ->
                    val chosen = selectedAnswers[index]
                    val isAnswered = chosen != null
                    val isCorrect = isAnswered && chosen == question.answerIndex

                    val pillBg = when {
                        isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                        isAnswered -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.background
                    }
                    val pillBorder = when {
                        isCorrect -> MaterialTheme.colorScheme.tertiary
                        isAnswered -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    }
                    val pillText = when {
                        isCorrect -> "Q${index + 1} ✓"
                        isAnswered -> "Q${index + 1} ×"
                        else -> "Q${index + 1}"
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quiz_step_pill_$index"),
                        shape = RoundedCornerShape(10.dp),
                        color = pillBg,
                        border = BorderStroke(themeSpec.borderWidth, pillBorder)
                    ) {
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningSectionBlock(
    section: LearningSection
) {
    val accentBlue = MaterialTheme.colorScheme.primary
    val themeSpec = LocalEdamThemeSpec.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = section.heading,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = section.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (section.examples.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                section.examples.forEach { example ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (themeSpec.isHighContrast) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                                }
                            )
                            .drawBehind {
                                drawRect(
                                    color = accentBlue,
                                    topLeft = Offset.Zero,
                                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                                )
                            }
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
                    ) {
                        Text(
                            text = example,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = if (themeSpec.isHighContrast) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeQuestionCard(
    questionIndex: Int,
    totalQuestions: Int,
    question: PracticeQuestion,
    selectedOptionIndex: Int?,
    onSelectOption: (Int) -> Unit
) {
    val isAnswered = selectedOptionIndex != null
    val themeSpec = LocalEdamThemeSpec.current
    val cardAlpha = if (themeSpec.isHighContrast) 1f else 0.85f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_$questionIndex"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha),
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${questionIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isAnswered) "Answered ✓" else "Remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAnswered) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${questionIndex + 1}. ${question.question}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(13.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                question.options.forEachIndexed { optionIndex, optionText ->
                    val isCorrectOption = optionIndex == question.answerIndex
                    val isSelectedWrong = isAnswered && selectedOptionIndex == optionIndex && !isCorrectOption
                    val showAsCorrect = isAnswered && isCorrectOption

                    val bgColor = when {
                        showAsCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                        isSelectedWrong -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    }

                    val borderColor = when {
                        showAsCorrect -> MaterialTheme.colorScheme.tertiary
                        isSelectedWrong -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    }

                    val textColor = when {
                        showAsCorrect -> MaterialTheme.colorScheme.onTertiaryContainer
                        isSelectedWrong -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Surface(
                        onClick = {
                            if (!isAnswered) {
                                onSelectOption(optionIndex)
                            }
                        },
                        enabled = !isAnswered,
                        shape = RoundedCornerShape(13.dp),
                        color = bgColor,
                        border = BorderStroke(themeSpec.borderWidth, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("option_button_${questionIndex}_$optionIndex")
                    ) {
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isAnswered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val isCorrect = selectedOptionIndex == question.answerIndex
                val feedbackText = if (isCorrect) {
                    stringResource(R.string.feedback_correct_prefix, question.explanation)
                } else {
                    stringResource(R.string.feedback_wrong_prefix, question.explanation)
                }
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    EdamMascotSpeechBanner(
                        title = if (isCorrect) "Spot On! (+10 XP)" else "Keep Going — Let's Review",
                        message = feedbackText,
                        expression = if (isCorrect) EdamExpression.SUCCESS else EdamExpression.ENCOURAGEMENT,
                        character = EdamCompanionCharacter.EDAM,
                        modifier = Modifier.testTag("feedback_text_$questionIndex")
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsModalDialog(
    uiState: EdamUiState,
    onClose: () -> Unit,
    onSelectPlan: (PlanTier) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromoCode: () -> Unit,
    onSelectTheme: (EdamThemeMode) -> Unit,
    onSignOut: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    val isDeveloperCodeRedeemed = uiState.promoCodeUsed == PlanTier.DEVELOPER_MAX_CODE

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .testTag("settings_modal"),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = if (themeSpec.isHighContrast) 0.dp else 24.dp,
                border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                                .testTag("close_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // 1. Account & Gmail Sign-In Section
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_account_section),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1F2937)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EdamMascot(
                                            expression = EdamExpression.HAPPY,
                                            character = uiState.selectedCompanion,
                                            size = 42.dp,
                                            showTablet = false
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = uiState.userDisplayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (uiState.userEmail.isNotBlank()) {
                                            Text(
                                                text = uiState.userEmail,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = onSignOut,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = stringResource(R.string.btn_sign_out))
                                }
                            }
                        }
                    }

                    // 2. Subscription Plans (Basic, Pro, Max) + Developer Code X7PLD9Q2RM4JY1S8W
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_plans_section),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_plans_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isDeveloperCodeRedeemed) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("developer_code_active_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_developer_unlocked),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }

                            // 3 Plan Cards: Basic, Pro, Max
                            PlanTier.entries.forEach { tier ->
                                val isCurrentPlan = uiState.planTier == tier
                                val isMaxWithDevDiscount = tier == PlanTier.MAX && isDeveloperCodeRedeemed

                                Surface(
                                    onClick = { onSelectPlan(tier) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isCurrentPlan) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    },
                                    border = BorderStroke(
                                        width = if (isCurrentPlan) 2.dp else themeSpec.borderWidth,
                                        color = if (isCurrentPlan) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("plan_card_${tier.id.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = tier.displayName,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = tier.badgeLabel,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (isMaxWithDevDiscount) {
                                                    Text(
                                                        text = tier.standardPrice,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            textDecoration = TextDecoration.LineThrough
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "$0.00 (100% OFF)",
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                } else {
                                                    Text(
                                                        text = tier.standardPrice,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = tier.tagline,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            tier.features.forEach { feature ->
                                                Text(
                                                    text = "• $feature",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Developer / Promo Code Redemption Input
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.promoCodeInput,
                                    onValueChange = onPromoCodeChange,
                                    placeholder = {
                                        Text(
                                            text = stringResource(R.string.settings_promo_placeholder),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("promo_code_input")
                                )

                                Button(
                                    onClick = onApplyPromoCode,
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("apply_promo_code_button")
                                ) {
                                    Text(text = stringResource(R.string.settings_apply_code))
                                }
                            }

                            if (uiState.promoStatusMessage.isNotBlank()) {
                                Text(
                                    text = uiState.promoStatusMessage,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (uiState.promoStatusMessage.startsWith("Invalid")) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.tertiary
                                    },
                                    modifier = Modifier.testTag("promo_status_message")
                                )
                            }
                        }
                    }

                    // 3. Appearance & Theme Section (Moved under Settings as requested)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.theme_sheet_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = stringResource(R.string.theme_sheet_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                EdamThemeMode.entries.forEach { mode ->
                                    val isSelected = mode == uiState.themeMode
                                    Surface(
                                        onClick = { onSelectTheme(mode) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.background
                                        },
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else themeSpec.borderWidth,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                            .testTag("theme_option_${mode.name.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            ThemeMiniSwatch(mode = mode)

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(mode.titleRes),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = stringResource(mode.descriptionRes),
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeMiniSwatch(
    mode: EdamThemeMode
) {
    Surface(
        modifier = Modifier.size(width = 56.dp, height = 44.dp),
        shape = RoundedCornerShape(10.dp),
        color = mode.previewBg,
        border = BorderStroke(1.5.dp, mode.previewBorder)
    ) {
        if (mode == EdamThemeMode.SYSTEM_DEFAULT) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(mode.previewBg)
                        .padding(5.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(mode.previewAccent)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(mode.previewSurface)
                        .padding(5.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(mode.previewAccent)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(mode.previewAccent)
                    )
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(mode.previewText)
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = mode.previewSurface,
                    border = BorderStroke(0.75.dp, mode.previewBorder)
                ) {}
            }
        }
    }
}

@Composable
private fun SavedCoursesBottomSheet(
    courses: List<Course>,
    activeCourseId: String?,
    onSelectCourse: (String) -> Unit,
    onDeleteCourse: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_saved_courses),
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(courses, key = { _, c -> c.id }) { _, course ->
                val isSelected = course.id == activeCourseId
                Surface(
                    onClick = { onSelectCourse(course.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.background
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_course_item_${course.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${course.level} · ${course.completedLessonsCount}/${course.totalLessons} lessons (${course.progressPercentage}%) · Offline Ready ✓",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onDeleteCourse(course.id) },
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "Delete ${course.title}"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete ${course.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
