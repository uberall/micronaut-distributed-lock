package com.uberall

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest
class JdbcLockSpec extends ImplementationSpec {

    @Inject MicronautDataJdbcLockService lockService

}
