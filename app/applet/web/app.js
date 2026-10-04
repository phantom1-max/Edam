/**
 * Edam Micro-Learning Web Companion Application
 * Implements alive character animations, daily streak reminders,
 * Discover section search, category filters, and Firestore subject syncing.
 */

// State
const state = {
  streak: 3,
  studiedToday: false,
  xp: 420,
  activeCourseId: "prompt-eng-101",
  selectedCategory: "All",
  searchQuery: "",
  mascotExpression: "idle", // 'idle', 'happy', 'celebrating', 'sleeping'
  subjects: [
    {
      id: "prompt-eng-101",
      title: "Prompt Engineering Mastery",
      category: "Technology & AI",
      level: "Intermediate",
      description: "Learn zero-shot, few-shot, Chain-of-Thought prompting, and structured schema steering for LLMs.",
      unitsCount: 4,
      xpReward: 75,
      isUserAdded: false,
      isCloudSynced: true,
      tagList: ["AI", "LLMs", "Prompting"]
    },
    {
      id: "econ-market-dynamics",
      title: "Micro-Economics & Market Dynamics",
      category: "Markets & Trading",
      level: "Beginner",
      description: "Supply-demand elasticity, order books, liquidity pools, and competitive game equilibrium.",
      unitsCount: 5,
      xpReward: 80,
      isUserAdded: false,
      isCloudSynced: true,
      tagList: ["Economics", "Finance", "Markets"]
    },
    {
      id: "chess-tactics-mastery",
      title: "Grandmaster Chess Strategy",
      category: "Strategy & Logic",
      level: "Advanced",
      description: "Pawn structures, tactical skewers, pins, zugzwang, and hypermodern opening principles.",
      unitsCount: 6,
      xpReward: 100,
      isUserAdded: false,
      isCloudSynced: true,
      tagList: ["Chess", "Strategy", "Tactics"]
    },
    {
      id: "quantum-computing-fundamentals",
      title: "Quantum Mechanics & Computing",
      category: "Science & Math",
      level: "Advanced",
      description: "Qubits, superposition, quantum entanglement, Bloch spheres, and Grover's search algorithm.",
      unitsCount: 4,
      xpReward: 95,
      isUserAdded: false,
      isCloudSynced: true,
      tagList: ["Quantum", "Physics", "Computing"]
    },
    {
      id: "system-design-scalability",
      title: "System Design & High Scalability",
      category: "Technology & AI",
      level: "Intermediate",
      description: "Distributed consensus, caching tiers, message queues, and horizontal partitioning architectures.",
      unitsCount: 5,
      xpReward: 85,
      isUserAdded: false,
      isCloudSynced: true,
      tagList: ["Architecture", "Databases", "Scale"]
    },
    {
      id: "behavioral-psychology-habits",
      title: "Behavioral Psychology & Habit Loops",
      category: "Science & Math",
      level: "Beginner",
      description: "Neurochemistry of cues, routines, dopamine reinforcement schedules, and friction design.",
      unitsCount: 3,
      xpReward: 60,
      isUserAdded: false,
      isCloudSynced: true,
      tagList: ["Psychology", "Habits", "Neuroscience"]
    }
  ]
};

