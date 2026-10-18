# Changelog

All notable changes to A-Music (fork of APlayer) are documented in this file.

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

[2.1.9.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.9.0
[2.1.8.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.8.0
[2.1.7.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.7.0
[2.1.6.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.6.0
[2.1.5.1]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.5.1
[2.1.5.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.5.0
[2.1.4.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.4.0
[2.1.3.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.3.0
[2.1.2.1]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.2.1
