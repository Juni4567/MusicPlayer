# Changelog

All notable changes to A-Music (fork of APlayer) are documented in this file.

## [2.9.7] — 2026-11-16

### Highlights
- **Direct Library Tabs & Clean Defaults**: Moved Favorites, Playlists, and Recent into the main top category tabs. By default, the app displays **Songs, Favorites, Playlists, and Folders**. Added customizable settings in Library categories to enable or disable any tab (Albums, Artists, Genres, Recent, Remote).
- **Zero-Friction In-Place Live Search**: Search is now located on the same line as "Shuffle" in the Songs tab. Typing instantly filters songs in real-time in the song list with no page redirection, no empty screen loading, no UI shifting, and no keyboard re-opening friction.
- **In-Place Favorites & Recents**: Tapping the Favorites or Recent tab renders their song collections directly in-place without opening separate screens.
- **Single-Line Header**: Combined the live search field and "Shuffle" button side-by-side into a single 40dp compact bar.
- **Solid Now Playing Surface**: Completely removed transparency from the main player background to guarantee crisp, clean visuals without underlying UI bleed-through.
- **Dedicated Lyrics Button**: Replaced the bullet navigation dots with a dedicated, nicely placed lyrics button on the secondary actions row.
- **Expanded Mood Presets & Pinned Normal Preset**: Enhanced the main player mood selector with a permanently pinned "Normal" (1.0x) reset button on the left and two neat rows of expanded presets (Slowed & Reverb, Nightcore, Vaporwave, Cardio, Lo-Fi, Warm, Dreamy, Deep, Turbo). Cover artwork size has been comfortably reduced to fit all controls cleanly.
- **Themed Player Controls & Seekbar**: Main player seekbar progress line, thumb, and play/pause icon button now dynamically reflect the app's accent theme color.
- **Clean Main Player Top Bar**: Removed the downward caret icon and sanitized song metadata to remove `<unknown>-Download` artifacts, displaying clean artist and album information.
- **Streamlined Song 3-Dots Menu**: Removed redundant "Add to playing queue" and "Ringtone" options from the song popup menu. Also removed "Play speed" and "Equalizer" from the main player overflow menu.
- **Minimalist Floating Pill Notifications**: Replaced blocking full-width snackbars and outdated toasts with a modern, floating pill banner ("Playing next") that never obstructs controls.
- **Dual Release Builds**: Official release includes standard `A-Music-V2.9.7.apk` and pure `A-Music-V2.9.7-FOSS.apk` builds.

## [2.9.6] — 2026-11-16

### Highlights
- **Interactive Mini-Player Seekbar**: Restored fully interactive slider seeking on the bottom miniplayer with dedicated touch targeting and isolated touch areas, preventing accidental track expansion or song tapping
- **Mood Presets on Main Player**: Moved immersion mood presets (Slowed & Reverb, Nightcore, Vaporwave, Cardio, Lo-Fi, Reset) directly to the Now Playing player screen as a fast, accessible chip carousel
- **Cover Swipe-Down to Minimize**: Swiping down anywhere on the cover artwork or main player smoothly collapses the player down to the miniplayer
- **Eliminated Unwanted Lyric Searching**: Removed the accidental swipe-up gesture that was getting stuck on empty "Searching..." lyrics states
- **High-Contrast Song Title & Clean Background**: Lowered the song title with comfortable top breathing room and eliminated the dark gradient overlay behind the title bar for crystal-clear readability
- **Dual Release Builds**: Official release includes standard `A-Music-V2.9.6.apk` and pure `A-Music-V2.9.6-FOSS.apk` builds

## [2.1.9.5] — 2026-11-16

### Highlights
- **Modern UI & Design Refresh**: Overhauled typography, spacing, elevations, and card surfaces across all main screens for a clean, sleek, high-end appearance
- **Modern Floating Capsule Mini-Player**: Replaced the legacy edge-to-edge docked bar with a floating capsule player card featuring smooth 16dp rounded corners, subtle outline stroke, integrated 2.5dp hairline progress bar, and circular tonal play/pause control
- **Compact High-Density Lists**: Streamlined song list item heights to 56dp (down from 68dp/64dp), rounded album cover corners (8dp), and vibrant playing track highlight indicator
- **Sleek Grids & Albums**: Updated album/artist grid covers from sharp 2dp edges to modern 10dp rounded corners with compact 48dp text info sections
- **Reclaimed Screen Real Estate**: Compressed library chrome and search header to save ~50dp of vertical space, displaying 1–2 more songs above the fold on all device sizes
- **Harmonious Now Playing Layout**: Balanced vertical spacing in the portrait player to prevent bottom control collisions on gesture-nav and short-screen devices, with refined 6dp seekbar track and compact play queue dialog

## [2.1.9.4] — 2026-11-16

