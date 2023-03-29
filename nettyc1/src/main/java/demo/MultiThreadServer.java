package demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.configureBlocking(false);
            Selector boss = Selector.open();
            SelectionKey bossKey = serverSocketChannel.register(boss, 0, null);
            bossKey.interestOps(SelectionKey.OP_ACCEPT);
            serverSocketChannel.bind(new InetSocketAddress(8080));
            Worker[] workers = new Worker[2];
            for (int i = 0; i < workers.length; i++) {
                workers[i] = new Worker("worker-" + i);
            }
            AtomicInteger index = new AtomicInteger();
            while (true) {
                boss.select();
                Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        workers[index.getAndIncrement() % workers.length].register(socketChannel);
                    }
                }
            }
        }
    }

    static class Worker implements Runnable{
        private Selector selector;
        private final String name;

        private volatile boolean start;
        public Worker(String name) {
            this.name = name;
        }

        public void register(SocketChannel socketChannel) throws IOException {
            if (!start){
                selector = Selector.open();
                Thread thread = new Thread(this, name);
                thread.start();
                start=true;
            }
            selector.wakeup();
            socketChannel.register(selector,SelectionKey.OP_READ,null);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isReadable())  {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) selectionKey.channel();
                            try {
                                int read = channel.read(buffer);
                                if (read == -1){
                                    selectionKey.cancel();
                                    channel.close();
                                }else {
                                    channel.read(buffer);
                                    buffer.flip();
                                    System.out.println(StandardCharsets.UTF_8.decode(buffer));
                                }
                            }catch (IOException e){
                                e.printStackTrace();
                                selectionKey.cancel();
                                channel.close();
                            }

                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}