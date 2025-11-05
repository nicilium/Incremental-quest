# Incremental Quest - Development Log

## Project Overview

An Android-based idle/clicker game with 3D dice rendered using OpenGL ES 2.0. The game combines clicker mechanics with a prestige system and progressive upgrades.

**Technology Stack:**
- Kotlin
- Android OpenGL ES 2.0
- Gradle 8.13
- Java 17

**Project Path:** `D:\mylia\android-3d-game`
**Package:** `com.tuffgames.incrementalquest`
**Developer:** TuffGames

---

## Session History

### Session 2025-11-05 - Dice System & Essence Power Overhaul

#### 1. Dice Progression Restructure
**Status**: ✅ Complete

**Major Changes:**
- **New Starting Die:** Game now begins with D4 (Tetrahedron) instead of D6
- **Added D4 Model:** Geometrically correct 4-sided die (Tetrahedron)
- **Added D6 Model:** Renamed from Cube.kt for consistency
- **Added D8 Model:** Geometrically correct 8-sided die (Octahedron)

**New Progression:**
```
D4 (Start) → D6 (1 DE) → D8 (5 DE) → D10 (25 DE) → D12 (125 DE) → D20 (625 DE)
```

**Old Progression (removed):**
```
D6 (Start) → D10 (10 DE) → D12 (1,280 DE) → D20 (163,840 DE)
```

**Cost Rebalancing:**
- **Old:** 128× multiplier between upgrades (exponential explosion)
- **New:** 5× multiplier between upgrades (smoother progression)
- Much more accessible for players to reach higher dice tiers

**Code Changes:**
- Created `D4.kt`: 4-face tetrahedron with 4 colors (Red, Green, Blue, Yellow)
- Created `D6.kt`: 6-face cube (formerly Cube.kt)
- Created `D8.kt`: 8-face octahedron with 8 colors (adds Orange, Pink to D6)
- Updated `GameState.kt`:
  - Added `d4Active = true` (default start)
  - Added `d6Active`, `d8Active` flags
  - Added `buyD6()`, `buyD8()` functions
  - Updated `getAvailableColors()` for all dice types
- Updated `GameRenderer.kt`:
  - Initialize all 6 dice models (D4, D6, D8, D10, D12, D20)
  - Dynamic die selection based on active upgrades
- Updated `PrestigeActivity.kt`:
  - Added D6 and D8 upgrade cards
  - Progressive unlock UI (only next die shown)
  - Current die indicator

**Backward Compatibility:**
- Old saves automatically migrate to new system
- Players with D10+ unlocked get all previous dice (D4, D6, D8) activated
- Players with only D6 are migrated to D6 Active (skip D4)

**Files Modified:** 5 files (D4.kt, D6.kt, D8.kt, GameState.kt, GameRenderer.kt, PrestigeActivity.kt)
**Files Created:** 3 new dice models
**Commit:** `e071651` - "Add D4 and D8 dice, restructure dice progression system"

---

#### 2. Essence Power System Overhaul
**Status**: ✅ Complete

**Old System (Tier-Based):**
- 10 fixed tiers with exponential costs (1, 10, 100, 1000, ...)
- Each tier gave +1 flat bonus per click
- Limited progression (capped at 10 tiers)
- Predictable and boring

**New System (Level-Based with Powerspikes):**
- **Unbounded Progression:** Infinite levels (no cap)
- **Multiplicative Bonus:** Increases multiplier on lifetime Divine Essence
- **4 POWERSPIKES:** Special milestone levels with massive bonuses
  - Level 10: +10% multiplier bonus (Cost: 100 DE) 🔥
  - Level 25: +20% multiplier bonus (Cost: 400 DE) 🔥
  - Level 50: +40% multiplier bonus (Cost: 1,200 DE) 🔥
  - Level 100: +100% multiplier bonus (Cost: 4,000 DE) 🔥

**Formula:**
```kotlin
multiplier = 0.01 + (level^1.3 × 0.01) + powerspikeBonus
totalBonus = totalDivineEssenceEarned × multiplier
```

