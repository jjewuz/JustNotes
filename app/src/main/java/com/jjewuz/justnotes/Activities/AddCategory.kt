package com.jjewuz.justnotes.Activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.jjewuz.justnotes.Category.Category
import com.jjewuz.justnotes.Category.CategoryAdapter
import com.jjewuz.justnotes.Category.CategoryViewModel
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.databinding.ActivityAddCategoryBinding

class AddCategory : AppCompatActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val theme = sharedPref.getString("theme", "standart")
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
        setContentView(binding.root)
        enableEdgeToEdge()

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        categoryViewModel = CategoryViewModel(application)

        categoryAdapter = CategoryAdapter { category ->
            deleteCategory(category)
        }


        binding.recyclerView.adapter = categoryAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        categoryViewModel.allCategories.observe(this) { categories ->
            categories?.let {
                categoryAdapter.setCategories(it)
            }
        }

        binding.addCategory.setOnClickListener {
            addDialog()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.addCategory) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = insets.bottom + 20
            params.rightMargin = insets.right + 40
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return false
    }

    private fun addDialog(){
        val view = layoutInflater.inflate(R.layout.add_category_dialog, null)
        val editText = view.findViewById<TextInputEditText>(R.id.edit_text)
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.label))
            .setView(view)
            .setNegativeButton(resources.getString(R.string.back)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.add)) { _, _ ->
                val categoryName = editText.text.toString().trim()

                if (categoryName.isNotEmpty()) {
                    val category = Category(name = categoryName)
                    categoryViewModel.insert(category)
                    categoryAdapter.addCategory(category)
                }
            }
            .show()
    }

    private fun deleteCategory(category: Category) {
        categoryAdapter.removeCategory(category)
        categoryViewModel.delete(category)
    }
}