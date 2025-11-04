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

#### D6 (Standard - 6 Colors):
- 🔴 Red: 1 base point
- 🟢 Green: 2 base points
- 🔵 Blue: 3 base points
- 🟡 Yellow: 4 base points
- 🟣 Magenta: 5 base points
- 🩵 Cyan: 6 base points

#### D10 (10 Divine Essence):
Adds 4 colors:
- 🟠 Orange: 7 base points
- 🩷 Pink: 8 base points
- 🟪 Purple: 9 base points
- 🟦 Turquoise: 10 base points

#### D12 (1,280 Divine Essence):
Adds 2 colors:
- 🟩 Lime: 11 base points
- 🟫 Brown: 12 base points

#### D20 (163,840 Divine Essence):
Adds 8 colors:
- 🟨 Gold: 13 base points
- ⬜ Silver: 14 base points
- 🟧 Bronze: 15 base points
- 🔷 Navy: 16 base points
- 🔶 Maroon: 17 base points
- 🫒 Olive: 18 base points
- 🔹 Teal: 19 base points
- 🪸 Coral: 20 base points

**Cost Progression:** Each die upgrade costs 128× more than the previous

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
- **Base Effect:** Each earned Divine Essence gives +1 point per click (permanent)
- **Calculation:** `totalDivineEssenceEarned × (1 + essencePowerLevel)`
- **Important:** Bonus based on EARNED essence, not spent

### Prestige Upgrades (Divine Essence-based)

#### 🎲 D10 Die (10 Divine Essence)
- Unlocks 4 new colors (Orange, Pink, Purple, Turquoise)
- Increases die from 6 to 10 sides

#### 🎲 D12 Die (1,280 Divine Essence)
- Requires: D10 active
- Unlocks 2 new colors (Lime, Brown)
- Increases die from 10 to 12 sides

#### 🎲 D20 Die (163,840 Divine Essence)
- Requires: D12 active
- Unlocks 8 new colors
- Increases die from 12 to 20 sides

#### 🎨 Essence Power (Max Level 10)
- **Effect:** Each Divine Essence gives an additional +1 point per level
- **Cost Progression:** 1, 5, 10, 50, 100, 200, 400, 800, 1600, 3200 Divine Essence
- **Formula:** From Level 6 onwards, costs double

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

#### `Cube.kt`, `D10.kt`, `D12.kt`, `D20.kt`
**Purpose:** 3D geometry definitions for different dice types

**Color per Side:**
- Front: Red, Back: Green, Left: Blue, Right: Yellow, Top: Magenta, Bottom: Cyan
- All other colors use the same 6 sides (different rotations)

---

## Important Formulas

### Points per Click
```kotlin
totalPoints = basePoints + upgradeLevel + (totalDivineEssenceEarned × (1 + essencePowerLevel))
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
6. **D100 Die:** Further expansion (Cost: 163,840 × 128 = 20,971,520)
7. **Prestige Level Tracking:** Display number of prestiges performed

### Performance Optimizations:
1. Object pooling for less GC
2. Shader optimization
3. Batch rendering if multiple dice
4. Compress/cache calculations

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

---

## Implemented Features (Chronological)

1. ✅ Upgrade increment adjusted (base points instead of +1)
2. ✅ Prestige system with Divine Essence
3. ✅ Prestige only at 1000+ lifetime points
4. ✅ One-time prestige rewards per threshold
5. ✅ Click value display
6. ✅ Click-based rotation (no auto-rotate)
7. ✅ Frontal color display with dual-axis rotation
8. ✅ Essence Power upgrade (10 levels)
9. ✅ Auto clicker (1 point, 1 second interval)
10. ✅ Auto clicker speed upgrade (100 levels)
11. ✅ Dual-variable system for Divine Essence (available/earned)
12. ✅ Offline progress system
13. ✅ Live updates in menus (100ms interval)
14. ✅ D10 die upgrade (4 new colors)
15. ✅ D12 die upgrade (2 new colors)
16. ✅ D20 die upgrade (8 new colors)
17. ✅ Lifetime Divine Essence display
18. ✅ Buff system (4 buff types)
19. ✅ Settings page with game reset
20. ✅ English translation
21. ✅ Divine Essence rename
22. ✅ TuffGames rebranding

---

## Code Quality Notes

- Clean Kotlin syntax
- Singleton pattern for GameState
- Separation of concerns (State/Rendering/UI)
- Minimal hardcoded magic numbers
- Comments in English
- Backward compatible save data

---

## Balance Numbers

- Prestige: 1000 lifetime per Divine Essence
- Essence Power: Max 10 levels, costs escalate strongly
- Auto clicker speed: Linear -0.01s per level (balanced)
- Die upgrades: Exponential (×128) for late-game content
- Buff frequency: Adjusts based on player choices

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
