package jp.panta.misskeyandroidclient.view.notes.editor

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.notes.PollExpiresAt
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import kotlinx.datetime.Instant
import java.util.*

class PollTimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener{

    private var mViewModel: NoteEditorViewModel? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]
        mViewModel = viewModel

        val date = viewModel.poll.value?.expiresAt?.asDate()?: Date()
        val c = Calendar.getInstance()
        c.time = date
        return TimePickerDialog(requireActivity(), this, c[Calendar.HOUR], c[Calendar.MINUTE], true)

    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val date = mViewModel?.poll?.value?.expiresAt?.asDate() ?: Date()
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.HOUR] = p1
        c[Calendar.MINUTE] = p2
        mViewModel?.let { viewModel ->
            viewModel.updateState(
                viewModel.state.value.copy(
                    poll = viewModel.state.value.poll?.copy(
                        expiresAt = PollExpiresAt.DateAndTime(
                            Instant.fromEpochMilliseconds(c.time.time)
                        )
                    )
                )
            )
        }

    }
}