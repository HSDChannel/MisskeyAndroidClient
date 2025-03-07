package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityReactionSettingBinding
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.settings.ReactionPickerType
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.view.reaction.ReactionAutoCompleteArrayAdapter
import jp.panta.misskeyandroidclient.view.reaction.ReactionChoicesAdapter
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator
import jp.panta.misskeyandroidclient.viewmodel.setting.reaction.ReactionPickerSettingViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.IllegalArgumentException

class ReactionSettingActivity : AppCompatActivity() {

    private lateinit var mCustomEmojiDecorator: CustomEmojiDecorator
    private var mEmojis: List<Emoji> = emptyList()
    private var mReactionPickerSettingViewModel: ReactionPickerSettingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        //setContentView(R.layout.activity_reaction_setting)
        val binding = DataBindingUtil.setContentView<ActivityReactionSettingBinding>(this, R.layout.activity_reaction_setting)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.reactionSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val miApplication = applicationContext as MiApplication
        mCustomEmojiDecorator = CustomEmojiDecorator()
        val flexBoxLayoutManager = FlexboxLayoutManager(this)
        flexBoxLayoutManager.flexDirection = FlexDirection.ROW
        flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
        flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        binding.reactionSettingListView.layoutManager = flexBoxLayoutManager

        val touchHelper = ItemTouchHelper(ItemTouchCallback())
        touchHelper.attachToRecyclerView(binding.reactionSettingListView)
        binding.reactionSettingListView.addItemDecoration(touchHelper)
        miApplication.getCurrentAccount().filterNotNull().onEach {
            mEmojis = miApplication.getCurrentInstanceMeta()?.emojis?: emptyList()
            mReactionPickerSettingViewModel = ViewModelProvider(this, ReactionPickerSettingViewModel.Factory(it, miApplication))[ReactionPickerSettingViewModel::class.java]
            binding.reactionPickerSettingViewModel = mReactionPickerSettingViewModel!!
            val reactionsAdapter = ReactionChoicesAdapter(mReactionPickerSettingViewModel!!)
            binding.reactionSettingListView.adapter = reactionsAdapter

            mReactionPickerSettingViewModel?.reactionSettingsList?.observe(this, { list ->
                reactionsAdapter.submitList(list.map{ rus ->
                    rus.reaction
                })
            })

            mReactionPickerSettingViewModel?.reactionSelectEvent?.observe(this, { rus ->
                showConfirmDeleteReactionDialog(rus.reaction)
            })

            binding.reactionPickerType.setSelection(mReactionPickerSettingViewModel?.reactionPickerType?.ordinal?: 0)


        }.launchIn(lifecycleScope)

        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?: emptyList()
        val reactionAutoCompleteArrayAdapter = ReactionAutoCompleteArrayAdapter(emojis, this)
        binding.reactionSettingField.setAdapter(reactionAutoCompleteArrayAdapter)
        binding.reactionSettingField.setOnItemClickListener { _, _, position, _ ->
            val emoji = reactionAutoCompleteArrayAdapter.suggestions[position]
            mReactionPickerSettingViewModel?.addReaction(emoji)
            binding.reactionSettingField.setText("")
        }
        binding.reactionSettingField.setOnEditorActionListener { textView, _, keyEvent ->
            val text = textView.text
            if(keyEvent?.keyCode == KeyEvent.KEYCODE_ENTER && text != null){
                if(keyEvent.action == KeyEvent.ACTION_UP){
                    if(text.isNotBlank()){
                        mReactionPickerSettingViewModel?.addReaction(text.toString())
                        binding.reactionSettingField.setText("")

                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }


        binding.reactionPickerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val pickerType = when(p2){
                    0 -> ReactionPickerType.LIST
                    1 -> ReactionPickerType.SIMPLE
                    else -> throw IllegalArgumentException("error")
                }
                mReactionPickerSettingViewModel?.setReactionPickerType(pickerType)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        /*binding.reactionPickerType.setOnItemClickListener { _, _, position,_ ->
            mReactionPickerSettingViewModel?.reactionPickerType?.value = ReactionPickerType.values()[position]
        }*/



    }

    override fun onStop(){
        super.onStop()
        mReactionPickerSettingViewModel?.save()
    }

    inner class ItemTouchCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT, ItemTouchHelper.ACTION_STATE_IDLE){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            val exList = mReactionPickerSettingViewModel?.reactionSettingsList?.value?: emptyList()
            val list = ArrayList(exList)
            val d = list.removeAt(from)
            list.add(to, d)
            //mReactionPickerSettingViewModel?.reactionSettingsList?.postValue(list)
            mReactionPickerSettingViewModel?.putSortedList(list)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    }


    private fun showConfirmDeleteReactionDialog(reaction: String){
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.confirm_delete_reaction))
            .setMessage(getString(R.string.delete_reaction) + " $reaction")
            .setNegativeButton(android.R.string.cancel) { _, _->

            }
            .setPositiveButton(android.R.string.ok){ _, _ ->
                mReactionPickerSettingViewModel?.deleteReaction(reaction)
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
