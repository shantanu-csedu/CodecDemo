package com.example.codecdemo;

/**
 * Created by shantanu on 5/29/17.
 */

public class JNIWrapper {

    static {
        try {
            System.loadLibrary("opus");
            initCodec();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    private static native void initCodec();
    private native void deInitCodec();
    public native byte[] encodeOpus(short[] pcm);
    public native short[] decodeOpus(byte[] encoded,int len);

    public void destroy(){
        deInitCodec();
    }
}
