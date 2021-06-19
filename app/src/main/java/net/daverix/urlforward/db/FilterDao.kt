package net.daverix.urlforward.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.daverix.urlforward.LinkFilter

interface FilterDao {
    suspend fun insert(filter: LinkFilter)

    suspend fun update(filter: LinkFilter)

    suspend fun delete(filterId: Long)

    fun queryFilters(): Flow<List<LinkFilter>>

    suspend fun queryFilter(filterId: Long): LinkFilter?
}

@ExperimentalCoroutinesApi
class DefaultFilterDao(context: Context) : FilterDao {
    private val updated = MutableSharedFlow<Unit>()
    private val dbHelper = UrlForwardDatabaseHelper(context)

    override suspend fun insert(filter: LinkFilter) {
        withContext(Dispatchers.IO) {
            val values = getValues(filter)
            dbHelper.writableTransaction {
                insert(UrlForwardDatabaseHelper.TABLE_FILTER, null, values)
            }
        }
        updated.emit(Unit)
    }

    override suspend fun update(filter: LinkFilter) {
        withContext(Dispatchers.IO) {
            dbHelper.writableTransaction {
                update(
                    UrlForwardDatabaseHelper.TABLE_FILTER,
                    getValues(filter),
                    "${BaseColumns._ID} = ?",
                    arrayOf(filter.id.toString())
                )
            }
        }
        updated.emit(Unit)
    }

    override suspend fun delete(filterId: Long) {
        withContext(Dispatchers.IO) {
            dbHelper.writableTransaction {
                delete(UrlForwardDatabaseHelper.TABLE_FILTER,
                    "${BaseColumns._ID} = ?",
                    arrayOf(filterId.toString())
                )
            }
        }
        updated.emit(Unit)
    }

    override fun queryFilters(): Flow<List<LinkFilter>> = flow {
        emit(queryAllFilters())

        emitAll(updated.map {
            queryAllFilters()
        })
    }.flowOn(Dispatchers.IO)

    private val columns = arrayOf(
        BaseColumns._ID,
        UrlForwarderContract.UrlFilterColumns.TITLE,
        UrlForwarderContract.UrlFilterColumns.FILTER,
        UrlForwarderContract.UrlFilterColumns.REPLACE_TEXT,
        UrlForwarderContract.UrlFilterColumns.CREATED,
        UrlForwarderContract.UrlFilterColumns.UPDATED,
        UrlForwarderContract.UrlFilterColumns.SKIP_ENCODE,
        UrlForwarderContract.UrlFilterColumns.REPLACE_SUBJECT
    )

    override suspend fun queryFilter(filterId: Long): LinkFilter? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase?.use {
            it.query(
                UrlForwardDatabaseHelper.TABLE_FILTER,
                columns,
                "${BaseColumns._ID} = ?",
                arrayOf(filterId.toString()),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.toFilter() else null
            }
        }
    }

    private fun queryAllFilters(): List<LinkFilter> = dbHelper.readableDatabase?.query(
        UrlForwardDatabaseHelper.TABLE_FILTER,
        columns,
        null,
        null,
        null,
        null,
        null
    )?.use { cursor ->
        val items = mutableListOf<LinkFilter>()
        while (cursor.moveToNext()) {
            items += cursor.toFilter()
        }
        items
    } ?: emptyList()

    private fun Cursor.toFilter() = LinkFilter(
        id = getLong(0),
        title = getString(1),
        filterUrl = getString(2),
        replaceText = getString(3),
        created = getLong(4),
        updated = getLong(5),
        encoded = getShort(6).toInt() != 1,
        replaceSubject = getString(7)
    )

    private fun getValues(filter: LinkFilter): ContentValues = ContentValues().apply {
        put(UrlForwarderContract.UrlFilterColumns.CREATED, filter.created)
        put(UrlForwarderContract.UrlFilterColumns.UPDATED, filter.updated)
        put(UrlForwarderContract.UrlFilterColumns.TITLE, filter.title)
        put(UrlForwarderContract.UrlFilterColumns.FILTER, filter.filterUrl)
        put(UrlForwarderContract.UrlFilterColumns.REPLACE_TEXT, filter.replaceText)
        put(UrlForwarderContract.UrlFilterColumns.SKIP_ENCODE, !filter.encoded)
        put(UrlForwarderContract.UrlFilterColumns.REPLACE_SUBJECT, filter.replaceSubject)
    }
}