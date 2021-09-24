package com.google.serial;

enum Baud {
    B1("",9600),
    B2("",115200);
    private String baud;
    private int value;

    Baud(String baud, int value) {
        this.baud = baud;
        this.value = value;
    }
}
