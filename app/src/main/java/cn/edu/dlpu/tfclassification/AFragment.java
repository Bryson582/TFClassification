package cn.edu.dlpu.tfclassification;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class AFragment extends Fragment {

    private TFLiteClassificationUtil tfLiteClassificationUtil;
    private ImageView imageView;
    private TextView textView;
    private ArrayList<String> classNames;

    private List<Fragment> mFragments;   //存放视图
    private ViewPager viewPager;
    private TabLayout mTabLayout;
    private List<String> mtitle;  //存放底部标题

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 我们需要迁移之前的功能从Activity到Fragment
        // 初始化模型
        initModel();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.afragment, container, false);
    }

    private void initModel() {
        if (!hasPermission())
        {
            requestPermission();
        }
        //加载模型
        classNames = Utils.ReadListFromFile(getActivity().getAssets(), "label_list.txt");
        String classificationModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "model_inceptionV3.tflite";
        // 这里需要先查清楚一点，context的作用以及在Activity中使用Fragment的时候如何和Activity实现数据通信。
        Utils.copyFileFromAsset(getActivity(),"model_inceptionV3.tflite",classificationModelPath);
        // 我们要查看一下Fragment中对类的实例化的用法
//        try {
//            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
//        }


    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
    // request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }



}
