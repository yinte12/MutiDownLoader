package com.asuper.library;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Joker on 2016/6/17.
 */
public class DownLoaderTask {
    private static final String TAG = "DownLoaderTask";

    // 下载完成
    private static final int DOWNLOAD_FINISHED = 1;
    // 下载过程跟踪进度
    private static final int DOWNLOAD_PROCESS = 2;
    
    private static final int COUNT = 5;

    private Context mContext;
    private boolean isDownloading = false;
    private String mFileUrl;
    private File mFile;
    private URL mUrl;
    private String mTag;
    private List<HashMap<String, Integer>> mList;
    private int mProcess = 0;
    // 需要下载的文件长度
    private int mFileLength = -1;

    private LoaderStateListener mListener;

    public Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_FINISHED:
                    mListener.onDownLoadFinish(mFile);
                    break;
                case DOWNLOAD_PROCESS:
                    mListener.onDownLoadProcessChange(mProcess);
                    break;
            }
        }
    };

    public DownLoaderTask(Context context, String fileUrl, String tag, LoaderStateListener listener) {
        try {
            this.mContext = context;
            this.mFileUrl = fileUrl;
            this.mTag = tag;
            this.mList = new ArrayList<>();
            this.mFile = new File(Environment.getExternalStorageDirectory(), getFileName(fileUrl));
            this.mListener = listener;
            this.mUrl = new URL(fileUrl);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(String fileUrl) {
        int index = fileUrl.lastIndexOf("/") + 1;
        return fileUrl.substring(index);
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public void stopDownload() {
        isDownloading = false;
    }

    public void startDownload() {
        isDownloading = true;
        mListener.onDownLoadStart(mFileLength);

        if (mList.size() == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mFileLength = Util.getFileLength(mFileUrl, mContext);

                    if (mFileLength < 0) {
                        mListener.onDownLoadFailed(mContext.getString(R.string.file_not_exist));
                        return;
                    }

                    if (!Util.ExistSDCard()) {
                        mListener.onDownLoadFailed(mContext.getString(R.string.SD_not_exist));
                    }

                    for (int i = 0; i < COUNT; i++) {
                        int begin = i * COUNT;
                        int end = (i + 1) * COUNT;
                        if (i == COUNT - 1) {
                            end = mFileLength;
                        }

                        HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put("begin", begin);
                        map.put("end", end);
                        map.put("finished", 0);
                        mList.add(map);

                        Thread thread = new Thread(new DownloadRunnable(i, begin, end));
                        thread.start();
                    }
                }
            }).start();
        } else {
            for (int i = 0; i < COUNT; i++) {
                HashMap<String, Integer> map = mList.get(i);
                int begin = map.get("begin");
                int end = map.get("end");
                int finished = map.get("finished");
                Thread thread = new Thread(new DownloadRunnable(i, begin + finished, end));
                thread.start();
            }
        }
    }

    private class DownloadRunnable implements Runnable {

        //Thread ID
        private int id;
        //start
        private int begin;
        //end
        private int end;

        public DownloadRunnable(int id, int begin, int end) {
            this.id = id;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            InputStream is = null;
            RandomAccessFile randomAccessFile = null;

            try {
                if (begin > end) {
                    return;
                }
                HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                connection.setRequestProperty("Range", "bytes=" + begin + "-" + end);

                is = connection.getInputStream();
                byte[] buf = new byte[1024 * 1024];
                randomAccessFile = new RandomAccessFile(mFile, "rw");
                randomAccessFile.seek(begin);

                int len = 0;

                while (((len = is.read(buf)) != -1) && isDownloading) {
                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);
                    //保存断点续传
                    mList.get(id).put("finished", mList.get(id).get("finished") + len);
                }

                if (is != null) {
                    is.close();
                }

                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private synchronized void updateProgress(int add) {
         mProcess += add;
         if (mProcess >= mFileLength) {
             Message message;
             message = Message.obtain();
             message.what = DOWNLOAD_FINISHED;
             mHandler.sendMessage(message);
         } else {
             Message message;
             message = Message.obtain();
             message.what = DOWNLOAD_PROCESS;
             mHandler.sendMessage(message);
         }

    }
}
