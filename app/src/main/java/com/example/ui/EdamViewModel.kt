package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EdamDatabase
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.repository.EdamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LessonModalState(
    val courseId: String,
    val unit: CourseUnit,
    val lessonSummary: LessonSummary,
    val isLoading: Boolean = true,
    val lessonContent: LessonContent? = null,
    val errorMessage: String? = null,
    val selectedAnswers: Map<Int, Int> = emptyMap()
)

data class EdamFormState(
    val courseName: String = "",
    val level: String = "",
    val goal: String = "",
    val isGeneratingCourse: Boolean = false,
    val statusMessage: String = "",
    val showSavedCoursesSheet: Boolean = false,
    val lessonModalState: LessonModalState? = null
)

data class EdamUiState(
    val courseName: String = "",
    val level: String = "",
    val goal: String = "",
    val isGeneratingCourse: Boolean = false,
    val statusMessage: String = "",
    val activeCourse: Course? = null,
    val savedCourses: List<Course> = emptyList(),
    val showSavedCoursesSheet: Boolean = false,
    val lessonModalState: LessonModalState? = null
)

class EdamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EdamRepository = EdamRepository(
        dao = EdamDatabase.getInstance(application).edamDao()
    )

    private val formState = MutableStateFlow(EdamFormState())

    val uiState: StateFlow<EdamUiState> = combine(
        formState,
        repository.activeCourseFlow,
        repository.allCoursesFlow
    ) { form, activeCourse, allCourses ->
        EdamUiState(
            courseName = form.courseName,
            level = form.level,
            goal = form.goal,
            isGeneratingCourse = form.isGeneratingCourse,
            statusMessage = form.statusMessage,
            activeCourse = activeCourse ?: allCourses.firstOrNull(),
            savedCourses = allCourses,
            showSavedCoursesSheet = form.showSavedCoursesSheet,
            lessonModalState = form.lessonModalState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EdamUiState()
    )

    val availableLevels = listOf(
        "Beginner",
        "Elementary",
        "Intermediate",
        "Advanced",
        "University",
        "Professional"
    )

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
                statusMessage = "Gemini is designing your learning path..."
            )
        }

        viewModelScope.launch {
            try {
                repository.createAndSaveCourse(
                    courseName = name,
                    level = level,
                    goal = goal
                )
                formState.update {
                    it.copy(
                        isGeneratingCourse = false,
                        statusMessage = "Course created."
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

    fun openLesson(
        unitId: String,
        lessonId: String,
        forceRefresh: Boolean = false
    ) {
        val course = uiState.value.activeCourse ?: return
        val unit = course.units.find { it.id == unitId } ?: return
        val lessonSummary = unit.lessons.find { it.id == lessonId } ?: return

        formState.update {
            it.copy(
                lessonModalState = LessonModalState(
                    courseId = course.id,
                    unit = unit,
                    lessonSummary = lessonSummary,
                    isLoading = true,
                    lessonContent = null,
                    errorMessage = null,
                    selectedAnswers = emptyMap()
                )
            )
        }

        viewModelScope.launch {
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
        formState.update { state ->
            val modal = state.lessonModalState ?: return@update state
            if (modal.selectedAnswers.containsKey(questionIndex)) {
                return@update state
            }
            state.copy(
                lessonModalState = modal.copy(
                    selectedAnswers = modal.selectedAnswers + (questionIndex to optionIndex)
                )
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
        }
    }

    fun closeLesson() {
        formState.update { it.copy(lessonModalState = null) }
    }

    fun toggleSavedCoursesSheet(show: Boolean) {
        formState.update { it.copy(showSavedCoursesSheet = show) }
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
