package com.genture.message_transfer.util;

import java.io.OutputStream;

public class Sender {

    private OutputStream os;

    public Sender(OutputStream os){
        this.os = os;
    }

    public byte[] escape(String json){
        return null;
    }

    public byte[] combine(byte[] frameBody){
        return null;
    }

    public void send(byte[] frame){

    }
}
