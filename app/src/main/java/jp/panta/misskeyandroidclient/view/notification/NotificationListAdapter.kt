package jp.panta.misskeyandroidclient.view.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.databinding.ItemNotificationBinding
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi


class NotificationListAdapter @ExperimentalCoroutinesApi constructor(
    diffUtilCallBack: DiffUtil.ItemCallback<NotificationViewData>,
    val notesViewModel: NotesViewModel,
    val notificationViewModel: NotificationViewModel,
    private val lifecycleOwner: LifecycleOwner
    ) : ListAdapter<NotificationViewData, NotificationListAdapter.NotificationHolder>(diffUtilCallBack){
    class NotificationHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.binding.notesViewModel = notesViewModel
        holder.binding.notification = getItem(position)
        holder.binding.simpleNote
        holder.binding.notificationViewModel = notificationViewModel

        val note = getItem(position).noteViewData
        note?: return

        setReactionCounter(note, holder.binding.simpleNote.reactionView)
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding = DataBindingUtil.inflate<ItemNotificationBinding>(LayoutInflater.from(parent.context), R.layout.item_notification, parent, false)
        return NotificationHolder(binding)
    }

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView){

        val reactionList = note.reactionCounts.value?.toList()?: emptyList()
        val adapter = ReactionCountAdapter(note, notesViewModel)
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        val observer = Observer<List<ReactionCount>> {
            adapter.submitList(it.toList())
        }
        note.reactionCounts.observe(lifecycleOwner, observer)

        val exLayoutManager = reactionView.layoutManager
        if(exLayoutManager !is FlexboxLayoutManager){
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionView.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionView.layoutManager = flexBoxLayoutManager
        }

        if(reactionList.isNotEmpty()){
            reactionView.visibility = View.VISIBLE
        }

    }
}