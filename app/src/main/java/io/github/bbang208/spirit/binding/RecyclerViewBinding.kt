package io.github.bbang208.spirit.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

object RecyclerViewBinding {

    @JvmStatic
    @BindingAdapter("submitList")
    fun bindSubmitList(recyclerView: RecyclerView, list: List<Any>?) {
        val adapter = recyclerView.adapter
        if (adapter is androidx.recyclerview.widget.ListAdapter<*, *>) {
            @Suppress("UNCHECKED_CAST")
            (adapter as androidx.recyclerview.widget.ListAdapter<Any, *>).submitList(list)
        }
    }
}
