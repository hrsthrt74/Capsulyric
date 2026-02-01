# Lyric Truncation Feature Documentation

## Overview
This feature addresses the issue of long lyrics being displayed incompletely in the Dynamic Island notification. It provides an option to split long lyrics into smaller pieces that can be displayed sequentially.

## Problem Statement
When lyrics are too long, they cannot fit in the Dynamic Island display area, resulting in incomplete or cut-off text. This makes it difficult for users to read and follow along with the song.

## Solution
The lyric truncation feature splits long lyrics into multiple pieces, where each piece:
- Contains a maximum of 12 visual weight units
- Is displayed for a duration proportional to its weight relative to the total lyric
- Rotates automatically to show the next piece

### Visual Weight Calculation
The feature uses a visual weight system to accurately measure display width:
- **Chinese characters** (CJK): 2 units each
- **English/Latin characters**: 1 unit each
- **Maximum per piece**: 12 units

This means each piece can contain:
- Up to 6 Chinese characters (6 × 2 = 12 units)
- Up to 12 English characters (12 × 1 = 12 units)
- Or any mix of Chinese and English not exceeding 12 units

## User Settings

### Location
Settings → General → Truncate Long Lyrics

### Default Value
Disabled (false)

### Storage
Stored in SharedPreferences with key: `truncate_lyrics_enabled`

## Implementation Details

### Key Constants
```kotlin
private val MAX_PIECE_WEIGHT = 12  // Max weight per piece
private val MIN_PIECE_DURATION_MS = 1000L  // Min display time per piece
private val DEFAULT_LYRIC_DURATION_MS = 3000L  // Fallback duration
private val PREFS_NAME = "IslandLyricsPrefs"  // SharedPreferences file
```

### Main Functions

#### 1. `splitLyricIntoPieces(lyric: String): List<String>`
Splits a lyric string into pieces based on visual weight.
- Handles edge cases like oversized single characters (e.g., special emojis)
- Returns list of lyric pieces

#### 2. `calculatePieceDuration(pieceWeight: Int, totalWeight: Int, totalDuration: Long): Long`
Calculates how long each piece should be displayed.
- Uses proportional distribution based on weight
- Ensures minimum display time of 1 second per piece
- Formula: `duration = (totalDuration × pieceWeight / totalWeight)`

#### 3. `getTotalLyricDuration(): Long`
Retrieves the total lyric duration from playback progress info.
- Falls back to 3000ms if progress info is unavailable

#### 4. `isTruncationEnabled(): Boolean`
Checks if the truncation feature is enabled in settings.

### Display Logic
When truncation is enabled:
1. **Lyric Change Detection**: When a new lyric arrives, check if its total weight exceeds `MAX_PIECE_WEIGHT`
2. **Splitting**: If too long, split into pieces using `splitLyricIntoPieces()`
3. **Duration Calculation**: Calculate display duration for the first piece
4. **Piece Rotation**: 
   - Track elapsed time since piece started displaying
   - When duration expires, switch to next piece
   - Last piece displays until lyric changes (no time limit)
5. **Display**: Show current piece in notification

## Examples

### Example 1: Pure Chinese Lyrics
**Input**: `我爱你中国我爱你` (14 characters)
- Total weight: 14 × 2 = 28 units
- Piece 1: `我爱你中国我` (6 chars, 12 units)
- Piece 2: `爱你` (2 chars, 4 units)
- Duration split: ~75% for piece 1, ~25% for piece 2

### Example 2: Pure English Lyrics
**Input**: `Hello world this is a test` (26 characters)
- Total weight: 26 units
- Piece 1: `Hello world ` (12 chars, 12 units)
- Piece 2: `this is a te` (12 chars, 12 units)
- Piece 3: `st` (2 chars, 2 units)
- Duration split: ~46% / ~46% / ~8%

### Example 3: Mixed Chinese/English
**Input**: `我love你China` (11 characters)
- Total weight: (2×2) + 4 + (2×1) + 5 = 15 units
- Piece 1: `我love你` (7 chars, 11 units)
- Piece 2: `China` (5 chars, 5 units)
- Duration split: ~69% / ~31%

## Testing Instructions

### How to Test
1. **Enable the Feature**
   - Go to Settings → General
   - Toggle on "Truncate Long Lyrics"

2. **Test with Long Lyrics**
   - Play a song with very long lyrics (e.g., rap songs)
   - Observe the Dynamic Island notification
   - Verify that lyrics are split and rotate properly

3. **Test with Short Lyrics**
   - Play a song with short lyrics
   - Verify that lyrics are NOT split (displayed normally)

4. **Test Mixed Content**
   - Test with Chinese-only lyrics
   - Test with English-only lyrics
   - Test with mixed Chinese/English lyrics

5. **Disable the Feature**
   - Toggle off "Truncate Long Lyrics" in Settings
   - Verify that the original scrolling behavior returns

### Edge Cases to Test
1. **Empty Lyrics**: Should display empty string
2. **Single Character**: Should display that character
3. **Oversized Single Character**: Should display in a single piece (even if > 12 units)
4. **Rapid Lyric Changes**: Should reset pieces and start fresh
5. **No Progress Info**: Should use 3-second fallback duration

## Technical Notes

### Performance
- Minimal performance impact: calculations only occur when lyrics change
- Efficient string operations using StringBuilder
- No memory leaks: pieces list is cleared when lyrics change

### Compatibility
- Works with existing scrolling feature (user can choose)
- Compatible with all music player apps
- No changes to external APIs or notification structure

### Future Enhancements (Optional)
1. Make `MAX_PIECE_WEIGHT` user-configurable in Settings
2. Add animation/transition between pieces
3. Show piece indicator (e.g., "1/3", "2/3")
4. Add preview in Settings to test before enabling
5. Support different truncation strategies (word boundaries, punctuation)

## Files Modified

### 1. `strings.xml`
- Added: `settings_truncate_lyrics` (title)
- Added: `settings_truncate_lyrics_desc` (description)

### 2. `activity_settings.xml`
- Added: Truncate Lyrics setting item with MaterialSwitch
- Located in General section, after Auto-Update

### 3. `SettingsActivity.kt`
- Added: UI references for truncate lyrics setting
- Added: Click listener to save setting to SharedPreferences
- Added: Initialization to load setting on start

### 4. `LyricCapsuleHandler.kt`
- Added: Truncation state variables (pieces, index, timing)
- Added: Constants for truncation logic
- Added: `splitLyricIntoPieces()` function
- Added: `calculatePieceDuration()` function
- Added: `getTotalLyricDuration()` helper
- Added: `isTruncationEnabled()` check
- Modified: `updateNotification()` to support truncation mode

## Conclusion
The lyric truncation feature provides a clean solution to the long lyrics display problem, with minimal code changes and full backward compatibility. Users can choose between the new truncation mode or the original scrolling mode based on their preference.
