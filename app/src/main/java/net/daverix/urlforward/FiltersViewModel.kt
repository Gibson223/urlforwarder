/*
    UrlForwarder makes it possible to use bookmarklets on Android
    Copyright (C) 2016 David Laurell

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daverix.urlforward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.daverix.urlforward.db.FilterDao
import javax.inject.Inject

sealed class FiltersState {
    object Loading : FiltersState()
    data class LoadedFilters(val filters: List<LinkFilter>) : FiltersState()
}

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val filterDao: FilterDao
): ViewModel() {
    private val _state: MutableStateFlow<FiltersState> = MutableStateFlow(FiltersState.Loading)
    val state: StateFlow<FiltersState> = _state

    init {
        viewModelScope.launch {
            _state.emitAll(
                filterDao.queryFilters().map { FiltersState.LoadedFilters(it) }
            )
        }
    }
}
