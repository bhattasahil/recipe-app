package com.binay.recipeapp.uis.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.recyclerview.widget.RecyclerView
import com.binay.recipeapp.data.model.RecipeData
import com.binay.recipeapp.databinding.ItemRecipeBinding
import com.squareup.picasso.Picasso

class RecipeRecyclerAdapter(
    private val context: Context,
    private val mListener: RecipeClickListener
) : RecyclerView.Adapter<RecipeRecyclerAdapter.RecipeViewHolder>() {

    private var recipeList: ArrayList<RecipeData> = ArrayList()

    fun setRecipes(recipes: ArrayList<RecipeData>) {
        recipeList.clear()
        recipeList.addAll(recipes)
        this.notifyDataSetChanged()
    }

    class RecipeViewHolder(binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        val recipeName = binding.recipeName
        val recipeImage = binding.recipeImage
        val calories = binding.recipeCalorie
        val cbFavorite = binding.cbFavorite
        val rootView = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.recipeName.text = recipe.title

        if (recipe.image?.isNotEmpty() == true)
            Picasso.with(context).load(recipeList[position].image).into(holder.recipeImage)

        holder.calories.text = "${recipe.readyInMinutes} mins"

        holder.cbFavorite.setOnCheckedChangeListener(null)
        holder.cbFavorite.isChecked = recipe.isFavorite ?: false

        holder.cbFavorite.setOnCheckedChangeListener(object : OnCheckedChangeListener,
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {

            }

            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                mListener.onFavoriteChanged(recipe, p1)
            }

        })

        holder.rootView.setOnClickListener {
            mListener.onRecipeClicked(recipe)
        }

    }

    fun removeRecipe(recipe: RecipeData) {
        val position = recipeList.indexOf(recipe)
        recipeList.remove(recipe)
        notifyItemRemoved(position)
    }

    fun addRecipe(recipe: RecipeData) {
        recipeList.add(recipe)
        notifyItemInserted(recipeList.size - 1)
    }

//Changes favourite status in HomeFragment when favorite is removed from FavouriteFragment
    fun changeFavoriteStatus(recipe: RecipeData, isFromHome: Boolean) {
        if (!isFromHome) {
            val recipeOfList = recipeList.find { it.id == recipe.id }
            recipeOfList?.let {
                val position = recipeList.indexOf(it)
                notifyItemChanged(position)
            }
        }
    }

    interface RecipeClickListener {
        fun onFavoriteChanged(recipe: RecipeData, isToFavorite: Boolean)
        fun onRecipeClicked(recipe: RecipeData)
    }
}