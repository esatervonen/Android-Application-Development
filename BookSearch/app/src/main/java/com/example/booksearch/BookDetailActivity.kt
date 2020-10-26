package com.example.booksearch

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.squareup.picasso.Picasso
import okhttp3.Headers
import okhttp3.internal.http2.Header
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BookDetailActivity : AppCompatActivity() {
    private var ivBookCover: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvAuthor: TextView? = null
    private var tvPublisher: TextView? = null
    private var tvPageCount: TextView? = null
    private var client: BookClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)
        // Fetch views
        ivBookCover = findViewById<View>(R.id.ivBookCover) as ImageView
        tvTitle = findViewById<View>(R.id.tvTitle) as TextView
        tvAuthor = findViewById<View>(R.id.tvAuthor) as TextView
        tvPublisher = findViewById<View>(R.id.tvPublisher) as TextView
        tvPageCount = findViewById<View>(R.id.tvPageCount) as TextView
        // Use the book to populate the data into our views
        val book =
            intent.getSerializableExtra("book") as Book
        loadBook(book)
    }

    // Populate data for the book
    private fun loadBook(book: Book) {
        //change activity title
        this.title = book.title
        // Populate data
        Picasso.with(this).load(Uri.parse(book.largeCoverUrl)).error(R.drawable.ic_nocover)
            .into(ivBookCover)
        tvTitle!!.text = book.title
        tvAuthor!!.text = book.author
        // fetch extra book data from books API
        client = BookClient()
        client!!.getExtraBookDetails(book.openLibraryId!!, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Headers?,
                response: JSON
            ) {
                try {
                    val res = response.jsonObject
                    if (res.has("publishers")) {
                        // display comma separated list of publishers
                        val publisher = res.getJSONArray("publishers")
                        val numPublishers = publisher.length()
                        val publishers =
                            arrayOfNulls<String>(numPublishers)
                        for (i in 0 until numPublishers) {
                            publishers[i] = publisher.getString(i)
                        }
                        tvPublisher!!.text = TextUtils.join(", ", publishers)
                    }
                    if (res.has("number_of_pages")) {
                        tvPageCount!!.text =
                            Integer.toString(res.getInt("number_of_pages")) + " pages"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.d("Get details Failed","Error: $response")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_book_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id: Int = item.getItemId()
        if (id == R.id.action_share) {
            setShareIntent();
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setShareIntent() {
        val ivImage =
            findViewById<ImageView>(R.id.ivBookCover)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        // Get access to the URI for the bitmap
        val bmpUri = getLocalBitmapUri(ivImage)
        // Construct a ShareIntent with link to image
        val shareIntent = Intent()
        // Construct a ShareIntent with link to image
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_TEXT, tvTitle.text as String)
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
        // Launch share menu
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }

    // Returns the URI path to the Bitmap displayed in cover imageview
    private fun getLocalBitmapUri(imageView: ImageView): Uri? {
        // Extract Bitmap from ImageView drawable
        val drawable = imageView.drawable
//        var bmp: Bitmap? = null
        val bmp = if (drawable is BitmapDrawable) {
            (imageView.drawable as BitmapDrawable).bitmap
        } else {
            return null
        }
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png"
            )
            file.parentFile?.mkdirs()
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
            Log.d("file ","$file")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("File save Failed","Error: $e")
        }
        return bmpUri
    }
}
