package net.daverix.urlforward

import kotlinx.coroutines.flow.MutableStateFlow

class DefaultEditableFields(
    private val state: MutableStateFlow<SaveFilterState>
) : EditableFields {
    override fun updateTitle(title: String) {
        val currentState = state.value
        if(currentState is SaveFilterState.Editing) {
            state.value = currentState.copy(
                filter = currentState.filter.copy(
                    title = title
                )
            )
        }
    }

    override fun updateFilterUrl(url: String) {
        val currentState = state.value
        if(currentState is SaveFilterState.Editing) {
            state.value = currentState.copy(
                filter = currentState.filter.copy(
                    filterUrl = url
                )
            )
        }
    }

    override fun updateReplaceUrl(url: String) {
        val currentState = state.value
        if(currentState is SaveFilterState.Editing) {
            state.value = currentState.copy(
                filter = currentState.filter.copy(
                    replaceText = url
                )
            )
        }
    }

    override fun updateReplaceSubject(subject: String) {
        val currentState = state.value
        if(currentState is SaveFilterState.Editing) {
            state.value = currentState.copy(
                filter = currentState.filter.copy(
                    replaceSubject = subject
                )
            )
        }
    }

    override fun updateEncoded(encoded: Boolean) {
        val currentState = state.value
        if(currentState is SaveFilterState.Editing) {
            state.value = currentState.copy(
                filter = currentState.filter.copy(
                    encoded = encoded
                )
            )
        }
    }
}