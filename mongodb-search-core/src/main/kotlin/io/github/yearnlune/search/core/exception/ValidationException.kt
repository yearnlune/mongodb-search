package io.github.yearnlune.search.core.exception

class ValidationException(msg: String = "") : RuntimeException("Failed to validate: $msg")