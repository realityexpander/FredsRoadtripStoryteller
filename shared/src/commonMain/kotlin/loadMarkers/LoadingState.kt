package loadMarkers

sealed class LoadingState<out TData>  {
    data object Loading : LoadingState<Nothing>()
    class Loaded<out TData>(val data: TData) : LoadingState<TData>()
    class Error(val message: String) : LoadingState<Nothing>()
    data object Idle: LoadingState<Nothing>()
}
