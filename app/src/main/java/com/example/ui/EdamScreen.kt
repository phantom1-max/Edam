package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.LearningSection
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.model.PracticeQuestion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdamApp(
    viewModel: EdamViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    val bgColor = MaterialTheme.colorScheme.background

    if (uiState.lessonModalState != null) {
        BackHandler {
            viewModel.closeLesson()
        }
    } else if (uiState.showSavedCoursesSheet) {
        BackHandler {
            viewModel.toggleSavedCoursesSheet(false)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = bgColor)
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
                // Sticky Glass Navigation Bar (matches <nav class="glass">)
                EdamNavigationBar(
                    savedCoursesCount = uiState.savedCourses.size,
                    onGoToCreate = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(1)
                        }
                    },
                    onOpenSavedCourses = {
                        viewModel.toggleSavedCoursesSheet(true)
                    },
                    modifier = Modifier
                        .padding(horizontal = horizontalMargin, vertical = 10.dp)
                )

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
                    // 0: Hero section (<section class="hero">)
                    item(key = "hero_section") {
                        EdamHeroSection(isExpanded = isExpanded)
                    }

                    // 1: Create a Course section (<section id="createSection" class="section glass">)
                    item(key = "create_section") {
                        CreateCourseSection(
                            courseName = uiState.courseName,
                            selectedLevel = uiState.level,
                            goal = uiState.goal,
                            availableLevels = viewModel.availableLevels,
                            isGenerating = uiState.isGeneratingCourse,
                            statusMessage = uiState.statusMessage,
                            onCourseNameChange = viewModel::onCourseNameChange,
                            onLevelChange = viewModel::onLevelChange,
                            onGoalChange = viewModel::onGoalChange,
                            onPresetClick = viewModel::applyQuickPreset,
                            onCreateCourse = {
                                viewModel.createCourse(
                                    onCourseCreated = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(2)
                                        }
                                    }
                                )
                            }
                        )
                    }

                    // 2: Active Course section (<section id="courseSection" class="section glass">)
                    val course = uiState.activeCourse
                    if (course != null) {
                        item(key = "course_header_${course.id}") {
                            CourseOverviewCard(
                                course = course,
                                onOpenLesson = { unitId, lessonId ->
                                    viewModel.openLesson(unitId = unitId, lessonId = lessonId)
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

            // Lesson Modal (<div id="lessonModal" class="modal">)
            val modalState = uiState.lessonModalState
            if (modalState != null) {
                val isCompleted = uiState.activeCourse?.completed?.contains(modalState.lessonSummary.id) == true
                LessonModalDialog(
                    modalState = modalState,
                    isLessonCompleted = isCompleted,
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

            // Saved courses switcher bottom sheet
            if (uiState.showSavedCoursesSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleSavedCoursesSheet(false) },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    SavedCoursesBottomSheet(
                        courses = uiState.savedCourses,
                        activeCourseId = uiState.activeCourse?.id,
                        onSelectCourse = { courseId ->
                            viewModel.selectSavedCourse(courseId)
                            coroutineScope.launch {
                                listState.animateScrollToItem(2)
                            }
                        },
                        onDeleteCourse = viewModel::deleteSavedCourse
                    )
                }
            }
        }
    }
}

@Composable
private fun EdamNavigationBar(
    savedCoursesCount: Int,
    onGoToCreate: () -> Unit,
    onOpenSavedCourses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("edam_nav_bar"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (savedCoursesCount > 0) {
                    TextButton(
                        onClick = onOpenSavedCourses,
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("nav_saved_courses_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = stringResource(R.string.nav_saved_courses),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$savedCoursesCount",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                TextButton(
                    onClick = onGoToCreate,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_create_course_button")
                ) {
                    Text(
                        text = stringResource(R.string.nav_create_course),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun EdamHeroSection(
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = if (isExpanded) 48.dp else 28.dp,
                bottom = 16.dp,
                start = 10.dp,
                end = 10.dp
            )
            .testTag("hero_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.hero_title),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isExpanded) 68.sp else 48.sp,
                lineHeight = if (isExpanded) 68.sp else 48.sp,
                letterSpacing = if (isExpanded) (-3.5).sp else (-2.0).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.hero_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 27.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 650.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .height(if (isExpanded) 200.dp else 156.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 4.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_edam_hero_banner_1790859716391),
                contentDescription = stringResource(R.string.hero_banner_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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
    statusMessage: String,
    onCourseNameChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onPresetClick: (String, String, String) -> Unit,
    onCreateCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("create_section"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.create_section_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.create_section_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick topic suggestions to make course creation effortless on mobile
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf(
                    Triple(
                        "English Conversation & Grammar",
                        "Intermediate",
                        "Speak naturally in daily conversations and write clear professional emails"
                    ),
                    Triple(
                        "Python & Data Analysis",
                        "Beginner",
                        "Write clean Python scripts and analyze real-world datasets from scratch"
                    ),
                    Triple(
                        "Modern Product Design",
                        "Advanced",
                        "Master interaction design systems, typography, and user research"
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
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
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
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    border = BorderStroke(
                        1.dp,
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
                                MaterialTheme.colorScheme.onBackground
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
                                    style = MaterialTheme.typography.bodyLarge
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
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
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

            // 5. Status Line (#status)
            if (statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (statusMessage.startsWith("Something went wrong") ||
                        statusMessage.startsWith("Fill in")
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.testTag("status_text")
                )
            }
        }
    }
}

@Composable
private fun CourseOverviewCard(
    course: Course,
    onOpenLesson: (unitId: String, lessonId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_section"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Level Badge (.badge)
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = course.level,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Course Title
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("course_title_text")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Course Description
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                            .height(9.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
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
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unit_card_${unit.id}"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("lesson_button_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                        color = MaterialTheme.colorScheme.primary
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
    modalState: LessonModalState,
    isLessonCompleted: Boolean,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onAnswerQuestion: (questionIndex: Int, optionIndex: Int) -> Unit,
    onMarkComplete: () -> Unit
) {
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
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f))
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
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.97f),
                shadowElevation = 24.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
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
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
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
                                Text(
                                    text = stringResource(R.string.lesson_error_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = modalState.errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                lesson = modalState.lessonContent,
                                selectedAnswers = modalState.selectedAnswers,
                                isLessonCompleted = isLessonCompleted,
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
    lesson: LessonContent,
    selectedAnswers: Map<Int, Int>,
    isLessonCompleted: Boolean,
    onAnswerQuestion: (questionIndex: Int, optionIndex: Int) -> Unit,
    onMarkComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Explanation Sections (.learning-section)
        lesson.sections.forEach { section ->
            LearningSectionBlock(section = section)
        }

        // Practice Section (.practice)
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

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                lesson.practice.forEachIndexed { qIndex, question ->
                    PracticeQuestionCard(
                        questionIndex = qIndex,
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
                contentColor = MaterialTheme.colorScheme.onPrimary
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
private fun LearningSectionBlock(
    section: LearningSection
) {
    val accentBlue = MaterialTheme.colorScheme.primary
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
                    // Matches .example { border-left: 4px solid var(--blue); border-radius: 12px; background: rgba(0,113,227,.06); }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
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
                            color = MaterialTheme.colorScheme.onBackground
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
    question: PracticeQuestion,
    selectedOptionIndex: Int?,
    onSelectOption: (Int) -> Unit
) {
    val isAnswered = selectedOptionIndex != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_$questionIndex"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
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
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    }

                    val borderColor = when {
                        showAsCorrect -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        isSelectedWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.outline
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
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("option_button_${questionIndex}_$optionIndex")
                    ) {
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onBackground,
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
                Text(
                    text = feedbackText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .testTag("feedback_text_$questionIndex")
                )
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
                                text = "${course.level} · ${course.completedLessonsCount}/${course.totalLessons} lessons (${course.progressPercentage}%)",
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
