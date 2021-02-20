package com.qiafeng.NettyBossWorker;

public class TestNetty {
    public static void main(String[] args) {
        //new io thread
        SelectorThreadGroup boss = new SelectorThreadGroup(3);
        SelectorThreadGroup worker = new SelectorThreadGroup(3);

        //server register to one of these selectors
        boss.setWorker(worker);
        boss.bind(9999);
        boss.bind(8888);
        boss.bind(7777);
        boss.bind(6666);
    }
}
