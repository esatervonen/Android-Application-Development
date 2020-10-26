package com.example.booksearch

import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class BookClient {
    private val client = AsyncHttpClient()
    private fun getApiUrl(relativeUrl: String): String {
        return API_BASE_URL + relativeUrl
    }

    // Method for accessing the search API
    fun getBooks(query: String, handler: JsonHttpResponseHandler) {
        try {
            val url = getApiUrl("search.json?q=")
            client.get(url + URLEncoder.encode(query, "utf-8"), handler)/*[url + URLEncoder.encode(query, "utf-8"), handler]*/
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun getExtraBookDetails(
        openLibraryId: String,
        handler: JsonHttpResponseHandler?
    ) {
        val url = getApiUrl("books/")
        client["$url$openLibraryId.json", handler]
    }

    companion object {
        private const val API_BASE_URL = "http://openlibrary.org/"
    }

/*    init {
        client = AsyncHttpClient()
    }*/
}