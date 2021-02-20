package com.qiafeng.NettyMixedMode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {
    SelectorThread[] sts;
    ServerSocketChannel server;
    AtomicInteger xid = new AtomicInteger(0);

    SelectorThreadGroup(int num) {
        sts = new SelectorThread[num];
        for (int i = 0; i < num ; i++) {
            sts[i] = new SelectorThread(this);
            new Thread(sts[i]).start();
        }
    }

    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));//which selector to register this server?
            nextSelector(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // cover both ServerSocetChannel and SocKetChannel
    public void nextSelector(Channel c) {
        SelectorThread selectorThread = next();
//        // Chennel can be ServerSocketChannel or SocketChannel
//        ServerSocketChannel s = (ServerSocketChannel)c;
//        try {
//            s.register(selectorThread.selector, SelectionKey.OP_ACCEPT); // may be blocked
//            selectorThread.selector.wakeup();
//        } catch (ClosedChannelException e) {
//            e.printStackTrace();
//        }
        selectorThread.lbq.add(c); //queue to pass data
        selectorThread.selector.wakeup(); // stop blocking status, so SelectorThread can register server
    }

    public SelectorThread next() {
        int index = xid.incrementAndGet() % sts.length;
        return sts[index];
    }

    public void nextSelectorV2(Channel c) {
        if (c instanceof ServerSocketChannel) {
            sts[0].lbq.add(c);
            sts[0].selector.wakeup();
        } else if (c instanceof SocketChannel) {
            SelectorThread st = nextV2();
            st.lbq.add(c);
            st.selector.wakeup();
        }
    }

    public SelectorThread nextV2() {
        int index = xid.incrementAndGet() % (sts.length-1) + 1;
        return sts[index];
    }
}

