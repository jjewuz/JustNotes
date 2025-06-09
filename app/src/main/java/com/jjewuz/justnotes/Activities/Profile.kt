package com.jjewuz.justnotes.Activities

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import com.jjewuz.justnotes.BuildConfig
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.Utils.GridAdapter
import com.jjewuz.justnotes.Utils.Option
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class Profile : AppCompatActivity() {
    private lateinit var appLayout: View
    private lateinit var userEmailText: TextView
    private lateinit var loginBtn: Button
    private lateinit var lastBackupText: TextView
    private lateinit var accountOptions: LinearLayout

    private lateinit var sharedPref: SharedPreferences

    private lateinit var auth: FirebaseAuth

    lateinit var backup: RoomBackup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val theme = sharedPref.getString("theme", "standart")
        var reverse = sharedPref.getBoolean("reversed", false)
        var isGrid = sharedPref.getBoolean("grid", false)
        if (enabledFont and (theme=="monet")) {
            setTheme(R.style.AppTheme)
        } else if (!enabledFont and (theme=="monet")) {
            setTheme(R.style.FontMonet)
        } else if (!enabledFont and (theme=="standart")) {
            setTheme(R.style.Font)
        } else if (enabledFont and (theme=="standart")) {
            setTheme(R.style.Nothing)
        } else if (!enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIceFont)
        } else if (enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIce)
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(findViewById(R.id.topAppBar))
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        backup = RoomBackup(this)

        appLayout = findViewById(R.id.app_layout)
        loginBtn = appLayout.findViewById(R.id.enter_account)
        userEmailText = appLayout.findViewById(R.id.userEmailTxt)
        accountOptions = appLayout.findViewById(R.id.account_option)
        lastBackupText = appLayout.findViewById(R.id.last_back)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        } else {
            accountOptions.visibility = View.GONE
        }

        loginBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            val inf = this.layoutInflater.inflate(R.layout.login_register, null)
            val email = inf.findViewById<EditText>(R.id.emailEditText)
            val passwd = inf.findViewById<EditText>(R.id.passwordEditText)

            builder.setIcon(R.drawable.account)
            builder.setTitle(R.string.inf)
                builder.setView(inf)
                .setNeutralButton(resources.getString(R.string.forgot_pass)) { dialog, which ->
                    resetPassword(email.text.toString())
                }
                .setNegativeButton(resources.getString(R.string.register)) { dialog, which ->
                    register(email.text.toString(), passwd.text.toString())

                }
                .setPositiveButton(resources.getString(R.string.login)) { dialog, which ->
                    login(email.text.toString(), passwd.text.toString())
                }
            builder.create().show()
        }

       appLayout.findViewById<Button>(R.id.delete_btn).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.account_delete_info)
                .setPositiveButton(R.string.delete_account) { _, _ ->
                    val userId = Firebase.auth.currentUser?.uid
                    val userEmail = Firebase.auth.currentUser?.email
                    val storageRef = Firebase.storage.reference
                    storageRef.child("user/$userId/database.aes").delete()
                    auth.currentUser?.delete()?.addOnSuccessListener {
                        userEmailText.text = resources.getString(R.string.no_account)
                        loginBtn.visibility = View.VISIBLE
                        accountOptions.visibility = View.GONE
                        Toast.makeText(this, R.string.deletion_succes, Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener {
                        Toast.makeText(this, R.string.auth_need, Toast.LENGTH_SHORT).show()
                        val builder = MaterialAlertDialogBuilder(this)
                        val inflater = this.layoutInflater.inflate(R.layout.auth_credential, null)
                        val pass = inflater.findViewById<TextInputEditText>(R.id.input)
                        builder.setView(inflater)
                            .setPositiveButton(R.string.delete_account) { _, _ ->
                                val credential = userEmail?.let { it1 ->
                                    EmailAuthProvider.getCredential(
                                        it1, pass.text.toString())
                                }
                                if (credential != null) {
                                    auth.currentUser?.reauthenticate(credential)!!.addOnSuccessListener {
                                        storageRef.child("user/$userId/database.aes").delete()
                                        auth.currentUser?.delete()?.addOnSuccessListener {
                                            userEmailText.text = resources.getString(R.string.no_account)
                                            loginBtn.visibility = View.VISIBLE
                                            accountOptions.visibility = View.GONE
                                            Toast.makeText(this, R.string.deletion_succes, Toast.LENGTH_SHORT).show()
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(this, R.string.deletion_error, Toast.LENGTH_SHORT).show()
                                    }

                                }
                            }
                            .setNegativeButton(R.string.back) { dialog, _ ->
                                dialog.cancel()
                            }
                        builder.create().show()

                    }

                }
                .setNegativeButton(R.string.back) { _, _ ->

                }
                .show()

           lastBackupText.text = sharedPref.getString("last_backup", "${resources.getString(R.string.last_backup)} - ")

        }

        appLayout.findViewById<Button>(R.id.logout_btn).setOnClickListener {
            auth.signOut()
            userEmailText.text = resources.getString(R.string.no_account)
            loginBtn.visibility = View.VISIBLE
            accountOptions.visibility = View.GONE
        }



        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val adapterCard = CardPagerAdapter(this)
        viewPager.adapter = adapterCard
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

        }.attach()

        viewPager.setPageTransformer { page, position ->
            val offset = 1 - abs(position)
            page.scaleX = 0.8f + offset * 0.2f
            page.scaleY = 0.8f + offset * 0.2f
            page.translationX = position * -32f
        }


        val gridView = findViewById<RecyclerView>(R.id.gridView)
        gridView.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return 1
                }
            }
        }

        val options = listOf(
            Option(R.drawable.swap, resources.getString(R.string.sort), resources.getString(R.string.change_sort)),
            Option(R.drawable.view, resources.getString(R.string.style), resources.getString(R.string.change_view)),
            Option(R.drawable.palette, resources.getString(R.string.app_theme), resources.getString(R.string.select_theme)),
            Option(R.drawable.label, resources.getString(R.string.manage_labels), resources.getString(R.string.open)),
            Option(R.drawable.info, resources.getString(R.string.appversion), "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"),
            Option(R.drawable.settings, resources.getString(R.string.settingsText), resources.getString(R.string.open)),
            Option(R.drawable.site, resources.getString(R.string.site), resources.getString(R.string.open)),
            Option(R.drawable.bug_report, resources.getString(R.string.reportBug), "GitHub"),

        )

        val adapter = GridAdapter(this, options) { option ->
            when (option.title) {
                resources.getString(R.string.sort) -> {sharedPref.edit() {
                    putBoolean(
                        "reversed",
                        !reverse
                    )
                }
                    reverse = !reverse
                    Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
                    restartApp()
                }
                resources.getString(R.string.style) -> {sharedPref.edit() {
                    putBoolean(
                        "grid",
                        !isGrid
                    )
                }
                    isGrid = !isGrid
                    Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
                    restartApp()
                }
                resources.getString(R.string.app_theme) -> changeTheme()
                resources.getString(R.string.manage_labels) -> openActivity(AddCategory::class.java)
                resources.getString(R.string.settingsText) -> openActivity(SettingsActivity::class.java)
                resources.getString(R.string.reportBug) ->  {val i = Intent(Intent.ACTION_VIEW,
                    "https://github.com/jjewuz/JustNotes/issues/new".toUri())
                    startActivity(i)}
                resources.getString(R.string.site) -> {val i = Intent(Intent.ACTION_VIEW,
                    "https://jjewuz.com/justnotes.html".toUri())
                    startActivity(i)}
            }
        }

        gridView.adapter = adapter
    }

    private fun openSomething() {
        Toast.makeText(this, "Открываем...", Toast.LENGTH_SHORT).show()
    }

    private fun openActivity(activity: Class<out AppCompatActivity>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    private fun changeTheme() {
        val builder = MaterialAlertDialogBuilder(this)
        val inf = this.layoutInflater.inflate(R.layout.theme_chooser, null)
        val standart = inf.findViewById<MaterialCardView>(R.id.standart)
        val monet = inf.findViewById<MaterialCardView>(R.id.monet)
        val ice = inf.findViewById<MaterialCardView>(R.id.ice)

        val colorMonet = inf.findViewById<Button>(R.id.monetcolor)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            monet.visibility = View.GONE
        } else {
            colorMonet.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this,
                    R.color.material_dynamic_primary50
                ))
        }
        builder.setIcon(R.drawable.palette)
        builder.setTitle(R.string.select_theme)
        builder.setView(inf)
            .setPositiveButton(R.string.close) { _, _ ->
            }
        builder.create().show()
        standart.setOnClickListener {
            with (sharedPref.edit()) {
                putString("theme", "standart")
                putBoolean("recreate", true)
                apply()
            }
            Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
            restartApp()
        }
        monet.setOnClickListener {
            with (sharedPref.edit()) {
                putString("theme", "monet")
                putBoolean("recreate", true)
                apply()
            }
            Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
            restartApp()
        }
        ice.setOnClickListener {
            with (sharedPref.edit()) {
                putString("theme", "ice")
                putBoolean("recreate", true)
                apply()
            }
            Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
            restartApp()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_extra, menu)

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.github -> {
                openLink("https://github.com/jjewuz/JustNotes")
                true
            }
            R.id.weblate -> {
                openLink("https://hosted.weblate.org/engage/justnotes/")
                true
            }
            R.id.legal -> {
                showDialogInfo(R.string.legal)
               true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDialogInfo(title: Int){
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(title))
            .setNeutralButton(resources.getString(R.string.terms_of_use)) { _, _ ->
                openLink("https://jjewuz.com/en/justnotes/termsofuse.html")
            }
            .setNegativeButton(resources.getString(R.string.privacy_policy)) { _, _ ->
                openLink("https://jjewuz.com/en/justnotes/privacypolicy.html")
            }
            .setPositiveButton(resources.getString(R.string.licenses)) { _, _ ->
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
            .show()
    }

    private fun openLink(url: String){
        val i = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(i)
    }

    private fun updateUI(user: FirebaseUser?){
        user?.let {
            val email = it.email

            userEmailText.text = email
            loginBtn.visibility = View.GONE
            accountOptions.visibility = View.VISIBLE
            lastBackupText.text = sharedPref.getString("last_backup", "${resources.getString(R.string.last_backup)} - ")
        }
    }

    private fun register(email: String, password: String){
        if (email != "" && password != "") {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
                        Toast.makeText(
                            this,
                            R.string.authSuc,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            R.string.authFail,
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null)
                    }
                }
        } else {
            Toast.makeText(
                this,
                R.string.no_info,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun login(email: String, password: String){
        if (email != "" && password != "") {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
                        Toast.makeText(
                            this,
                            R.string.authSuc,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            R.string.authFail,
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null)
                    }
                }
        } else {
            Toast.makeText(
                this,
                R.string.no_info,
                Toast.LENGTH_SHORT,
            ).show()
        }

    }

    private fun resetPassword(email: String){
        if (email != "") {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, R.string.reset_email_sent, Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(
                this,
                R.string.no_info,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun backup(local: Boolean){
        val context = (this)
        val backup = context.backup
        val storageRef = Firebase.storage.reference

        val sdf = SimpleDateFormat("dd.MM.yyyy_HH:mm", Locale.getDefault())
        val currentDateAndTime: String = sdf.format(Date().time)

        if (local) {
            backup
                .database(NoteDatabase.getDatabase(context))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .customBackupFileName("JustNotes_Backup_$currentDateAndTime.sqlite")
                .backupIsEncrypted(false)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                    }
                }
                .backup()
            restartApp()
        } else {
            val userId = Firebase.auth.currentUser?.uid
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sure)
                .setIcon(R.drawable.info)
                .setMessage(R.string.sure_backup)
                .setPositiveButton("OK") { _, _ ->
                    if (userId == null) {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.cloud_backup_account),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    backup
                        .database(NoteDatabase.getDatabase(context))
                        .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                        .customBackupFileName("database")
                        .backupIsEncrypted(true)
                        .customEncryptPassword(userId.toString())
                        .apply {
                            onCompleteListener { success, message, exitCode ->
                                Log.d(
                                    ContentValues.TAG,
                                    "success: $success, message: $message, exitCode: $exitCode"
                                )
                            }
                        }
                        .backup()

                    storageRef.child("user/$userId/database.aes").putFile(
                        File(
                            context.filesDir,
                            "databasebackup/database.aes"
                        ).toUri()
                    ).addOnSuccessListener {
                        val sdf2 = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                        val currentDateAndTime2: String = sdf2.format(Date().time)
                        val currentTime = "${context.resources.getString(R.string.last_backup)} $currentDateAndTime2"
                        with (sharedPref.edit()) {
                            putString("last_backup", currentTime)
                            apply()
                        }
                        lastBackupText.text = currentTime
                        Toast.makeText(this, R.string.backup_complete, Toast.LENGTH_SHORT).show()
                        restartApp()
                    }
                }
                .setNegativeButton(R.string.back) { _, _ ->

                }
                .show()
        }
    }

    fun restore(local: Boolean){
        val context = this
        val backup =context.backup
        val storageRef = Firebase.storage.reference

        if (local) {
            backup
                .database(NoteDatabase.getDatabase(context))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                        if (success) {
                           restartApp()
                        }
                    }
                }
                .restore()
        } else {
            val userId = Firebase.auth.currentUser?.uid
            if (userId == null){
                Toast.makeText(this, resources.getString(R.string.cloud_backup_account), Toast.LENGTH_SHORT).show()
            }
            val storagePath = File(context.filesDir, "databasebackup")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "database.aes")

            val dbRef = storageRef.child("user/$userId/database.aes")
            dbRef.getFile(myFile).addOnSuccessListener {
                backup
                    .database(NoteDatabase.getDatabase(context))
                    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                    .backupIsEncrypted(true)
                    .customEncryptPassword(userId.toString())
                    .apply {
                        onCompleteListener { success, message, exitCode ->
                            Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                            if (success) {
                                restartApp()
                            }
                        }
                    }
                    .restore()
            }.addOnFailureListener {
                Toast.makeText(context, R.string.restore_fail, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun showProgressDialog() {
        val dialog = Dialog(this).apply {
            setContentView(ProgressBar(this@Profile).apply {
                setPadding(50, 50, 50, 50)
            })
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setCancelable(false)
        }
        dialog.show()
    }

    private fun restartApp() = runBlocking {

        showProgressDialog()

        launch {
            delay(1300L)
            val ctx = applicationContext
            val pm = ctx.packageManager
            val intent = pm.getLaunchIntentForPackage(ctx.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
            ctx.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }

    }
}

class CardFragment : Fragment() {

    private var title: String? = null
    private var description: String? = null
    private var button1Action: (() -> Unit)? = null
    private var button2Action: (() -> Unit)? = null
    private var buttonInfo: (() -> Unit)? = null

    companion object {
        fun newInstance(
            titleResId: Int,
            descriptionResId: Int,
            button1Action: () -> Unit,
            button2Action: () -> Unit,
            buttonInfo: () -> Unit
        ): CardFragment {
            val fragment = CardFragment()
            val args = Bundle().apply {
                putInt("title", titleResId)
                putInt("description", descriptionResId)
            }
            fragment.arguments = args
            fragment.button1Action = button1Action
            fragment.button2Action = button2Action
            fragment.buttonInfo = buttonInfo
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.backup_card, container, false)

        // Получаем строки из ресурсов
        title = arguments?.getInt("title")?.let { getString(it) }
        description = arguments?.getInt("description")?.let { getString(it) }

        // Устанавливаем текст
        view.findViewById<TextView>(R.id.cardTitle).text = title
        view.findViewById<TextView>(R.id.cardDescription).text = description

        // Настройка кнопок
        view.findViewById<Button>(R.id.backup).setOnClickListener {
            button1Action?.invoke()
        }
        view.findViewById<Button>(R.id.restore).setOnClickListener {
            button2Action?.invoke()
        }
        view.findViewById<ImageView>(R.id.info).setOnClickListener {
            buttonInfo?.invoke()
        }

        return view
    }
}

// CardPagerAdapter.kt
class CardPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val cardData = listOf(
        CardItem(
            R.string.cloud_backup,
            R.string.cloud_backup_account,
            { (fragmentActivity as Profile).backup(false) },
            { (fragmentActivity as Profile).restore(false) },
            { showDialogBackup(fragmentActivity, R.string.cloud_backup, fragmentActivity.resources.getString(R.string.backup_rules) +
                    "\n${fragmentActivity.resources.getString(R.string.backup_warn)}") }
        ),
        CardItem(
            R.string.local_backup,
            R.string.not_encrypted,
            { (fragmentActivity as Profile).backup(true) },
            { (fragmentActivity as Profile).restore(true) },
            { showDialogBackup(fragmentActivity, R.string.local_backup, fragmentActivity.resources.getString(R.string.backup_rules) +
                    "\n${fragmentActivity.resources.getString(R.string.backup_warn)}") }
        )
    )

    override fun getItemCount(): Int = cardData.size


    override fun createFragment(position: Int): Fragment {
        val item = cardData[position]
        return CardFragment.newInstance(
            item.titleResId,
            item.descriptionResId,
            item.button1Action,
            item.button2Action,
            item.buttonInfo
        )
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDialogBackup(context: Context, title: Int, desc: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(title))
            .setMessage(desc)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    // Класс для хранения данных карточки
    data class CardItem(
        val titleResId: Int,
        val descriptionResId: Int,
        val button1Action: () -> Unit,
        val button2Action: () -> Unit,
        val buttonInfo: () -> Unit
    )
}