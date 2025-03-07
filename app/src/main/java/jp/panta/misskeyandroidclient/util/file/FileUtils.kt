package jp.panta.misskeyandroidclient.util.file

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.file.File
import java.lang.IllegalArgumentException

fun Uri.toAppFile(context: Context): AppFile.Local {
    val fileName = try{
        context.getFileName(this)
    }catch(e: Exception){
        Log.d("FileUtils", "ファイル名の取得に失敗しました", e)
        null
    }

    val mimeType = context.contentResolver.getType(this)

    val isMedia = mimeType?.startsWith("image")?: false || mimeType?.startsWith("video")?: false
    val thumbnail = if(isMedia) this.toString() else null
    return AppFile.Local(
        fileName?: "name none",
        path = this.toString(),
        type  = mimeType ?: "",
        thumbnailUrl = thumbnail,
        isSensitive = false,
        folderId = null
    )
}
fun Uri.toFile(context: Context): File{
    val fileName = try{
        context.getFileName(this)
    }catch(e: Exception){
        Log.d("FileUtils", "ファイル名の取得に失敗しました", e)
        null
    }

    val mimeType = context.contentResolver.getType(this)

    val isMedia = mimeType?.startsWith("image")?: false || mimeType?.startsWith("video")?: false
    val thumbnail = if(isMedia) this.toString() else null
    return File(
        fileName?: "name none",
        this.toString(),
        type  = mimeType,
        remoteFileId = null,
        localFileId = null,
        thumbnailUrl = thumbnail,
        isSensitive = null
    )
}

private fun Context.getFileName(uri: Uri) : String{
    return when(uri.scheme){
        "content" ->{
            this.contentResolver
                .query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use{
                    if(it.moveToFirst()){
                        val index = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        if(index != -1) {
                            it.getString(index)
                        }else{
                            null
                        }
                    }else{
                        null
                    }
                }?: throw IllegalArgumentException("ファイル名の取得に失敗しました")
        }
        "file" ->{
            java.io.File(uri.path!!).name
        }
        else -> throw IllegalArgumentException("scheme不明")
    }
}