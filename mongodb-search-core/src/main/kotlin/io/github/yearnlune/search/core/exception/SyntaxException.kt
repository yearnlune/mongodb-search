package io.github.yearnlune.search.core.exception

class SyntaxException(msg: String = "") : RuntimeException("Illegal syntax for set operator: $msg")