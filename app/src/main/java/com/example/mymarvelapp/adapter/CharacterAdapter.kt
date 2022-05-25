package com.example.mymarvelapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymarvelapp.databinding.LayoutCharacterListItemBinding
import com.example.mymarvelapp.network.entity.CharacterEntity

class CharacterAdapter : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    private var characterList: List<CharacterEntity> = ArrayList(0)

    inner class CharacterViewHolder(binding: LayoutCharacterListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.text
    }

    fun bindData(characters: List<CharacterEntity>) {
        characterList = ArrayList(characters)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val itemView = LayoutCharacterListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.title.text = characterList[position].name
    }

    override fun getItemCount(): Int {
        return characterList.size
    }
}