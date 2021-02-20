package com.qiafeng.NettyBossWorker;

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

    SelectorThreadGroup stg = this;
    public void setWorker(SelectorThreadGroup stg) {
        this.stg = stg;
    }

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
            nextSelectorV3(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextSelectorV3(Channel c) {
        if (c instanceof ServerSocketChannel) {
            SelectorThread st = next();
            st.setWorker(stg);
            st.lbq.add(c);
            st.selector.wakeup();
        } else if (c instanceof SocketChannel) {
            SelectorThread st = nextV3();
            st.lbq.add(c);
            st.selector.wakeup();
        }
    }

    public SelectorThread next() {
        int index = xid.incrementAndGet() % sts.length;
        return sts[index];
    }

    public SelectorThread nextV3() {
        int index = xid.incrementAndGet() % stg.sts.length;
        return stg.sts[index];
    }
}
