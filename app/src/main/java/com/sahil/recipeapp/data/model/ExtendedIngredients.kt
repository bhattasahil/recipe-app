package com.sahil.recipeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sahil.recipeapp.uis.view.IngredientInstructionInterface

@Entity(tableName = "ingredients")
data class ExtendedIngredients (
    @PrimaryKey var id           : Int?              = null,
    var name         : String?           = null,
    var original     : String?           = null,
    var image        : String?           = null,
    var count        : Int               = 1,
): IngredientInstructionInterface