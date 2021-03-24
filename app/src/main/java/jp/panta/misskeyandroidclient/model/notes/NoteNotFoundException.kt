package jp.panta.misskeyandroidclient.model.notes

class NoteNotFoundException(noteId: Note.Id, msg: String = "ノートを見つけることができませんでした: $noteId") : Exception(msg)