package com.sahil.recipeapp.uis.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sahil.recipeapp.R
import com.sahil.recipeapp.data.model.ExtendedIngredients
import com.sahil.recipeapp.databinding.ActivityShoppingListBinding
import com.sahil.recipeapp.uis.intent.DataIntent
import com.sahil.recipeapp.uis.view.base.BaseActivity
import com.sahil.recipeapp.uis.viewmodel.MainViewModel
import com.sahil.recipeapp.uis.viewstate.DataState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingListActivity : BaseActivity(),
    ShoppingListRecyclerAdapter.GroceryItemClickListener {

    private lateinit var mBinding: ActivityShoppingListBinding
    private val mViewModel: MainViewModel by viewModels()
    private lateinit var mAdapter: ShoppingListRecyclerAdapter

    private var groceryList: MutableList<ExtendedIngredients> =
        ArrayList() //this stores the current shopping list from room
    private var groceryDeleteList: MutableList<ExtendedIngredients> =
        ArrayList() //this stores the list to be deleted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initView()
        initViewModel()
    }

    private fun initView() {

        mBinding.toolbar.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        mBinding.toolbar.toolbarTitle.text = getString(R.string.list_name)

        mBinding.delete.setOnClickListener {
            deleteItems(groceryDeleteList)
        }

        mBinding.clearAll.setOnClickListener {
            if (groceryList.isEmpty()) {
                Snackbar.make(mBinding.root, "Your list is empty", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            deleteItems(groceryList)
        }

        mBinding.recyclerView.setHasFixedSize(true)
        mBinding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = ShoppingListRecyclerAdapter(this, this)
        mBinding.recyclerView.adapter = mAdapter
    }

    private fun deleteItems(groceryList: MutableList<ExtendedIngredients>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("DELETE")
        builder.setMessage(
            "Are you sure you want to delete ".plus(groceryList.size)
                .plus(" item(s) from your list?")
        )

        builder.setPositiveButton("YES") { dialog, _ ->
            for (item in groceryList) {
                lifecycleScope.launch {
                    mViewModel.dataIntent.send(
                        DataIntent.RemoveFromShoppingList(
                            item
                        )
                    )
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()

    }

    private fun initViewModel() {

        lifecycleScope.launch {
            mViewModel.dataState.collect {
                when (it) {
                    is DataState.Loading -> {

                    }

                    is DataState.IngredientResponse -> {
                        groceryList = it.ingredients
                        Log.e("list", "initViewModel: " + groceryList.size)
                        mBinding.itemCount.text = "Total Items: ".plus(groceryList.size)
                        mAdapter.setIngredients(groceryList)
                    }

                    is DataState.Error -> {
                        showError(mBinding.root, it.error)
                    }

                    else -> {

                    }
                }
            }
        }
        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            mViewModel.dataIntent.send(
                DataIntent.FetchShoppingListData
            )
        }
    }

    override fun onIngredientSelected(ingredient: ExtendedIngredients, isChecked: Boolean) {
        if (isChecked)
            groceryDeleteList.add(ingredient)
        else
            groceryDeleteList.remove(ingredient)

        if (groceryDeleteList.isNotEmpty())
            mBinding.delete.visibility = View.VISIBLE
        else
            mBinding.delete.visibility = View.GONE
    }

    override fun onCounterValueChanged(ingredients: List<ExtendedIngredients>) {
        lifecycleScope.launch {
            mViewModel.dataIntent.send(
                DataIntent.AddToShoppingList(
                    ingredients
                )
            )
        }
    }
}