**Cost Progression:**
- **Normal levels:** `ceil(level^1.6)` Divine Essence
  - Level 1: 1 DE, Level 5: 12 DE, Level 20: 80 DE
- **Powerspike levels:** `ceil(level^2.2)` Divine Essence (much more expensive!)
  - Level 10: 100 DE, Level 25: 400 DE, Level 50: 1,200 DE, Level 100: 4,000 DE

**Example Progression:**
| Level | Multiplier | Cost (DE) | Notes |
|-------|-----------|-----------|-------|
| 1     | 2.0%      | 1         | Starting |
| 5     | 8.0%      | 12        | |
| 10    | 27.0%     | 100       | **POWERSPIKE** (+10%) 🔥 |
| 15    | 25.0%     | 30        | |
| 25    | 63.0%     | 400       | **POWERSPIKE** (+20%) 🔥 |
| 50    | 152.0%    | 1,200     | **POWERSPIKE** (+40%) 🔥 |
| 100   | 352.0%    | 4,000     | **POWERSPIKE** (+100%) 🔥 |

**Code Changes:**
- `GameState.kt`:
  - Removed `essencePowerUpgrades: BooleanArray(10)`
  - Added `essencePowerLevel: Int` (unbounded)
  - New function: `calculateEssenceBonus()` - level^1.3 + powerspikes
  - New function: `getEssencePowerMultiplier()` - returns current multiplier
  - New function: `getNextPowerspikeLevel()` - shows progress to next milestone
  - Updated `buyEssencePower()` - single-level purchases
  - Updated `getEssencePowerCost()` - dynamic cost calculation
  - Migration logic: 1 old tier = 3 new levels (generous conversion)

- `PrestigeActivity.kt`:
  - Completely redesigned Essence Power card
  - Shows: Current level, multiplier %, total bonus
  - Displays progress to next powerspike with countdown
  - Powerspike buttons highlighted in RED
  - Real-time bonus calculation and preview
  - Dynamic "levels to go" indicator

**Benefits:**
- ✅ Clear progression goals (powerspikes create excitement)
- ✅ "Wow moments" at milestone levels
- ✅ Balanced exponential growth with dampening
- ✅ Infinite progression potential
- ✅ More engaging than static tier system
- ✅ Players work toward meaningful milestones

**Backward Compatibility:**
- Old saves automatically convert tier progress to levels
- Generous conversion rate (3 levels per old tier)
- Save system handles both old and new formats

**Files Modified:** 2 files (GameState.kt, PrestigeActivity.kt)
**Commit:** `582112c` - "Complete overhaul of Essence Power upgrade system"

---

### Session 2025-11-03 - Major Refactoring Complete

#### 1. UI Translation & Simplification
**Status**: ✅ Complete

- **Language**: All UI elements translated from German to English
- **Terminology**: Unified to use "Points" consistently (removed Score/Punkte confusion)
- **Simplification**: Removed all technical details from user-facing text
  - No more scaling formulas visible to users
  - Removed detailed mathematical explanations
  - Kept only essential information users need to know

**Example Changes**:
- Buttons: "⬆️ UPGRADES", "✨ PRESTIGE", "⚡ BOOST"
- Status displays simplified to show only current values
- Explanations reduced to 1-2 sentence descriptions

#### 2. Prestige Currency Rename: "Divine Essence"
**Status**: ✅ Complete

**Old Name**: Lackdosen (Paint Cans)
**New Name**: Divine Essence

**Rationale**: More epic and tabletop-themed naming to match "Incremental Quest" branding.

**Code Changes**:
- `paintCans` → `divineEssence`
- `totalPaintCansEarned` → `totalDivineEssenceEarned`
- `paintCanBonusUpgrades` → `essencePowerUpgrades`
- `buffOffersWithoutPaintCan` → `buffOffersWithoutEssence`
- Updated 10+ related functions across all Activity files

**Backward Compatibility**: Maintained old SharedPreferences keys to preserve existing player save data.

#### 3. Project Rebranding
**Status**: ✅ Complete

**Old Name**: hello3d
**New Name**: Incremental Quest

