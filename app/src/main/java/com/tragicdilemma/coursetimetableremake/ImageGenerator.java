package com.tragicdilemma.coursetimetableremake;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ImageGenerator {
    public static Bitmap generateImage(Context context, int height, int width){
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(context.getColor(R.color.colorGray));
        canvas.drawRect((float)0.09 * width, (float)0.1 * height, (float)0.91 * width, (float)0.9 * height, paint);

        paint = new Paint();
        paint.setShader(new LinearGradient((float)(0.1*width), (float)(0.15*height), (float)(0.1*width), (float)(0.16*height),  new int[]{context.getColor(R.color.shadowDark), context.getColor(R.color.shadowCenter), context.getColor(R.color.shadowLight)}, null, Shader.TileMode.CLAMP));
        canvas.drawRect((float)(0.09*width), (float)(0.15*height), (float)(0.91*width), (float)(0.16*height), paint);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize((float) (0.04*width));
        canvas.drawText(context.getString(R.string.table_mon), (float)(0.15 + 0.8 * 1 / 6)*width, (float) (0.13*height), paint);
        canvas.drawText(context.getString(R.string.table_tue), (float)(0.15 + 0.8 * 2 / 6)*width, (float) (0.13*height), paint);
        canvas.drawText(context.getString(R.string.table_wed), (float)(0.15 + 0.8 * 3 / 6)*width, (float) (0.13*height), paint);
        canvas.drawText(context.getString(R.string.table_thurs), (float)(0.15 + 0.8 * 4 / 6)*width, (float) (0.13*height), paint);
        canvas.drawText(context.getString(R.string.table_fri), (float)(0.15 + 0.8 * 5 / 6)*width, (float) (0.13*height), paint);

        String[] sessionName = new String[]{"A", "B", "C", "D", "X", "E", "F", "G", "H", "Y", "I", "J", "K"};
        String[] sessionStart = new String[]{" 8:00", " 9:00", "10:10", "11:10", "12:20", "13:20", "14:20", "15:30", "16:30", "17:30", "18:30", "19:30", "20:30"};
        String[] sessionEnd = new String[]{" 8:50", " 9:50", "11:00", "12:00", "13:10", "14:10", "15:10", "16:20", "17:10", "18:20", "19:20", "20:20", "21:20"};
        paint.setTextSize((float)(0.04*width));
        paint.setTextAlign(Paint.Align.CENTER);
        for(int i = 1; i <= 13; i++)canvas.drawText(sessionName[i-1], (float)(0.12 + 0.05 + 0.8 / 6)*width/2, (float)((0.16 + (0.9 - 0.16) / 13 * (i - 0.7)) * height), paint);
        paint.setTextSize((float)(0.02*width));
        for(int i = 1; i <= 13; i++)canvas.drawText(sessionStart[i-1], (float)(0.12 + 0.05 + 0.8 / 6)*width/2, (float)((0.16 + (0.9 - 0.16) / 13 * (i - 0.5)) * height), paint);
        for(int i = 1; i <= 13; i++)canvas.drawText(sessionEnd[i-1], (float)(0.12 + 0.05 + 0.8 / 6)*width/2, (float)((0.16 + (0.9 - 0.16) / 13 * (i - 0.2)) * height), paint);
        for(int i = 1; i <= 13; i++)canvas.drawText("~", (float)(0.12 + 0.05 + 0.8 / 6)*width/2, (float)((0.16 + (0.9 - 0.16) / 13 * (i - 0.35)) * height), paint);

        paint.setTextSize((float) (0.03*width));
        List<ContentValues> list = DBHelperTimeTable.getInstance(context).getData();
        for(int i = 0; i < 13*7; i++){
            if(i % 7 == 0 || i % 7 == 6)continue;
            ContentValues cv = list.get(i);
            String name = cv.getAsString(DBHelperTimeTable.COLUMN_NAME);
            canvas.drawText(name, (float)(0.11 + 0.8 * (0.5 + i % 7) / 6)*width, (float)((0.16 + (0.9 - 0.16) / 13 * (i / 7 + 0.3)) * height), paint);
            if(!cv.getAsString(DBHelperTimeTable.COLUMN_ROOM).isEmpty()){
                Paint rPaint = new Paint();
                try {
                    JSONObject jsonObject = new JSONObject(cv.getAsString(DBHelperTimeTable.COLUMN_COLOR));
                    rPaint.setColor(Color.HSVToColor(new float[]{jsonObject.getInt("h"), (float) jsonObject.getDouble("s"), (float) jsonObject.getDouble("v")}));
                    canvas.drawRoundRect((float)(0.11 + 0.8 * (0.5 + i % 7) / 6 - 0.055)*width, (float)((0.17 + (0.9 - 0.16) / 13 * (i / 7 + 0.3)) * height), (float)(0.11 + 0.8 * (0.5 + i % 7) / 6 + 0.055)*width, (float)((0.19 + (0.9 - 0.16) / 13 * (i / 7 + 0.3)) * height), 10, 10,rPaint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                canvas.drawText(cv.getAsString(DBHelperTimeTable.COLUMN_ROOM), (float)(0.11 + 0.8 * (0.5 + i % 7) / 6)*width, (float)((0.185 + (0.9 - 0.16) / 13 * (i / 7 + 0.3)) * height), paint);
            }
        }

        paint.setStrokeWidth((float) (0.002 * width));
        for(int i = 0; i < 12; i++)canvas.drawLine((float)(0.09 + 0.8 * 1 / 6)*width, (float)((0.2 + (0.9 - 0.16) / 13 * (i + 0.3)) * height),(float)0.9*width,  (float)((0.2 + (0.9 - 0.16) / 13 * (i + 0.3)) * height), paint);

        canvas.drawBitmap(bitmap, 0, 0, null);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, (int)(0.09*width), (int)(0.1*height), (int)(0.91*width - 0.09*width), (int)(0.9*height - 0.1*height));
        return resizedBitmap;
    }

    public static Bitmap generateSizedImage(Activity activity, int color){
        int width = activity.findViewById(R.id.clParent).getWidth();
        ConstraintLayout clTitle = activity.findViewById(R.id.clTitle);
        ScrollView svTimetable = activity.findViewById(R.id.svTimetable);
        ImageView ivShadow = activity.findViewById(R.id.ivShadow);
        Bitmap bitmapTitle = Bitmap.createBitmap(width, clTitle.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasTitle = new Canvas(bitmapTitle);
        clTitle.draw(canvasTitle);

        Bitmap bitmapTimetable = Bitmap.createBitmap(svTimetable.getWidth(), svTimetable.getChildAt(0).getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasTimetable = new Canvas(bitmapTimetable);
        svTimetable.draw(canvasTimetable);

        Bitmap bitmapShadow = Bitmap.createBitmap(width, ivShadow.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasShadow = new Canvas(bitmapShadow);
        ivShadow.draw(canvasShadow);

        Bitmap result = Bitmap.createBitmap(width, bitmapTitle.getHeight() + bitmapTimetable.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, result.getWidth(), result.getHeight(), paint);
        canvas.drawBitmap(bitmapTitle, 0, 0, null);
        canvas.drawBitmap(bitmapTimetable, 0, bitmapTitle.getHeight(), null);
        canvas.drawBitmap(bitmapShadow, 0, bitmapTitle.getHeight(), null);
        return result;
    }


    public static String saveImage(Bitmap bitmap){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyMMddHHmmss");
        String fileName = "CourseTimeTable" + dateFormat.format(new Date()) + ".png";
        try{
            File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"CourseTimeTable");
            directory.mkdirs();

            FileOutputStream fos = new FileOutputStream(directory.getPath() + File.separator + fileName, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
