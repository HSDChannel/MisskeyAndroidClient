package jp.panta.misskeyandroidclient.viewmodel

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.TaskExecutor
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.messaging.MessageStreamFilter
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStore
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepository
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.drive.FileUploaderProvider
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryRepository
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.group.GroupRepository
import jp.panta.misskeyandroidclient.model.instance.MetaStore
import jp.panta.misskeyandroidclient.model.messaging.MessageRepository
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryPaginator
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.sw.register.SubscriptionRegistration
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserRepositoryEventToFlow
import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow

interface MiCore{


    @ExperimentalCoroutinesApi
    @FlowPreview
    var messageStreamFilter: MessageStreamFilter

    val loggerFactory: Logger.Factory

    fun getAccounts(): StateFlow<List<Account>>

    fun getCurrentAccount(): StateFlow<Account?>

    @Throws(AccountNotFoundException::class)
    suspend fun getAccount(accountId: Long) : Account

    fun getAccountRepository(): AccountRepository

    fun getUrlPreviewStore(account: Account): UrlPreviewStore?

    fun getNoteDataSource(): NoteDataSource

    fun getNoteRepository(): NoteRepository

    fun getUserDataSource(): UserDataSource

    fun getUserRepository(): UserRepository

    fun getNotificationDataSource(): NotificationDataSource

    fun getNotificationRepository(): NotificationRepository

    fun getUserRepositoryEventToFlow(): UserRepositoryEventToFlow

    fun getGroupDataSource(): GroupDataSource

    fun getGroupRepository(): GroupRepository

    fun getFilePropertyDataSource() : FilePropertyDataSource

    fun getDriveFileRepository() : DriveFileRepository

    fun getSubscriptionRegistration() : SubscriptionRegistration

    suspend fun setCurrentAccount(account: Account)

    fun logoutAccount(account: Account)

    suspend fun addAccount(account: Account)

    fun addPageInCurrentAccount(page: Page)

    fun replaceAllPagesInCurrentAccount(pages: List<Page>)

    fun removePageInCurrentAccount(page: Page)

    fun removeAllPagesInCurrentAccount(pages: List<Page>)


    fun getMisskeyAPI(instanceDomain: String): MisskeyAPI


    fun getMisskeyAPI(account: Account): MisskeyAPI

    fun getEncryption(): Encryption


    suspend fun getChannelAPI(account: Account): ChannelAPI

    fun getNoteCaptureAdapter() : NoteCaptureAPIAdapter

    fun getSocket(account: Account): Socket

    fun getNoteCaptureAPI(account: Account) : NoteCaptureAPI

    fun getCurrentInstanceMeta(): Meta?



    fun getSettingStore(): SettingStore

    fun getGetters(): Getters

    fun getMessageRepository(): MessageRepository

    fun getMessageDataSource(): MessageDataSource

    fun getUnreadMessages(): UnReadMessages


    fun getDraftNoteDAO(): DraftNoteDao

    fun getUnreadNotificationDAO() : UnreadNotificationDAO

    fun getTaskExecutor() : TaskExecutor

    fun getMisskeyAPIProvider(): MisskeyAPIProvider

    fun getMetaStore(): MetaStore

    fun getReactionHistoryPaginatorFactory(): ReactionHistoryPaginator.Factory

    fun getReactionHistoryDataSource(): ReactionHistoryDataSource

    fun getGalleryDataSource(): GalleryDataSource

    fun getGalleryRepository(): GalleryRepository

    fun getFileUploaderProvider(): FileUploaderProvider

    fun getTranslationStore(): NoteTranslationStore

}