package com.google.serial;

import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author zhaosenshan
 */
public class SerialPort {

    private static final String TAG = "SerialPort";

    static {
        System.loadLibrary("native-lib");
    }

    private final FileInputStream inputStream;
    private final FileOutputStream outputStream;

    public SerialPort(File device, int baudRate, int dataBits, int stopBits, char parity) throws SecurityException, IOException {
        FileDescriptor descriptor = open(device.getAbsolutePath(), baudRate, dataBits, stopBits, parity);
        if (descriptor == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        inputStream = new FileInputStream(descriptor);
        outputStream = new FileOutputStream(descriptor);
    }

    private native static FileDescriptor open(String path, int baudRate, int dataBits, int stopBits, char parity);

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public native void close();
}
