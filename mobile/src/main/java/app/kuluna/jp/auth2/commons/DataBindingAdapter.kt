package app.kuluna.jp.auth2.commons

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates

abstract class DataBindingAdapter<E, T : ViewDataBinding>(val context: Context, @LayoutRes private val layoutId: Int) : RecyclerView.Adapter<DataBindingAdapter.DataBindingViewHolder<T>>() {
    var items: List<E> by Delegates.observable(emptyList()) { _, old, new -> diff(old, new).detect().dispatchUpdatesTo(this) }

    private val diff: (old: List<E>, new: List<E>) -> DataDiff<E> = { old, new ->
        DataDiff(old, new, false, { oldE, newE -> areItemsTheSame(oldE, newE) })
    }

    open val detectMove = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder<T> {
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return DataBindingViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DataBindingViewHolder<T>, position: Int) {
        val item = items[holder.adapterPosition]
        bind(holder, item)
        holder.binding.executePendingBindings()
    }

    abstract fun bind(holder: DataBindingViewHolder<T>, item: E)

    open fun areItemsTheSame(old: E, new: E): Boolean = (old == new)

    class DataBindingViewHolder<out T : ViewDataBinding>(view: View) : RecyclerView.ViewHolder(view) {
        val binding: T = DataBindingUtil.bind(view)!!
    }
}

open class DataDiff<E>(private val old: List<E>, private val new: List<E>, private val detectMove: Boolean = false, private val itemSame: (E, E) -> Boolean) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = itemSame.invoke(old[oldItemPosition], new[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition] == new[newItemPosition]

    fun detect(): DiffUtil.DiffResult = DiffUtil.calculateDiff(this, detectMove)
}
