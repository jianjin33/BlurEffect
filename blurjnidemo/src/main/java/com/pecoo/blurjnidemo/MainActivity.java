package com.pecoo.blurjnidemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import static com.pecoo.blurjnidemo.NativeHelper.blurBitmap;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private static final String TAG = "BLUR_MAIN_ACTIVITY";
    private ImageView mImageView;
    private Bitmap bitmap1;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.blur_img);
        mSeekBar = (SeekBar) findViewById(R.id.blur_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.blur_img);
        // 对bitmap进行缩放
        bitmap1 = scaleBitmap(bitmap);

        mImageView.setBackgroundResource(R.mipmap.blur_img);
    }

    public void start(View v){
        blur(bitmap1,20);
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        Display display = getWindowManager().getDefaultDisplay();
        int newWidth = display.getWidth();
        int newHeight = display.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Log.d(TAG, "scaleWidth:" + scaleWidth);
        Log.d(TAG, "scaleHeight:" + scaleHeight);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
        // 实现模糊效果之前，这里可对bitmap进行更大缩放，减少像素点还可提高性能
        float scaleFactor = 10;
        float scale = 1f / scaleFactor;
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true);
        bitmap.recycle();
        return newbm;
    }


    protected void blur(final Bitmap bitmap, final int radius) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                final Bitmap ret = blurNatively(bitmap, radius,true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setBackground(new BitmapDrawable(getResources(), ret));
                    }
                });
            }
        };
        thread.start();
    }

    public Bitmap blurNatively(Bitmap original, int radius, boolean canReuseInBitmap) {
        if (radius < 1) {
            return null;
        }

        Bitmap bitmap = buildBitmap(original, canReuseInBitmap);

        // Return this none blur
        if (radius == 1) {
            return bitmap;
        }

        //Jni BitMap Blur
        blurBitmap(bitmap, radius);

        return (bitmap);
    }


    private static Bitmap buildBitmap(Bitmap original, boolean canReuseInBitmap) {
        // First we should check the original
        if (original == null)
            throw new NullPointerException("Blur bitmap original isn't null");

        Bitmap.Config config = original.getConfig();
        if (config != Bitmap.Config.ARGB_8888 && config != Bitmap.Config.RGB_565) {
            throw new RuntimeException("Blur bitmap only supported Bitmap.Config.ARGB_8888 and Bitmap.Config.RGB_565.");
        }

        // If can reuse in bitmap return this or copy
        Bitmap rBitmap;
        if (canReuseInBitmap) {
            rBitmap = original;
        } else {
            rBitmap = original.copy(config, true);
        }
        return (rBitmap);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG,progress+"");
        blur(bitmap1,progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
