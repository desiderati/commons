package io.herd.common.validation

import arrow.core.EitherNel
import io.herd.common.exception.ApplicationException

@Suppress("unused")
interface ValidArrowEntity<Error> where Error : ApplicationException {

    fun isValid(): EitherNel<Error, Unit>

}
