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
import com.example.mymarvelapp.adapter.CharacterAdapter
import com.example.mymarvelapp.databinding.ActivityHomeBinding
import com.example.mymarvelapp.databinding.LayoutCharacterListItemBinding
import com.example.mymarvelapp.network.entity.CharacterEntity
import com.example.mymarvelapp.network.interactor.FetchCharactersInteractor
import kotlinx.coroutines.*
import java.math.BigInteger
import java.security.MessageDigest

class HomeActivity : AppCompatActivity() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: CharacterAdapter
    private lateinit var fetchCharactersInteractor: FetchCharactersInteractor

    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchCharactersInteractor = FetchCharactersInteractor()

        initViews()
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

    private fun initViews() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchQuestions()
        }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        adapter = CharacterAdapter()
        binding.recycler.adapter = adapter
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
        binding.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideProgressIndication() {
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = false
            binding.loaderLayout.visibility = View.GONE
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
}