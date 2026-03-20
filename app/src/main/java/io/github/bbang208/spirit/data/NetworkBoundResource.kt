package io.github.bbang208.spirit.data

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        fetchFromNetwork()
    }

    private fun fetchFromNetwork() {
        val apiResponse = createCall()
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            when (response) {
                is ApiSuccessResponse -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val processed = processResponse(response)
                        CoroutineScope(Dispatchers.Main).launch {
                            result.value = Resource.success(processResult(processed))
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        result.value = Resource.success(null)
                    }
                }
                is ApiErrorResponse -> {
                    onFetchFailed()
                    result.value = Resource.error(response.errorMessage)
                }
            }
        }
    }

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>): RequestType {
        return response.body
    }

    @MainThread
    protected open fun processResult(item: RequestType): ResultType? {
        @Suppress("UNCHECKED_CAST")
        return item as? ResultType
    }

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}
