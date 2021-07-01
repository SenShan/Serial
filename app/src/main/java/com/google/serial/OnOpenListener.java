package com.google.serial;

public interface OnOpenListener {
    /**
     * 打开
     */
    void onOpen();

    /**错误
     * @param error 错误信息
     */
    void onError(String error);
}
