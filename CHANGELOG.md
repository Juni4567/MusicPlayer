# Changelog

All notable changes to A-Music (fork of APlayer) are documented in this file.

## [2.1.4.0] — 2026-10-18

### Highlights
- Playback sheet on the Now Playing screen: **speed**, **pitch**, **echo**, and **volume boost up to 200%**
- Restored original upstream heart, shuffle, and repeat icons (and matching control icons)

### Playback & audio
- Volume boost slider (100%–200%) with live preview; persisted across sessions
- Pitch control (0.75–1.5) via ExoPlayer `PlaybackParameters`
- Echo on/off with Small / Medium / Large / Hall / Large hall / Plate presets (`PresetReverb`)
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

[2.1.4.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.4.0
[2.1.3.0]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.3.0
[2.1.2.1]: https://github.com/Juni4567/MusicPlayer/releases/tag/v2.1.2.1
