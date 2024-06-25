package io.herd.common.validation

import arrow.core.Either
import io.herd.common.exception.ApplicationException

@Suppress("unused")
interface ValidArrowEntity<Error> where Error : ApplicationException {
    fun isValid(): Either<Error, Unit>

}
