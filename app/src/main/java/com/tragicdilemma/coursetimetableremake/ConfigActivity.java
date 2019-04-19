package com.tragicdilemma.coursetimetableremake;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

public class ConfigActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private GridView gvTimeTable;
    private AdapterTimeTable adapter;
    private boolean[] choosedSession;

    private Integer duplicatePosition;
    private NavigationView navigationView;
    private boolean darkMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        darkMode = Boolean.parseBoolean(DBHelperTimeTable.getInstance(getApplicationContext()).getPref(DBHelperTimeTable.PREF_DARK_MODE));
        init();
    }

    private void init(){
        if(darkMode)setContentView(R.layout.activity_main_dark);
        else setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        new Thread(new Runnable() {
            @Override
            public void run() {
                initTimetable();
            }
        }).start();
        initNav();

        choosedSession = new boolean[13*7];

        gvTimeTable = findViewById(R.id.gvTimeTable);
    }

    private void initTimetable(){
        ConstraintLayout cvHeight = findViewById(R.id.cvHeight);
        while(cvHeight.getHeight() == 0);
        final int height = findViewById(R.id.cvHeight).getHeight();
        int width = findViewById(R.id.cvHeight).getWidth();
        findViewById(R.id.tvLabelBlk).getLayoutParams().width = width;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(darkMode)adapter = new AdapterTimeTable(getApplicationContext(), R.layout.item_course_dark, DBHelperTimeTable.getInstance(getApplicationContext()).getCursor(),
                        new String[]{}, new int[]{}, 0, height, choosedSession, darkMode);
                else adapter = new AdapterTimeTable(getApplicationContext(), R.layout.item_course, DBHelperTimeTable.getInstance(getApplicationContext()).getCursor(),
                        new String[]{}, new int[]{}, 0, height, choosedSession, darkMode);
                gvTimeTable.setOnItemClickListener(clrItem);
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void initNav(){
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_paste).setVisible(false);
        View view = menu.findItem(R.id.nav_night).getActionView();
        Switch swDark = view.findViewById(R.id.swDark);
        swDark.setOnCheckedChangeListener(clrDark);
        swDark.setChecked(darkMode);
    }

    private Switch.OnCheckedChangeListener clrDark = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(darkMode != b){
                darkMode = b;
                DBHelperTimeTable.getInstance(getApplicationContext()).setPref(DBHelperTimeTable.PREF_DARK_MODE, String.valueOf(darkMode));
                recreate();
            }
        }
    };

    private AdapterView.OnItemClickListener clrItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            choosedSession[i] = !choosedSession[i];
            if(!(choosedSession[i] ^ darkMode)){
                view.findViewById(R.id.clCourse).setBackgroundColor(Color.WHITE);
                TextView tvName = view.findViewById(R.id.tvCourseName);
                tvName.setTextColor(Color.BLACK);
            }else{
                view.findViewById(R.id.clCourse).setBackgroundColor(getColor(R.color.colorGray));
                TextView tvName = view.findViewById(R.id.tvCourseName);
                tvName.setTextColor(Color.WHITE);
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if(id == R.id.nav_add) {
            if(getChoosedCount() > 0){
                String name = null, room = null;
                Integer color = null;
                DBHelperTimeTable dbHelperTimeTable = DBHelperTimeTable.getInstance(getApplicationContext());
                for(int i = 0; i < 13*7; i++){
                    if(choosedSession[i]){
                        ContentValues cv = dbHelperTimeTable.getSingleData(i+1);
                        if(name != null) {
                            if (!name.equals("") && !cv.getAsString(DBHelperTimeTable.COLUMN_NAME).equals(name))name = "";
                        }else name = cv.getAsString(DBHelperTimeTable.COLUMN_NAME);
                        if(room != null){
                            if (!room.equals("") && !cv.getAsString(DBHelperTimeTable.COLUMN_ROOM).equals(room))room = "";
                        }else room = cv.getAsString(DBHelperTimeTable.COLUMN_ROOM);
                        if(color != null){
                            if(!color.equals(Color.HSVToColor(new float[]{340,0.749f ,1})) && !cv.getAsInteger(DBHelperTimeTable.COLUMN_COLOR).equals(color))color = Color.HSVToColor(new float[]{340, 0.749f, 1});
                        }else color = cv.getAsInteger(DBHelperTimeTable.COLUMN_COLOR);
                    }
                }
                addingDialog(name, room, color).show();
            }else showMessage(getApplicationContext(), getString(R.string.msg_warning_select), true).show();
        }else if(id == R.id.nav_copy){
            if(getChoosedCount() == 0)showMessage(getApplicationContext(), getString(R.string.msg_warning_select), true).show();
            else if(getChoosedCount() > 1)showMessage(getApplicationContext(), getString(R.string.msg_warning_select_one), true).show();
            else{
                for(duplicatePosition = 0; duplicatePosition < 13*7; duplicatePosition++)if(choosedSession[duplicatePosition])break;
                ContentValues cv = DBHelperTimeTable.getInstance(getApplicationContext()).getSingleData(duplicatePosition + 1);
                String message = getString(R.string.msg_detail_1) + cv.getAsString(DBHelperTimeTable.COLUMN_NAME) + "\n"
                        + getString(R.string.msg_detail_2) + cv.getAsString(DBHelperTimeTable.COLUMN_ROOM);
                showMessage(getApplicationContext(), message, false).show();
                navigationView.getMenu().findItem(R.id.nav_paste).setVisible(true);
                choosedSession = new boolean[13*7];
                updateTimeTable();
            }
        }else if(id == R.id.nav_delete) {
            if (getChoosedCount() == 0)showMessage(getApplicationContext(), getString(R.string.msg_warning_select), true).show();
            else {
                showMessage(getApplicationContext(), getString(R.string.msg_delete_1) + " " + getChoosedCount() + " " + getString(R.string.msg_delete_2), false).show();
                updateDB("", "" , Color.HSVToColor(new float[]{340, 0.749f, 1}));
            }
        }else if(id == R.id.nav_paste){
            if(getChoosedCount() == 0)showMessage(getApplicationContext(), getString(R.string.msg_warning_select), true).show();
            else{
                showMessage(getApplicationContext(), getString(R.string.msg_copy_1) + " " + getChoosedCount() + " " + getString(R.string.msg_copy_2), false).show();
                ContentValues cv = DBHelperTimeTable.getInstance(getApplicationContext()).getSingleData(duplicatePosition + 1);
                updateDB(cv.getAsString(DBHelperTimeTable.COLUMN_NAME), cv.getAsString(DBHelperTimeTable.COLUMN_ROOM), cv.getAsInteger(DBHelperTimeTable.COLUMN_COLOR));
                navigationView.getMenu().findItem(R.id.nav_paste).setVisible(false);
            }
        }else if(id == R.id.nav_clear){
            showMessage(getApplicationContext(), getString(R.string.msg_clear), false).show();
            for(int i = 0; i < 13*7; i++)choosedSession[i] = true;
            updateDB("", "" , Color.HSVToColor(new float[]{340, 0.749f, 1}));
        }else if(id == R.id.nav_setting_context){
            settingContextDialog().show();
        }else if(id == R.id.nav_setting_widget){
            settingWidgetDialog().show();
        }else if(id == R.id.nav_output){
            if (Build.VERSION.SDK_INT >= 23) {
                int REQUEST_CODE_CONTACT = 101;
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                for(String str : permissions){
                    if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED)this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
                for(String str : permissions){
                    if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED){
                        showMessage(getApplicationContext(), getString(R.string.msg_permission), true).show();
                        return true;
                    }
                }
                String fileName;
                if(darkMode)fileName = ImageGenerator.saveImage(ImageGenerator.generateSizedImage(this, getColor(R.color.colorGray)));
                else fileName = ImageGenerator.saveImage(ImageGenerator.generateSizedImage(this, Color.WHITE));
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "CourseTimeTable" + File.separator + fileName);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".org.tragicdilemma.provider", file);
                intent.setDataAndType(uri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                showMessage(getApplicationContext(), getString(R.string.msg_create_img) + file.getPath(), false).show();
            }
        }
        if(id != R.id.nav_night)drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private AlertDialog settingContextDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_context_setting, null);
        final SeekBar sbContextSize = view.findViewById(R.id.sbContextName);
        final SeekBar sbContextSizeRoom = view.findViewById(R.id.sbContextRoom);
        sbContextSize.setProgress(Integer.parseInt(DBHelperTimeTable.getInstance(getApplicationContext()).getPref(DBHelperTimeTable.PREF_CONTEXT_SIZE)));
        sbContextSizeRoom.setProgress(Integer.parseInt(DBHelperTimeTable.getInstance(getApplicationContext()).getPref(DBHelperTimeTable.PREF_CONTEXT_SIZE_ROOM)));
        dialogBuilder.setView(view)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DBHelperTimeTable.getInstance(getApplicationContext()).setPref(DBHelperTimeTable.PREF_CONTEXT_SIZE, String.valueOf(sbContextSize.getProgress()));
                        DBHelperTimeTable.getInstance(getApplicationContext()).setPref(DBHelperTimeTable.PREF_CONTEXT_SIZE_ROOM, String.valueOf(sbContextSizeRoom.getProgress()));
                        updateTimeTable();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null);
        return dialogBuilder.create();
    }

    private AlertDialog settingWidgetDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_widget_setting, null);

        final DBHelperTimeTable dbHelperTimeTable = DBHelperTimeTable.getInstance(getApplicationContext());
        final RadioGroup rgTitleColor = view.findViewById(R.id.rgTitleColor);
        if(Boolean.valueOf(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_TITLE_COLOR)))rgTitleColor.check(R.id.rbTitleColorDark);
        else rgTitleColor.check(R.id.rbTitleColor);
        final ImageButton ibtnTitleColor = view.findViewById(R.id.ibtnTitleColor);
        ibtnTitleColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = (int) view.getTag();
                float[] hsv = new float[3];
                Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
                colorDialog((ImageButton) view, (int)(hsv[1]*1000), (int)(hsv[2]*1000)).show();
            }
        });
        int color = Integer.valueOf(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_TITLE_BACKGROUND));
        ibtnTitleColor.setBackgroundColor(color);
        ibtnTitleColor.setTag(color);
        final SeekBar sbTitleSize = view.findViewById(R.id.sbTitleSize);
        sbTitleSize.setProgress(Integer.parseInt(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_TITLE_SIZE)));
        final RadioGroup rgContextColor = view.findViewById(R.id.rgContextColor);
        if(Boolean.valueOf(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_COLOR)))rgContextColor.check(R.id.rbContextColorDark);
        else rgContextColor.check(R.id.rbContextColor);
        final ImageButton ibtnContextColor = view.findViewById(R.id.ibtnContextColor);
        ibtnContextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = (int) view.getTag();
                float[] hsv = new float[3];
                Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
                colorDialog((ImageButton)view, (int)(hsv[1]*1000), (int)(hsv[2]*1000)).show();
            }
        });
        final SeekBar sbOpacity = view.findViewById(R.id.sbContextOpacity);
        color = Integer.valueOf(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_BACKGROUND));
        sbOpacity.setProgress(Color.alpha(color));
        ibtnContextColor.setBackgroundColor(color | 0xff000000);
        ibtnContextColor.setTag(color);
        final SeekBar sbContextSize = view.findViewById(R.id.sbContextSize);
        final SeekBar sbContextSizeRoom = view.findViewById(R.id.sbContextSizeRoom);
        sbContextSize.setProgress(Integer.parseInt(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_SIZE)));
        sbContextSizeRoom.setProgress(Integer.parseInt(dbHelperTimeTable.getPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_SIZE_ROOM)));

        dialogBuilder.setView(view)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(rgTitleColor.getCheckedRadioButtonId() == R.id.rbTitleColorDark)dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_TITLE_COLOR, "true");
                        else dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_TITLE_COLOR, "false");
                        dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_TITLE_BACKGROUND, String.valueOf(ibtnTitleColor.getTag()));
                        dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_TITLE_SIZE, String.valueOf(sbTitleSize.getProgress()));
                        if(rgContextColor.getCheckedRadioButtonId() == R.id.rbContextColorDark)dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_COLOR, "true");
                        else dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_COLOR, "false");
                        int color = (int) ibtnContextColor.getTag();
                        int a = sbOpacity.getProgress();
                        color = (color & 0xffffff) | ((a & 0xff) << 24);
                        dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_BACKGROUND, String.valueOf(color));
                        dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_SIZE, String.valueOf(sbContextSize.getProgress()));
                        dbHelperTimeTable.setPref(DBHelperTimeTable.PREF_WIDGET_CONTEXT_SIZE_ROOM, String.valueOf(sbContextSizeRoom.getProgress()));
                        updateWidget();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null);
        return dialogBuilder.create();

    }

    private AlertDialog addingDialog(String name, String room, int color){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_adding, null);
        final EditText etName = view.findViewById(R.id.etName);
        final EditText etRoom = view.findViewById(R.id.etRoom);
        final ImageButton ibtnColor = view.findViewById(R.id.ibtnColor);

        etName.setText(name);
        etRoom.setText(room);
        ibtnColor.setBackgroundColor(color);
        ibtnColor.setTag(color);

        dialogBuilder.setView(view)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showMessage(getApplicationContext(), getString(R.string.msg_add_1) + " " + getChoosedCount() + " " + getString(R.string.msg_add_2), false).show();
                        updateDB(etName.getText().toString(), etRoom.getText().toString(), (int)ibtnColor.getTag());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog dialog = dialogBuilder.create();
        ibtnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = (int) view.getTag();
                float[] hsv = new float[3];
                Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
                colorDialog((ImageButton) view, (int)(hsv[1]*1000), (int)(hsv[2]*1000)).show();
            }
        });

        return dialog;
    }

    private AlertDialog colorDialog(final ImageButton imageButton, int s, int v){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_color, null);
        final SeekBar sbS = view.findViewById(R.id.sbS);
        final SeekBar sbV = view.findViewById(R.id.sbV);
        sbS.setProgress(s);
        sbV.setProgress(v);
        GridView gvColor = view.findViewById(R.id.gvColor);
        final AdapterColorSelector adapterColorSelector = new AdapterColorSelector(s/1000.f, v/1000.f);
        dialogBuilder.setView(view)
                .setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog dialog = dialogBuilder.create();
        gvColor.setAdapter(adapterColorSelector);
        gvColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int h = i * 3;
                float s = sbS.getProgress()/1000.f, v = sbV.getProgress()/1000.f;
                imageButton.setBackgroundColor(Color.HSVToColor(new float[]{h, s, v}));
                imageButton.setTag(Color.HSVToColor(new float[]{h, s, v}));
                dialog.dismiss();
            }
        });
        sbS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                adapterColorSelector.updateColor((double) (i/1000.f), null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                adapterColorSelector.updateColor(null, (double) (i/1000.f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return dialog;
    }

    private int getChoosedCount(){
        int result = 0;
        for(int i = 0; i < 13*7; i++)if(choosedSession[i])result++;
        return result;
    }

    private void updateTimeTable(){
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    private void updateDB(String name, String room, int color){
        final ArrayList<ContentValues> arrayList = new ArrayList<>();
        ArrayList<Integer> id = new ArrayList<>();
        for(int j = 0; j < 13*7; j++){
            if(choosedSession[j]){
                id.add(j);
                ContentValues cv = new ContentValues();
                cv.put("id", j+1);
                cv.put(DBHelperTimeTable.COLUMN_NAME, String.valueOf(name));
                cv.put(DBHelperTimeTable.COLUMN_ROOM, String.valueOf(room));
                cv.put(DBHelperTimeTable.COLUMN_COLOR, color);
                arrayList.add(cv);
            }
        }
        choosedSession = new boolean[13*7];
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBHelperTimeTable.getInstance(getApplicationContext()).setData(arrayList);
                updateTimeTable();
                updateWidget();
            }
        }).start();
    }

    private Toast showMessage(Context context, String msg, boolean isWarning){
        View view;
        if(isWarning)view = LayoutInflater.from(context).inflate(R.layout.toast_warning, null);
        else view = LayoutInflater.from(context).inflate(R.layout.toast_msg, null);
        TextView tvMsg = view.findViewById(R.id.tvMsg);
        tvMsg.setText(msg);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 32);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        return toast;
    }

    private void updateWidget(){
        Intent intent = new Intent(getApplicationContext(), WidgetProvider.class);
        intent.setAction(WidgetProvider.ACTION_WIDGET_UPDATE);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName name =new ComponentName(getApplicationContext(), WidgetProvider.class);
        intent.putExtra("ids", appWidgetManager.getAppWidgetIds(name));
        sendBroadcast(intent);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    gvTimeTable.setAdapter(adapter);
                    break;
                case 1:
                    adapter.changeCursor(DBHelperTimeTable.getInstance(getApplicationContext()).getCursor());
                    adapter.updateChoosed(choosedSession);
                    break;
            }
        }
    };
}
