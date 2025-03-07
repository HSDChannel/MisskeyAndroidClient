package jp.panta.misskeyandroidclient.model.notes


import jp.panta.misskeyandroidclient.model.ITask
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll

/**
 * @param noExtractEmojis 本文からカスタム絵文字を展開しないか否か
 * @param noExtractMentions 本文からメンションを展開しないか否か
 * @param noExtractHashtags 本文からハッシュタグを展開しないか否か
 */
data class CreateNote(
    val author: Account,
    val visibility: Visibility,
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var files: List<AppFile>? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val poll: CreatePoll? = null,
    val draftNoteId: Long? = null


)

class CreateNoteTask(
    val noteRepository: NoteRepository,
    val createNote: CreateNote
) : ITask<Note> {
    override suspend fun execute(): Note {
        return noteRepository.create(createNote)
    }
}

fun CreateNote.task(noteRepository: NoteRepository) : CreateNoteTask{
    return CreateNoteTask(noteRepository, this)
}