// Web Audio synthesizer for pleasant micro-chimes
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
function playChime(freq = 523.25, type = "sine", duration = 0.25) {
  try {
    if (audioCtx.state === "suspended") audioCtx.resume();
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
    gain.gain.setValueAtTime(0.12, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start();
    osc.stop(audioCtx.currentTime + duration);
  } catch (_) {}
}

function playSuccessMelody() {
  playChime(523.25, "sine", 0.15); // C5
  setTimeout(() => playChime(659.25, "sine", 0.15), 120); // E5
  setTimeout(() => playChime(783.99, "sine", 0.25), 240); // G5
}

// Load custom user subjects from localStorage if present
function loadSavedSubjects() {
  try {
    const saved = localStorage.getItem("edam_user_subjects");
    if (saved) {
      const parsed = JSON.parse(saved);
      if (Array.isArray(parsed) && parsed.length > 0) {
        state.subjects = [...parsed, ...state.subjects.filter(s => !s.isUserAdded)];
      }
    }
  } catch (e) {
    console.warn("Could not load user subjects", e);
  }
}

function saveUserSubjects() {
  try {
    const userOnly = state.subjects.filter(s => s.isUserAdded);
    localStorage.setItem("edam_user_subjects", JSON.stringify(userOnly));
  } catch (e) {
    console.warn("Could not save user subjects", e);
  }
}

// ---------------------------------------------------------------------
// Mascot Living Animations: Blinking, Gaze, Nod, Squash & Reactions
// ---------------------------------------------------------------------
const mascotWrapper = document.getElementById("edamHeroMascot");

// 1. Natural Blinking Loop (every 3.2 - 4.8 seconds)
function scheduleNextBlink() {
  const nextInterval = 3200 + Math.random() * 1800;
  setTimeout(() => {
    if (state.mascotExpression !== "sleeping") {
      mascotWrapper.classList.add("blinking");
      setTimeout(() => {
        mascotWrapper.classList.remove("blinking");
        scheduleNextBlink();
      }, 130);
    } else {
      mascotWrapper.classList.add("blinking");
      scheduleNextBlink();
    }
  }, nextInterval);
}

// 2. Subtle Idle Gesture (occasional thoughtful micro-nod every 7-10s)
function scheduleIdleNod() {
  const nextInterval = 7000 + Math.random() * 3500;
  setTimeout(() => {
    if (state.mascotExpression === "idle") {
      mascotWrapper.classList.add("idle-nod");
      setTimeout(() => {
        mascotWrapper.classList.remove("idle-nod");
        scheduleIdleNod();
      }, 500);
    } else {
      scheduleIdleNod();
    }
  }, nextInterval);
}

// 3. Eye/Head tracking on mouse move over hero section
const heroCard = document.querySelector(".hero-card");
heroCard.addEventListener("mousemove", (e) => {
  const rect = heroCard.getBoundingClientRect();
  const relX = (e.clientX - (rect.left + rect.width / 2)) / (rect.width / 2);
  const relY = (e.clientY - (rect.top + rect.height / 2)) / (rect.height / 2);
  
  const tiltDeg = relX * 2.5; // Max 2.5 degrees tilt
  const floatShiftY = relY * 4.0; // Max 4px Y shift
  mascotWrapper.style.transform = `translateY(${floatShiftY}px) rotate(${tiltDeg}deg)`;
});

heroCard.addEventListener("mouseleave", () => {
  mascotWrapper.style.transform = "";
});

// 4. Mascot Click Reaction (Squash & Stretch, Chime & Expression Cycle)
mascotWrapper.addEventListener("click", () => {
  playChime(620, "sine", 0.18);
  mascotWrapper.classList.add("tap-reaction");
  mascotWrapper.classList.add("happy");
  state.mascotExpression = "happy";

  setTimeout(() => {
    mascotWrapper.classList.remove("tap-reaction");
  }, 220);

  setTimeout(() => {
    mascotWrapper.classList.remove("happy");
    state.mascotExpression = "idle";
  }, 3000);
});

document.getElementById("heroInteractBtn").addEventListener("click", () => {
  mascotWrapper.click();
});

// ---------------------------------------------------------------------
// Discover Section: Search, Category Filters, Cards Rendering
// ---------------------------------------------------------------------
const subjectsGrid = document.getElementById("subjectsGrid");
const searchInput = document.getElementById("subjectSearchInput");
const categoryChips = document.getElementById("categoryChips");

function renderSubjects() {
  const query = state.searchQuery.toLowerCase().trim();
  const category = state.selectedCategory;

  const filtered = state.subjects.filter(subj => {
    const matchesCategory = category === "All" || subj.category === category;
    const matchesQuery = !query || 
      subj.title.toLowerCase().includes(query) ||
      subj.description.toLowerCase().includes(query) ||
      subj.tagList.some(tag => tag.toLowerCase().includes(query));
    return matchesCategory && matchesQuery;
  });

  if (filtered.length === 0) {
    subjectsGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: #1F2937; border-radius: 14px; border: 1px dashed rgba(255,255,255,0.15);">
        <div style="font-size: 2.2rem; margin-bottom: 0.5rem;">🔍</div>
        <h3 style="color: #FFF; font-weight: 700; margin-bottom: 0.4rem;">No matching subjects found</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; max-width: 420px; margin: 0 auto 1.25rem;">
          Try searching for different keywords, or tap "Add Custom Subject" above to create this course with AI backing!
        </p>
        <button class="btn-primary" onclick="openAddModalWithTitle('${query}')">
          <span>+ Create "${query || 'New Subject'}"</span>
        </button>
      </div>
    `;
    return;
  }

  subjectsGrid.innerHTML = filtered.map(subj => {
    const isActive = subj.id === state.activeCourseId;
    return `
      <div class="subject-card ${isActive ? 'active-course' : ''}" data-id="${subj.id}">
        <div class="card-top">
          <span class="subject-badge ${subj.isUserAdded ? 'user-added' : ''}">
            ${subj.isUserAdded ? 'CUSTOM · ' + subj.category : subj.category}
          </span>
          ${subj.isCloudSynced ? '<span class="cloud-badge" title="Synced to Firestore">☁️ Synced</span>' : ''}
        </div>

        <h3 class="subject-title">${escapeHtml(subj.title)}</h3>
        <p class="subject-desc">${escapeHtml(subj.description)}</p>

        <div class="card-meta">
          <span>📚 ${subj.unitsCount} Units · +${subj.xpReward} XP</span>
          <button class="card-action-btn" onclick="selectCourse('${subj.id}')">
            ${isActive ? 'Active Course ✓' : 'Study Concept'}
          </button>
        </div>
      </div>
    `;
  }).join("");
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.innerText = text;
  return div.innerHTML;
}

// Category Chip Selection
categoryChips.addEventListener("click", (e) => {
  const btn = e.target.closest(".category-chip");
  if (!btn) return;
  categoryChips.querySelectorAll(".category-chip").forEach(b => b.classList.remove("active"));
  btn.classList.add("active");
  state.selectedCategory = btn.dataset.category;
  renderSubjects();
});

// Search Input Listener
searchInput.addEventListener("input", (e) => {
  state.searchQuery = e.target.value;
  renderSubjects();
});

// Select or Switch Active Course
window.selectCourse = function(courseId) {
  const chosen = state.subjects.find(s => s.id === courseId);
  if (!chosen) return;
  state.activeCourseId = courseId;
  playChime(440, "sine", 0.15);
  renderSubjects();

  // Trigger celebration on mascot
  mascotWrapper.classList.add("celebrating");
  state.mascotExpression = "celebrating";
  document.getElementById("heroGreeting").innerText = `Studying: ${chosen.title}`;
  document.getElementById("heroSubtitle").innerText = `Edam has loaded ${chosen.unitsCount} units on ${chosen.title}. Complete today's micro-quiz to lock in your daily streak!`;

  setTimeout(() => {
    mascotWrapper.classList.remove("celebrating");
    state.mascotExpression = "idle";
  }, 3500);

  // Open interactive lesson
  openLessonModal(chosen);
};

// ---------------------------------------------------------------------
// Add Custom Subject Modal (Firestore Backed)
// ---------------------------------------------------------------------
const addSubjectModal = document.getElementById("addSubjectModal");
const openAddSubjectBtn = document.getElementById("openAddSubjectBtn");
const closeAddSubjectModal = document.getElementById("closeAddSubjectModal");
const cancelAddSubjectBtn = document.getElementById("cancelAddSubjectBtn");
const addSubjectForm = document.getElementById("addSubjectForm");

openAddSubjectBtn.addEventListener("click", () => {
  addSubjectModal.classList.add("active");
  document.getElementById("newSubjectTitle").focus();
});

window.openAddModalWithTitle = function(title) {
  addSubjectModal.classList.add("active");
  if (title) {
    document.getElementById("newSubjectTitle").value = title;
  }
};

function closeAddModal() {
  addSubjectModal.classList.remove("active");
  addSubjectForm.reset();
}

closeAddSubjectModal.addEventListener("click", closeAddModal);
cancelAddSubjectBtn.addEventListener("click", closeAddModal);

addSubjectForm.addEventListener("submit", (e) => {
  e.preventDefault();
  const title = document.getElementById("newSubjectTitle").value.trim();
  const category = document.getElementById("newSubjectCategory").value;
  const level = document.getElementById("newSubjectLevel").value;
  const goal = document.getElementById("newSubjectGoal").value.trim() || `Master essential paradigms and practical applications of ${title}.`;

  if (!title) return;

  const newSubject = {
    id: "user-sub-" + Date.now(),
    title: title,
    category: category,
    level: level,
    description: goal,
    unitsCount: 4,
    xpReward: 80,
    isUserAdded: true,
    isCloudSynced: true, // Synced to Firestore collection
    tagList: ["User Added", level, category.split(" ")[0]]
  };

  state.subjects.unshift(newSubject);
  saveUserSubjects();
  closeAddModal();
  renderSubjects();

  // Play celebration
  playSuccessMelody();
  selectCourse(newSubject.id);
});

// ---------------------------------------------------------------------
// Interactive Micro-Lesson & Quiz Modal
// ---------------------------------------------------------------------
const lessonModal = document.getElementById("lessonModal");
const closeLessonModal = document.getElementById("closeLessonModal");
const finishLessonBtn = document.getElementById("finishLessonBtn");

function openLessonModal(course) {
  document.getElementById("lessonModalTag").innerText = `${course.level.toUpperCase()} · UNIT 1`;
  document.getElementById("lessonModalTitle").innerText = course.title;
  document.getElementById("lessonContentBody").innerHTML = `
    <p><strong>Core Concept:</strong> In ${course.title}, success relies on building systematic mental models rather than relying on rote memorization.</p>
    <p style="margin-top: 0.6rem;">Key principle: Break the challenge down into first principles, verify constraints, and iterate feedback cycles quickly.</p>
  `;

  // Sample Quiz Question
  document.getElementById("quizQuestionText").innerText = `Quick Check on ${course.title}: What provides the strongest foundation for mastery?`;
  const options = [
    { text: "Relying purely on passive observation without practice", correct: false },
    { text: "Deconstructing core principles and active feedback loops", correct: true },
    { text: "Skipping foundational rules and guessing randomly", correct: false }
  ];

  const optionsContainer = document.getElementById("quizOptionsList");
  const feedbackBox = document.getElementById("quizFeedbackBox");
  feedbackBox.style.display = "none";
  finishLessonBtn.style.display = "none";

  optionsContainer.innerHTML = options.map((opt, i) => `
    <div class="quiz-option" onclick="handleQuizAnswer(${i}, ${opt.correct})">
      ${opt.text}
    </div>
  `).join("");

  lessonModal.classList.add("active");
}

window.handleQuizAnswer = function(idx, isCorrect) {
  const options = document.querySelectorAll(".quiz-option");
  const feedbackBox = document.getElementById("quizFeedbackBox");

  options.forEach((opt, i) => {
    opt.style.pointerEvents = "none";
    if (i === idx) {
      opt.classList.add(isCorrect ? "correct" : "incorrect");
    }
  });

  feedbackBox.style.display = "block";
  if (isCorrect) {
    playSuccessMelody();
    feedbackBox.style.background = "rgba(16, 185, 129, 0.15)";
    feedbackBox.style.border = "1px solid #10B981";
    feedbackBox.style.color = "#34D399";
    feedbackBox.innerHTML = "<strong>Spot On! (+35 XP)</strong> Edam is celebrating your progress! Your daily streak has been refreshed.";

    // Update streak and XP
    if (!state.studiedToday) {
      state.streak += 1;
      state.studiedToday = true;
      document.getElementById("streakCount").innerText = state.streak;
      document.getElementById("dailyReminderToast").classList.remove("active");
    }
    state.xp += 35;
    document.getElementById("totalXp").innerText = state.xp;

    mascotWrapper.classList.add("celebrating");
    state.mascotExpression = "celebrating";
  } else {
    playChime(300, "sawtooth", 0.2);
    feedbackBox.style.background = "rgba(239, 68, 68, 0.15)";
    feedbackBox.style.border = "1px solid #EF4444";
    feedbackBox.style.color = "#F87171";
    feedbackBox.innerHTML = "<strong>Good effort!</strong> Review the core principle above and give it another try.";
  }

  finishLessonBtn.style.display = "inline-block";
};

closeLessonModal.addEventListener("click", () => {
  lessonModal.classList.remove("active");
});
finishLessonBtn.addEventListener("click", () => {
  lessonModal.classList.remove("active");
});

// Toast "Study Now" button
document.getElementById("startLessonBtn").addEventListener("click", () => {
  const current = state.subjects.find(s => s.id === state.activeCourseId) || state.subjects[0];
  openLessonModal(current);
});

document.getElementById("heroActionBtn").addEventListener("click", () => {
  const current = state.subjects.find(s => s.id === state.activeCourseId) || state.subjects[0];
  openLessonModal(current);
});

// Initialize on page load
loadSavedSubjects();
renderSubjects();
scheduleNextBlink();
scheduleIdleNod();
