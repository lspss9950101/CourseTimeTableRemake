package com.tragicdilemma.coursetimetableremake;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class AdapterTimeTable extends SimpleCursorAdapter{

    private int height;
    private boolean[] choosedSession;
    private boolean darkMode;

    public AdapterTimeTable(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int height, boolean[] choosedSession, boolean darkMode) {
        super(context, layout, c, from, to, flags);
        this.height = height;
        this.choosedSession = choosedSession;
        this.darkMode = darkMode;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView tvName, tvRoom;
        ConstraintLayout clContainer, clCourse;
        clCourse = view.findViewById(R.id.clCourse);
        clContainer = view.findViewById(R.id.clContainer);
        tvName = view.findViewById(R.id.tvCourseName);
        tvRoom = view.findViewById(R.id.tvCourseRoom);
        int settingContext = Integer.parseInt(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_CONTEXT_SIZE));
        int settingContextRoom = Integer.parseInt(DBHelperTimeTable.getInstance(context).getPref(DBHelperTimeTable.PREF_CONTEXT_SIZE_ROOM));
        tvName.setTextSize(8 + 12 * settingContext / 100.f);
        tvRoom.setTextSize(8 + 12 * settingContextRoom / 100.f);
        tvName.setText(cursor.getString(cursor.getColumnIndex(DBHelperTimeTable.COLUMN_NAME)));
        tvRoom.setText(cursor.getString(cursor.getColumnIndex(DBHelperTimeTable.COLUMN_ROOM)));

        if(!(choosedSession[cursor.getPosition()] ^ darkMode)){
            clCourse.setBackgroundColor(Color.WHITE);
            tvName.setTextColor(context.getColor((R.color.colorGray)));
        }else{
            clCourse.setBackgroundColor(context.getColor(R.color.colorGray));
            tvName.setTextColor(Color.WHITE);
        }

        if(!tvRoom.getText().equals("")){
            tvRoom.setVisibility(View.VISIBLE);
            tvRoom.getBackground().setColorFilter(cursor.getInt(cursor.getColumnIndex(DBHelperTimeTable.COLUMN_COLOR)), PorterDuff.Mode.ADD);
        }
        else tvRoom.setVisibility(View.INVISIBLE);
        clContainer.getLayoutParams().height = height;
    }

    public void updateChoosed(boolean[] choosedSession){
        this.choosedSession = choosedSession;
        notifyDataSetChanged();
    }
}
