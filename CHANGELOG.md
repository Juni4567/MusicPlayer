# Changelog

All notable changes to A-Music (fork of APlayer) are documented in this file.

## [2.9.11] — 2026-11-18

### Highlights
- **Audio-Reactive Dancing Cover Animation**: Cover art now dynamically grooves and dances with the rhythm, beats, and waves of the music. Features punchy kick-drum pulse animations with syncopated sub-bounces, fluid rhythmic rotational sway (±1.6°), and vertical floating wave bobbing, dynamically scaled and synced with playback speed.
- **Enlarged, Prominent Moods UI & Compacted Next Song View**:
  - Expanded mood preset chips to 32dp height, larger 12.5sp typography, generous horizontal padding, and a prominent 72dp pinned Normal reset tile.
  - Streamlined the "Up Next" bar to a compact 36dp pill to maximize vertical breathing space for the player and mood presets.
- **Dedicated "Reset Mood" Button in Secondary Actions**:
  - Moved song file Sharing into the main player 3-dots popup menu.
  - Added an instant "Reset Mood" action button (`ic_refresh_24dp`) right beside the Equalizer and Sleep Timer for effortless one-tap restoration of 1.0x speed, 1.0x pitch, and flat echo.
- **Fluid Cover Drag-Down Dismissal**: Fixed gesture contention on the cover art where vertical drag events were intercepted. Downward dragging anywhere on the cover image now translates seamlessly into real-time interactive player sheet dismissal.
- **Zero-Allocation Smooth List Scrolling**: Eliminated list scrolling jank by pre-computing and caching all Canvas paths, radial brushes, and gradients in `PatternCover.kt`. Zero heap allocations during scroll frames deliver locked 60/120fps performance.
- **Custom Cover Upload Fix**: Re-enabled custom thumbnail resolution across songs, albums, and playlists in `UriFetcher.kt`, coupled with automatic Glide memory cache eviction upon cropping new album art.
- **12 Curated Palettes & 8 Procedural Art Styles**: Expanded procedural generative covers to 12 curated aesthetic palettes with a 64-bit mixer for maximum entropy and 8 distinct generative styles (Constellations, Liquid Blobs, Low-Poly Crystal, Synthwave Sunset, Isometric 3D Cube, Geometric Mandala, Aurora Waves, Orbital Planetary).
- **Overhauled Notification Miniplayer & MediaSession**:
  - Real Song Title & Artist Display: Replaced generic `"A-Music is running"` text with accurate song titles and artist/album metadata.
  - Persistent Artwork: Retains and renders cover art reliably across notifications and lockscreen media controls (`METADATA_KEY_ALBUM_ART`, `METADATA_KEY_ART`, `METADATA_KEY_DISPLAY_ICON`).
  - One-Tap Close Action: Added a dedicated Close (`ic_close_white_24dp`) button to immediately exit playback and dismiss the notification from anywhere.
  - Dynamic App Color Theming: Tints the notification player background and media controls with the user's active app theme secondary accent color.
- **Dual Release Builds**: Official release includes standard `A-Music-V2.9.11.apk` and pure `A-Music-V2.9.11-FOSS.apk` builds.

## [2.9.10] — 2026-11-17

### Highlights
- **Flat Matte iOS-Style Vector App Icon**: Redesigned the adaptive app icon with a crisp, flat matte aesthetic. Features a glowing white beamed eighth note (♫) centered on a vibrant `#fb24e1` (hot fuchsia) to indigo/royal blue (`#6366F1` / `#2563EB`) gradient, accented with subtle concentric soundwave rings and delicate light sparkles. Strictly zero glossy overlay and zero reflections.
- **Redesigned Modern "Up Next" Pill in Player**: Transformed the old flat rectangular Next Song box in the main player utility bar into a sleek capsule pill card with next track thumbnail, `"UP NEXT"` accent badge, clean title + artist, and subtle navigation chevron.
- **5 New Randomized Minimalist Generative Cover Concepts**: Upgraded procedural covers with 5 brand-new geometric and abstract concepts across 6 curated palettes (Neon Purples, Deep Emerald Greens, Warm Sunset Oranges, Cosmic Blues, Liquid Metallic, Cyber Neon):
  - *Minimalist Constellations with Fluid Watercolor/Gradient Backdrops*
  - *Abstract Liquid Metallic Blobs with Chromatic Specular Highlights*
  - *Sharp Low-Poly Wireframe Crystals with Faceted Shading*
  - *Geometric Neon Wireframe Landscapes with Perspective Horizon Grid*
  - *Floating 3D Shapes with Ambient Drop Shadows and Wireframe Edges*
