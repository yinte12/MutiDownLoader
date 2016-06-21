package com.asuper.mutidownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.asuper.library.DownLoaderTask;
import com.asuper.library.LoaderStateListener;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String url = "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk";

    private boolean isLoading = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DownLoaderTask task = new DownLoaderTask(this, url, toString(), new LoaderStateListener() {
            @Override
            public void onDownLoadProcessChange(int process) {
                System.out.println("process =" + process);
            }

            @Override
            public void onDownLoadStart(int fileLength) {
                System.out.println("fileLength =" + fileLength);
            }

            @Override
            public void onDownLoadResume(int process) {
                System.out.println("process =" + process);
            }

            @Override
            public void onDownLoadFinish(File file) {
                System.out.println("file =" + file.toString());
            }

            @Override
            public void onDownLoadFailed(String error) {
                System.out.println("error =" + error);
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoading) {
                    task.startDownload();
                    isLoading = false;
                } else {
                    task.stopDownload();
                    isLoading = true;
                }
            }
        });
    }
}
