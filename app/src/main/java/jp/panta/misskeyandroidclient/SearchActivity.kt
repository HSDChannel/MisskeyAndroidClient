package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivitySearchBinding
import jp.panta.misskeyandroidclient.view.users.ClickableUserListAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class SearchActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_SEARCH_WORD = "jp.panta.misskeyandroidclient.SearchActivity.EXTRA_SEARCH_WORD"
    }

    private var mSearchView: SearchView? = null

    private var mSearchWord: String? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    private lateinit var mSearchUserViewModel: SearchUserViewModel

    private val binding: ActivitySearchBinding by dataBinding()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_search)
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        mSearchWord = intent.getStringExtra(EXTRA_SEARCH_WORD)

        val miCore = applicationContext as MiCore
        mSearchUserViewModel = ViewModelProvider(this, SearchUserViewModel.Factory(miCore, null))[SearchUserViewModel::class.java]

        val usersAdapter = ClickableUserListAdapter(this)
        binding.searchedUsers.adapter = usersAdapter
        binding.searchedUsers.layoutManager = LinearLayoutManager(this)
        mSearchUserViewModel.getUsers().observe(this, {
            usersAdapter.submitList(it)
        })
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_search, menu)

        val searchView = menu.findItem(R.id.app_bar_search)?.actionView as SearchView
        mSearchView = searchView

        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.isIconified = false
        searchView.setQuery(mSearchWord,false)

        return super.onCreateOptionsMenu(menu)
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
    private val queryTextListener = object : SearchView.OnQueryTextListener{
        override fun onQueryTextChange(newText: String?): Boolean {
            mSearchUserViewModel.userName.value = newText?: ""
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            if(!query.isNullOrBlank()){
                //search(query)
                showSearchResult(query)
                return true
            }
            return false
        }
    }

    fun showSearchResult(searchWord: String){
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, searchWord)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                finish()
                overridePendingTransition(0, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        overridePendingTransition(0, 0)
    }
}
