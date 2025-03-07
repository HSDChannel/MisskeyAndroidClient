package jp.panta.misskeyandroidclient.viewmodel.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.model.IllegalVersionException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.gallery.*
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GalleryPostsViewModel(
    val pageable: Pageable.Gallery,
    private var accountId: Long?,
    galleryDataSource: GalleryDataSource,
    galleryRepository: GalleryRepository,
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    miCore: MiCore
) : ViewModel(), GalleryToggleLikeOrUnlike{

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val pageable: Pageable.Gallery,
        val accountId: Long?,
        val miCore: MiCore
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryPostsViewModel(
                pageable,
                accountId,
                miCore.getGalleryDataSource(),
                miCore.getGalleryRepository(),
                miCore.getAccountRepository(),
                miCore.getMisskeyAPIProvider(),
                miCore
            ) as T
        }
    }

    private val galleryPostsStore = miCore.createGalleryPostsStore(pageable, this::getAccount)

    private val _galleryPosts = MutableStateFlow<PageableState<List<GalleryPostState>>>(PageableState.Fixed(StateContent.NotExist()))
    val galleryPosts: StateFlow<PageableState<List<GalleryPostState>>> = _galleryPosts
    val lock = Mutex()

    private val galleryPostSendFavoriteStore = GalleryPostSendFavoriteStore(galleryRepository)

    private val _error = MutableSharedFlow<Throwable>(extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error: Flow<Throwable> = _error

    private val currentIndexes = MutableStateFlow<Map<GalleryPost.Id, Int>>(emptyMap())


    init {
        galleryDataSource.events().mapNotNull {
            it as? GalleryDataSource.Event.Deleted
        }.onEach { ev ->
            lock.withLock {
                val state = _galleryPosts.value
                val content = state.content
                if(content is StateContent.Exist) {
                    _galleryPosts.value = PageableState.Fixed(
                        content.copy(
                            content.rawContent.filterNot {
                                it.galleryPost.id == ev.galleryPostId
                            }
                        )
                    )
                }
            }
        }.launchIn(viewModelScope + Dispatchers.IO)
        loadInit()
    }

    init {

        // FIXME: 毎回Stateオブジェクトを生成してしまうのでユーザーの捜査情報が初期化されてしまうので何とかする
        val relations = galleryPostsStore.state.combine(galleryDataSource.state) { it, _ ->
          
            it.convert {
                runBlocking {
                    it.map { id ->
                        async {
                            runCatching {
                                miCore.getGalleryDataSource().find(id)
                            }.getOrNull()
                        }

                    }.awaitAll().filterNotNull().map { post ->
                        post to miCore.getFilePropertyDataSource().findIn(post.fileIds)
                    }.filter { postWithFiles ->
                        postWithFiles.second.isNotEmpty()
                    }.map { postWithFiles ->
                        GalleryPostRelation(
                            postWithFiles.first,
                            miCore.getFilePropertyDataSource().findIn(postWithFiles.first.fileIds),
                            miCore.getUserRepository().find(postWithFiles.first.userId, false)
                        )
                    }
                }
            }
        }

        combine(relations, currentIndexes, galleryPostSendFavoriteStore.state) { posts, indexes, sends ->
            posts.convert {
                it.map { relation ->
                    GalleryPostState(
                        galleryPost = relation.galleryPost,
                        files = relation.files,
                        user = relation.user,
                        currentIndex = indexes[relation.galleryPost.id] ?: 0,
                        isFavoriteSending = sends.contains(relation.galleryPost.id)
                    )
                }
            }
        }.onEach {
            this._galleryPosts.value = it
            if(it is PageableState.Error) {
                _error.emit(it.throwable)
            }
        }.launchIn(viewModelScope + Dispatchers.IO)


    }

    fun loadInit() {
       if(galleryPostsStore.mutex.isLocked) {
           return
       }
        viewModelScope.launch(Dispatchers.IO) {
            galleryPostsStore.clear()
            galleryPostsStore.loadPrevious()
        }
    }

    fun loadFuture() {
        if(galleryPostsStore.mutex.isLocked) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            galleryPostsStore.loadFuture()
        }
    }

    fun loadPrevious() {
        if(galleryPostsStore.mutex.isLocked) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            galleryPostsStore.loadPrevious()
        }
    }



    fun changeIndex(id: GalleryPost.Id, index: Int) {
        currentIndexes.value = currentIndexes.value.toMutableMap().also {
            it[id] = index
        }
    }

    fun toggleFavorite(galleryId: GalleryPost.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            toggle(galleryId)
        }
    }

    override suspend fun toggle(galleryId: GalleryPost.Id) {
        runCatching {
            galleryPostSendFavoriteStore.toggleFavorite(galleryId)
        }.onFailure {
            _error.emit(it)
        }
    }

    suspend fun getAccount(): Account {
        return accountId?.let {
            accountRepository.get(it)
        }?: accountRepository.getCurrentAccount().also {
            accountId = it.accountId
        }
    }

    private fun Account.getMisskeyAPI(): MisskeyAPIV1275 {
        return misskeyAPIProvider.get(this.instanceDomain) as? MisskeyAPIV1275
            ?: throw IllegalVersionException()
    }






}

