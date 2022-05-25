package com.example.mymarvelapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mymarvelapp.network.entity.CharacterEntity
import com.example.mymarvelapp.network.interactor.FetchCharactersInteractor
import kotlinx.coroutines.*
import java.math.BigInteger
import java.security.MessageDigest

class HomeActivity : AppCompatActivity() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CharacterAdapter
    private lateinit var fetchCharactersInteractor: FetchCharactersInteractor

    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchCharactersInteractor = FetchCharactersInteractor()

        setContentView(R.layout.layout_questions_list)

        // init pull-down-to-refresh
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            fetchQuestions()
        }

        // init recycler view
        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CharacterAdapter()
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        if (!isDataLoaded) {
            fetchQuestions()
        }
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
    }

    private fun fetchQuestions() {
        coroutineScope.launch {
            showProgressIndication()
            val timeStamp = getTimeStamp()
            fetchCharactersInteractor.fetchCharacters(
                timeStamp = timeStamp,
                hashData = md5(timeStamp),
                onComplete = {
                    hideProgressIndication()
                },
                onSuccess = { result ->
                    adapter.bindData(result.characters ?: listOf())
                },
                onFailure = {
                    onFetchFailed()
                }
            )
        }
    }

    private fun onFetchFailed() {
        // Do nothing
    }

    private fun showProgressIndication() {
        swipeRefresh.isRefreshing = true
    }

    private fun hideProgressIndication() {
        if (swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = false
        }
    }

    private fun getTimeStamp() = (System.currentTimeMillis() / 1000).toString()

    private fun md5(timeStamp: String): String {
        val inputHash = timeStamp + BuildConfig.PRIVATE_KEY + BuildConfig.PUBLIC_KEY
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(inputHash.toByteArray()))
            .toString(16)
            .padStart(32, '0')
    }

    class CharacterAdapter : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

        private var characterList: List<CharacterEntity> = ArrayList(0)

        inner class CharacterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.text)
        }

        fun bindData(characters: List<CharacterEntity>) {
            characterList = ArrayList(characters)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_character_list_item, parent, false)
            return CharacterViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
            holder.title.text = characterList[position].name
        }

        override fun getItemCount(): Int {
            return characterList.size
        }
    }
}