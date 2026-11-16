package remix.myplayer.ui.activity

import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import remix.myplayer.BuildConfig
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.helper.VolumeBoostHudController
import remix.myplayer.service.MusicServiceRemote
import remix.myplayer.ui.activity.base.BaseMusicActivity
import remix.myplayer.ui.nav.AppNav
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.playingScreenDeepLink
import remix.myplayer.ui.theme.APlayerTheme
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.LocalThemeController
import remix.myplayer.ui.theme.ProvideAppFontScale
import remix.myplayer.ui.theme.ThemeController
import remix.myplayer.ui.widget.app.VolumeBoostHudOverlay
import remix.myplayer.util.ThemeUtil
import remix.myplayer.viewmodel.LibraryViewModel
import remix.myplayer.viewmodel.MainViewModel
import remix.myplayer.viewmodel.PlaybackViewModel
import remix.myplayer.viewmodel.PlayingScreenValue
import remix.myplayer.viewmodel.ProvideViewModels
import remix.myplayer.viewmodel.settings.SettingViewModel
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ComposeActivity : BaseMusicActivity() {

  private val libraryViewModel: LibraryViewModel by viewModels()
  private val playbackViewModel: PlaybackViewModel by viewModels()
  private val mainViewModel: MainViewModel by viewModels()
  private val settingViewModel: SettingViewModel by viewModels()

  @Inject
  lateinit var themeController: ThemeController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addMusicServiceEventListener(libraryViewModel)
    addMusicServiceEventListener(playbackViewModel)

    enableEdgeToEdge(
      navigationBarStyle = SystemBarStyle.auto(
        android.graphics.Color.WHITE,
        android.graphics.Color.WHITE
      )
    )
    setContent {
      AppCompositionLocalProvider(themeController) {
        val theme = LocalTheme.current
        val color = if (theme.coloredNaviBar) {
          theme.primary
        } else {
          Color.White
        }
        SideEffect {
          window.navigationBarColor = color.toArgb()
          ThemeUtil.setLightNavigationBarAuto(this, theme.isPrimaryLight)
        }

        APlayerTheme {
          Box(modifier = Modifier.fillMaxSize()) {
            AppNav()
            VolumeBoostHudOverlay()
          }
        }
      }
    }

    if (BuildConfig.FLAVOR == "normal") {
      lifecycleScope.launch {
        mainViewModel.checkInAppUpdate()
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    val audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
      ?: return super.onKeyDown(keyCode, event)
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val currentBoost = settingViewModel.settingsState.value.play.volumeBoost

    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      if (current >= max) {
        val newBoost = (currentBoost + 0.05f).coerceAtMost(SettingPrefs.VOLUME_BOOST_MAX)
        settingViewModel.setVolumeBoost(newBoost)
        MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(newBoost)
        VolumeBoostHudController.show(newBoost)
        return true
      }
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      if (currentBoost > 1.0f) {
        val newBoost = (currentBoost - 0.05f).coerceAtLeast(1.0f)
        settingViewModel.setVolumeBoost(newBoost)
        MusicServiceRemote.service?.replayGainController?.processor?.setVolumeBoost(newBoost)
        VolumeBoostHudController.show(newBoost)
        return true
      }
    }
    return super.onKeyDown(keyCode, event)
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    handleIntent()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    themeController.onSystemThemeChanged(newConfig.uiMode)
  }

  private fun handleIntent() {
    intent?.data?.let {
      when (it.scheme) {
        playingScreenDeepLink.scheme -> {
          Timber.v("deepLink")
          lifecycleScope.launch {
            val state = mainViewModel.playingScreenState
            snapshotFlow {
              state.anchors.hasPositionFor(PlayingScreenValue.Expanded)
            }.filter { it }.first()
            state.snapTo(PlayingScreenValue.Expanded)
          }
        }

        else -> {
          lifecycleScope.launch {
            delay(500)
            libraryViewModel.playFromUri(it)
          }
          intent = Intent()
        }
      }
    }
  }
}

@Composable
fun AppCompositionLocalProvider(
  themeController: ThemeController,
  content: @Composable (() -> Unit)
) {
  CompositionLocalProvider(
    LocalThemeController provides themeController,
    LocalTheme provides themeController.appTheme,
    LocalNavController provides rememberNavController()
  ) {
    ProvideViewModels {
      val settingState by remix.myplayer.viewmodel.settingViewModel.settingsState.collectAsStateWithLifecycle()
      ProvideAppFontScale(settingState.common.uiFontScale) {
        content()
      }
    }
  }
}
