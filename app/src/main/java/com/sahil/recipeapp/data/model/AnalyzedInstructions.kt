package com.sahil.recipeapp.data.model

import com.sahil.recipeapp.uis.view.IngredientInstructionInterface

data class AnalyzedInstructions (
    var name  : String?          = null,
    var steps : ArrayList<Steps>? = arrayListOf(),
    var readyInMinutes : Int?
): IngredientInstructionInterface
