package com.afares.emergency.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.afares.emergency.data.model.Request
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirestorePagingSource (
    private val queryProductsByName: Query
) : PagingSource<QuerySnapshot, Request>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Request>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Request> {
        return try {
            val currentPage = params.key ?: queryProductsByName.get().await()
            val lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryProductsByName.startAfter(lastVisibleProduct).get().await()
            LoadResult.Page(
                data = currentPage.toObjects(Request::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}