package com.uberall.interceptor;

import com.uberall.LockService;
import com.uberall.annotation.DistributedLock;
import com.uberall.model.Lock;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class DistributedLockInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedLockInterceptor.class);
    private static final String NAME_PARAMETER = "name";
    private static final String TTL_PARAMETER = "ttl";
    private static final String APPEND_PARAMETER = "appendParameters";

    final LockService lockService;

    @Inject
    public DistributedLockInterceptor(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String lockName = context.stringValue(getClazz(), NAME_PARAMETER).orElse(createLockName(context));
        final int ttl = context.intValue(getClazz(), TTL_PARAMETER).orElse(60);
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

        lockService.create(new Lock(lockName, LocalDateTime.now().plusSeconds(ttl)));
        return context.proceed();
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
