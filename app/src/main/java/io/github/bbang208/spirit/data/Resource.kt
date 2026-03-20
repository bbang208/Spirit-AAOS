package io.github.bbang208.spirit.data

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?,
    val responseCode: Int? = null
) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T? = null, responseCode: Int? = null): Resource<T> {
            return Resource(Status.ERROR, data, msg, responseCode)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}
