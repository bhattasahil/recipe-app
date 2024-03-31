package com.sahil.recipeapp.uis.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sahil.recipeapp.R
import com.sahil.recipeapp.data.model.RecipeData
import com.sahil.recipeapp.databinding.ActivityRecipedetailBinding
import com.sahil.recipeapp.uis.intent.DataIntent
import com.sahil.recipeapp.uis.view.base.BaseActivity
import com.sahil.recipeapp.uis.view.cookingTimer.CookingTimerFragment
import com.sahil.recipeapp.uis.viewmodel.FragmentDataViewModel
import com.sahil.recipeapp.uis.viewmodel.MainViewModel
import com.sahil.recipeapp.uis.viewstate.DataState
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class RecipeDetailActivity : BaseActivity(), TabLayout.OnTabSelectedListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var mBinding: ActivityRecipedetailBinding
    private var recipeId = 0
    private var readyInMinutes: Int? = null
    private var recipeName = ""

    private var isToolbarVisible = false
    private val titleList = arrayOf("Ingredients", "Instructions")

    private lateinit var fragmentViewModel: FragmentDataViewModel
    private lateinit var recipeData: RecipeData
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRecipedetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        supportFragmentManager.setFragmentResultListener(
            STARTED_TIMER_KEY,
            this
        ) { _, bundle ->
            startTime = bundle.getLong(STARTED_TIMER_RESULT, 0)
            val timeRemaining = bundle.getLong(STARTED_TIMER_RESULT_REM, 0)
            if (timeRemaining <= 0) {
                startTime = 0
                mBinding.btnStartCooking.text = getString(R.string.label_start_cooking)
            }
        }

        recipeId = intent?.data?.getQueryParameter("id")?.toInt() ?: 0
        if (recipeId == 0)
            recipeId = intent.getIntExtra("recipe_id", 0)

        initViewModel()
        initView()

        fetchDetailData(recipeId)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        recipeId = intent?.data?.getQueryParameter("id")?.toInt() ?: 0
        fetchDetailData(recipeId)
    }

    private fun initView() {
        setSupportActionBar(mBinding.toolbar)

        mBinding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        mBinding.addToListButton.setOnClickListener {
            addToShoppingList()
        }

        mBinding.btnStartCooking.setOnClickListener {
            requestNotificationPermission()
            if (readyInMinutes != null) {
                mBinding.btnStartCooking.text = getString(R.string.label_view_timer)
                val cookingFragment =
                    CookingTimerFragment.newInstance(readyInMinutes!!, startTime, recipeName)
                cookingFragment.show(
                    supportFragmentManager,
                    CookingTimerFragment::class.java.canonicalName
                )
            }
        }

        mBinding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxScroll = appBarLayout.totalScrollRange
            val percentage = (abs(verticalOffset).toFloat() / maxScroll.toFloat())

            if (percentage > 0.7 && !isToolbarVisible) {
                // Change toolbar title color to white and toolbar background color
                mBinding.toolbar.background =
                    ContextCompat.getDrawable(this, R.drawable.toolbar_background)
                mBinding.toolbarTitle.visibility = View.VISIBLE
                mBinding.recipeName.visibility = View.GONE
                isToolbarVisible = true
            } else if (percentage <= 0.7 && isToolbarVisible) {
                // Change toolbar title color to your desired color and toolbar background color
                mBinding.toolbar.background = ColorDrawable(Color.TRANSPARENT)
                mBinding.toolbarTitle.visibility = View.INVISIBLE
                mBinding.recipeName.visibility = View.VISIBLE
                isToolbarVisible = false
            }
        }

        mBinding.shareBtn.setOnClickListener {
            val deepLink = "http://open.my.recipe/detail?id=$recipeId"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this recipe: $deepLink")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                Notification granted
                }
            }

            else -> {
//                For other permissions
            }
        }
    }

    private fun requestNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

    private fun initViewModel() {
        fragmentViewModel = ViewModelProvider(this)[FragmentDataViewModel::class.java]
        fragmentViewModel.isChecked.observe(this) {
            if (it)
                mBinding.addToListButton.visibility = View.VISIBLE
            else
                mBinding.addToListButton.visibility = View.GONE
        }

        lifecycleScope.launch {
            viewModel.dataState.collect {
                when (it) {
                    is DataState.Loading -> {

                    }

                    is DataState.RecipeDetail -> {
                        recipeData = it.recipeData
                        recipeName = recipeData.title ?: ""
                        readyInMinutes = recipeData.readyInMinutes
                        populateView(recipeData)
                    }

                    is DataState.Error -> {
                        showError(mBinding.root, it.error)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun populateView(recipeData: RecipeData) {
        mBinding.toolbarTitle.text = recipeData.title
        mBinding.recipeName.text = recipeData.title

        var mealType = ""
        for (cuisine in recipeData.cuisines!!) {
            mealType += cuisine.plus(", ") //-2 below coz i added 2 characters here
        }

        if (mealType.isNotEmpty())
            mBinding.mealType.text = mealType.substring(0, mealType.length - 2)

        Picasso.with(this).load(recipeData.image).into(mBinding.recipeImage)

        if (recipeData.vegetarian == true) {
            mBinding.recipeDetail.vegImage.setImageResource(R.drawable.icon_veg)

            mBinding.recipeDetail.vegTag.text = getString(R.string.label_vegetarian)
        } else {
            mBinding.recipeDetail.vegImage.setImageResource(R.drawable.icon_meat)
            mBinding.recipeDetail.vegTag.text = getString(R.string.label_eggs_meat)
        }

        mBinding.recipeDetail.timerTag.text = "${recipeData.readyInMinutes.toString()} mins"
        mBinding.recipeDetail.servingTag.text = recipeData.servings.toString() + " servings"

        for (nutrients in recipeData.nutrition?.nutrients!!) {
            if (nutrients.name.equals("calories", true)) {
                mBinding.recipeDetail.calorieTag.text =
                    nutrients.amount.toString() + " " + nutrients.unit + "/serving"
                break
            }
        }

        val fragments: ArrayList<Fragment> = ArrayList()
        fragments.add(IngredientsFragment())
        fragments.add(InstructionsFragment())
        mBinding.recipeDetail.tabLayout.addOnTabSelectedListener(this)
        val pagerAdapter = MyPagerAdapter(this, fragments)
        mBinding.recipeDetail.viewPager.adapter = pagerAdapter

        TabLayoutMediator(
            mBinding.recipeDetail.tabLayout,
            mBinding.recipeDetail.viewPager
        ) { tab, position ->
            // Set tab text or leave it empty if you want to display only icons
            tab.text = titleList[position]
        }.attach()
    }

    private fun addToShoppingList() {
        fragmentViewModel.groceryList.observe(this) { items ->
            lifecycleScope.launch {
                viewModel.dataIntent.send(
                    DataIntent.AddToShoppingList(
                        items
                    )
                )
            }
            fragmentViewModel.groceryList.removeObservers(this)
        }
        fragmentViewModel.isAddedToList.value = true
        mBinding.addToListButton.visibility = View.GONE
        Snackbar.make(mBinding.root, "Added to Grocery List successfully", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun fetchDetailData(id: Int) {
        lifecycleScope.launch {
            viewModel.dataIntent.send(
                DataIntent.FetchRecipeDetail(
                    id
                )
            )
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            0 -> { //ingredients
                mBinding.btnStartCooking.visibility = View.GONE
                fragmentViewModel.ingredients.value = recipeData.extendedIngredients
            }

            else -> { //instructions
                mBinding.addToListButton.visibility = View.GONE
                if (readyInMinutes != null) mBinding.btnStartCooking.visibility =
                    View.VISIBLE
                val instructions = recipeData.analyzedInstructions
                if (!instructions.isNullOrEmpty()) instructions[0].readyInMinutes = readyInMinutes
                fragmentViewModel.instructions.value = instructions
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
        const val STARTED_TIMER_KEY = "startedTimerKey"
        const val STARTED_TIMER_RESULT = "startedTimerResult"
        const val STARTED_TIMER_RESULT_REM = "startedTimerResultRemaining"
    }
}

class MyPagerAdapter(
    fragmentActivity: FragmentActivity?,
    private val fragments: ArrayList<Fragment>
) :
    FragmentStateAdapter(fragmentActivity!!) {

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}