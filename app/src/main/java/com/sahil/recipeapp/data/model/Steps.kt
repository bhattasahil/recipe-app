package com.sahil.recipeapp.data.model

import com.sahil.recipeapp.uis.view.IngredientInstructionInterface

data class Steps (
    var number      : Int?                   = null,
    var step        : String?                = null
): IngredientInstructionInterface
