package com.google.serial;

public interface OnSendListener {
    /**接收
     * @param data 数据
     */
    void onReceive(String data);

    /**错误
     * @param error 错误
     */
    void onFail(String error);
}
