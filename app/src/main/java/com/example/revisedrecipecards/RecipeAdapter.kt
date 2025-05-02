package com.example.revisedrecipecards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.revisedrecipecards.databinding.ItemRecipeCardBinding

class RecipeAdapter(
    private val onClick: (Recipe) -> Unit,
    private val onDelete: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) = ViewHolder(
        ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onClick, onDelete
    )

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) =
        holder.bind(getItem(pos))

    class ViewHolder(
        private val b: ItemRecipeCardBinding,
        private val onClick: (Recipe) -> Unit,
        private val onDelete: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(r: Recipe) {
            b.tvRecipeName.text = r.name
            Glide.with(b.root)
                .load(r.imageUrl.ifBlank { null })
                .placeholder(R.drawable.ic_default_recipe)
                .error(R.drawable.ic_default_recipe)
                .centerCrop()
                .into(b.ivRecipeImage)

            val tvs = listOf(
                b.tvIngredient1,
                b.tvIngredient2,
                b.tvIngredient3,
                b.tvIngredient4
            )
            tvs.forEachIndexed { i, tv ->
                if (i < r.ingredients.size) {
                    val (name, measure) = r.ingredients[i]
                    tv.text = "• $measure $name"
                    tv.visibility = View.VISIBLE
                } else {
                    tv.visibility = View.GONE
                }
            }

            b.root.setOnClickListener { onClick(r) }
            b.btnDeleteRecipe.setOnClickListener { onDelete(r) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(old: Recipe, new: Recipe) = old.id == new.id
        override fun areContentsTheSame(old: Recipe, new: Recipe) = old == new
    }
}
