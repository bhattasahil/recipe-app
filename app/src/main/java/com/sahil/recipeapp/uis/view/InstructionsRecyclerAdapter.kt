package com.sahil.recipeapp.uis.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sahil.recipeapp.data.model.Steps
import com.sahil.recipeapp.databinding.ItemInstructionBinding

class InstructionsRecyclerAdapter(private val dataList: List<Steps>): RecyclerView.Adapter<InstructionsRecyclerAdapter.InstructionViewHolder>() {

    class InstructionViewHolder(binding: ItemInstructionBinding): RecyclerView.ViewHolder(binding.root) {
        val instructionText = binding.text
        val number = binding.number
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
        val mBinding = ItemInstructionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InstructionViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
        val item = dataList[position]

        holder.number.text = item.number.toString()
        holder.instructionText.text = item.step.toString()

    }
}