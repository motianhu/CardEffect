package com.smona.aoplib;

public class InterceptDoubleClick {
    private static long lastClickTime = 0;
    private final static int SPACE_TIME = 500;

    public synchronized static boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2 = (currentTime - lastClickTime <= SPACE_TIME);
        lastClickTime = currentTime;
        return isClick2;
    }
}
