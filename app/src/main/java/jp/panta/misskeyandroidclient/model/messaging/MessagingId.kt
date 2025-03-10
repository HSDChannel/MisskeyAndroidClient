package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.users.User
import java.io.Serializable
import jp.panta.misskeyandroidclient.model.group.Group as GroupEntity

sealed class MessagingId : Serializable{

    val accountId: Long
        get() {
            return when(this) {
                is Group -> {
                    groupId.accountId
                }
                is Direct -> {
                    userId.accountId
                }
            }
        }

    data class Group(
        val groupId: GroupEntity.Id
    ) : MessagingId()

    data class Direct(
        val userId: User.Id
    ) : MessagingId() {
        constructor(message: Message.Direct, account: Account) : this(message.partnerUserId(account))
    }


}