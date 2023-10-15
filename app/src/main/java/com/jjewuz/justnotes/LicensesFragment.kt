package com.jjewuz.justnotes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jjewuz.justnotes.ui.theme.AppTheme

class LicensesFragment : Fragment() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val isDynamicColor = sharedPref.getBoolean("enabledMonet", true)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme(isDynamicColor, enabledFont) {
                    Column ( horizontalAlignment = Alignment.CenterHorizontally) {
                        UI()
                    }
                }
            }
        }
    }
    @Preview
    @Composable
    fun UI(
        licenceText: String = stringResource(id = R.string.my_license),
        thxText: String = stringResource(id = R.string.thx_license),
        dbText: String = stringResource(id = R.string.android_room_database_backup),
        dbLicText: String = stringResource(id = R.string.dbbackup_license),
        dbGitText: String = stringResource(id = R.string.github)
    ){
        Column( modifier = Modifier
            .verticalScroll(rememberScrollState()))
        {
            Text(text = licenceText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = thxText, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Card(modifier = Modifier.fillMaxWidth().padding(10.dp)){
                Column(modifier = Modifier.padding(5.dp)) {
                    Text(text = dbText, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = dbLicText, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Button(onClick = {openDB()}){
                        Text(text = dbGitText)
                    }
                }
            }

        }
    }

    private fun openDB(){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rafi0101/Android-Room-Database-Backup/tree/master"))
        startActivity(i)
    }
}