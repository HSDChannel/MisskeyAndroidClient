package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.messaging.Message
import java.lang.IllegalArgumentException

class MessageViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val miApplication: MiApplication,
    private val messageHistory: Message
): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == MessageViewModel::class.java){
            val misskeyAPI = miApplication.misskeyAPIService!!
            return MessageViewModel(connectionInstance, misskeyAPI, messageHistory) as T
        }
        throw IllegalArgumentException("use MessageViewModel::class.java")
    }
}