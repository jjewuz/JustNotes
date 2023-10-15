package com.jjewuz.justnotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.jjewuz.justnotes.ui.theme.AppTheme


class SettingsFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences

    private var customFont: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val isDynamicColor = sharedPref.getBoolean("enabledMonet", true)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)

        var securitySupport = true
        var monetSupport = true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            securitySupport = false
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            monetSupport = false
        }

        val enabledpass = sharedPref.getBoolean("enabledPassword", false)
        customFont = sharedPref.getBoolean("enabledFont", false)
        val enabledMonet = sharedPref.getBoolean("enabledMonet", true)
        val preview = sharedPref.getBoolean("enabledPreview", false)
        val reversed = sharedPref.getBoolean("reversed", false)
        val tasksOpen = sharedPref.getBoolean("isTask", false)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme(isDynamicColor, enabledFont) {
                    Column ( modifier = Modifier
                        .padding(10.dp, 0.dp)
                        .verticalScroll(
                        rememberScrollState()
                    ), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = resources.getString(R.string.appearance), color = MaterialTheme.colorScheme.primary)
                        CardSetting(R.drawable.font, R.string.fontToggle, enabledFont, "enabledFont")
                        if(monetSupport) CardSetting(R.drawable.palette, R.string.monet, enabledMonet, "enabledMonet")
                        CardSetting(R.drawable.sort, R.string.reverseSetting, reversed, "reversed")
                        CardSetting(R.drawable.preview, R.string.preview, preview, "enabledPreview")
                        CardSetting(R.drawable.opening, R.string.openTask, tasksOpen, "isTask")
                        if(securitySupport) {
                            Divider()
                            Text(
                                text = resources.getString(R.string.security),
                                color = MaterialTheme.colorScheme.primary
                            )
                            CardSetting(
                                R.drawable.security,
                                R.string.passwordSettings,
                                enabledpass,
                                "enabledPassword"
                            )
                        }

                        Button({ replaceFragment(OtherFragment()) },
                            modifier = Modifier.padding(0.dp, 15.dp)){
                            Text(text = stringResource(id = R.string.back))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CardSetting(image: Int, text: Int, checked: Boolean, param: String){
        val checkedState = remember { mutableStateOf(checked)}
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Image(painter = painterResource(id = image), contentDescription = null,)
                Text(text = resources.getString(text))
                Switch(checked = checkedState.value, onCheckedChange =
                { checkedState.value = it
                    if (checkedState.value) {
                        with (sharedPref.edit()) {
                            putBoolean(param, true)
                            apply()
                        }
                    } else {
                        with (sharedPref.edit()) {
                            putBoolean(param, false)
                            apply()
                        }
                    }
                    if (param == "enabledFont" || param == "enabledMonet"){
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(resources.getString(R.string.needRestart))
                            .setPositiveButton("OK") { dialog, which -> killAndRestartApp(requireActivity())
                            }
                            .show()
                    }
                })
            }
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.commit ()
    }

    private fun killAndRestartApp(activity: Activity) {

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }
}