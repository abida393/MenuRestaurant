# Admin module — implementation log

This document records what was wrong, what was changed, and where to look when you need to adjust the admin area later.

**Date:** 2026-05-15  
**Scope:** Administration console (dishes, categories, users, dashboard, login gate)

---

## 1. Problems found (before)

| Issue | Impact |
|--------|--------|
| `MainActivity` used as admin host with **wrong bottom menu** (`menu_bottom_nav.xml` had client IDs like `navigation_cart`) | Bottom nav did not match `nav_admin.xml` → crashes or wrong screens |
| `DashboardFragment` **redeclared** `viewModel` and referenced `SharedDishViewModel` / `ViewModelProvider` incorrectly (line 33) | Would not compile / wrong data source |
| **No session guard** on admin activity | Anyone could open admin UI without login |
| **No role check** (chef could not be blocked from admin if manifest/intent changed) | Security gap |
| Categories / users fragments talked to **Room directly** instead of a shared ViewModel | Inconsistent, hard to test |
| `AddEditDishFragment` used `SharedDishViewModel` and did not set `isValidatedByAdmin` | New admin dishes might not appear on client menu |
| `CategoryDao` had only `insert` | No delete/update for category management |
| Chef and admin shared `GestionPlatsFragment` without mode detection | Risk of chef validating dishes |

---

## 2. Solution overview

### New entry point: `AdminActivity`

- **Path:** `app/src/main/java/com/savoria/app/ui/admin/AdminActivity.kt`
- **Layout:** `app/src/main/res/layout/activity_admin.xml`
- **Menu:** `app/src/main/res/menu/menu_admin_bottom_nav.xml` (4 tabs aligned with `nav_admin.xml`)
- **Toolbar menu:** `menu_admin.xml` → Déconnexion

**Behaviour:**

1. On launch: `StaffSessionManager.isAdmin()` → else redirect to `LoginActivity`
2. Edge-to-edge insets (same pattern as client/chef)
3. Bottom nav: Dashboard | Plats | Catégories | Utilisateurs
4. FAB visible on Dashboard + Plats → navigates to `navigation_add_dish`
5. Logout clears session and returns to login

`MainActivity.kt` was **removed**. Manifest now registers `AdminActivity`.

### Login routing

- `LoginActivity` sends `UserRole.ADMIN` → `AdminActivity`
- If already logged in as admin, `LoginActivity` auto-forwards to `AdminActivity`

### Session helper

- `StaffSessionManager.isAdmin(context)` → logged in **and** role `ADMIN`

---

## 3. ViewModel layer

### `AdminViewModel` (expanded)

**Path:** `app/src/main/java/com/savoria/app/ui/viewmodel/AdminViewModel.kt`

| Responsibility | Methods / flows |
|----------------|-----------------|
| Dishes | `allDishes`, `addDish`, `updateDish`, `deleteDish`, `setAvailability`, `validateDish` |
| Validation queue | `pendingDishes`, `dashboardStats.pendingValidation` |
| Categories | `allCategories`, `addCategory`, `deleteCategory` |
| Users | `users`, `refreshUsers`, `addUser`, `deleteUser` |

**Important:** Admin-created/updated dishes get `isValidatedByAdmin = true` so they show on the **client** menu (`DishDao.getAvailableDishes()` filters on that flag).

### `AdminViewModelFactory`

**Path:** `app/src/main/java/com/savoria/app/ui/viewmodel/AdminViewModelFactory.kt`

Wires `DishRepository` + `CategoryDao` + `UserDao` from `SavoriaApplication.database`.

`ViewModelFactory.kt` (old, dish-only) was **deleted**.

---

## 4. Database changes

**No schema version bump** (still v7). DAO only:

### `CategoryDao`

- `countCategories(): Flow<Int>`
- `updateCategory`, `deleteCategory`
- Order: `ordreAffichage, nom ASC`

Existing tables unchanged. Dish ↔ category FK remains `ON DELETE SET NULL` (safe category delete).

---

## 5. Screen-by-screen

| Screen | File | Notes |
|--------|------|--------|
| Dashboard | `DashboardFragment.kt` | Uses `AdminViewModel`; stats + top dishes; “Gérer catégories” → categories tab |
| Plats | `GestionPlatsFragment.kt` | Tabs **Tous / À valider** (admin only); validate button; edit → `AddEditDishFragment` or chef dialog |
| Add/Edit plat | `AddEditDishFragment.kt` | `AdminViewModel`; categories from DB; `isValidatedByAdmin = true` |
| Quick dialog | `DishDialogFragment.kt` | Still used from **chef** flow; `isChefMode` → pending validation |
| Catégories | `GestionCategoriesFragment.kt` | Add (dialog), long-press delete |
| Utilisateurs | `GestionUsersFragment.kt` | Add/delete; passwords via `UserSeeder.hashPassword` |
| Login | `LoginActivity.kt` | Routes admin → `AdminActivity` |

