package biz.paluch.spinach.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import biz.paluch.spinach.api.DisqueConnection;

import com.google.common.reflect.AbstractInvocationHandler;
import com.lambdaworks.redis.LettuceFutures;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * Invocation-handler to synchronize API calls which use Futures as backend. This class leverages the need to implement a full
 * sync class which just delegates every request.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class FutureSyncInvocationHandler<K, V> extends AbstractInvocationHandler {

    private final DisqueConnection<?, ?> connection;
    private final Object asyncApi;

    public FutureSyncInvocationHandler(DisqueConnection<?, ?> connection, Object asyncApi) {
        this.connection = connection;
        this.asyncApi = asyncApi;
    }

    /**
     * 
     * @see com.google.common.reflect.AbstractInvocationHandler#handleInvocation(java.lang.Object, java.lang.reflect.Method,
     *      java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {

        try {

            Method targetMethod = asyncApi.getClass().getMethod(method.getName(), method.getParameterTypes());

            Object result = targetMethod.invoke(asyncApi, args);

            if (result instanceof RedisCommand) {
                RedisCommand<?, ?, ?> command = (RedisCommand<?, ?, ?>) result;

                return LettuceFutures.await(command, connection.getTimeout(), connection.getTimeoutUnit());
            }

            return result;

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

    }
}