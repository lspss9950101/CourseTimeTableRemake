package com.tragicdilemma.coursetimetableremake;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class AdapterColorSelector extends BaseAdapter {

    private float s, v;

    public AdapterColorSelector(double s, double v){
        this.s = (float) s;
        this.v = (float) v;
    }

    @Override
    public int getCount() {
        return 120;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_color_block, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else viewHolder = (ViewHolder) view.getTag();

        viewHolder.ivColor.setBackgroundColor(Color.HSVToColor(new float[]{i*3, s, v}));
        WindowManager windowManager = (WindowManager) viewGroup.getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = (int) (windowManager.getDefaultDisplay().getWidth() * 0.75 / 6);
        viewHolder.ivColor.getLayoutParams().height = viewHolder.ivColor.getLayoutParams().width = width;
        return view;
    }

    private class ViewHolder{
        ImageView ivColor;
        public ViewHolder(View view){
            ivColor = view.findViewById(R.id.ivColor);
        }
    }

    public void updateColor(@Nullable Double s, @Nullable Double v){
        if(s != null)this.s = s.floatValue();
        if(v != null)this.v = v.floatValue();
        notifyDataSetChanged();
    }
}