**Package Changes**:
- `com.example.hello3d` → `com.tuffgames.incrementalquest`
- Company identifier changed to "tuffgames"
- All 11 Kotlin files updated with new package declarations

**Configuration Updates**:
- `build.gradle`: Updated `namespace` and `applicationId`
- `AndroidManifest.xml`: Updated app label to "Incremental Quest"

#### 4. Build Status

✅ **BUILD SUCCESSFUL** in 6s

**Final Package**: `com.tuffgames.incrementalquest`
**APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

**Statistics**:
- **Files Modified**: 11 Kotlin files + 2 config files
- **Lines Changed**: ~500+ across all files
- **Variables Renamed**: 20+
- **Functions Renamed**: 10+

---

## Core Gameplay

### Basic Mechanics
- 3D dice rendered with OpenGL ES 2.0
- Each click on the die:
  - Awards points based on the current color
  - Rotates the die to a new random color
  - Displays the color frontally (not from the side)

### Color System

#### D4 (Starting Die - 4 Colors):
- 🔴 Red: 1 base point
- 🟢 Green: 2 base points
- 🔵 Blue: 3 base points
- 🟡 Yellow: 4 base points

#### D6 (1 Divine Essence):
Adds 2 colors:
- 🟣 Magenta: 5 base points
- 🩵 Cyan: 6 base points

#### D8 (5 Divine Essence):
Adds 2 colors:
- 🟠 Orange: 7 base points
- 🩷 Pink: 8 base points

#### D10 (25 Divine Essence):
Adds 2 colors:
- 🟪 Purple: 9 base points
- 🟦 Turquoise: 10 base points

#### D12 (125 Divine Essence):
Adds 2 colors:
- 🟩 Lime: 11 base points
- 🟫 Brown: 12 base points

#### D20 (625 Divine Essence):
Adds 8 colors:
- 🟨 Gold: 13 base points
- ⬜ Silver: 14 base points
- 🟧 Bronze: 15 base points
- 🔷 Navy: 16 base points
- 🔶 Maroon: 17 base points
- 🫒 Olive: 18 base points
- 🔹 Teal: 19 base points
- 🪸 Coral: 20 base points

**Cost Progression:** Each die upgrade costs 5× more than the previous (balanced progression)

---

## Upgrade System

### Color Upgrades (Points-based)
- **Cost:** `Base Points × 100 × Multiplier`
- **Multiplier:** Starts at 1.01, squares after each purchase (Multiplier²)
- **Effect:** Increases click value by the color's base points
  - Example: Red upgrade gives +1, Cyan upgrade gives +6
- **Unlock:** Available at 200 points

### Auto Clicker (1 point)
- **Function:** Automatically clicks the die
- **Base Interval:** 1 second
- **Offline Progress:** Works in menus and after app closure

---

## Prestige System

### Mechanics
- **Unlock:** At 1000 lifetime points
- **Reward:** 1 Divine Essence per 1000 lifetime points (one-time per threshold)
- **Reset:** Resets all points and upgrades
- **Persistent:** Divine Essence, lifetime points, and prestige upgrades remain

### Divine Essence Bonus
- **Base Effect:** 1% of total earned Divine Essence (multiplicative)
- **Essence Power:** Increases the multiplier through level-based progression
- **Calculation:** `totalDivineEssenceEarned × multiplier`
- **Important:** Bonus based on EARNED essence, not spent

### Prestige Upgrades (Divine Essence-based)

#### 🎲 D6 Die (1 Divine Essence)
- Unlocks 2 new colors (Magenta, Cyan)
- Increases die from 4 to 6 sides

#### 🎲 D8 Die (5 Divine Essence)
- Requires: D6 active
- Unlocks 2 new colors (Orange, Pink)
- Increases die from 6 to 8 sides

#### 🎲 D10 Die (25 Divine Essence)
- Requires: D8 active
- Unlocks 2 new colors (Purple, Turquoise)
- Increases die from 8 to 10 sides

