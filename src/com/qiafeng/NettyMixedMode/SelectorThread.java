package com.qiafeng.NettyMixedMode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class SelectorThread implements Runnable{
    Selector selector = null;
    LinkedBlockingDeque<Channel> lbq = new LinkedBlockingDeque<>();
    SelectorThreadGroup stg;

    SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                //1.select
//                System.out.println(Thread.currentThread().getName()+" :before select...."+selector.keys().size());
                int nums = selector.select(); //wakeup()
//                System.out.println(Thread.currentThread().getName()+" :after select...."+selector.keys().size());
                //2.selectedkeys
                if (nums > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter= selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {//which selector resgister this client
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {
                            writeHandler(key);
                        }
                    }
                }
                //3.other tasks
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel)c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName()+" register listen");
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel)c;
                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                        System.out.println(Thread.currentThread().getName()+" register client "+client.getRemoteAddress());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName()+" accept");
        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
        try {
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            stg.nextSelector(sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName()+" read...");
        SocketChannel client = (SocketChannel)key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        while(true) {
            try {
                int num = client.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    while(buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (num == 0) {
                    break;
                } else if (num < 0) {
                    System.out.println("client: " + client.getRemoteAddress()+" closed...");
                    key.cancel(); //why
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeHandler(SelectionKey key) {

    }
}