### Highlights
- Major codebase cleanup: stripped legacy telemetry, obsolete ROM floating hacks, unused libraries, and redundant background services
- Removed **Tencent Bugly SDK** crash reporting & tracking dependencies
- Removed **Google Play In-App Billing** library (`BillingClient`)
- Removed **Shake to Skip** sensor listener and wake-lock background logic
- Removed legacy **Custom Lock Screen Activity** in favor of native system MediaSession lock screen integration
- Removed **Desktop / Floating Window Lyrics** and eliminated the dangerous `SYSTEM_ALERT_WINDOW` permission
- Removed 500ms foreground polling loop from LyricManager, optimizing battery and idle CPU usage

### Permissions & Privacy
- Removed `android.permission.SYSTEM_ALERT_WINDOW`
- Removed accelerometer sensor hardware requirement
- Zero third-party telemetry, trackers, or commercial SDKs

### Notes
- Signed for sideload with this fork’s debug keystore

## [2.1.9.3] — 2026-11-16

### Highlights
- Added **Mood & Immersion Presets** (Slowed & Reverb, Nightcore, Vaporwave, Cardio, Lo-Fi, Reset) to the Playback settings tab
- Added **Acoustic & Audio Enhancement Profiles** (Clear Vocals, Sub-Bass Surge, 3D Spatial, Anti-Fatigue, Acoustic Hall) to the Equalizer tab
- Removed volume boost slider from the audio sheet; volume boost is now seamlessly controlled via Volume Up/Down buttons and icons
- Volume boost can only exceed 100% when the app is focused, with an animated in-app Volume Boost HUD bar

### Audio & Controls
- Quick mood presets instantly apply speed, pitch, and reverb combinations with live audio response
- Equalizer presets apply target frequency curves and automatically adjust Bass Boost and Virtualizer strengths
- Volume bar on the Now Playing screen supports the complete 0%–200% scale with distinct amber boost indicator and readout
- Tapping the Volume Up icon past 100% raises volume boost by +5% increments up to 200%; Volume Down decreases boost before lowering system volume
- Hardware volume keys increase boost above 100% only while the app is active in foreground
- In-app floating Volume Boost HUD banner displays real-time boost percentage and visual progress

### Notes
- Signed for sideload with this fork’s debug keystore

## [2.1.9.2] — 2026-11-16

### Highlights
- Unified **Playback & Equalizer** bottom sheet with compact segmented tabs
- Continuous sliders for **Playback Speed** (0.50x – 2.00x) and **Pitch** (0.50 – 1.50) with instant real-time audio update
- Streamlined Now Playing secondary action bar with optimized touch targets

### UI & UX
- Merged playback effects and equalizer into a single compact sheet (`PlaybackAndEqSheet`) with `[Playback]` | `[Equalizer]` tabs
- Speed slider with live readout, quick `1.0x` reset button, and quick-preset chips
- Pitch slider with live readout, quick `1.0` reset button, and preset chips
- Upgraded `LineSlider` with rounded capsule track ends and zero-latency drag interaction
- Marquee text scrolling for long song titles and artist details in Now Playing top bar
- Streamlined secondary actions bar: Favorite, Sleep Timer, Sound & EQ, Share

### Localization & Stability
- Added localized strings for merged audio controls in English, Simplified Chinese, Traditional Chinese, Japanese, and Polish
- Resolved missing default resource warning for Polish locale
- Configured unit test Android resources for AGP 9.0+ Compose preview support

### Notes
- Signed for sideload with this fork’s debug keystore

## [2.1.9.1] — 2026-10-19

### Highlights
- Launcher icon rebuilt from a clean **SVG** source (sharper note, proper adaptive layers)

### UI
- Vector source of truth under `app/src/main/art/launcher/`
- Adaptive icon uses separate gradient background + white-note foreground
- Regenerated legacy mipmaps (mdpi–xxxhdpi) from the SVG geometry

### Notes
- Signed for sideload with this fork’s debug keystore. Clear launcher cache if the old icon is still shown.

## [2.1.9.0] — 2026-10-19

### Highlights
- New **A-Music** launcher icon (pink–orange gradient with white note)

### UI
- Updated adaptive icon background and legacy mipmaps (mdpi–xxxhdpi)

### Notes
- Signed for sideload with this fork’s debug keystore

## [2.1.8.0] — 2026-10-19

### Highlights
- Removed **audio trim** (waveform editor / scissors) from the player and song menus

### Changes
- Scissors control removed from Now Playing secondary actions
- Trim entry removed from the song popup
- Dropped FFmpegKit audio trim dependency (smaller APK)
- `minSdk` restored to **21**

### Notes
- Tag editing remains available from the overflow menu / song popup
- Signed for sideload with this fork’s debug keystore

## [2.1.7.0] — 2026-10-19

### Highlights
- Cleaner, more compact **Playback**, **Equalizer**, and **Sleep timer** bottom sheets

### UI
- Shared sheet chrome: tighter chips, denser sliders, consistent section labels
- Playback effects: horizontal chip rows for speed / pitch / echo; slimmer volume boost
- Equalizer: compact presets, labeled bass/virtualizer %, denser band rows with live dB
- Sleep timer: chip grid instead of tall list rows; active timer shown as a compact status bar
- Bottom sheets use a subtle drag handle and reduced padding