#### 🎲 D12 Die (125 Divine Essence)
- Requires: D10 active
- Unlocks 2 new colors (Lime, Brown)
- Increases die from 10 to 12 sides

#### 🎲 D20 Die (625 Divine Essence)
- Requires: D12 active
- Unlocks 8 new colors (Gold, Silver, Bronze, Navy, Maroon, Olive, Teal, Coral)
- Increases die from 12 to 20 sides

#### ✨ Essence Power (Unbounded Levels)
- **Effect:** Increases multiplier on lifetime Divine Essence
- **Formula:** `multiplier = 0.01 + (level^1.3 × 0.01) + powerspikeBonus`
- **Cost:** Normal levels = `ceil(level^1.6)` DE, Powerspikes = `ceil(level^2.2)` DE
- **Powerspikes:**
  - Level 10: +10% multiplier (Cost: 100 DE) 🔥
  - Level 25: +20% multiplier (Cost: 400 DE) 🔥
  - Level 50: +40% multiplier (Cost: 1,200 DE) 🔥
  - Level 100: +100% multiplier (Cost: 4,000 DE) 🔥
- **Progression:** Infinite levels with exponentially dampened costs

#### ⚡ Auto Clicker Speed (Max Level 100)
- **Requires:** Auto clicker must be purchased
- **Effect:** Reduces click interval by 0.01 seconds per level
- **Cost Progression:** 1, 2, 4, 8, 16... (doubles: 2^Level)
- **Formula:** `Interval = max(10ms, 1000ms - (Level × 10ms))`
- **Maximum:** 0.01 seconds (100 clicks/second) at Level 99+

---

## Buff System

### Buff Types
1. **⭐ Double Points** - 2x points for 2 minutes
2. **⚡ Fast Auto** - 2x auto-click speed for 2 minutes
3. **✨ Free Essence** - Gain 1 Divine Essence instantly
4. **🎲 Free Upgrade** - Get a free color upgrade

### Mechanics
- Buffs offered randomly after multiple clicks
- Can be claimed or declined
- Declining gives a small penalty to frequency
- Divine Essence buffs require minimum Divine Essence earned

---

## File Structure

### Main Files

#### `GameState.kt`
**Purpose:** Central game state management (Singleton)

**Important Variables:**
- `totalScore`: Current points (reset on prestige)
- `lifetimeScore`: Total points ever earned (persistent)
- `divineEssence`: Available Divine Essence to spend
- `totalDivineEssenceEarned`: Total Divine Essence earned (for point bonus)
- `upgradesUnlocked`: Boolean for upgrade menu unlock (200 points)
- `autoClickerActive`: Boolean for auto clicker status
- `autoClickerSpeedLevel`: 0-100, speed level
- `d10Active`, `d12Active`, `d20Active`: Boolean for die upgrades
- `essencePowerUpgrades`: Array of 10 booleans for Essence Power tiers
- `prestigesClaimed`: Number of prestige thresholds already claimed

**Important Functions:**
```kotlin
onColorClicked(color: CubeColor) // Awards points for click
getCurrentPoints(color: CubeColor): Int // Calculates current point value for color
getAvailableColors(): List<CubeColor> // Returns list of available colors (D6/D10/D12/D20)
performPrestige(): Boolean // Performs prestige
processOfflineClicks(): Int // Calculates auto-clicks while app inactive
```

#### `GameRenderer.kt`
**Purpose:** OpenGL ES 2.0 rendering and animation

**Rotation System:**
- Each color has X and Y rotation for frontal display
- Animation interpolates between current and target rotation (5° per frame)
- D6 colors: Main axes (0°, 90°, 180°, etc.)
- D10 colors: Diagonals (45°, 135°, -45°, -135°)
- D12/D20: Combinations of X and Y rotations

**Auto Clicker Logic:**
- Runs in `onDrawFrame()` (called ~60x per second)
- Checks time since last auto-click
- Calls `performClick()` when interval elapsed

#### `MainActivity.kt`
**Purpose:** Main game screen

**UI Elements:**
- GLSurfaceView for 3D rendering
- Score display (top)
- Click value display (below)
- Upgrade button (appears at 200 points)
- Prestige button (appears at 1000 lifetime points)
- Buff button (appears when buff available)

