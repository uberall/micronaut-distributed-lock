package com.uberall.interceptors;

import com.uberall.LockService;
import com.uberall.annotations.DistributedLock;
import com.uberall.models.Lock;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.convert.ConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class DistributedLockInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedLockInterceptor.class);
    private static final String NAME_PARAMETER = "name";
    private static final String TTL_PARAMETER = "ttl";
    private static final String CLEANUP_PARAMETER = "cleanup";
    private static final String APPEND_PARAMETER = "appendParameters";


    final LockService lockService;
    @Value("${micronaut.distributed-lock.enabled:true}")
    boolean enabled;

    @Inject
    public DistributedLockInterceptor(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (!enabled) {
            return context.proceed();
        }

        String lockName = context.stringValue(getClazz(), NAME_PARAMETER).orElse(createLockName(context));

        final String ttl = context.stringValue(getClazz(), TTL_PARAMETER).orElse("1m");
        final boolean cleanup = context.booleanValue(getClazz(), CLEANUP_PARAMETER).orElse(true);

        final Duration duration = ConversionService.SHARED.convert(ttl, Duration.class).orElseGet(() -> {
            LOG.error(ttl + "could not be converted to Duration, falling back to 1m for " + createLockName(context));
            return Duration.ofMinutes(1);
        });

        boolean appendParameters = context.booleanValue(getClazz(), APPEND_PARAMETER).orElse(false);

        if (appendParameters) {
            lockName = lockName + "-" + convertWithStream(context.getParameterValueMap());
        }

        Lock lock = lockService.get(lockName).orElse(null);
        if (lock != null && LocalDateTime.now().isAfter(lock.getUntil())) {
            if (!lockService.isAutoCleanCapable()) {
                lockService.delete(lock);
            }

            lock = null;
        }

        if (lock != null) {
            LOG.debug(lockName + "s lock is still valid, skipping execution");
            return null;
        }

        lock = new Lock(lockName, LocalDateTime.now().plus(duration));
        lockService.create(lock);

        Object result;

        try {
            result = context.proceed();
        } finally {
            if (cleanup) {
                lockService.delete(lock);
            }
        }

        return result;
    }

    private Class<? extends Annotation> getClazz() {
        return DistributedLock.class;
    }

    protected String createLockName(MethodInvocationContext<Object, Object> context) {
        return context.getDeclaringType().getName() + "::" + context.getTargetMethod().getName();
    }

    protected String convertWithStream(Map<String, ?> map) {
        return map
                .keySet()
                .stream()
                .map(key -> key + "=" + map.get(key).toString())
                .collect(Collectors.joining(",", "{", "}"));
    }
}