### Notes
- Signed for sideload with this fork’s debug keystore

## [2.1.6.0] — 2026-10-19

### Highlights
- **Audio trim** for local tracks: waveform editor with start/end handles, selection preview, and save

### Audio editing
- Stream-copy trim via FFmpegKit when possible (no quality loss); light re-encode fallback for unsupported copy cases
- **Save as new** (default): writes `{name}_trim.{ext}` beside the original and refreshes the library
- **Replace original** (opt-in): requests MediaStore write access, then overwrites the file safely
- Scissors on Now Playing open trim; tag edit stays in the overflow menu
- Song popup includes **Trim** for local songs

### Notes
- APK size increases due to the FFmpegKit audio package
- `minSdk` raised to **24** (Android 7.0) to match FFmpegKit
- Remote / cloud songs are not supported
- Signed for sideload with this fork’s debug keystore

## [2.1.5.1] — 2026-10-18

### Fixes
- Fix **Unable to play: ERROR_CODE_FAILED_RUNTIME_CHECK**
  - Echo DSP processor now fully consumes audio buffers and is thread-safe
  - Volume boost no longer drives `Player.setVolume` above 1.0 (Media3 clamps it); boost is applied in the ReplayGain audio processor instead

### Notes
- Signed for sideload with this fork’s debug keystore

## [2.1.5.0] — 2026-10-18

### Highlights
- **Working DSP echo** in the ExoPlayer audio pipeline (feedback delay, not system `PresetReverb`)
- Now Playing seekbar is **full width** with a single `elapsed / duration` time label

### Playback & audio
- Echo uses an in-player `EchoDelayAudioProcessor` with Small / Medium / Large / Hall / Large hall / Plate delay presets
- Removed the non-working session-based `PresetReverb` echo path
- Echo still toggles from the Playback sheet on the main player

### UI
- Seekbar no longer flanks times on both ends; one centered time under a full-width scrubber

### Notes
- Signed for sideload with this fork’s debug keystore. Uninstall an upstream-signed build first if signatures conflict

## [2.1.4.0] — 2026-10-18

### Highlights
- Playback sheet on the Now Playing screen: **speed**, **pitch**, **echo**, and **volume boost up to 200%**
- Restored original upstream heart, shuffle, and repeat icons (and matching control icons)

### Playback & audio
- Volume boost slider (100%–200%) with live preview; persisted across sessions
- Pitch control (0.75–1.5) via ExoPlayer `PlaybackParameters`
- Echo on/off with Small / Medium / Large / Hall / Large hall / Plate presets (session reverb; replaced by DSP in 2.1.5.0)
- Speed control remains on the main player secondary action row

### UI
- Favorite uses upstream like icons (`widget_btn_like_*`)
- Play mode / prev / next / queue use upstream `play_btn_*` assets again
- Mini-player next button uses the same upstream next icon

### Notes
- Volume boost amplifies above system volume and may distort very loud tracks
- Signed for sideload with this fork’s debug keystore. Uninstall an upstream-signed build first if signatures conflict

## [2.1.3.0] — 2026-10-18

### Highlights
- Rebranded to **A-Music** with a new adaptive launcher icon
- Full Mi Music–inspired dark UI redesign for library and Now Playing
- Equalizer, playback speed, and sleep timer available as bottom sheets from the player

### UI / UX
- Near-black library chrome with search, shortcuts, and shuffle
- Redesigned Now Playing: cover, controls, secondary actions, and Up Next
- Favorite heart shows an active filled state when the track is loved
- Equalizer icon restored on the main player; presets available again
- Theme tokens aligned for light and dark (surfaces, chips, contrast)
- Removed redundant toasts where icon/state feedback is enough
- Sleep timer entry from the home app bar; EQ/timer/share/trim from player actions

### Playback & audio
- Equalizer sheet with presets and virtualizer support
- Playback speed dialog
- Sleep timer sheet
- APE format support (upstream)

### Platform & build
- 16 KB page-size ELF alignment for Android 15+ / Play compatibility (NDK r27)
- TagLib native module pinned to CMake 3.22.1 and NDK 27.1 for local SDK builds
- Polish language pack
- Search IME layout fix; hide tab bar when only one tab is shown
- Replace `printStackTrace` with proper logging

### Notes
- Signed for sideload with this fork’s debug keystore (not the upstream Play signing key). Uninstall an upstream-signed build before installing if signatures conflict.

## [2.1.2.1] — 2026-09-14

- `targetSdk` upgraded to Android 16

## [2.1.2.0] — 2026-09-13

- See upstream APlayer release notes

[2.1.9.3]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.9.3
[2.1.9.2]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.9.2
[2.1.9.1]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.9.1
[2.1.9.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.9.0
[2.1.8.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.8.0
[2.1.7.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.7.0
[2.1.6.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.6.0
[2.1.5.1]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.5.1
[2.1.5.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.5.0
[2.1.4.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.4.0
[2.1.3.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.3.0
[2.1.2.1]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.2.1