### Shared chef/admin: `GestionPlatsFragment`

- `activity is AdminActivity` → admin UI (tabs, validate, full editor)
- Else → chef UI (`DishDialogFragment`, no validate)

### `DishAdminAdapter`

- New `btnValidate` in `item_dish_admin.xml`
- `showValidateButton` flag for chef vs admin

---

## 6. Navigation graph

**File:** `app/src/main/res/navigation/nav_admin.xml`

- Start: `navigation_dashboard`
- Optional arg on add dish: `dishId` (nullable `String`)

Bottom menu IDs **must** match fragment IDs:

- `navigation_dashboard`
- `navigation_plats`
- `navigation_categories`
- `navigation_users`

---

## 7. Strings added

See `app/src/main/res/values/strings.xml`:

- `admin_toolbar_title`, `admin_logout`, `nav_users`, `admin_validate_dish`
- Category / user / dish form strings

---

## 8. Tests (simulation)

| Test | Path | What it checks |
|------|------|----------------|
| `DashboardStatsTest` | `app/src/test/.../DashboardStatsTest.kt` | Counts on `DashboardStats` data class |

Run locally:

```bash
./gradlew testDebugUnitTest
```

**Result (2026-05-15):** `BUILD SUCCESSFUL` — includes `DashboardStatsTest`.

### Extra fixes during build (same session)

| File | Fix |
|------|-----|
| `fragment_gestion_categories.xml` | Repaired broken XML (unclosed `NestedScrollView`) |
| `MenuFragment.kt` | Dynamic category chips (layout no longer has static `chip_mains` IDs) |
| `ClientActivity.kt` | Removed duplicate `bottomNav` variable; added `enableEdgeToEdge` import |
| `ChefActivity.kt` | Added missing `enableEdgeToEdge` import |

Instrumented UI tests were not added (no emulator in CI here). Manual checklist:

1. Client app → drawer → Admin login → `admin@savoria.com` / `admin123`
2. Dashboard loads counts
3. Bottom nav switches all 4 tabs
4. Add dish → appears in list and client menu (validated)
5. Chef adds dish (chef login) → appears under Plats **À valider** → Valider → visible client-side
6. Add category → spinner in add dish reflects it
7. Add user → listed; long-press delete
8. Toolbar → Déconnexion → back to login

---

## 9. Demo credentials

| Role | Email | Password |
|------|--------|----------|
| Admin | admin@savoria.com | admin123 |
| Chef | chef@savoria.com | chef123 |
| Serveur | serveur@savoria.com | serveur123 |

---

## 10. Files touched (quick index)

**Created**

- `ui/admin/AdminActivity.kt`
- `res/layout/activity_admin.xml`
- `res/menu/menu_admin_bottom_nav.xml`
- `res/menu/menu_admin.xml`
- `ui/viewmodel/AdminViewModelFactory.kt`
- `docs/ADMIN_IMPLEMENTATION_LOG.md`
- `app/src/test/.../DashboardStatsTest.kt`

**Deleted**

- `MainActivity.kt`
- `ui/viewmodel/ViewModelFactory.kt`

**Modified**

- `AdminViewModel.kt`, all admin fragments, `DishAdminAdapter`, `CategoryDao`, `StaffSessionManager`, `LoginActivity`, `AndroidManifest.xml`, `nav_admin.xml`, `strings.xml`, `fragment_gestion_plats.xml`, `item_dish_admin.xml`, `fragment_dashboard.xml`

**Legacy (unused by admin now)**

- `res/menu/menu_bottom_nav.xml` — old mixed client/admin menu; safe to delete if nothing references it

---

## 11. Future change hints

- **New admin tab:** Add fragment to `nav_admin.xml` + item in `menu_admin_bottom_nav.xml` + top-level destination in `AdminActivity` `AppBarConfiguration`
- **Stricter security:** Replace SHA-256 with salted hashes; add `MainActivity`/`AdminActivity` launch protection via `exported=false` (already) + session on every fragment
- **Category edit:** Use `CategoryDao.updateCategory` (DAO ready)
- **Revenue on dashboard:** `OrderDao.todayRevenue()` already exists — bind in `AdminViewModel` if needed

---

*End of log.*
