package com.google.serial;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 串口辅助工具类
 */
public class SerialHelper {

    /**
     * native交互类
     */
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread readThread;
    /**
     * 串口地址
     */
    private String port;
    private int baudRate;
    /**
     * char校验类型 取值N ,E, O,,S
     */
    private char parity;
    /**
     * dataBits 类型 int数据位 取值 位7或8
     */
    private int dataBit;
    /**
     * stopBits 类型 int 停止位 取值1 或者 2
     */
    private int stopBit;
    private String tag;

    private OnOpenListener onOpenListener;

    public void setOnOpenListener(OnOpenListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    private OnSendListener sendListener;

    public void setSendListener(OnSendListener sendListener) {
        this.sendListener = sendListener;
    }

    /**
     * 打开串口，并启动线程
     */
    public void open() {
        String msg = "port=" + port + ",baudRate=" + baudRate + ",dataBit=" + dataBit + ",stopBit=" + stopBit + ",parity=" + parity;
        try {
            mSerialPort = new SerialPort(new File(port), baudRate, dataBit, stopBit, parity);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            readThread = new ReadThread();
            readThread.start();
            if (onOpenListener != null) {
                onOpenListener.onOpen();
            }
        } catch (IOException e) {
            if (onOpenListener != null) {
                onOpenListener.onError("serial port msg:" + msg + ",open fail!" + e.getMessage());
            }
        }
    }


    public void send(String comm) {
        try {
            if (mOutputStream != null) {
                dataResult = "";
                mOutputStream.write(comm.getBytes(StandardCharsets.US_ASCII));
            }
        } catch (IOException e) {
            if (sendListener != null) {
                sendListener.onFail(e.getMessage());
            }
        }
    }


    private String dataResult = "";

    /**
     * 数据传导
     */
    private void onData(byte[] bytes) {
        String str = new String(bytes, StandardCharsets.US_ASCII);
        dataResult += str;
        if (dataResult.contains(tag)) {
            //数据接收完成
            if (sendListener != null) {
                sendListener.onReceive(dataResult);
            }
        }
    }

    private boolean pauseFlag = false;

    /**
     * 唤醒线程
     */
    public synchronized void setResume() {
        this.pauseFlag = false;
        readThread.notify();
    }

    /**
     * 线程暂停
     */
    public void setPause() {
        this.pauseFlag = true;
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (pauseFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    byte[] buffer = new byte[512];
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        ComBean bean = new ComBean(buffer, size);
                        byte[] bytes = bean.bRec;
                        onData(bytes);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 关闭接收与发送线程
     */
    public void close() {
        try {
            if (readThread != null) {
                readThread.interrupt();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    SerialHelper(Builder builder) {
        this.port = builder.sPort;
        this.baudRate = builder.baudRate;
        this.dataBit = builder.dataBit;
        this.stopBit = builder.stopBit;
        this.parity = builder.parity;
        this.tag = builder.tag;
    }

    public static final class Builder {
        /**
         * 串口名
         */
        private String sPort = "/dev/ttyHS0";
        /**
         * 波特率
         */
        private int baudRate = 115200;
        /**
         * dataBits 类型 int数据位 取值 位7或8
         */
        private int dataBit = 8;
        /**
         * stopBits 类型 int 停止位 取值1 或者 2
         */
        private int stopBit = 1;

        /**
         * char校验类型 取值N ,E, O,,S
         */
        private char parity = 'N';
        /**
         * 与硬件工程师约定数据传输完成标记
         */
        private String tag = "OK\r\n";

        public Builder setsPort(String sPort) {
            this.sPort = sPort;
            return this;
        }

        public Builder setBaudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public Builder setDataBit(int dataBit) {
            this.dataBit = dataBit;
            return this;
        }

        public Builder setStopBit(int stopBit) {
            this.stopBit = stopBit;
            return this;
        }

        public Builder setParity(char parity) {
            this.parity = parity;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public SerialHelper build() {
            return new SerialHelper(this);
        }
    }
}