package com.example.booksearch

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso


class BookAdapter(
    context: Context?,
    aBooks: ArrayList<Book?>?
) :
    ArrayAdapter<Book?>(context!!, 0, aBooks!!) {
    // View lookup cache
    private class ViewHolder {
        var ivCover: ImageView? = null
        var tvTitle: TextView? = null
        var tvAuthor: TextView? = null
    }

    // Translates a particular `Book` given a position
    // into a relevant row within an AdapterView
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        // Get the data item for this position
        var conView = convertView
        val book = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder // view lookup cache stored in tag
        if (conView == null) {
            viewHolder = ViewHolder()
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            conView = inflater.inflate(R.layout.item_book, parent, false)
            viewHolder.ivCover =
                conView!!.findViewById<View>(R.id.ivBookCover) as ImageView
            viewHolder.tvTitle =
                conView.findViewById<View>(R.id.tvTitle) as TextView
            viewHolder.tvAuthor =
                conView.findViewById<View>(R.id.tvAuthor) as TextView
            conView.tag = viewHolder
        } else {
            viewHolder = conView.tag as ViewHolder
        }
        // Populate the data into the template view using the data object
        viewHolder.tvTitle!!.text = book!!.title
        viewHolder.tvAuthor!!.text = book.author
        Picasso.with(context).load(Uri.parse(book.coverUrl)).error(R.drawable.ic_nocover)
            .into(viewHolder.ivCover)
        // Return the completed view to render on screen
        return conView
    }
}