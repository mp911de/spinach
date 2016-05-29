package biz.paluch.spinach.commands.rx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.lambdaworks.redis.internal.AbstractInvocationHandler;
import com.lambdaworks.redis.internal.LettuceLists;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.sync.DisqueCommands;
import rx.Observable;

public class RxSyncInvocationHandler<K, V> extends AbstractInvocationHandler {

    private final DisqueConnection<?, ?> connection;
    private final Object rxApi;

    public RxSyncInvocationHandler(DisqueConnection<?, ?> connection, Object rxApi) {
        this.connection = connection;
        this.rxApi = rxApi;
    }

    /**
     * 
     * @see AbstractInvocationHandler#handleInvocation(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {

        try {

            Method targetMethod = rxApi.getClass().getMethod(method.getName(), method.getParameterTypes());

            Object result = targetMethod.invoke(rxApi, args);

            if (result == null || !(result instanceof Observable<?>)) {
                return result;
            }
            Observable<?> observable = (Observable<?>) result;

            Iterable<?> objects = observable.toBlocking().toIterable();

            if (method.getReturnType().equals(List.class)) {
                return LettuceLists.newList(objects);
            }

            if (method.getReturnType().equals(Set.class)) {
                return new LinkedHashSet<>(LettuceLists.newList(objects));
            }

            Iterator<?> iterator = objects.iterator();

            if (iterator.hasNext()) {
                return iterator.next();
            }

            return null;

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public static <K, V> DisqueCommands<K, V> sync(DisqueConnection<K, V> connection) {

        RxSyncInvocationHandler<K, V> handler = new RxSyncInvocationHandler<K, V>(connection, connection.reactive());
        return (DisqueCommands<K, V>) Proxy.newProxyInstance(handler.getClass().getClassLoader(),
                new Class<?>[] { DisqueCommands.class }, handler);
    }
}