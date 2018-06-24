package com.sethchhim.kuboo_client.data.task.recent

import android.arch.lifecycle.MutableLiveData
import com.sethchhim.kuboo_client.Extensions.recentListToBookList
import com.sethchhim.kuboo_client.data.AppDatabaseDao
import com.sethchhim.kuboo_client.data.model.Recent
import com.sethchhim.kuboo_client.data.task.base.Task_LocalBase
import com.sethchhim.kuboo_remote.model.Book
import com.sethchhim.kuboo_remote.model.Login
import timber.log.Timber

class Task_RecentDelete(login: Login, book: Book) : Task_LocalBase() {

    internal val liveData = MutableLiveData<List<Book>>()

    init {
        executors.diskIO.execute {
            try {
                appDatabaseDao.deleteAllThatMatch(book)
                Timber.d("Recent deleteDownload: ${book.title}")
                val result = appDatabaseDao.getAllBookRecent()
                val resultFilteredByActiveServer = mutableListOf<Recent>().apply {
                    result.forEach { if (it.server == login.server) add(it) }
                }
                val resultSortedByDescending = resultFilteredByActiveServer.sortedByDescending { it.timeAccessed }
                executors.mainThread.execute { liveData.value = resultSortedByDescending.recentListToBookList() }
            } catch (e: Exception) {
                Timber.e("message[${e.message}] title[${book.title}]")
                executors.mainThread.execute { liveData.value = null }
            }
        }
    }

    private fun AppDatabaseDao.deleteAllThatMatch(book: Book) {
        getAllBookRecent().forEach {
            val isMatch = it.id == book.id && it.title == book.title && it.server == book.server
            if (isMatch) appDatabaseDao.deleteRecent(it)
        }
    }

}


