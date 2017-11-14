package com.nhancv.fragmentlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    protected static final int NUMBER_OF_PAGES = 30;

    protected MyAdapter mAdapter;
    protected ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int[] colorList = new int[NUMBER_OF_PAGES];
        for (int i = 0; i < colorList.length; i++) {
            Random rand = new Random();
            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            int randomColor = Color.rgb(r, g, b);
            colorList[i] = randomColor;
        }

        mAdapter = new MyAdapter(getSupportFragmentManager(), colorList);
        mPager = findViewById(R.id.viewpager);
        mPager.setAdapter(mAdapter);

        // TODO: 11/14/17 Gen bitmap to file in thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                View v = getLayoutInflater().inflate(R.layout.fragment, null, false);
                TextView textView = v.findViewById(R.id.textview);
                for (int i = 0; i < colorList.length; i++) {
                    v.setBackgroundColor(colorList[i]);
                    textView.setText("Page " + i);
                    genBitmapFile(MainActivity.this, v, i);
                }
            }
        }).start();

    }

    public static void genBitmapFile(Context context, View targetView, int num) {
        int sizePixels = 1080;

        Bitmap result = Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(result);

        // measure view first
        int sizeSpec = View.MeasureSpec.makeMeasureSpec(sizePixels, View.MeasureSpec.EXACTLY);
        targetView.measure(sizeSpec, sizeSpec);

        // then layout
        int width = targetView.getMeasuredWidth();
        int height = targetView.getMeasuredHeight();
        targetView.layout(0, 0, width, height);

        // now you can draw it
        targetView.draw(c);

        saveBitmapToFile(context, "testbm_" + num + ".png", result);
    }

    public static File saveBitmapToFile(Context context, String filename, Bitmap bitmap) {
        File f = new File(context.getCacheDir(), filename);
        Log.e(TAG, "saveBitmapToFile: " + f.getAbsolutePath());
        try {
            f.createNewFile();
            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public static class MyAdapter extends FragmentPagerAdapter {

        final int[] colorList;

        public MyAdapter(FragmentManager fm, final int[] colorList) {
            super(fm);
            this.colorList = colorList;
        }

        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentOne.newInstance(position, colorList[position]);
        }
    }


    public static class FragmentOne extends Fragment {

        private static final String MY_NUM_KEY = "num";
        private static final String MY_COLOR_KEY = "color";

        private int mNum;
        private int mColor;

        // You can modify the parameters to pass in whatever you want
        static FragmentOne newInstance(int num, int color) {
            FragmentOne f = new FragmentOne();
            Bundle args = new Bundle();
            args.putInt(MY_NUM_KEY, num);
            args.putInt(MY_COLOR_KEY, color);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt(MY_NUM_KEY) : 0;
            mColor = getArguments() != null ? getArguments().getInt(MY_COLOR_KEY) : Color.BLACK;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment, container, false);
            v.setBackgroundColor(mColor);
            TextView textView = v.findViewById(R.id.textview);
            textView.setText("Page " + mNum);
            return v;
        }
    }
}
