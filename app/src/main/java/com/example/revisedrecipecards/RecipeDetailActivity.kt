package com.example.revisedrecipecards

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.revisedrecipecards.databinding.ActivityRecipeDetailBinding

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        private const val REQ_EDIT = 2001
    }

    private lateinit var b: ActivityRecipeDetailBinding
    private lateinit var recipe: Recipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbarDetail)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        recipe = intent.getParcelableExtra("RECIPE") ?: return finish()
        supportActionBar?.title = recipe.name
        b.tvDetailTitle.text = recipe.name

        Glide.with(this)
            .load(recipe.imageUrl.ifBlank { null })
            .placeholder(R.drawable.ic_default_recipe)
            .error(R.drawable.ic_default_recipe)
            .fitCenter()
            .into(b.ivDetailImage)

        setupToggle(b.headerDescription, b.tvDescription, b.arrowDescription, recipe.description)
        setupToggle(b.headerNotes,       b.tvNotes,       b.arrowNotes,       recipe.notes)
        setupToggle(b.headerSteps,       b.tvSteps,       b.arrowSteps,       recipe.steps)

        b.tvServings.text = getString(R.string.servings_format, recipe.servings)

        recipe.ingredients.forEach { (name, measure) ->
            val tv = TextView(this).apply {
                text = "• $measure $name"
                setPadding(0,4,0,4)
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.text_size_small)
                )
            }
            b.llIngredients.addView(tv)
        }

        b.fabEditRecipe.setOnClickListener {
            startActivityForResult(
                Intent(this, AddRecipeActivity::class.java)
                    .putExtra("RECIPE", recipe),
                REQ_EDIT
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu) =
        menuInflater.inflate(R.menu.menu_recipe_detail, menu).let { true }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_cancel -> { finish(); true }
            else               -> super.onOptionsItemSelected(item)
        }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == REQ_EDIT && res == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun setupToggle(
        header: View,
        content: TextView,
        arrow: ImageView,
        text: String
    ) {
        content.text = if (text.isNotBlank()) text else getString(R.string.none_provided)
        header.setOnClickListener {
            val shown = content.visibility == View.VISIBLE
            content.visibility = if (shown) View.GONE else View.VISIBLE
            arrow.rotation     = if (shown) 0f else 180f
        }
    }
}