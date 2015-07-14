package biz.paluch.spinach.impl;

import java.util.Collection;

import rx.Observable;
import rx.Subscriber;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.output.SupportsObservables;

import com.google.common.base.Supplier;
import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandArgs;
import com.lambdaworks.redis.protocol.CommandOutput;
import io.netty.buffer.ByteBuf;

/**
 * Reactive command dispatcher.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class ReactiveCommandDispatcher<K, V, T> implements Observable.OnSubscribe<T> {

    private Supplier<? extends Command<K, V, T>> commandSupplier;
    private Command<K, V, T> command;
    private DisqueConnection<K, V> connection;
    private boolean dissolve;

    /**
     * 
     * @param staticCommand static command
     * @param connection the connection
     * @param dissolve dissolve collections into particular elements
     */
    public ReactiveCommandDispatcher(final Command<K, V, T> staticCommand, DisqueConnection<K, V> connection, boolean dissolve) {
        this(new Supplier<Command<K, V, T>>() {
            @Override
            public Command<K, V, T> get() {
                return staticCommand;
            }
        }, connection, dissolve);
    }

    /**
     * 
     * @param commandSupplier command supplier
     * @param connection the connection
     * @param dissolve dissolve collections into particular elements
     */
    public ReactiveCommandDispatcher(Supplier<Command<K, V, T>> commandSupplier, DisqueConnection<K, V> connection,
            boolean dissolve) {
        this.commandSupplier = commandSupplier;
        this.connection = connection;
        this.dissolve = dissolve;
        this.command = commandSupplier.get();
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {

        // Reuse the first command but then discard it.
        Command<K, V, T> command = this.command;
        if (command == null) {
            command = commandSupplier.get();
        }

        connection.dispatch(new ObservableCommand<K, V, T>(command, subscriber, dissolve));

        this.command = null;

    }

    private static class ObservableCommand<K, V, T> extends Command<K, V, T> {

        private final Command<K, V, T> command;
        private final Subscriber<? super T> subscriber;
        private final boolean dissolve;
        private boolean completed = false;

        public ObservableCommand(Command<K, V, T> command, Subscriber<? super T> subscriber, boolean dissolve) {
            super(null, null, null);
            this.command = command;
            this.subscriber = subscriber;
            this.dissolve = dissolve;

            if (command.getOutput() instanceof SupportsObservables) {
                SupportsObservables support = (SupportsObservables) command.getOutput();
                support.setSubscriber(subscriber);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void complete() {
            if (completed) {
                return;
            }

            command.complete();
            if (command.getException() != null) {
                subscriber.onError(command.getException());
                return;
            }

            if (getOutput() != null) {
                Object result = getOutput().get();
                if (result != null && !(getOutput() instanceof SupportsObservables)) {

                    if (dissolve && result instanceof Collection) {
                        Collection<T> collection = (Collection<T>) result;
                        for (T t : collection) {
                            subscriber.onNext(t);
                        }
                    } else {
                        subscriber.onNext((T) result);
                    }
                }

                if (getOutput().hasError()) {
                    subscriber.onError(new RedisCommandExecutionException(getOutput().getError()));
                    completed = true;
                    return;
                }
            }

            completed = true;
            subscriber.onCompleted();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {

            if (completed) {
                return false;
            }

            command.cancel(true);
            subscriber.onCompleted();
            completed = true;
            return false;
        }

        @Override
        public CommandOutput<K, V, T> getOutput() {
            return command.getOutput();
        }

        @Override
        public void encode(ByteBuf buf) {
            command.encode(buf);
        }

        @Override
        public void setOutput(CommandOutput<K, V, T> output) {
            command.setOutput(output);
        }

        @Override
        public Throwable getException() {
            return command.getException();
        }

        @Override
        public boolean setException(Throwable exception) {
            return command.setException(exception);
        }

        @Override
        public CommandArgs<K, V> getArgs() {
            return command.getArgs();
        }
    }
}