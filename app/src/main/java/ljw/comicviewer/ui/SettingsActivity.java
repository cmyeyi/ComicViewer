package ljw.comicviewer.ui;

import android.content.Context;
import android.os.Bundle;
import android.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ljw.comicviewer.R;
import ljw.comicviewer.http.ComicService;
import ljw.comicviewer.others.MyAppCompatActivity;
import ljw.comicviewer.ui.fragment.setting.SettingFragment;
import ljw.comicviewer.ui.fragment.setting.ThemeFragment;
import ljw.comicviewer.util.StoreUtil;

public class SettingsActivity extends MyAppCompatActivity
        implements ComicService.RequestCallback {
    private String TAG = SettingsActivity.class.getSimpleName()+"----";
    private Context context;
    private FragmentManager fragmentManager;
    private SettingFragment settingFragment;
    private ThemeFragment themeFragment;
    @BindView(R.id.nav_child_title)
    TextView title;
    @BindView(R.id.setting_content)
    RelativeLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;
        ButterKnife.bind(this);
        initView();
    }

    private void initView(){
        title.setText(R.string.mine_setting);
        if(settingFragment==null){
            settingFragment = new SettingFragment();
        }


        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.setting_content, settingFragment).commit();


//        LinearLayout layout = new LinearLayout(context);
//        layout.setOrientation(LinearLayout.HORIZONTAL);
//        layout.setLayoutParams(new
//                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT));
//        Button button1 = new Button(context);
//        button1.setText("漫画柜");
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                StoreUtil.initRuleStore(context,R.raw.manhuagui);
//            }
//        });
//        Button button2 = new Button(context);
//        button2.setText("漫画台");
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                StoreUtil.initRuleStore(context,R.raw.manhuatai);
//            }
//        });
//        Button button3 = new Button(context);
//        button3.setText("知音漫客");
//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                StoreUtil.initRuleStore(context,R.raw.zymk);
//            }
//        });
//        LinearLayout.LayoutParams params = new
//                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.weight = 1;
//        button1.setLayoutParams(params);
//        button2.setLayoutParams(params);
//        button3.setLayoutParams(params);
//        layout.addView(button1);
//        layout.addView(button2);
//        layout.addView(button3);
//        content.addView(layout);
    }

    //按标题栏返回按钮
    public void onBack(View view) {
        finish();
    }

    @Override
    public void onFinish(Object data, String what) {

    }

    @Override
    public void onError(String msg, String what) {

    }
}