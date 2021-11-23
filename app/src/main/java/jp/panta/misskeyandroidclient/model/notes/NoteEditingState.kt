package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant
import java.util.*

data class NoteEditingState(
    val author: Account? = null,
    val visibility: Visibility = Visibility.Public(false),
    val text: String? = null,
    val cw: String? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val files: List<File> = emptyList(),
    val poll: PollEditingState? = null,
    val viaMobile: Boolean = true,
    val draftNoteId: Long? = null,
) {

    val hasCw: Boolean
        get() = cw != null

    val totalFilesCount: Int
        get() = this.files.size

    val isSpecified: Boolean
        get() = this.visibility is Visibility.Specified

    val isLocalOnly: Boolean
        get() = this.visibility.isLocalOnly()


    fun checkValidate(textMaxLength: Int = 3000) : Boolean {
        return !(this.files.isEmpty()
                && this.files.size > 4
                && this.renoteId == null
                && this.text.isNullOrBlank()
                && (this.text?.codePointCount(0, this.text.length) ?: 0) > textMaxLength
                && this.poll?.checkValidate() == true)
    }

    fun changeText(text: String) : NoteEditingState {
        return this.copy(
            text = text
        )
    }

    fun changeCw(text: String) : NoteEditingState {
        return this.copy(
            cw = text
        )
    }

    fun addFile(file: File) : NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                add(file)
            }
        )
    }

    fun removeFile(file: File) : NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                remove(file)
            }
        )
    }

    fun setAccount(account: Account?) : NoteEditingState{
        if(author == null) {
            return this.copy(
                author = account
            )
        }
        if(account == null) {
            throw IllegalArgumentException("現在の状態に未指定のAccountを指定することはできません")
        }
        if(files.any { it.remoteFileId != null }) {
            throw IllegalArgumentException("リモートファイル指定時にアカウントを変更することはできません(files)。")
        }
        if(!(replyId == null || author.instanceDomain == account.instanceDomain)) {
            throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(replyId)。")
        }

        if(!(renoteId == null || author.instanceDomain == account.instanceDomain)) {
            throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(renoteId)。")
        }

        if(visibility is Visibility.Specified
            && (visibility.visibleUserIds.isNotEmpty()
                    || author.instanceDomain == account.instanceDomain)
        ) {
            throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(visibility)。")
        }

        return this.copy(
            author = account,
            files = files,
            replyId = replyId?.copy(accountId = account.accountId),
            renoteId = renoteId?.copy(accountId = account.accountId),
            visibility = if(visibility is Visibility.Specified) {
                visibility.copy(visibleUserIds = visibility.visibleUserIds.map {
                    it.copy(accountId = account.accountId)
                })
            }else{
                visibility
            }
        )

    }

    fun removePollChoice(id: UUID) : NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.filterNot { choice ->
                        choice.id == id
                    }
                )
            }
        )
    }

    fun addPollChoice() : NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.toMutableList().also { list ->
                        list.add(
                            PollChoiceState("")
                        )
                    }
                )
            }
        )
    }

    fun updatePollChoice(id: UUID, text: String) : NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.map { choice ->
                        if(choice.id == id) {
                            choice.copy(
                                text = text
                            )
                        }else{
                            choice
                        }
                    }
                )
            }
        )
    }

    fun toggleCw() : NoteEditingState {
        return this.copy(
            cw = if(this.hasCw) null else ""
        )
    }

    fun togglePoll() : NoteEditingState {
        return this.copy(
            poll = if(poll == null) PollEditingState(emptyList(), false, null) else null
        )
    }

    fun clear() : NoteEditingState {
        return NoteEditingState(author = this.author)
    }

}


data class PollEditingState(
    val choices: List<PollChoiceState>,
    val multiple: Boolean,
    val expiresAt: Instant?
) {
    fun checkValidate() : Boolean {
        return choices.all {
            it.text.isNotBlank()
        }
    }
}

data class PollChoiceState(
    val text: String,
    val id: UUID = UUID.randomUUID()
)

fun DraftNote.toNoteEditingState() : NoteEditingState{
    return NoteEditingState(
        text = this.text,
        cw = this.cw,
        draftNoteId = this.draftNoteId,
        visibility = Visibility(type = this.visibility, isLocalOnly = this.localOnly ?: false, visibleUserIds = this.visibleUserIds?.map {
            User.Id(accountId = accountId, id = it)
        }),
        viaMobile = this.viaMobile ?: true,
        poll = this.draftPoll?.let {
            PollEditingState(
                choices = it.choices.map { choice ->
                    PollChoiceState(choice)
                },
                expiresAt = it.expiresAt?.let { ex ->
                    Instant.fromEpochMilliseconds(ex)
                },
                multiple = it.multiple
            )
        },
        replyId = this.replyId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        renoteId = this.renoteId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        files = this.files ?: emptyList()
    )
}

fun PollEditingState.toCreatePoll() : CreatePoll {
    return CreatePoll(
        choices = this.choices.map {
            it.text
        },
        multiple = multiple,
        expiresAt = expiresAt?.toEpochMilliseconds()
    )
}

fun PollEditingState.toDraftPoll() : DraftPoll {
    return DraftPoll(
        choices = this.choices.map {
            it.text
        },
        multiple = multiple,
        expiresAt = expiresAt?.toEpochMilliseconds()
    )
}