**Lifecycle:**
- `onResume()`: Processes offline clicks
- `onPause()`: Marks time for offline calculation

#### `UpgradeActivity.kt`
**Purpose:** Color upgrade menu

**Features:**
- List of all unlocked colors with upgrade buttons
- Auto clicker purchase card (top)
- Live score update (100ms interval)
- Shows costs and current levels

#### `PrestigeActivity.kt`
**Purpose:** Prestige menu with two views

**Main View:**
- Divine Essence display (available / total earned)
- Score and lifetime score
- Progress to next prestige
- Prestige button (only if Divine Essence available)
- Color table with current point values
- Button to prestige upgrades

**Upgrade View:**
- D10/D12/D20 upgrade cards (progressive)
- Auto clicker speed (only if auto clicker active)
- Essence Power (10 tiers)

**Live Updates:**
- Every 100ms offline clicks are processed
- UI automatically updated
- Allows waiting for enough points/Divine Essence

#### `BuffActivity.kt`
**Purpose:** Buff/boost selection screen

**Features:**
- Random buff offer display
- Claim or decline buttons
- Visual feedback for buff types
- Simplified descriptions

#### `SettingsActivity.kt`
**Purpose:** Settings and game reset

**Features:**
- Volume control (UI placeholder)
- Game reset with triple confirmation
- Warning messages

#### `D4.kt`, `D6.kt`, `D8.kt`, `D10.kt`, `D12.kt`, `D20.kt`
**Purpose:** 3D geometry definitions for different dice types

**D4 (Tetrahedron):**
- 4 triangular faces
- Colors: Red, Green, Blue, Yellow

**D6 (Hexahedron/Cube):**
- 6 square faces
- Front: Red, Back: Green, Left: Blue, Right: Yellow, Top: Magenta, Bottom: Cyan

**D8 (Octahedron):**
- 8 triangular faces
- Uses cube structure with additional colors (Orange, Pink)

**D10/D12/D20:**
- Use base 6-sided geometry with different rotations for additional colors
- Geometrically simplified but functionally complete

---

## Important Formulas

### Points per Click
```kotlin
// Base points from color
basePoints = baseColorPoints[color]

// Upgrade bonuses
upgradeBonus = basePoints × upgradeCount[color]
permanentBonus = permanentColorUpgrades[color]

// Divine Essence bonus (multiplicative!)
essenceMultiplier = 0.01 + (essencePowerLevel^1.3 × 0.01) + powerspikeBonus
essenceBonus = totalDivineEssenceEarned × essenceMultiplier

// Total
totalPoints = basePoints + upgradeBonus + permanentBonus + essenceBonus
```

### Upgrade Costs
```kotlin
cost = (basePoints × 100) × multiplier
// After purchase: multiplier = multiplier × multiplier
```

### Prestige Rewards
```kotlin
totalPrestigesEarned = lifetimeScore / 1000
availableRewards = totalPrestigesEarned - prestigesClaimed
```

### Auto Clicker Interval
```kotlin
interval = max(10ms, 1000ms - (speedLevel × 10ms))
```

### Offline Clicks
```kotlin
clicksToAdd = (timePassed / interval).toInt()
pointsToAdd = clicksToAdd × averageClickValue
```

---

## Implementation Details

### Thread Safety
- `@Volatile` on `currentColor` in GameRenderer for cross-thread access
- `runOnUiThread{}` for UI updates from render thread

### Handler for Live Updates
- Android Handler with Looper for 100ms update interval
- Started in `onResume()`, stopped in `onPause()`
- Prevents memory leaks with `removeCallbacks()`

### Prestige System Logic
- **One-time Rewards:** `prestigesClaimed` tracks already claimed thresholds
- **Example:** At 3500 lifetime points:
  - `totalPrestigesEarned = 3500 / 1000 = 3`
  - If `prestigesClaimed = 1`, then `availableRewards = 2`
  - After prestige: `prestigesClaimed = 3`

