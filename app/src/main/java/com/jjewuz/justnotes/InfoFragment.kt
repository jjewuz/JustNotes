package com.jjewuz.justnotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.play.core.review.ReviewManagerFactory
import com.jjewuz.justnotes.ui.theme.AppTheme


class InfoFragment : Fragment() {

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
                    UI()
                }
            }
        }
    }
    @Preview
    @Composable
    fun UI(
        aboutText: String = stringResource(id = R.string.about2),
        ghText: String = stringResource(id = R.string.gh),
        siteText: String = stringResource(id = R.string.site),
        licenseText: String = stringResource(id = R.string.licenses),
        about2Text: String = stringResource(id = R.string.socialLinksText),
    ){
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)){
                Column(modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = aboutText, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Button(onClick = {gh_a()}, modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp)){
                        Text(text = ghText)
                    }
                    Button(onClick = {site_a()}, modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp)){
                        Text(text = siteText)
                    }
                    Button(onClick = {replaceFragment(LicensesFragment())}, modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp)){
                        Text(text = licenseText)
                    }
                }
            }

            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)){
                Column(modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = about2Text, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row (modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,) {
                        Button(R.drawable.vk)
                        Button(R.drawable.tg)
                    }
                }
            }

            Button({ replaceFragment(OtherFragment()) },
                modifier = Modifier.padding(0.dp, 15.dp)){
                Text(text = stringResource(id = R.string.back))
            }
        }
    }

    @Composable
    fun Button(
        image: Int,
    ) {
        if (image == R.drawable.vk){
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).padding((5.dp))) {
            Image(painter = painterResource(id = image), contentDescription = null,
                modifier = Modifier
                    .clickable{vk_a()}
                    .size(60.dp))
        }}
        else {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).padding((5.dp))) {
                Image(painter = painterResource(id = image), contentDescription = null,
                    modifier = Modifier
                        .clickable{tg_a()}
                        .size(60.dp))
            }}
    }

    fun vk_a(){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/jjewuzhub"))
        startActivity(i)
    }

    fun tg_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jjewuzhub"))
        startActivity(i)
    }

    fun gh_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes"))
        startActivity(i)
    }

    fun site_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://jjewuz.github.io/"))
        startActivity(i)
    }

    fun ratestore() {
        requestReviewFlow(requireActivity())
        /*val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jjewuz.justnotes"))
        startActivity(i)*/
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.addToBackStack( "tag" )
        fragmentTransaction.commit ()
    }

    private fun requestReviewFlow(activity: Activity) {

        val reviewManager = ReviewManagerFactory.create(activity)

        val requestReviewFlow = reviewManager.requestReviewFlow()

        requestReviewFlow.addOnCompleteListener { request ->

            if (request.isSuccessful) {

                val reviewInfo = request.result

                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)

                flow.addOnCompleteListener {

                }

            }
        }
    }

}