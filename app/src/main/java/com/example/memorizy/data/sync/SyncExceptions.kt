package com.example.memorizy.data.sync

class SyncAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

class SyncRetryException(message: String, cause: Throwable? = null) : Exception(message, cause)