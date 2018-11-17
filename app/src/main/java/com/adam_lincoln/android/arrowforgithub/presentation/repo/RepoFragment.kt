package com.adam_lincoln.android.arrowforgithub.presentation.repo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.*
import android.view.animation.AnimationUtils
import com.adam_lincoln.android.arrowforgithub.R
import kotlinx.android.synthetic.main.fragment_repo.*
import kotlinx.android.synthetic.main.fragment_repo.view.*

class RepoFragment : Fragment(), RepoContract.View {

    companion object {

        fun newInstance() = RepoFragment()

    }

    override lateinit var presenter: RepoContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    interface RepoOnClickListener {

        fun onGitHubUrlClick(repo: RepoViewModel)

        fun onHomepageUrlClick(repo: RepoViewModel)

    }

    private var repoOnClickListener: RepoOnClickListener = object : RepoOnClickListener {

        override fun onGitHubUrlClick(repo: RepoViewModel) {
            presenter.onGitHubUrlClick(repo)
        }

        override fun onHomepageUrlClick(repo: RepoViewModel) {
            presenter.onHomepageUrlClick(repo)
        }

    }

    private val repoAdapter = RepoAdapter(ArrayList(0), repoOnClickListener)

    override fun onResume() {
        super.onResume()

        presenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_repo, container, false)

        with(root) {
            recyclerView.adapter = repoAdapter
//            val dividerItemDecoration = if (recyclerView.layoutManager is LinearLayoutManager) (recyclerView.layoutManager as LinearLayoutManager).orientation else DividerItemDecoration.VERTICAL
//            recyclerView.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
//            recyclerView.setHasFixedSize(true)
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
            swipeRefreshLayout.setOnRefreshListener { presenter.onSwipeRefresh() }
        }

        requireActivity().findViewById<FloatingActionButton>(R.id.fab_refresh).apply {
            setOnClickListener { presenter.onRefreshClick() }
        }

        setHasOptionsMenu(true)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_repo_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> {
                presenter.onForceRefreshClick()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        errorState.visibility = View.GONE
        loadingState.visibility = View.GONE
        recyclerView.visibility = View.GONE
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = false }
    }

    override fun showLoadingState() {
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
        loadingState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        swipeRefreshLayout?.post { swipeRefreshLayout?.isRefreshing = true }
    }

    override fun showErrorState() {
        emptyState.visibility = View.GONE
        errorState.visibility = View.VISIBLE
        loadingState.visibility = View.GONE
        recyclerView.visibility = View.GONE
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = false }
    }

    override fun showRepoList(repoList: List<RepoViewModel>) {
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
        loadingState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = false }
        repoAdapter.repoList = repoList
        recyclerView.scheduleLayoutAnimation()  // Forces items to always animate.
    }

    override fun navigateToUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}