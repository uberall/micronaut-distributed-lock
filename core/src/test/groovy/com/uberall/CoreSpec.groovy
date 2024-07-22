package com.uberall

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Unroll

@MicronautTest
@Unroll
class CoreSpec extends ImplementationSpec {

    @Inject LockServiceImpl lockService

}
