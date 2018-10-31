package com.lincoln.adam.githubshopifylauncher.data.source.remote

import com.lincoln.adam.githubshopifylauncher.BuildConfig
import com.lincoln.adam.githubshopifylauncher.data.RepoModel
import com.lincoln.adam.githubshopifylauncher.data.source.RepoDataSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor





class RepoRemoteDataSource : RepoDataSource {

    companion object {

        const val GITHUB_BASE_URL = "https://api.github.com/"
        const val GITHUB_ORG_NAME = "shopify"

        private var INSTANCE: RepoRemoteDataSource? = null

        @JvmStatic
        fun getInstance(): RepoRemoteDataSource {
            if (INSTANCE == null) {
                synchronized(RepoRemoteDataSource::javaClass) {
                    if (INSTANCE == null) {
                        INSTANCE = RepoRemoteDataSource()
                    }
                }
            }
            return INSTANCE!!
        }
    }

    private val REPO_SERVICE_DATA = LinkedHashMap<Int, RepoModel>(0)

    private val httpClient: OkHttpClient
    private val retrofit: Retrofit
    private val gitHubService: GitHubService

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC

        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG)
            builder.addInterceptor(logging)

        httpClient = builder.build()

        retrofit = Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        gitHubService = retrofit.create(GitHubService::class.java)
    }

    override fun getRepos(repoCallback: RepoDataSource.RepoCallback) {
        val call = gitHubService.getRepos(GITHUB_ORG_NAME)
        call.enqueue(object : Callback<List<RepoModel>> {
            override fun onResponse(call: Call<List<RepoModel>>, response: Response<List<RepoModel>>) {
                val modelList = response.body()!!
                repoCallback.onLoaded(modelList)
            }

            override fun onFailure(call: Call<List<RepoModel>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    override fun refreshRepos() {
        // N/A - Implemented by RepoRepository.
    }

    override fun saveRepo(repo: RepoModel) {
        REPO_SERVICE_DATA[repo.id] = repo
    }

    override fun deleteAllRepos() {
        REPO_SERVICE_DATA.clear()
    }
}