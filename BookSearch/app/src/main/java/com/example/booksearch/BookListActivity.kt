package com.example.booksearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.android.synthetic.main.activity_book_list.*
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONException


class BookListActivity : AppCompatActivity() {

    val BOOK_DETAIL_KEY = "book"
    var progress: ProgressBar? = null
    private var bookAdapter: BookAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        progress = findViewById(R.id.progress)

        val lvBooks = findViewById<View>(R.id.lvBooks) as ListView
        val aBooks =
            ArrayList<Book?>()
        bookAdapter = BookAdapter(this, aBooks)
        lvBooks.adapter = bookAdapter

        setupBookSelectedListener();
    }

    fun setupBookSelectedListener() {
        lvBooks.setOnItemClickListener(OnItemClickListener { parent, view, position, id -> // Launch the detail view passing book as an extra
            val intent = Intent(this@BookListActivity, BookDetailActivity::class.java)
            intent.putExtra("book", bookAdapter!!.getItem(position))
            startActivity(intent)
        })
    }

    // Executes an API call to the OpenLibrary search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
     private fun fetchBooks(query: String) {
        // show progressbar
        progress?.visibility = View.VISIBLE
        val client = BookClient()
        client.getBooks(query, object : JsonHttpResponseHandler() {

            override fun onSuccess(
                statusCode: Int,
                headers: Headers?,
                response: JSON
            ) {
                Log.d("Fetch books succeeded","Status: $statusCode")

                try {
                    // hide progressbar
                    progress?.visibility = View.INVISIBLE
                    var jobj = response.jsonObject
                    if (response != null) {
                        // Get the docs json array
                        val docs = jobj.getJSONArray("docs")
                        // Parse json array into array of model objects
                        val books: ArrayList<Book> =
                            Book().fromJson(docs)
                        // Remove all books from the adapter
                        bookAdapter!!.clear()
                        // Load model objects into the adapter
                        for (book in books) {
                            bookAdapter!!.add(book) // add book through the adapter
                        }
                        bookAdapter!!.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    // Invalid JSON format, show appropriate error.
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                // hide progressbar
                progress?.visibility = View.INVISIBLE
                Log.d("Fetch books Failed ","Error: $response")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_book_list, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Fetch the data remotely
                fetchBooks(query!!)
                // Reset SearchView
                searchView.clearFocus()
                searchView.setQuery("", false)
                searchView.setIconified(true)
                searchItem.collapseActionView()
                // Set activity title to search query
                this@BookListActivity.title = query
                return true
            }

            override fun onQueryTextChange(s: String?): Boolean {
                return false
            }
        })
        return true
    }
}