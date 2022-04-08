package cn.edu.dlpu.tfclassification;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TFLiteClassificationUtil tfLiteClassificationUtil;
    private ImageView imageView;
    private TextView textView;
    private ArrayList<String> classNames;

    private List<Fragment> mFragments;   //存放视图
    private ViewPager viewPager;
    private TabLayout mTabLayout;
    private List<String> mtitle;  //存放底部标题

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化控件
        initModel();
        initView();

    }

    private void initModel() {
        if (!hasPermission()) {
            requestPermission();
        }
        // 加载模型和标签
        classNames = Utils.ReadListFromFile(getAssets(), "label_list.txt");
        String classificationModelPath = getCacheDir().getAbsolutePath() + File.separator + "model_inceptionV3.tflite";
        Utils.copyFileFromAsset(MainActivity.this, "model_inceptionV3.tflite", classificationModelPath);
        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(MainActivity.this, "模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        // 获取控件
        Button selectImgBtn = findViewById(R.id.select_img_btn);
        Button openCamera = findViewById(R.id.open_camera);
        Button objectDetection = findViewById(R.id.object_detection);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.result_text);


        selectImgBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 打开相册
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开实时拍摄识别页面
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
        objectDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //, 打开实时摄像头进行目标检测
                Intent intent = new Intent(MainActivity.this,ObjectDetection.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mTabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);

        mFragments=new ArrayList<>();
        mFragments.add(new AFragment());
        mFragments.add(new BFragment());
        mFragments.add(new CFragment());
//        mFragments.add(new me());

        mtitle=new ArrayList<String>();
        mtitle.add("首页");
        mtitle.add("算力比拼");
        mtitle.add("识别记录");
//        mtitle.add("我的");

        //实例化适配器
        MyAdapt adapt = new MyAdapt(getSupportFragmentManager(), mFragments, mtitle);
        viewPager.setAdapter(adapt);

        mTabLayout.setupWithViewPager(viewPager);//给tab设置一个viewpager
        //viewpager的监听
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override  //选中
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        Toast.makeText(MainActivity.this, "首页", Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        Toast.makeText(MainActivity.this, "算力比拼", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        Toast.makeText(MainActivity.this, "识别记录", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
//                        Toast.makeText(MainActivity.this, "这是我的", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override   // //state的状态有三个，0表示什么都没做，1正在滑动，2滑动完毕
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String image_path;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    Log.w("onActivityResult", "user photo data is null");
                    return;
                }
                Uri image_uri = data.getData();
                image_path = getPathFromURI(MainActivity.this, image_uri);
                try {
                    // 预测图像
                    FileInputStream fis = new FileInputStream(image_path);
                    imageView.setImageBitmap(BitmapFactory.decodeStream(fis));
                    long start = System.currentTimeMillis();
                    float[] result = tfLiteClassificationUtil.predictImage(image_path);
                    long end = System.currentTimeMillis();
                    String show_text = "预测结果标签：" + (int) result[0] +
                            "\n名称：" +  classNames.get((int) result[0]) +
                            "\n概率：" + result[1] +
                            "\n时间：" + (end - start) + "ms";
                    textView.setText(show_text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

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

    // check had permission
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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