package com.tragicdilemma.coursetimetableremake;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.RemoteViews;


public class WidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_UPDATE = "org.tragicdilemma.coursetimetable.UPDATE_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i = 0; i < appWidgetIds.length; i++)appWidgetManager.updateAppWidget(appWidgetIds[i], getUpdatedRemoteViews(context, appWidgetIds[i]));
    }

    private RemoteViews getUpdatedRemoteViews(Context context, int appWidgetId){
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_list);
        Intent it = new Intent(context, RemoteViewsServiceWidget.class);
        it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        rv.setRemoteAdapter(R.id.lv_timetable, it);

        DBHelperTimeTable dbHelperTimeTable = DBHelperTimeTable.getInstance(context);
        int[] tvTitle = {R.id.mon, R.id.tues, R.id.wed, R.id.thurs, R.id.fri};
        int settingTitle = Integer.parseInt(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_WIDGET_TITLE_SIZE));
        for (int i = 0; i < 5; i++)rv.setTextViewTextSize(tvTitle[i], TypedValue.COMPLEX_UNIT_DIP,10 + 24 * settingTitle / 100.f);
        if(Boolean.valueOf(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_TITLE_COLOR)))for(int i = 0; i < 5; i++)rv.setTextColor(tvTitle[i], context.getColor(R.color.colorGray));
        else for(int i = 0; i < 5; i++)rv.setTextColor(tvTitle[i], Color.WHITE);
        int color = Integer.valueOf(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_TITLE_BACKGROUND));
        rv.setInt(R.id.ll_container, "setBackgroundColor", color);

        Intent intent = new Intent(context, ConfigActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.ll_list, pendingIntent);
        for(int i = 0; i < 5; i++)rv.setOnClickPendingIntent(tvTitle[i], pendingIntent);

        return rv;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().equals(ACTION_WIDGET_UPDATE)){
            int[] ids = intent.getIntArrayExtra("ids");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
            for(int i = 0; i < ids.length; i++)appWidgetManager.updateAppWidget(ids, getUpdatedRemoteViews(context,ids[i]));
            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.lv_timetable);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
