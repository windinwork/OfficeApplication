package com.windinwork.officeapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.util.HashMap;

public class TbsReaderFragment extends Fragment {

    private static final String TAG = "TbsReaderFragment";
    private static final String INTENT_PATH = "INTENT_PATH";

    TbsReaderView mReaderView;
    boolean mReaderOpened = false;

    public static TbsReaderFragment newFragment(@NonNull String path) {
        TbsReaderFragment fragment = new TbsReaderFragment();
        Bundle arguement = new Bundle();
        arguement.putString(INTENT_PATH, path);
        fragment.setArguments(arguement);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        mReaderView = new TbsReaderView(context, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        return mReaderView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguement = getArguments();
        if (arguement != null) {
            String path = arguement.getString(INTENT_PATH);
            if (!TextUtils.isEmpty(path)) {
                tryOpen(new File(path));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mReaderView != null) {
            mReaderView.onStop();
        }
    }

    /**
     * @param file
     * @return 0: 正常 1: 异常 2: 应用内不支持，需要用QQ插件打开 3: 格式不支持
     */
    public int tryOpen(File file) {
        if (file == null
                || !file.exists()) {
            return 1;
        }

        if (mReaderOpened) {
            resetReaderView();
        }

        int open = open(file);
        mReaderOpened = open == 0;
        return open;
    }

    /**
     * 重置状态
     */
    private void resetReaderView() {
        mReaderView.onStop();
    }

    /**
     * @param file
     * @return 0: 正常 1: 异常 2: 应用内不支持，使用外部应用打开 3: 不支持的格式
     */
    private int open(@NonNull File file) {
        if (mReaderView == null) {
            return 1;
        }
        Context context = getContext();
        if (context == null) {
            return 1;
        }

        String path = file.getPath();
        String format = parseFormat(path);
        boolean preOpen = mReaderView.preOpen(format, false); // 该状态标志x5文件能力是否成功初始化并支持打开文件的格式
        if (preOpen) { // 使用x5内核打开office文件
            Bundle bundle = new Bundle();
            bundle.putString("filePath", path);
            bundle.putString("tempPath", StorageUtils.getTempDir(context).getPath());
            mReaderView.openFile(bundle);
            return 0;
        } else { // 打开文件失败，可能是由于x5内核未成功初始化引起
            if (QbSdk.isSuportOpenFile(format, 1)) { // 再次检查文件是否支持
                HashMap<String, String> params = new HashMap<>();
                params.put("style", "1");
                params.put("local", "false");
                TbsReaderAssist.openFileReader(context, path, params, null); // 使用修改后的代码，避免Uri.fromFile(file)在Android7.0及以上的崩溃
                return 2;
            } else {
                Log.e(TAG, "Unsupport format");
                return 3;
            }
        }
    }

    /**
     * 解析文件格式
     * @param fileName
     * @return
     */
    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
