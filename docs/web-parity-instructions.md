# Web parity instructions — Android branch (uncommitted)

This document describes **functional and data behavior** from the current Android working tree so the web app can match it. UI polish (launcher icons, Play Store assets, Material theme tokens) is noted only where it affects product behavior.

**Audience:** Web developers implementing equivalent features.  
**Reference platform:** Android (`app/` module).  
**Language:** Section titles in Turkish (app-first); formulas and field names in English for copy-paste into code.

---

## 1. Genel bakış / Scope

### 1.1 In scope for web parity

| Area | Summary |
|------|---------|
| **Curriculum resolution** | Per-student grade + admin `Settings/gradeCurriculum` + profile `curriculumProgram` → TYMM (Maarif) vs LEGACY (TYT/AYT) |
| **Subject lists** | Program-specific study picker / teacher studies grid; English variants by grade; extras (Paragraf, Problem, Diğer) |
| **Student home stats** | Three progress rings: study minutes, questions, distinct report days vs **weekly goals** |
| **Teacher home stats** | Three rings: reporting rate, class study vs benchmark, rating rate; requires time range |
| **Teacher → student studies** | Curriculum subtitle + subject grid + **same three rings** scaled to selected date range |
| **Weekly goals (student)** | Settings UI + local persistence + Firestore sync on `School/.../Student/{id}` |
| **Stat range scaling** | Pro-rate weekly targets to arbitrary calendar ranges (teacher + teacher-view-student) |
| **Stats collapse** | Persisted expand/collapse on main home stats cards |
| **Study graph routing** | TYMM → graph with `program` + `sinif`; LEGACY → TYT/AYT picker dialog |

### 1.2 Out of scope (do not mirror on web unless separately requested)

- Play Store asset pipeline (`app/play-store-assets/`)
- Android adaptive launcher / vector subject icons (web may keep existing icon strategy)
- Legacy Excel export column reorder in `Util.kt` (Android-only report layout)
- `CoachingAppApplication.kt` / instrumented test package rename
- Night theme color resource files (optional visual alignment only)

### 1.3 Git diff snapshot (tracked files)

```
39 files changed, 1444 insertions(+), 540 deletions(-)
```

**New untracked (behavior-critical):**

- `app/src/main/java/.../fragments/MainHomeStatsBinder.kt`
- `app/src/main/java/.../services/StatRangeScaling.kt`
- `app/src/main/java/.../services/StudentWeeklyGoalsPreferences.kt`
- `app/src/main/res/layout/main_stats_student.xml`, `main_stats_teacher.xml`, `view_main_stat_ring.xml`
- Unit tests: `StatRangeScalingTest.kt`, `StudentWeeklyGoalsPreferencesTest.kt`, `SubjectsTest.kt`

---

## 2. Müfredat çözümleme (TYMM vs TYT-AYT)

### 2.1 Concepts

| Enum | Firestore value | UI label |
|------|-----------------|----------|
| `CurriculumProgram.LEGACY` | `legacy` | TYT/AYT |
| `CurriculumProgram.TYMM` | `tymm` | Maarif |

| `GradeCurriculumMode` | Firestore | Meaning |
|----------------------|-----------|---------|
| `LEGACY` | `legacy` | Force TYT/AYT for that grade |
| `TYMM` | `tymm` | Force Maarif |
| `CHOICE` | `choice` | Student may pick program in settings (grade 12 default) |

### 2.2 Admin config document

- **Path:** `Settings/gradeCurriculum`
- **Shape:** `grades` map (or root map) keyed by grade string → mode string  
  Example: `{ "grades": { "9": "tymm", "12": "choice", "13": "legacy" } }`

**Android defaults** (`GradeCurriculumRepository.defaultGradeModes`):

| Grade | Mode |
|-------|------|
| 0 (Hazırlık) | TYMM |
| 9, 10, 11 | TYMM |
| 12 | CHOICE |
| 13 (Mezun) | LEGACY |

**Fallback** if grade missing in config: `grade >= 11` → LEGACY, else TYMM.

### 2.3 Resolution algorithm (implement identically)

```text
function preferredProgram(config, grade, profileProgram):
  if offersProgramChoice(config, grade) AND profileProgram != null:
    return profileProgram
  return programForGrade(config, grade)

function programForGrade(config, grade):
  switch modeForGrade(config, grade):
    LEGACY → LEGACY
    TYMM → TYMM
    CHOICE → (grade >= 11 ? LEGACY : TYMM)   // default when no profile choice

function preferredProgramForStudent(db, studentId):
  user = User/{studentId}
  grade = user.grade (long or string, default 12)
  profileProgram = CurriculumProgram.fromFirestore(user.curriculumProgram)
  config = load Settings/gradeCurriculum or defaults
  return (grade, preferredProgram(config, grade, profileProgram))
```

