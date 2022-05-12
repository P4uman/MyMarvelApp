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
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigInteger
import java.security.MessageDigest


private const val URL = "https://gateway.marvel.com"
private const val PUBLIC_KEY = "56648a248c17e14c8b3cf59d293b99b8"
private const val PRIVATE_KEY = "b5860460de0f24c1191329c107b7f5c3109a1bb2"

class HomeActivity : AppCompatActivity() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CharacterAdapter
    private lateinit var marvelAPI: MarvelAPI

    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // init retrofit

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        marvelAPI = retrofit.create(MarvelAPI::class.java)
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
            try {
                val timeStamp = getTimeStamp()
                val response = marvelAPI.getCharacters(
                    timeStamp = timeStamp,
                    apiKey = PUBLIC_KEY,
                    hash = md5(timeStamp))
                if (response.isSuccessful && response.body() != null) {
                    adapter.bindData(response.body()?.data?.characters ?: listOf())
                    isDataLoaded = true
                } else {
                    onFetchFailed()
                }
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    onFetchFailed()
                }
            } finally {
                hideProgressIndication()
            }
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

    private fun getTimeStamp() = (System.currentTimeMillis()/1000).toString()

    private fun md5(timeStamp: String): String {
        val inputHash = timeStamp + PRIVATE_KEY + PUBLIC_KEY
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