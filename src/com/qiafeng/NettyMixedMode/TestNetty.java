package com.qiafeng.NettyMixedMode;

public class TestNetty {
    public static void main(String[] args) {
        //new io thread
        SelectorThreadGroup stg = new SelectorThreadGroup(3); //mixed mode

        //server register to one of these selectors
        stg.bind(9090);
    }
}