**Session override** (student study entry from class picker): `activeProgram` uses `sessionProgram` if set, else `preferredProgram`.

### 2.4 Profile fields

| Collection | Document | Field | Type | Notes |
|------------|----------|-------|------|-------|
| `User` | `{studentUid}` | `grade` | number or string | 0, 9–13 |
| `User` | `{studentUid}` | `curriculumProgram` | `"legacy"` \| `"tymm"` | Only meaningful when grade is in CHOICE mode |

Settings UI: if `offersProgramChoice`, show picker (TYT/AYT vs Maarif); else show read-only resolved program.

### 2.5 Subject lists (`Subjects` — mirror web `subjects.ts` if present)

**LEGACY study tiles (order matters for UI):**  
Matematik, Fizik, Kimya, Biyoloji, Türkçe-Edebiyat, Coğrafya, Tarih, Geometri, Felsefe, Din, Paragraf, Problem, Diğer

**TYMM core subjects:** see `TYMM_SUBJECT_NAMES` in `Subjects.kt`.

**English filtering by grade:**

```text
"İngilizce (9-12)"      → include iff grade != 0
"İngilizce (Hazırlık-12)" → include iff grade == 0
```

**TYMM extras (always appended):** Paragraf, Problem

**Teacher studies grid** (`StudiesActivity.loadStudiesForDateRange`):

1. Resolve `(grade, program)` via `preferredProgramForStudent`.
2. Start with `subjectNamesForProgram(program, grade)`.
3. Merge any `dersAdi` keys from aggregated studies in range that are not in the list (orphan / legacy names still shown).

**Display subtitle:** `StudyLabels.programDisplayName(program, grade)`  
→ `"Maarif · {grade}. sınıf"` or `"TYT/AYT · {grade}. sınıf"`

### 2.6 Study record labels (export / lists)

- **Tracking label:** If Maarif (`program == tymm` OR `tür == Maarif`): `temaAdi` if non-empty, else `konuAdi`; else `konuAdi`.
- **Type label:** TYMM → `"Maarif · {sinif}. sınıf · {temaAdi?}"`; LEGACY → `tür` (with Maarif tema suffix rules). See `StudyLabels.kt`.

### 2.7 Teacher: class graph entry (`ActivityStudiesByClasses`)

| Student program | Action |
|-----------------|--------|
| TYMM | Open graph with `program=tymm`, `sinif=studentGrade` (no TYT/AYT dialog) |
| LEGACY | Show dialog: **TYT** or **AYT**, then open graph with `tür` |

---

## 3. Haftalık hedefler (öğrenci)

### 3.1 Purpose

- Drive **main home progress rings** and **teacher view of a single student’s rings**.
- **Not** used for rings: per-subject teacher goals in `School/.../Student/{id}/HaftalikHedefler` (those remain on Goals screen only).

### 3.2 Defaults and validation

| Field | Default | Min | Max | Storage |
|-------|---------|-----|-----|---------|
| Study time | 420 min (7 h) | 60 min (1 h) | 5940 min (99 h) | Minutes in Firestore; **hours** in settings UI |
| Questions | 350 | 10 | 9999 | Integer |
| Report days | 7 | 1 | 7 | Integer |

**Hours ↔ minutes:**

```text
hoursToMinutes(h) = clamp(h, 1..99) * 60, then clamp to [60, 5940]
minutesToDisplayHours(m) = ceil(m/60) clamped to [1, 99]   // legacy minute values round up
```

### 3.3 Firestore sync (student document)

**Path:** `School/{kurumKodu}/Student/{studentId}`  
**Merge fields:**

| Field | Constant |
|-------|----------|
| `weeklyGoalMinutes` | `StudyQueryHelper.FIELD_WEEKLY_GOAL_MINUTES` |
| `weeklyGoalQuestions` | `StudyQueryHelper.FIELD_WEEKLY_GOAL_QUESTIONS` |
| `weeklyGoalReportDays` | `StudyQueryHelper.FIELD_WEEKLY_GOAL_REPORT_DAYS` |

**Read:** `fetchWeeklyGoalsForStudent` — missing fields → defaults above; clamp on read.

