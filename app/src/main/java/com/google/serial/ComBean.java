package com.google.serial;

/**
 *
 * @author neo
 */
class ComBean {

    byte[] bRec;

    ComBean(byte[] buffer, int size) {
        bRec = new byte[size];
        System.arraycopy(buffer, 0, bRec, 0, size);
    }
}