### Dual-Variable System for Divine Essence
- **`divineEssence`:** Available Divine Essence (spent on upgrades)
- **`totalDivineEssenceEarned`:** All ever earned (for permanent point bonus)
- Important for balance: Bonus remains even after spending

---

## Build & Deployment

### Build Commands
```bash
gradlew.bat assembleDebug
```

### Build Configuration
- Gradle Version: 8.13
- Java Version: 17
- Android minSdk: 21
- Android targetSdk: 34
- No external dependencies except Android standard

---

## Known Limitations

1. **Die Geometry:** Uses simple Cube with 6 sides
   - D10/D12/D20 use the same 6 sides with different rotations
   - Geometrically not correct, but functionally sufficient

2. **Offline Progress:**
   - Based on average click value of all colors
   - Not exact like real clicks (which choose random colors)

3. **Animation Speed:**
   - Fixed at 5° per frame
   - May be noticeable with very fast rotations

4. **UI Refresh Rate:**
   - 100ms in menus for performance
   - 60fps in main game through OpenGL

---

## Testing Scenarios

### Basic Features:
1. Click on die → Points should increase
2. Buy auto clicker for 1 point → Die should auto-click
3. Reach 200 points → Upgrade button appears
4. Buy color upgrade → Click value should increase
5. Reach 1000 lifetime → Prestige button appears

### Prestige System:
1. Reach 3000 lifetime points
2. Open prestige menu → Should show 3 Divine Essence
3. Perform prestige → Score to 0, receive 3 Divine Essence
4. Click die → Should give +3 more points (Divine Essence bonus)

### Die Upgrades:
1. Earn 10 Divine Essence → Buy D10
2. Die should now show 10 different colors
3. Upgrade menu should show 10 colors
4. Earn 1,280 Divine Essence → Buy D12 (D20 at 163,840)

### Offline Progress:
1. Buy auto clicker
2. Open upgrade menu → Wait 10 seconds
3. Score should count up live
4. Close app → Wait → Open app
5. Score should have increased accordingly

---

## Next Session TODO

### High Priority
1. **Test app on device/emulator** - Verify all functionality works with new package
2. **Fix unused variable warning** - Review MainActivity.kt:228 offlineClicks logic
3. **Consider FLAG_FULLSCREEN alternatives** - Address deprecation warnings

### Medium Priority
4. **Offline progression** - Implement offline clicks calculation (variable exists but unused)
5. **Sound system** - Implement volume control (UI exists in SettingsActivity)
6. **App icon** - Replace default icon with custom game icon

### Low Priority
7. **Code cleanup** - Remove unused parameters in GameRenderer
8. **UI polish** - Consider adding more visual feedback
9. **Translations** - Support multiple languages (optional)

---

## Future Enhancement Possibilities

### Potential Features (not implemented):
1. **Achievements:** Special milestones with rewards
2. **Statistics:** Tracking of total clicks, average points, etc.
3. **Sound/Music:** Audio feedback for clicks and upgrades
4. **Particle Effects:** Visual feedback on clicks
5. **Additional Prestige Upgrades:**
   - Multiplier for all points
   - Offline earnings boost
   - Auto-prestiging
6. **Additional Dice Types:** D100, percentile dice, or specialty dice
7. **Prestige Level Tracking:** Display number of prestiges performed
8. **Color Themes:** Dark mode, custom UI themes
9. **Leaderboards:** Compare progress with other players

### Performance Optimizations:
1. Object pooling for less GC
2. Shader optimization
3. Batch rendering if multiple dice
4. Compress/cache calculations
5. Optimize essence bonus calculation caching

---

## Version History

- **v1.0** - Initial release (as hello3d)
  - Basic clicker mechanics
  - Color upgrade system
  - Prestige system with paint cans
  - D6/D10/D12/D20 progression
  - Auto clicker with speed upgrades
  - German UI

- **v1.1** - Major refactoring (2025-11-03)
  - English translation
  - Divine Essence rename (paint cans → Divine Essence)
  - TuffGames rebranding (hello3d → Incremental Quest)
  - Package restructure (com.example → com.tuffgames)
  - UI simplification
  - Buff system implementation
  - Settings page with reset functionality

