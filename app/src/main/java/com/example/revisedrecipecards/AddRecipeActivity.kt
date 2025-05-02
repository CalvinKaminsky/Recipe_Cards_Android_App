package com.example.revisedrecipecards

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.revisedrecipecards.databinding.ActivityAddRecipeBinding

class AddRecipeActivity : AppCompatActivity() {

    companion object {
        private const val REQ_PICK_IMAGE = 3001
    }

    private lateinit var b: ActivityAddRecipeBinding
    private var existing: Recipe? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(b.root)

        existing = intent.getParcelableExtra("RECIPE")
        setSupportActionBar(b.toolbarAdd)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title =
            existing?.let { getString(R.string.edit_recipe) }
                ?: getString(R.string.add_recipe)

        b.etAddName.setText(existing?.name.orEmpty())
        b.etAddDescription.setText(existing?.description.orEmpty())
        b.etAddNotes.setText(existing?.notes.orEmpty())
        b.etAddSteps.setText(existing?.steps.orEmpty())
        b.etAddServings.setText(existing?.servings?.toString().orEmpty())

        existing?.imageUrl?.takeIf { it.isNotBlank() }?.let {
            imageUri = Uri.parse(it)
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_default_recipe)
                .fitCenter()
                .into(b.ivAddImage)
        }

        b.ivAddImage.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).run {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                startActivityForResult(this, REQ_PICK_IMAGE)
            }
        }

        val ingList = existing?.ingredients ?: emptyList()
        repeat(maxOf(4, ingList.size)) { i ->
            addIngredientRow(
                ingList.getOrNull(i)?.first.orEmpty(),
                ingList.getOrNull(i)?.second.orEmpty()
            )
        }

        b.btnAddIngredient.setOnClickListener { addIngredientRow("", "") }

        b.fabSaveRecipe.setOnClickListener {
            val name   = b.etAddName.text.toString()
            val desc   = b.etAddDescription.text.toString()
            val notes  = b.etAddNotes.text.toString()
            val steps  = b.etAddSteps.text.toString()
            val serv   = b.etAddServings.text.toString().toIntOrNull() ?: 1

            val ingredients = mutableListOf<Pair<String,String>>()
            repeat(b.llAddIngredients.childCount) { idx ->
                val row    = b.llAddIngredients.getChildAt(idx) as LinearLayout
                val inName = (row.getChildAt(0) as EditText).text.toString()
                val inMeas = (row.getChildAt(1) as EditText).text.toString()
                if (inName.isNotBlank() || inMeas.isNotBlank())
                    ingredients += inName to inMeas
            }

            val recipe = Recipe(
                id          = existing?.id ?: System.currentTimeMillis().toString(),
                name        = name,
                imageUrl    = imageUri?.toString().orEmpty(),
                ingredients = ingredients,
                description = desc,
                notes       = notes,
                steps       = steps,
                servings    = serv
            )

            setResult(Activity.RESULT_OK, Intent().putExtra("RECIPE", recipe))
            finish()
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        if (req == REQ_PICK_IMAGE && res == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_default_recipe)
                    .fitCenter()
                    .into(b.ivAddImage)
            }
            return
        }
        super.onActivityResult(req, res, data)
    }

    override fun onCreateOptionsMenu(menu: Menu) =
        menuInflater.inflate(R.menu.menu_add_recipe, menu).let { true }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_cancel -> { setResult(Activity.RESULT_CANCELED); finish(); true }
            else               -> super.onOptionsItemSelected(item)
        }

    private fun addIngredientRow(name: String, measure: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val nameField = EditText(this).apply {
            hint = getString(R.string.ingredient_hint)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setText(name)
        }
        val measureField = EditText(this).apply {
            hint = getString(R.string.measure_hint)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(measure)
        }
        row.addView(nameField)
        row.addView(measureField)
        b.llAddIngredients.addView(row)
    }
}