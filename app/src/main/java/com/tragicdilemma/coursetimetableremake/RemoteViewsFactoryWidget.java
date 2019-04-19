package com.tragicdilemma.coursetimetableremake;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;


public class RemoteViewsFactoryWidget implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private List<ContentValues> data;

    public RemoteViewsFactoryWidget(Context context, Intent intent){
        this.context = context;
        data = DBHelperTimeTable.getInstance(context).getData();
    }

    @Override
    public void onCreate() {
        data = DBHelperTimeTable.getInstance(context).getData();
    }

    @Override
    public void onDataSetChanged() {
        data = DBHelperTimeTable.getInstance(context).getData();
    }

    @Override
    public void onDestroy() {
        data.clear();
    }

    @Override
    public int getCount() {
        return 13;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.item_widget);

        String[] sessionName = new String[]{"A", "B", "C", "D", "X", "E", "F", "G", "H", "Y", "I", "J", "K"};
        String[] sessionStart = new String[]{" 8:00", " 9:00", "10:10", "11:10", "12:20", "13:20", "14:20", "15:30", "16:30", "17:30", "18:30", "19:30", "20:30"};
        String[] sessionEnd = new String[]{" 8:50", " 9:50", "11:00", "12:00", "13:10", "14:10", "15:10", "16:20", "17:20", "18:20", "19:20", "20:20", "21:20"};

        rv.setTextViewText(R.id.txt_sessionName, sessionName[i]);
        rv.setTextViewText(R.id.txt_sessionTime1, sessionStart[i]);
        rv.setTextViewText(R.id.txt_sessionTime3, sessionEnd[i]);
        int color = Integer.valueOf(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_BACKGROUND));
        rv.setInt(R.id.ll_item, "setBackgroundColor", color);

        boolean textColor = Boolean.valueOf(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_COLOR));
        if(textColor){
            rv.setTextColor(R.id.txt_sessionName, context.getColor(R.color.colorGray));
            rv.setTextColor(R.id.txt_sessionTime1, context.getColor(R.color.colorGray));
            rv.setTextColor(R.id.txt_sessionTime2, context.getColor(R.color.colorGray));
            rv.setTextColor(R.id.txt_sessionTime3, context.getColor(R.color.colorGray));
        }else{
            rv.setTextColor(R.id.txt_sessionName, Color.WHITE);
            rv.setTextColor(R.id.txt_sessionTime1, Color.WHITE);
            rv.setTextColor(R.id.txt_sessionTime2, Color.WHITE);
            rv.setTextColor(R.id.txt_sessionTime3, Color.WHITE);
        }

        int[] tvClassName = new int[]{R.id.txt_class_1, R.id.txt_class_2, R.id.txt_class_3, R.id.txt_class_4, R.id.txt_class_5};
        int[] tvClassRoom = new int[]{R.id.txt_room_1, R.id.txt_room_2, R.id.txt_room_3, R.id.txt_room_4, R.id.txt_room_5};
        int[] imgClassRoom = new int[]{R.id.img_room_1, R.id.img_room_2, R.id.img_room_3, R.id.img_room_4, R.id.img_room_5};

        for(int j = 0; j < 5; j++){
            ContentValues cv = data.get(7 * i + j + 1);
            rv.setTextViewText(tvClassName[j], cv.getAsString(DBHelperTimeTable.COLUMN_NAME));
            rv.setTextViewText(tvClassRoom[j], cv.getAsString(DBHelperTimeTable.COLUMN_ROOM));
            int settingContext = Integer.parseInt(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_SIZE));
            int settingContextRoom = Integer.parseInt(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_SIZE_ROOM));
            rv.setTextViewTextSize(tvClassName[j], TypedValue.COMPLEX_UNIT_DIP, 10 + 14 * settingContext / 100.f);
            rv.setTextViewTextSize(tvClassRoom[j], TypedValue.COMPLEX_UNIT_DIP, 10 + 14 * settingContextRoom / 100.f);
            if(textColor)rv.setTextColor(tvClassName[j], context.getColor(R.color.colorGray));
            else rv.setTextColor(tvClassName[j], Color.WHITE);
            color = cv.getAsInteger(DBHelperTimeTable.COLUMN_COLOR);
            rv.setInt(imgClassRoom[j], "setColorFilter", color);
            if(cv.getAsString(DBHelperTimeTable.COLUMN_ROOM).isEmpty())rv.setInt(imgClassRoom[j], "setVisibility", View.INVISIBLE);
            else rv.setInt(imgClassRoom[j], "setVisibility", View.VISIBLE);
        }
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_loading);
        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
