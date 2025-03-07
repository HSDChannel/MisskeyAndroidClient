package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class DriveViewModelFactory(
    private val driveSelectableMode: DriveSelectableMode?
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == DriveViewModel::class.java){
            return DriveViewModel(driveSelectableMode) as T
        }
        throw IllegalArgumentException("DriveViewModel::class.javaを指定してください")
    }
}