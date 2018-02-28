package com.wyt.searchdialog;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.wyt.searchbox.SearchFragment;
import com.wyt.searchbox.custom.IOnSearchClickListener;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, IOnSearchClickListener {

//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
    @BindView(R.id.search_info)
    TextView searchInfo;
    @BindView(R.id.btn)
    Button mBtn;

    private SearchFragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        toolbar.setTitle("SearchDialog");//标题
//        setSupportActionBar(toolbar);
        String[] mVals = new String[]
                {"Hello", "Android", "Weclome Hi ", "Button", "TextView", "Hello",
                        "Android", "Weclome", "Button ImageView", "TextView", "Helloworld",
                        "Android", "Weclome Hello", "Button Text", "TextView"};
        searchFragment = SearchFragment.newInstance(Arrays.asList(mVals),true);
//        searchFragment.setHeadView();

//        toolbar.setOnMenuItemClickListener(this);

        searchFragment.setOnSearchClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单文件
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search://点击搜索
                searchFragment.show(getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
        return true;
    }

    @Override
    public void OnSearchClick(String keyword) {
        searchInfo.setText(keyword);
    }

    @OnClick(R.id.btn)
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn:
                searchFragment.show(getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
    }
}