- **Exact Viewport-Centering Floating Button**: Enhanced the Song screen floating action button (`ic_my_location`) to compute exact viewport dimensions and item center offsets, scrolling the playing track smoothly to the dead center of the screen.
- **Dynamic Search Placeholder Track Count**: Refined the search placeholder to `"Search in %d Audio files"`, dynamically referencing the library's total track count.
- **Complete Elimination of `<unknown>` and Empty Fallbacks**: Thoroughly sanitized metadata to strip `<unknown>`, `unknown`, `[unknown]`, `(unknown)`, and bracketed tags. If artist or album information is absent, the UI leaves it cleanly empty without displaying `" — "` or `"Unknown artist"` or fallback placeholders.
- **Modern & Accessible Settings UI**: Re-categorized settings into clean, rounded card containers with accessible typography, minimum 52dp touch targets, transparent row backgrounds, and eliminated duplicate Equalizer entries.
- **Dual Release Builds**: Official release includes standard `A-Music-V2.9.10.apk` and pure `A-Music-V2.9.10-FOSS.apk` builds.

## [2.9.9] — 2026-11-17

### Highlights
- **Hybrid Mood Presets with Playback & Equalizer Fusion**: Mood presets now simultaneously orchestrate playback dynamics (speed, pitch, spatial echo) and fine-tuned 5-band hardware Equalizer profiles (custom dB gains, Bass Boost +800, 3D Virtualizer +850). Added audiophile acoustic configurations including *Bass Slam*, *Spatial 3D*, *Vocal Pure*, *Slowed & Reverb*, *Nightcore*, *Lo-Fi*, *Vaporwave*, *Turbo Club*, *Warm Acoustic*, *Dreamy*, and *Cardio*. Selecting *Normal* instantly resets both playback and audio effects to pure flat default.
- **Beat-Reactive Cover Animation**: Animate cover artwork rhythmically with music playback via a subtle, dynamic pulsating scale effect. Fully configurable in Settings with a dedicated toggle (default: enabled).
- **Redesigned Modern "Up Next" / Play Queue**: Completely revamped the Up Next queue sheet into an elegant, modern card-based interface featuring a top drag pill, accent-bordered "NOW PLAYING" hero card, track count badge, sleek rounded queue cards with active playing indicators, seamless reordering handles, and circular delete buttons.
- **Glossy iOS-Style Vector App Icon**: Designed a brand-new adaptive app icon in the app's signature color palette, featuring a glowing white eighth musical note centered on a vibrant blue-to-pink gradient background with subtle concentric soundwave rings, specular glass highlight curvature, and delicate light sparkles.
- **Center-on-Playing Floating Button**: Added a modern floating action button (`ic_my_location`) in the Songs screen that smoothly scrolls and centers the list on the currently playing track with one tap.
- **Dynamic Search Placeholder Track Count**: Updated the library search placeholder to dynamically reflect the total number of audio files (`"Search in %d audio files"`).
- **Comprehensive Metadata Sanitization**: Cleaned all `<unknown>` artifacts, bracketed tags, and file download suffixes across song titles, artists, and albums in song lists, grid items, detail views, miniplayer, queue, and player top bar.
- **8 Rich Procedural Generative Fallback Covers**: Upgraded deterministic procedural artwork with 8 stunning vector generative styles for songs without embedded album art, including *Celestial Constellations & Twinkling Stars*, *Concentric Soundwave Rings*, *Isometric Vector Grid*, *Cosmic Glowing Orbs*, *Crystal Shards*, *Harmonic Sine Waves*, and *Radial Starlight Rays*.
- **Modernized & Categorized Settings**: Streamlined settings categories with modern Material 3 rounded cards, circular tinted icon containers, descriptive subtitles, and a direct Equalizer shortcut in the Playback section.
- **Dual Release Builds**: Official release includes standard `A-Music-V2.9.9.apk` and pure `A-Music-V2.9.9-FOSS.apk` builds.

## [2.9.8] — 2026-11-16

### Highlights
- **Search Keyboard & Focus Management**: Fixed an issue where the search field persistently retained focus, causing the software keyboard to randomly re-open after searching or starting playback. Focus and the keyboard are now proactively cleared upon playing a track, scrolling through the songs list, pressing the keyboard's Search action, tapping Clear (✕), tapping Shuffle, or swiping library tabs.
- **Seamless Live Search Dismissal**: Integrated `ImeAction.Search` with keyboard action callbacks into the inline search field for a fluid, natural typing and search experience.
- **Dual Release Builds**: Official release includes standard `A-Music-V2.9.8.apk` and pure `A-Music-V2.9.8-FOSS.apk` builds.

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
