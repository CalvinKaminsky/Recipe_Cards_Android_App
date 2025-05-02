package com.example.revisedrecipecards

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).recipeDao()

    private val allRecipesLiveData = dao.getAll()

    val allRecipes: LiveData<List<Recipe>> = allRecipesLiveData

    private val searchQuery    = MutableLiveData("")
    private val includeFilter  = MutableLiveData<List<String>>(emptyList())
    private val excludeFilter  = MutableLiveData<List<String>>(emptyList())

    val filteredRecipes: LiveData<List<Recipe>> = MediatorLiveData<List<Recipe>>().apply {
        fun update() {
            val list = allRecipesLiveData.value ?: return
            val q    = searchQuery.value!!.trim().lowercase()
            val inc  = includeFilter.value!!.map { it.trim().lowercase() }
            val exc  = excludeFilter.value!!.map { it.trim().lowercase() }

            val filtered = list.filter { recipe ->
                val nameOk = recipe.name.lowercase().contains(q)
                val ings   = recipe.ingredients.map { it.first.lowercase() }
                val incOk  = inc.all { must -> ings.any { it.contains(must) } }
                val excOk  = exc.all { not -> ings.none { it.contains(not) } }
                nameOk && incOk && excOk
            }
            this.value = filtered
        }

        addSource(allRecipesLiveData) { update() }
        addSource(searchQuery)    { update() }
        addSource(includeFilter)  { update() }
        addSource(excludeFilter)  { update() }
    }

    fun setSearchQuery(q: String) =
        searchQuery.postValue(q)

    fun setFilters(include: List<String>, exclude: List<String>) {
        includeFilter.postValue(include)
        excludeFilter.postValue(exclude)
    }

    fun deleteRecipe(recipe: Recipe) =
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(recipe)
        }

    fun upsertRecipe(recipe: Recipe) =
        viewModelScope.launch(Dispatchers.IO) {
            dao.upsert(recipe)
        }
}