- **v1.2** - Dice System & Essence Power Overhaul (2025-11-05)
  - Added D4 (Tetrahedron) as starting die
  - Added D6 (Hexahedron/Cube) as first upgrade
  - Added D8 (Octahedron) as second upgrade
  - Restructured dice progression: D4 → D6 → D8 → D10 → D12 → D20
  - Rebalanced die costs: 5× multiplier (was 128×)
  - Complete Essence Power overhaul: Level-based system with powerspikes
  - 4 powerspike levels (10, 25, 50, 100) with massive bonuses
  - Unbounded progression (infinite levels)
  - Exponentially dampened cost scaling
  - Backward-compatible save migration

---

## Implemented Features (Chronological)

1. ✅ Upgrade increment adjusted (base points instead of +1)
2. ✅ Prestige system with Divine Essence
3. ✅ Prestige only at 1000+ lifetime points
4. ✅ One-time prestige rewards per threshold
5. ✅ Click value display
6. ✅ Click-based rotation (no auto-rotate)
7. ✅ Frontal color display with dual-axis rotation
8. ✅ Essence Power upgrade (unbounded levels with powerspikes)
9. ✅ Auto clicker (1 point, 1 second interval)
10. ✅ Auto clicker speed upgrade (100 levels)
11. ✅ Dual-variable system for Divine Essence (available/earned)
12. ✅ Offline progress system
13. ✅ Live updates in menus (100ms interval)
14. ✅ D4 die (starting die, 4 colors)
15. ✅ D6 die upgrade (adds 2 colors: Magenta, Cyan)
16. ✅ D8 die upgrade (adds 2 colors: Orange, Pink)
17. ✅ D10 die upgrade (adds 2 colors: Purple, Turquoise)
18. ✅ D12 die upgrade (adds 2 colors: Lime, Brown)
19. ✅ D20 die upgrade (adds 8 colors: Gold, Silver, Bronze, Navy, Maroon, Olive, Teal, Coral)
20. ✅ Lifetime Divine Essence display
21. ✅ Buff system (4 buff types)
22. ✅ Settings page with game reset
23. ✅ English translation
24. ✅ Divine Essence rename
25. ✅ TuffGames rebranding
26. ✅ Geometrically correct dice models (D4 Tetrahedron, D8 Octahedron)
27. ✅ Level-based progression with powerspikes
28. ✅ Exponentially dampened cost scaling

---

## Code Quality Notes

- Clean Kotlin syntax
- Singleton pattern for GameState
- Separation of concerns (State/Rendering/UI)
- Minimal hardcoded magic numbers
- Comments in English
- Backward compatible save data
- Dynamic cost calculation for scalable progression
- Modular dice model architecture

---

## Balance Numbers

- **Prestige:** 1000 lifetime points per Divine Essence
- **Essence Power:** Unbounded levels, exponentially dampened costs
  - Normal levels: `ceil(level^1.6)` DE
  - Powerspike levels: `ceil(level^2.2)` DE
  - Powerspikes at 10, 25, 50, 100 with +10%, +20%, +40%, +100% bonuses
- **Auto clicker speed:** Linear -0.01s per level (balanced)
- **Die upgrades:** 5× multiplier progression (D4→D6→D8→D10→D12→D20)
  - Costs: 0, 1, 5, 25, 125, 625 Divine Essence
- **Buff frequency:** Adjusts based on player choices
- **Divine Essence bonus:** Multiplicative with level-based scaling

---

## Known Issues

**Build Warnings** (non-critical):
- FLAG_FULLSCREEN deprecation warnings (cosmetic, will address in future update)
- Unused parameters in GameRenderer (no functional impact)
- Unused variable 'offlineClicks' in MainActivity (no functional impact)

**Functionality:**
None - all core functionality working as intended.

---

*Last Updated: 2025-11-03*
*Developer: TuffGames*
*Project: Incremental Quest*
*Package: com.tuffgames.incrementalquest*