**Write:** On settings save and when student app applies local prefs (`merge: true`).

**Web:** Use same fields so teacher class benchmark (sum of student weekly minutes) stays consistent cross-platform.

### 3.4 Local persistence (Android)

- SharedPreferences name: `student_weekly_goals`
- Web equivalent: `localStorage` or user profile extension — but **Firestore is source of truth for teacher**; student web should sync the same three fields.

---

## 4. Stat range scaling (`StatRangeScaling`)

All ring **targets** for a selected calendar period are derived from **weekly** goals unless noted.

### 4.1 Constants

```text
DAYS_PER_WEEK = 7.0
isWeeklyRange(rangeDays) = abs(rangeDays - 7.0) < 0.5
```

### 4.2 `rangeDays` calculation

```text
daysBetween(start, end) = max(((end.time - start.time) / 86400000) + 1, 1.0)
```

Inclusive calendar-day span (same as `ExcelExportHelper.daysBetween`).

### 4.3 Scale functions

Let `days = max(rangeDays, 1.0)`.

**Minutes & questions:**

```text
scaleWeeklyMinutes(weeklyMinutes, rangeDays):
  if weeklyMinutes <= 0: return 1
  return max(round(weeklyMinutes * days / 7), 1)

scaleWeeklyQuestions(weeklyQuestions, rangeDays):
  if weeklyQuestions <= 0: return 1
  return max(round(weeklyQuestions * days / 7), 1)
```

**Report days** (also capped by distinct days in range):

```text
scaleWeeklyReportDays(weeklyReportDays, rangeDays):
  weekly = clamp(weeklyReportDays, 1, 7)
  if weekly <= 0: return 1
  maxDistinctDays = max(round(days), 1)
  scaled = max(round(weekly * days / 7), 1)
  return min(scaled, maxDistinctDays)
```

**Progress percent:**

```text
progressPercent(actual, target):
  if target <= 0: return 0
  return clamp(round(actual * 100 / target), 0, 100)
```

### 4.4 Verified examples (from unit tests)

| Input | rangeDays | Output |
|-------|-----------|--------|
| 420 min/week | 1 | target 60, 60 actual → 100% |
| 420 min/week | 7 | target 420 |
| 420 min/week | 30 | target 1800 |
| 7 report days/week | 1 | target 1 |
| 7 report days/week | 3 | target 3 |
| Sum 700 min/week (class) | 1 | benchmark 100; 300 actual → 100% |

---

## 5. Ana sayfa — öğrenci istatistik halkaları

### 5.1 Data window

- Studies: `School/{kurumKodu}/Student/{uid}/Studies` where `timestamp > now - 7 days` (rolling window, not necessarily Mon–Sun).
- Ring targets: `rangeDays = 7.0` always on student home (labels use **“Haftalık …”**).

### 5.2 Aggregates (`computeStudentStats`)

For each study in list:

| Metric | Source field | Aggregation |
|--------|--------------|-------------|
| Minutes | `toplamCalisma` (parse int) | Sum |
| Questions | `çözülenSoru` (parse int) | Sum |
| Report days | `timestamp` | Count **distinct** calendar days: `year * 1000 + dayOfYear` |

Targets: `scaleWeekly*(weeklyGoal*, rangeDays=7)`.

**Actual report days displayed:** `min(distinctDays, reportTarget)` (cap).

### 5.3 UI binding

| Ring | Label (weekly) | Subtitle format |
|------|----------------|-----------------|
| Study | Haftalık süre | `{formatted actual} / {formatted target}` |
| Questions | Haftalık soru | `{actual} / {target}` |
| Report | Rapor günü | `{actual} / {target} gün` |

- Center value: `{percent}%`
- “Hedefleri düzenle” → student settings (weekly goals section)

**Period labels** (when `rangeDays` ≠ ~7): use “Çalışma süresi”, “Soru sayısı”, “Rapor günü” — used on teacher student studies screen.

### 5.4 Minute formatting

- `< 60`: `"{n} dk"`
- `≥ 60`: `"{h} sa"` if remainder 0, else `"{h} sa {m} dk"`
- Weekly target display: if `minutes >= 60 && minutes % 60 == 0` → show hours only

---

## 6. Ana sayfa — öğretmen istatistik halkaları

### 6.1 Preconditions

- If time range is **“Seçiniz”** (not selected): all three rings show `—`, progress 0, subtitle **“Zaman aralığı seçin”**.
- If **no students**: 0%, subtitle **“Öğrenci yok”**.

