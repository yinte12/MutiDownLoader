package com.asuper.library;

import java.io.File;

/**
 * Created by Joker on 2016/6/17.
 */
public interface LoaderStateListener {

    /**
     * 下载进度变化
     *
     * @param process
     */
    void onDownLoadProcessChange(int process);

    /**
     * 下载开始回调
     * @param fileLength
     */
    void onDownLoadStart(int fileLength);

    /**
     * 暂停下载
     *
     * @param process
     */
    void onDownLoadResume(int process);

    /**
     * 下载完成
     *
     * @param file
     */
    void onDownLoadFinish(File file);

    /**
     *下载失败
     *
     * @param error
     */
    void onDownLoadFailed(String error);

}
