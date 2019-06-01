package Provider;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 *  Provide the thread pools to read and write data, register and unregister events to SocketChannel to
 *  dispatch assignments to thread pools
 *
 *  Can set attachment and send it to adapter
 */
public interface IoProvider extends Closeable {
    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    abstract class HandleInputCallback implements Runnable {
        @Override
        public final void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable {
        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        public final <T> T getAttach() {
            @SuppressWarnings({"UnnecessaryLocalVariable", "Unchecked"})
            T attach = (T) this.attach;
            return attach;
        }

        protected abstract void canProviderOutput(Object attach);
    }
}
