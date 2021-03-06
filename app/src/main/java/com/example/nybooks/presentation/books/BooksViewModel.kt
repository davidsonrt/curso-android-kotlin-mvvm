package com.example.nybooks.presentation.books

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nybooks.R
import com.example.nybooks.data.BooksResult
import com.example.nybooks.data.model.Book
import com.example.nybooks.data.repository.BooksApiDataSource
import com.example.nybooks.data.repository.BooksRepository
import kotlinx.coroutines.launch

class BooksViewModel(private val dataSource: BooksApiDataSource) : ViewModel() {

    val booksLiveData: MutableLiveData<List<Book>> = MutableLiveData()
    val viewFlipperLiveData: MutableLiveData<Pair<Int, Int?>> = MutableLiveData()

    fun getBooks() = viewModelScope.launch {
        when (val result = dataSource.getBooks()) {
            is BooksResult.Success -> {
                booksLiveData.value = result.books
                viewFlipperLiveData.value = Pair(VIEW_FLIPPER_BOOKS, null)
            }
            is BooksResult.ApiError -> {
                if (result.statusCode == 401) {
                    viewFlipperLiveData.value =
                        Pair(VIEW_FLIPPER_ERROR, R.string.books_error_401)
                } else {
                    viewFlipperLiveData.value =
                        Pair(VIEW_FLIPPER_ERROR, R.string.books_error_400_generic)
                }
            }
            is BooksResult.ServerError -> viewFlipperLiveData.value =
                Pair(VIEW_FLIPPER_ERROR, R.string.books_error_500_generic)
        }
    }

    class ViewModelFactory(private val dataSource: BooksApiDataSource) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BooksViewModel::class.java)) {
                return BooksViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val VIEW_FLIPPER_BOOKS = 1
        private const val VIEW_FLIPPER_ERROR = 2
    }
}