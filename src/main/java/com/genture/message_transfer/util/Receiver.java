package com.genture.message_transfer.util;

import java.io.InputStream;

public class Receiver {

    private InputStream is;
    private byte[] frameBody;
    private boolean isFirstFrame;
    private boolean isLastFrame;

    public Receiver(InputStream is){
        this.is = is;
    }

    public boolean isValid(){
        return false;
    }

    public void pareseFrame(){
    }

    public String conventFrameBody2String(){
        return null;
    }

    public byte[] receiveFrame(){
        return null;
    }
}
