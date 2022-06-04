package cn.edu.dlpu.tfclassification;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //构建试图 初始化页面中的控件
        //获取控件 三个按钮
        Button selectImgBtn = getActivity().findViewById(R.id.select_img_btn);
        Button openCamera = getActivity().findViewById(R.id.open_camera);
//        Button objectDetection = getActivity().findViewById(R.id.object_detection);
        imageView = getActivity().findViewById(R.id.image_view);
        textView = getActivity().findViewById(R.id.result_text);

        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(intent);
//                intent.setType("image/*");
//                startActivityForResult(intent, 1);
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开实时拍摄识别页面
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });

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
        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(getActivity(), "图像识别模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "图像识别模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        }

    }

    public final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    Uri image_uri = result.getData().getData();
                    //use photoUri here
                    String image_path;
                    image_path = getPathFromURI(getActivity(), image_uri);
                    try {
                    // 预测图像
                    FileInputStream fis = new FileInputStream(image_path);
                    imageView.setImageBitmap(BitmapFactory.decodeStream(fis));
                    long start = System.currentTimeMillis();
                    float[] result1 = tfLiteClassificationUtil.predictImage(image_path);
                    long end = System.currentTimeMillis();
                    DecimalFormat df = new DecimalFormat("0.00%");
                    String show_text =
                            "名称：" +  classNames.get((int) result1[0]) +
                            "\n概率：" + df.format(result1[1])  +
                            "\n时间：" + (end-start)+ "ms";
                    textView.setText(show_text);

                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                    SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put("NAME"," 名称：" +  classNames.get((int) result1[0]) +
                            " 概率：" + df.format(result1[1])  +
                            " 预测时间：" + (end-start) + "ms");
                    sqLiteDatabase.insert("Users_Table",null,cv);
                    sqLiteDatabase.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                }
            }
    );

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        String image_path;
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == 1) {
//                if (data == null) {
//                    Log.w("onActivityResult", "user photo data is null");
//                    return;
//                }
//                Uri image_uri = data.getData();
//                image_path = getPathFromURI(getActivity(), image_uri);
//                try {
//                    // 预测图像
//                    FileInputStream fis = new FileInputStream(image_path);
//                    imageView.setImageBitmap(BitmapFactory.decodeStream(fis));
//                    long start = System.currentTimeMillis();
//                    float[] result = tfLiteClassificationUtil.predictImage(image_path);
//                    long end = System.currentTimeMillis();
//                    DecimalFormat df = new DecimalFormat("0.00%");
//                    String show_text =
//                            "名称：" +  classNames.get((int) result[0]) +
//                            "\n概率：" + df.format(result[1])  +
//                            "\n时间：" + (end-start)+ "ms";
//                    textView.setText(show_text);
//
//                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
//                    SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
//                    ContentValues cv = new ContentValues();
//                    cv.put("NAME"," 名称：" +  classNames.get((int) result[0]) +
//                            " 概率：" + df.format(result[1])  +
//                            " 预测时间：" + (end-start) + "ms");
//                    sqLiteDatabase.insert("Users_Table",null,cv);
//                    sqLiteDatabase.close();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    // 根据相册的Uri获取图片的路径
    public static String getPathFromURI(Context context, Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
