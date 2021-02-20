package com.qiafeng.multiplexing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class Multiplexing {
    private Selector selector;

    public void acceptHandler(SelectionKey selectionKey) {
        ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
        try {
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            sc.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("New connection from: " + sc.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readHandler(SelectionKey selectionKey) {
        SocketChannel sc = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
        try {
            buffer.clear();
            int read = sc.read(buffer);
            if (read > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.println((char) buffer.get());
                }
            } else if (read == 0) {

            } else {
                sc.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void writeHandler(SelectionKey selectionKey) {

    }

    public void run() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(8080));
            server.configureBlocking(false);

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            while (selector.select() > 0) {
//                if (selector.select() > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for (SelectionKey selectionKey : selectionKeys) {
                        if (selectionKey.isAcceptable()) {
                            acceptHandler(selectionKey);
                        } else if (selectionKey.isReadable()) {
                            readHandler(selectionKey);
                        } else if (selectionKey.isWritable()) {
                            writeHandler(selectionKey);
                        }
                    }
//                    selector.keys().forEach(k ->
//                            System.out.println(k.interestOps()));
                    selectionKeys.clear();
//                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        new Multiplexing().run();

    }
}
