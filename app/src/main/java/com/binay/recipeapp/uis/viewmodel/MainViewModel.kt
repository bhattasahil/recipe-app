package com.binay.recipeapp.uis.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binay.recipeapp.R
import com.binay.recipeapp.data.local.WebsiteDao
import com.binay.recipeapp.data.local.ingredientDb.IngredientDao
import com.binay.recipeapp.data.local.recipesDb.RecipeDao
import com.binay.recipeapp.data.model.ExtendedIngredients
import com.binay.recipeapp.data.model.RecipeData
import com.binay.recipeapp.data.model.SearchedRecipe
import com.binay.recipeapp.data.model.WebsiteData
import com.binay.recipeapp.data.repository.MainRepository
import com.binay.recipeapp.uis.intent.DataIntent
import com.binay.recipeapp.uis.viewstate.DataState
import com.binay.recipeapp.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mRepository: MainRepository,
    private val recipeDao: RecipeDao,
    private val websiteDao: WebsiteDao,
    private val ingredientDao: IngredientDao,
    @SuppressLint("StaticFieldLeak") @ApplicationContext private val mContext: Context
) : ViewModel() {

    val dataIntent = Channel<DataIntent>(Channel.UNLIMITED)
    val dataState = MutableStateFlow<DataState>(DataState.Inactive)

    init {
        handleIntent()
        insertWebsiteData()
    }

    private fun insertWebsiteData() {
        viewModelScope.launch {
            if (websiteDao.getAll().isNotEmpty()) {
                val websiteList: MutableList<WebsiteData> = ArrayList()
                websiteList.add(WebsiteData("Yummy", "https://www.yummly.com/recipes"))
                websiteList.add(WebsiteData("MyRecipes", "https://www.myrecipes.com/"))
                websiteList.add(WebsiteData("Allrecipes", "https://www.allrecipes.com/"))
                websiteList.add(WebsiteData("Food Network", "https://www.foodnetwork.com/recipes"))
                websiteList.add(WebsiteData("BBC Good Food", "https://www.bbcgoodfood.com/"))
                websiteList.add(WebsiteData("Tasty", "https://tasty.co/"))
                websiteList.add(WebsiteData("Epicurious", "https://www.epicurious.com/"))
                websiteList.add(WebsiteData("Serious Eats", "https://www.seriouseats.com/recipes"))
                websiteList.add(WebsiteData("Bon Appétit", "https://www.bonappetit.com/recipes"))
                websiteList.add(WebsiteData("Simply Recipes", "https://www.simplyrecipes.com/"))
                websiteList.add(WebsiteData("EatingWell", "https://www.eatingwell.com/recipes"))
                websiteList.add(
                    WebsiteData(
                        "The Spruce Eats",
                        "https://www.thespruceeats.com/recipes-4160606/"
                    )
                )
                websiteList.add(WebsiteData("Skinnytaste", "https://www.skinnytaste.com/"))
                websiteList.add(WebsiteData("Cookstr", "https://www.cookstr.com/"))
                websiteList.add(
                    WebsiteData(
                        "Taste of Home",
                        "https://www.tasteofhome.com/recipes/"
                    )
                )
                websiteList.add(WebsiteData("Delish", "https://www.delish.com/"))
                websiteList.add(WebsiteData("Food52", "https://food52.com/recipes"))
                websiteList.add(
                    WebsiteData(
                        "Cooking Channel",
                        "https://www.cookingchanneltv.com/recipes"
                    )
                )
                websiteList.add(WebsiteData("Delish", "https://www.delish.com/"))

                websiteDao.insert(websiteList)
            }
        }
    }

    private fun handleIntent() {
        viewModelScope.launch {
            dataIntent.consumeAsFlow().collect {
                when (it) {
                    is DataIntent.FetchRecipeData -> fetchData(
                        it.tag
                    )

                    is DataIntent.FetchRecipeDetail -> fetchRecipeDetailData(
                        it.recipeId
                    )

                    is DataIntent.ChangeFavoriteStatus -> changeFavoriteStatus(
                        it.recipe,
                        it.isToFavorite, it.isFromHome
                    )

                    is DataIntent.FetchFavoriteRecipe -> fetchFavoriteRecipes()

                    is DataIntent.SearchRecipe -> searchRecipes(it.query)

                    is DataIntent.ChangeFavoriteStatusFromSearch -> changeFavoriteStatus(
                        it.recipe,
                        it.isToFavorite
                    )

                    is DataIntent.SearchRecipesByNutrients -> searchRecipesByNutrients(it.query)

                    is DataIntent.FetchShoppingListData -> fetchShoppingListData()

                    is DataIntent.AddToShoppingList -> addToShoppingList(
                        it.ingredients
                    )

                    is DataIntent.RemoveFromShoppingList -> removeFromShoppingList(
                        it.ingredients
                    )

                    is DataIntent.FetchWebsiteList -> fetchWebsiteData()

                    is DataIntent.FetchRandomRecipe -> fetchRandomRecipe()

                    else -> {}
                }
            }
        }
    }

    private fun fetchData(tag: String) {
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                val recipeResponse = mRepository.getRecipes(tag)
                DataState.ResponseData(recipeResponse)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun fetchRecipeDetailData(recipeID: Int) {
        if (!isNetworkAvailable()) return
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                val recipeDetail = mRepository.getRecipeDetail(recipeID)
                DataState.RecipeDetail(recipeDetail)
            } catch (e: Exception) {
                DataState.Error(e.message)
            }
        }
    }

    private fun changeFavoriteStatus(
        recipe: RecipeData,
        isToFavorite: Boolean,
        isFromHome: Boolean
    ) {
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                recipe.isFavorite = isToFavorite
//               Checks recipe Dao for favorites and updates data accordingly
                recipeDao.changeRecipeFavoriteStatus(recipe)
                DataState.AddToFavoriteResponse(recipe, isFromHome)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private suspend fun fetchFavoriteRecipes() {
        dataState.value = try {
            val favoriteRecipes = recipeDao.getAllFavoriteRecipes()
            DataState.FavoriteResponse(favoriteRecipes.toCollection(ArrayList()))
        } catch (e: Exception) {
            DataState.Error(e.localizedMessage)
        }
    }

    private fun searchRecipes(query: String) {
        if (!isNetworkAvailable()) return
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                val searchedRecipeData = mRepository.searchRecipes(query)
//               Checks recipe Dao for favorites and updates data accordingly
//                Note: If recipe has isFavorite flag set, then it is favorite
                val searchedRecipes = searchedRecipeData.results ?: return@launch
                searchedRecipes.forEach {
                    val recipeId = it.id
                    if (recipeId != null) {
                        val favoriteRecipe = recipeDao.getFavoriteRecipe(recipeId)
                        if (favoriteRecipe != null) {
                            it.isFavorite = true
                        }
                    }
                }
                DataState.SearchRecipes(searchedRecipeData)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun changeFavoriteStatus(recipe: SearchedRecipe, isToFavorite: Boolean) {
        val recipeId = recipe.id ?: return
        if (!isNetworkAvailable()) return
        viewModelScope.launch {
            try {
                val deferredRecipeDetail = async {
                    Log.e("Recipe Detail ", " Here")
                    return@async mRepository.getRecipeDetail(recipeId)
                }

                val recipeDetail = deferredRecipeDetail.await()
                dataState.value = try {
                    recipeDetail.isFavorite = isToFavorite
                    recipeDetail.tagToBeSearchedBy = ""
//               Checks recipe Dao for favorites and updates data accordingly
                    val localRecipes = mRepository.getAllLocalRecipes()
//                    Change favorite status if recipe is already present otherwise add it to favorite
                    if (localRecipes != null) {
                        val filteredRecipes =
                            localRecipes.recipes.filter { recipe -> recipe.id == recipeId }
                        if (filteredRecipes.isEmpty()) {
                            recipeDao.addFavoriteRecipe(recipeDetail)
                        } else {
                            recipeDao.changeRecipeFavoriteStatus(recipeDetail)
                        }
                    }
                    DataState.AddToFavoriteResponse(recipeDetail, false)
                } catch (e: Exception) {
                    DataState.Error(e.localizedMessage)
                }
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }

        }
    }

    private fun searchRecipesByNutrients(query: String) {
        if (!isNetworkAvailable()) return
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                val searchedRecipes: ArrayList<SearchedRecipe> =
                    mRepository.searchRecipesByIngredients(query)
                Log.e("Recipes by nutrients ", "" + searchedRecipes)
//               Checks recipe Dao for favorites and updates data accordingly
//                Note: If room has isFavorite flag set, then it is automatically favorite
                searchedRecipes.forEach {
                    val recipeId = it.id
                    if (recipeId != null) {
                        val favoriteRecipe = recipeDao.getFavoriteRecipe(recipeId)
                        if (favoriteRecipe != null) {
                            it.isFavorite = true
                        }
                    }
                }
                DataState.SearchRecipesByNutrients(searchedRecipes)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun fetchShoppingListData() {
        viewModelScope.launch {
            dataState.value = try {
                val ingredientData = ingredientDao.getAllIngredients()
                DataState.IngredientResponse(ingredientData.toCollection(ArrayList()))
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun addToShoppingList(ingredients: List<ExtendedIngredients>) {
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                ingredientDao.addAllIngredients(ingredients)
                DataState.AddToShoppingList(ingredients)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun removeFromShoppingList(ingredients: ExtendedIngredients) {
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                ingredientDao.removeIngredient(ingredients)
                val updatedList = ingredientDao.getAllIngredients()
                DataState.IngredientResponse(updatedList.toCollection(ArrayList()))
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun fetchWebsiteData() {
        viewModelScope.launch {
            dataState.value = try {
                val websiteData = websiteDao.getAll()
                DataState.FetchWebsiteList(websiteData)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun fetchRandomRecipe() {
        viewModelScope.launch {
            dataState.value = DataState.Loading
            dataState.value = try {
                val recipes = mRepository.getRandomRecipe()
                DataState.RandomRecipe(recipes)
            } catch (e: Exception) {
                DataState.Error(e.localizedMessage)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        if (!NetworkUtil.isNetworkAvailable(mContext)) {
            dataState.value = DataState.Error(mContext.getString(R.string.no_connection))
            return false
        }
        return true
    }

}