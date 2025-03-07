package jp.panta.misskeyandroidclient.view.notes.editor

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.notes.PollExpiresAt
import jp.panta.misskeyandroidclient.model.notes.expiresAt
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

class PollDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener{

    private var mViewModel: NoteEditorViewModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel  = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]
        mViewModel = viewModel
        val date = viewModel.poll.value?.expiresAt?.expiresAt()?: Clock.System.now()


        val calendar = Calendar.getInstance()
        calendar.time = Date(date.toEpochMilliseconds())

        return DatePickerDialog(requireActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = mViewModel?.poll?.value?.expiresAt?.expiresAt()?: Clock.System.now()

        val c = Calendar.getInstance()
        c.time = Date(date.toEpochMilliseconds())

        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)

        mViewModel?.state?.value?.let { state ->
            mViewModel?.updateState(
                state.copy(
                    poll = state.poll?.copy(
                        expiresAt = PollExpiresAt.DateAndTime(Instant.fromEpochMilliseconds(c.time.time))
                    )
                )
            )
        }
    }
}