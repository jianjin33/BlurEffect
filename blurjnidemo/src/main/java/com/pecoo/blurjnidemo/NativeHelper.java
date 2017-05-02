package com.pecoo.blurjnidemo;

/**
 * Created by Administrator 2017/5/2.
 */

public class NativeHelper {

    static {
        System.loadLibrary("blur_lib");
    }
    static native void blurBitmap(Object bitmap, int r);
}
