package com.windinwork.officeapplication;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = this;

        // 将文件从assets目录中复制出来
        File file = new File(StorageUtils.getFileDir(context), "test.pptx");
        try {
            InputStream inputStream = getAssets().open("test.pptx");
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int b;
            while(-1 != (b = inputStream.read(buffer))) {
                fos.write(buffer, 0, b);
            }
            inputStream.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 使用TbsReader打开Office文件
        TbsReaderFragment fragment = TbsReaderFragment.newFragment(file.getAbsolutePath());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .show(fragment)
                .commit();
    }
}
