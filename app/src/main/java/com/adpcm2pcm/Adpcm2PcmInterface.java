package com.adpcm2pcm;

public class Adpcm2PcmInterface {
    public static native void Adpcm2Pcm(byte[] data, int size, int sample, int index, byte[] output);
    public static native void Pcm2Adpcm(byte[] data, int size, int sample, int index, byte[] output);
    static {
        try {
            System.loadLibrary("adpcm2pcm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

