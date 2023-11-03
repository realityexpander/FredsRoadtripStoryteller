package data.util

import kotlinx.serialization.Serializable

@Serializable
sealed class LoadingState<out TData>  {
    @Serializable
    data object Loading : LoadingState<Nothing>()
    class Loaded<out TData>(val data: TData) : LoadingState<TData>()
    class Error(val errorMessage: String) : LoadingState<Nothing>()
    data object Finished: LoadingState<Nothing>()
}
