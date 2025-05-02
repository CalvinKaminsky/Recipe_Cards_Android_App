package com.example.revisedrecipecards

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.revisedrecipecards.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_ADD   = 1001
        private const val REQ_EDIT  = 1002
    }

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()
    private val adapter by lazy {
        RecipeAdapter(
            onClick  = { openDetail(it) },
            onDelete = { vm.deleteRecipe(it) }
        )
    }

    private var includeFilters = emptyList<String>()
    private var excludeFilters = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvRecipes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter       = this@MainActivity.adapter
        }

        vm.filteredRecipes.observe(this) {
            adapter.submitList(it)
        }

        binding.etSearch.addTextChangedListener { text: Editable? ->
            vm.setSearchQuery(text?.toString().orEmpty())
        }

        binding.btnFilter.setOnClickListener { showFilterDialog() }
        binding.fabAddRecipe.setOnClickListener {
            startActivityForResult(
                Intent(this, AddRecipeActivity::class.java),
                REQ_ADD
            )
        }
    }

    private fun openDetail(recipe: Recipe) {
        startActivityForResult(
            Intent(this, RecipeDetailActivity::class.java)
                .putExtra("RECIPE", recipe),
            REQ_EDIT
        )
    }

    private fun showFilterDialog() {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_filters, null)
        val incInput = view.findViewById<MultiAutoCompleteTextView>(R.id.include_input)
        val excInput = view.findViewById<MultiAutoCompleteTextView>(R.id.exclude_input)

        val suggestions = vm.allRecipes.value
            ?.flatMap { it.ingredients.map { ing -> ing.first.trim() } }
            ?.distinct()
            ?.sorted()
            ?: emptyList()

        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions).also {
            incInput.setAdapter(it)
            excInput.setAdapter(it)
        }
        incInput.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        excInput.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        incInput.setText(includeFilters.joinToString(", "))
        excInput.setText(excludeFilters.joinToString(", "))

        AlertDialog.Builder(this)
            .setTitle(R.string.filter_title)
            .setView(view)
            .setPositiveButton(R.string.apply) { _, _ ->
                includeFilters = incInput.text.split(",")
                    .map(String::trim).filter(String::isNotEmpty)
                excludeFilters = excInput.text.split(",")
                    .map(String::trim).filter(String::isNotEmpty)
                vm.setFilters(includeFilters, excludeFilters)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (res != Activity.RESULT_OK || data == null) return
        data.getParcelableExtra<Recipe>("RECIPE")?.let {
            vm.upsertRecipe(it)
        }
    }
}