### 6.2 Metrics (`computeTeacherStats`)

Let `N = studentCount`, `rangeDays = daysBetween(baslangic, bitis)`.

| Ring | Actual | Target | Percent |
|------|--------|--------|---------|
| **Rapor oranı** | `reportingCount` = students with ≥1 study in range | `N` | `progressPercent(reportingCount, N)` |
| **Sınıf çalışması** | Sum of all students’ minutes in range | `scaleWeeklyMinutes(Σ weeklyGoalMinutes, rangeDays)` | `progressPercent(classMinutes, benchmark)` |
| **Değerlendirme** | `ratingCount` = students with any rating doc | `N` | `progressPercent(ratingCount, N)` |

**Class minutes:** Sum `toplamCalisma` over all studies for all class students in `[start, end]` (same query bounds as row status).

**Weekly goal sum:** For each student id, read `weeklyGoalMinutes` from student doc (default 420), sum → `weeklyGoalMinutesSum`.

**Rating detection:** `School/.../Student/{id}/Degerlendirme` ordered by `degerlendirmeDate` desc, limit 1 — if exists, student counts as rated (stars value not used for percent).

### 6.3 UI strings

- Title: **Sınıf özeti**
- Reporting subtitle: `{reportingCount} / {studentCount} öğrenci`
- Class study subtitle: formatted minutes actual / benchmark

---

## 7. Öğretmen → öğrenci çalışmaları sayfası

**Android:** `StudiesActivity` (teacher opens one student for selected time range).

### 7.1 Layout

- Include `main_stats_student` as `studentStudiesStats` above subject grid.
- Title: `"{secilenZamanAraligi} özeti"` (e.g. “Bu Hafta özeti”).
- Hide “Hedefleri düzenle”; hide collapse toggle (`showToggle = false`).

### 7.2 Rings

- Load studies: `studiesInRangeQuery(kurumKodu, studentID, baslangic, bitis)`.
- Goals: `fetchWeeklyGoalsForStudent` (Firestore on **that student’s** school doc).
- `rangeDays = daysBetween(baslangic, bitis)`.
- Same `computeStudentStats` + `bindStudentRings` as home, with **period** labels when range ≠ 7 days.

### 7.3 Subject grid

Same curriculum resolution and subject list rules as §2.5.

---

## 8. İstatistik kartı — daralt / genişlet

### 8.1 Behavior

- Toggle on student and teacher home stats cards (`statsCollapseToggle`).
- Hides/shows `statsRingsContent`.
- Chevron rotation: collapsed 0°, expanded 180°.
- Persisted separately per role:
  - Prefs name: `main_home_stats_collapse`
  - Keys: `collapsed_student`, `collapsed_teacher`
  - Default: **expanded** (`false`)

**Web:** `localStorage` keys e.g. `kts.statsCollapsed.student` / `.teacher`.

---

## 9. Web’e taşınmaması gerekenler (emoji UI geri alındı)

Android **reverted** emoji-based subject tiles:

- **Before:** `SubjectTile(name, emoji)` e.g. `"Matematik", "∑"`, TYMM map with 🧬, 🌍, etc.
- **After:** Vector drawables per subject (`ic_subject_*`), `SubjectTile(name)` only.

**Do not implement on web:**

- Emoji as primary subject tile iconography matching the reverted Android experiment
- `tymmTileIcons` map or per-tile emoji strings in `LEGACY_STUDY_SUBJECT_TILES`

**Do implement:**

- Canonical subject **names** and program/grade resolution (§2)
- Optional: subject icons/colors consistent with your design system (not emoji requirement)

---

## 10. API / veri sözleşmesi özeti

### 10.1 Studies query (date range)

```
School/{kurumKodu}/Student/{studentId}/Studies
  .where('timestamp', '>', start)
  .where('timestamp', '<', end)
```

**Study document fields used in stats:**

| Field | Type (stored) | Use |
|-------|---------------|-----|
| `timestamp` | Timestamp | Range filter, report-day bucketing |
| `toplamCalisma` | string/number | Minutes |
| `çözülenSoru` | string/number | Questions |
| `dersAdi` | string | Aggregation key |
| `program` | string | Labels, Maarif detection |
| `tür` | string | LEGACY type, Maarif fallback |
| `temaAdi`, `konuAdi`, `sinif` | various | Labels, TYMM graph |

### 10.2 Student weekly goals

