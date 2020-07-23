package com.uberall.interceptors;

import com.uberall.LockService;
import com.uberall.annotations.DistributedLock;
import com.uberall.exceptions.DistributedLockCreationException;
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

    @Value("${micronaut.distributed-lock.enabled:true}") boolean enabled;

    @Inject
    public DistributedLockInterceptor(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (!enabled) {
            LOG.debug("Interceptor is disabled, going on");
            return context.proceed();
        }

        final String methodName = createLockName(context);
        final boolean appendParameters = context.booleanValue(getClazz(), APPEND_PARAMETER).orElse(false);
        String lockName = context.stringValue(getClazz(), NAME_PARAMETER).orElse(methodName);

        if (appendParameters) {
            lockName = lockName + "-" + convertWithStream(context.getParameterValueMap());
        }

        final String ttl = context.stringValue(getClazz(), TTL_PARAMETER).orElse("1m");
        final boolean cleanup = context.booleanValue(getClazz(), CLEANUP_PARAMETER).orElse(true);
        final Duration duration = getDuration(ttl, methodName);

        Lock lock = lockService.get(lockName).orElse(null);
        if (lock != null && LocalDateTime.now().isAfter(lock.getUntil())) {
            if (!lockService.isAutoCleanCapable()) {
                LOG.debug("deleting {}", lock.getName());
                lockService.delete(lock);
            }

            lock = null;
        }

        if (lock != null) {
            LOG.debug("{}s lock is still valid, skipping execution", lockName);
            return null;
        }

        lock = new Lock(lockName, LocalDateTime.now().plus(duration));
        Object result;

        try {
            LOG.debug("creating {}", lock.getName());
            lockService.save(lock);
        } catch (DistributedLockCreationException e) {
            LOG.debug(e.getMessage());
            return null;
        }

        try {
            result = context.proceed();
        } finally {
            if (cleanup) {
                lockService.delete(lock);
            }
        }

        return result;
    }

    private Duration getDuration(String ttl, String name) {
        return ConversionService.SHARED.convert(ttl, Duration.class).orElseGet(() -> {
            LOG.error("{} could not be converted to Duration, falling back to 1m for {}", ttl, name);
            return Duration.ofMinutes(1);
        });
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