See §3.3.

### 10.3 Teacher row status (optional parity for student list badges)

`fetchStudentRowStatuses` per student:

| Flag | Logic |
|------|--------|
| `hasStudyInRange` | ∃ study in range |
| `ratingStars` | Latest `Degerlendirme.yildizSayisi` |
| `reportTimestamp` | `LastReports/{studentId}.timestamp` |

### 10.4 TYMM themes

```
LessonsTYMM/{dersAdi}/{sinif}  ordered by temaAdi
```

---

## 11. Web QA kontrol listesi

### Curriculum

- [ ] Grade 9–11 student → Maarif subjects; grade 13 → TYT/AYT subjects (per admin defaults).
- [ ] Grade 12 + `curriculumProgram: legacy` → TYT/AYT; `tymm` → Maarif.
- [ ] Grade 12 CHOICE: settings picker updates `User.curriculumProgram`.
- [ ] Hazırlık (0): only `İngilizce (Hazırlık-12)`, not `İngilizce (9-12)`.
- [ ] Teacher student studies: subtitle `Maarif · 10. sınıf` / `TYT/AYT · 12. sınıf`.
- [ ] Studies with extra `dersAdi` not in catalog still appear in grid.
- [ ] Class graph: Maarif skips TYT/AYT dialog; legacy shows picker.

### Student home rings

- [ ] Default goals 420 min, 350 questions, 7 report days if Firestore empty.
- [ ] Settings save updates rings and Firestore fields.
- [ ] Two studies same day → report days +1 not +2.
- [ ] `HaftalikHedefler` changes do **not** affect rings.
- [ ] Rolling 7-day study query matches ring period.

### Teacher home rings

- [ ] No range selected → em dash + hint.
- [ ] 10/20 students with studies → 50% reporting.
- [ ] Class benchmark = sum(student weekly minutes) scaled by `daysBetween`.
- [ ] Custom 1-day range: student with 420 min/week goal → benchmark 60 min for that student’s contribution to sum.

### Teacher student studies rings

- [ ] Same student goals as Firestore `weeklyGoal*`.
- [ ] “Bugün” (1 day): targets = weekly/7; 60 min logged → 100% if goal 420/week.
- [ ] Labels switch to period wording when range ≠ 7 days.

### Collapse

- [ ] Toggle persists across reload per role.

### Regression

- [ ] No emoji-only subject tiles added for parity.

---

## 12. Android dosya referansları

| Topic | Primary files |
|-------|----------------|
| Range scaling | `app/src/main/java/.../services/StatRangeScaling.kt`, `StatRangeScalingTest.kt` |
| Ring UI logic | `app/src/main/java/.../fragments/MainHomeStatsBinder.kt` |
| Weekly goals | `app/src/main/java/.../services/StudentWeeklyGoalsPreferences.kt`, `SettingsFragment.kt` |
| Firestore queries | `app/src/main/java/.../services/StudyQueryHelper.kt` |
| Student home wiring | `app/src/main/java/.../fragments/MainFragment.kt` |
| Teacher student studies | `app/src/main/java/.../StudiesActivity.kt` |
| Curriculum | `app/src/main/java/.../curriculum/GradeCurriculumRepository.kt`, `Subjects.kt`, `CurriculumProgram.kt`, `StudyLabels.kt` |
| Graph routing | `app/src/main/java/.../ActivityStudiesByClasses.kt` |
| Layouts / strings | `app/src/main/res/layout/main_stats_*.xml`, `view_main_stat_ring.xml`, `values/strings.xml`, `values/colors.xml` |
| daysBetween | `app/src/main/java/.../services/ExcelExportHelper.kt` |
| Subject tests | `app/src/test/java/.../curriculum/SubjectsTest.kt` |

---

## 13. Önerilen web modül yapısı

```text
lib/
  statRangeScaling.ts      // port StatRangeScaling + unit tests
  studentWeeklyGoals.ts    // defaults, clamp, Firestore field map
  gradeCurriculum.ts       // port GradeCurriculumRepository logic
  subjects.ts              // align with Subjects.kt (already referenced in comments)
  mainHomeStats.ts         // computeStudentStats, computeTeacherStats, bind helpers
```

Port Android unit tests (`StatRangeScalingTest`, `SubjectsTest`) to Jest/Vitest first — they are the acceptance spec for cross-platform parity.

---

*Generated from Android working tree on branch `master` (uncommitted). Re-run `git diff` before release if the branch changes.*
