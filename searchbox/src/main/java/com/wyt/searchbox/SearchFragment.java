package com.wyt.searchbox;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wyt.searchbox.adapter.SearchHistoryAdapter;
import com.wyt.searchbox.custom.CircularRevealAnim;
import com.wyt.searchbox.custom.IOnItemClickListener;
import com.wyt.searchbox.custom.IOnSearchClickListener;
import com.wyt.searchbox.db.SearchHistoryDB;
import com.wyt.searchbox.tagview.FlowTagLayout;
import com.wyt.searchbox.tagview.OnTagClickListener;
import com.wyt.searchbox.tagview.TagAdapter;
import com.wyt.searchbox.utils.KeyBoardUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Won on 2017/1/13.
 */

public class SearchFragment extends DialogFragment implements DialogInterface.OnKeyListener, ViewTreeObserver.OnPreDrawListener, CircularRevealAnim.AnimListener, IOnItemClickListener, View.OnClickListener {

    public static final String TAG = "SearchFragment";
    private ImageView ivSearchBack;
    private EditText etSearchKeyword;
    private ImageView ivSearchSearch;
    private RecyclerView rvSearchHistory;
    private View searchUnderline;
    private TextView tvSearchClean;
    private View viewSearchOutside;

    private LayoutInflater mInflater;
    private View view;

    //动画
    private CircularRevealAnim mCircularRevealAnim;
    //历史搜索记录
    private ArrayList<String> allHistorys = new ArrayList<>();
    private ArrayList<String> historys = new ArrayList<>();
    //适配器
    private SearchHistoryAdapter searchHistoryAdapter;
    //数据库
    private SearchHistoryDB searchHistoryDB;

    //topview
    private View top;
    private boolean isLateye = false;//是否启用 最近标签
    //常用标签
    private FlowTagLayout mCommonFlowLayout;
    private TagAdapter<String> mCommonTagAdapter;
    private List<String> commonList;

    //最近
    private FlowTagLayout mLatelyFlowLayout;
    private TagAdapter<String> mLatelyTagAdapter;
    private List<String> latelyList;


    public static SearchFragment newInstance(List<String> commonList, boolean isLateye) {
        Bundle bundle = new Bundle();
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(bundle);
        if (commonList != null) {
            searchFragment.commonList = commonList;
        } else {
            List<String> list = new ArrayList<>();
            searchFragment.commonList = list;
        }
        List<String> list = new ArrayList<>();
        searchFragment.latelyList = list;
        searchFragment.isLateye = isLateye;

        return searchFragment;
    }

    public static SearchFragment newInstance() {
        Bundle bundle = new Bundle();
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(bundle);
        return searchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
    }

    @Override
    public void onStart() {
        super.onStart();
        initDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        view = inflater.inflate(R.layout.dialog_search, container, false);
        init();//实例化
        return view;
    }

    private void init() {
        ivSearchBack = (ImageView) view.findViewById(R.id.iv_search_back);
        etSearchKeyword = (EditText) view.findViewById(R.id.et_search_keyword);
        ivSearchSearch = (ImageView) view.findViewById(R.id.iv_search_search);
        rvSearchHistory = (RecyclerView) view.findViewById(R.id.rv_search_history);
        searchUnderline = (View) view.findViewById(R.id.search_underline);
        tvSearchClean = (TextView) view.findViewById(R.id.tv_search_clean);
        viewSearchOutside = (View) view.findViewById(R.id.view_search_outside);

        //实例化动画效果
        mCircularRevealAnim = new CircularRevealAnim();
        //监听动画
        mCircularRevealAnim.setAnimListener(this);

        getDialog().setOnKeyListener(this);//键盘按键监听
        ivSearchSearch.getViewTreeObserver().addOnPreDrawListener(this);//绘制监听

        //实例化数据库
        searchHistoryDB = new SearchHistoryDB(getContext(), SearchHistoryDB.DB_NAME, null, 1);

        allHistorys = searchHistoryDB.queryAllHistory();
        historys=allHistorys;
//        setAllHistorys();
        //初始化recyclerView
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(getContext()));//list类型
        searchHistoryAdapter = new SearchHistoryAdapter(R.layout.item_search_history, historys);
        rvSearchHistory.setAdapter(searchHistoryAdapter);
        setHeadView();
        //设置删除单个记录的监听
        searchHistoryAdapter.setOnItemClickListener(this);
        //监听编辑框文字改变
        etSearchKeyword.addTextChangedListener(new TextWatcherImpl());
        //监听点击
        ivSearchBack.setOnClickListener(this);
        viewSearchOutside.setOnClickListener(this);
        ivSearchSearch.setOnClickListener(this);
        tvSearchClean.setOnClickListener(this);

    }

    /**
     * 设置常用（预设数据）和最近(5条输入过的记录)
     */
    public void setHeadView() {
        if (commonList != null) {
            top = mInflater.inflate(R.layout.item_head_search_common, (ViewGroup) rvSearchHistory.getParent(), false);
            TextView title = (TextView) top.findViewById(R.id.title_common);
            title.setVisibility(View.VISIBLE);
            mCommonFlowLayout = (FlowTagLayout) top.findViewById(R.id.size_flow_layout);
            mCommonTagAdapter = new TagAdapter<>(getActivity());
            mCommonFlowLayout.setAdapter(mCommonTagAdapter);
            mCommonFlowLayout.setOnTagClickListener(new OnTagClickListener() {
                @Override
                public void onItemClick(FlowTagLayout parent, View view, int position) {
//                Snackbar.make(view, "颜色:" + parent.getAdapter().getItem(position), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                    iOnSearchClickListener.OnSearchClick(commonList.get(position));
                    hideAnim();
                }
            });
            mCommonTagAdapter.onlyAddAll(commonList);
        }
        if (isLateye) {
            if (top == null) {
                top = mInflater.inflate(R.layout.item_head_search_common, (ViewGroup) rvSearchHistory.getParent(), false);
            }
            final RelativeLayout layout = (RelativeLayout) top.findViewById(R.id.title_lately_layout);
            layout.setVisibility(View.VISIBLE);
            final Button delete = (Button) top.findViewById(R.id.lately_delete_btn);
            mLatelyFlowLayout = (FlowTagLayout) top.findViewById(R.id.lately_flow_layout);
            mLatelyTagAdapter = new TagAdapter<>(getActivity());
            mLatelyFlowLayout.setAdapter(mLatelyTagAdapter);
            mLatelyFlowLayout.setOnTagClickListener(new OnTagClickListener() {
                @Override
                public void onItemClick(FlowTagLayout parent, View view, int position) {
                    iOnSearchClickListener.OnSearchClick(latelyList.get(position));
                    hideAnim();
                }
            });
            List<String> list = getSpData();
            if (list != null) {
                latelyList.clear();
                latelyList.addAll(list);
            } else {
                latelyList.clear();
                layout.setVisibility(View.GONE);
                mLatelyFlowLayout.setVisibility(View.GONE);
            }
            mLatelyTagAdapter.onlyAddAll(latelyList);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //todo:清除sp里面的数据
                    deleteSpDataAll();
                    layout.setVisibility(View.GONE);
                    mLatelyFlowLayout.setVisibility(View.GONE);
                }
            });
        }
        //加个头View
        if (mCommonFlowLayout != null || mLatelyFlowLayout != null) {
            TextView title = (TextView) top.findViewById(R.id.title_history);
            title.setVisibility(View.VISIBLE);
            searchHistoryAdapter.addHeaderView(top);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_search_back || view.getId() == R.id.view_search_outside) {
            hideAnim();
        } else if (view.getId() == R.id.iv_search_search) {
            search();
        } else if (view.getId() == R.id.tv_search_clean) {
            searchHistoryDB.deleteAllHistory();
            historys.clear();
            searchUnderline.setVisibility(View.GONE);
            searchHistoryAdapter.setNewData(historys);
//            searchHistoryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 初始化SearchFragment
     */
    private void initDialog() {
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.98); //DialogSearch的宽
        window.setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.TOP);
        window.setWindowAnimations(R.style.DialogEmptyAnimation);//取消过渡动画 , 使DialogSearch的出现更加平滑
    }

    /**
     * 监听键盘按键
     */
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            hideAnim();
        } else if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            search();
        }
        return false;
    }

    /**
     * 监听搜索键绘制时
     */
    @Override
    public boolean onPreDraw() {
        ivSearchSearch.getViewTreeObserver().removeOnPreDrawListener(this);
        mCircularRevealAnim.show(ivSearchSearch, view);
        return true;
    }

    /**
     * 搜索框动画隐藏完毕时调用
     */
    @Override
    public void onHideAnimationEnd() {
        etSearchKeyword.setText("");
        dismiss();
    }

    /**
     * 搜索框动画显示完毕时调用
     */
    @Override
    public void onShowAnimationEnd() {
        if (isVisible()) {
            KeyBoardUtils.openKeyboard(getContext(), etSearchKeyword);
        }
    }

    /**
     * 监听编辑框文字改变
     */
    private class TextWatcherImpl implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String keyword = editable.toString();
            if (TextUtils.isEmpty(keyword.trim())) {
                if (top != null) {
                    top.setVisibility(View.VISIBLE);
                }
                setAllHistorys();
//                searchHistoryAdapter.notifyDataSetChanged();
            } else {
                if (top != null) {
                    top.setVisibility(View.GONE);
                }
                setKeyWordHistorys(editable.toString());
            }
        }
    }

    /**
     * 点击单个搜索记录
     */
    @Override
    public void onItemClick(String keyword) {
        //保存进sp
        saveSpData(keyword);
        iOnSearchClickListener.OnSearchClick(keyword);
        hideAnim();
    }

    /**
     * 删除单个搜索记录
     */
    @Override
    public void onItemDeleteClick(String keyword) {
        System.out.println("ttttt");
        searchHistoryDB.deleteHistory(keyword);
        historys.remove(keyword);
        System.out.println(historys.size()+"====");
//        searchHistoryAdapter.getData().remove(keyword);
        searchHistoryAdapter.setNewData(historys);
        checkHistorySize();
//        searchHistoryAdapter.notifyDataSetChanged();
    }

    private void hideAnim() {
        KeyBoardUtils.closeKeyboard(getContext(), etSearchKeyword);
        mCircularRevealAnim.hide(ivSearchSearch, view);
    }

    private void search() {
        String searchKey = etSearchKeyword.getText().toString();
        if (TextUtils.isEmpty(searchKey.trim())) {
            Toast.makeText(getContext(), "请输入关键字", Toast.LENGTH_SHORT).show();
        } else {
            iOnSearchClickListener.OnSearchClick(searchKey);//接口回调
            if (!searchHistoryDB.queryHistory(searchKey)){
                searchHistoryDB.insertHistory(searchKey);//插入到数据库
            }
            saveSpData(searchKey);//插入到sp
            hideAnim();
        }
    }

    private void checkHistorySize() {
        if (historys.size() < 1) {
            searchUnderline.setVisibility(View.GONE);
        } else {
            searchUnderline.setVisibility(View.VISIBLE);
        }
    }

    private void setAllHistorys() {
        allHistorys = searchHistoryDB.queryAllHistory();
//        historys.clear();
//        historys.addAll(allHistorys);
        searchHistoryAdapter.setNewData(allHistorys);
        checkHistorySize();
    }

    private void setKeyWordHistorys(String keyword) {
        allHistorys = searchHistoryDB.queryAllHistory();
        historys.clear();
        for (String string : allHistorys) {
            if (string.contains(keyword)) {
                historys.add(string);
            }
        }
//        searchHistoryAdapter.notifyDataSetChanged();
        searchHistoryAdapter.setNewData(historys);
        checkHistorySize();
    }

    private IOnSearchClickListener iOnSearchClickListener;

    public void setOnSearchClickListener(IOnSearchClickListener iOnSearchClickListener) {
        this.iOnSearchClickListener = iOnSearchClickListener;
    }

    /**
     * 获取最近数据
     */
    private List<String> getSpData() {
        SharedPreferences userSettings = getActivity().getSharedPreferences("setting", 0);
        String list = userSettings.getString("latelyList", null);
        if (list != null) {
            String[] xx = list.split(",");
            List<String> newlist = Arrays.asList(xx);
            List<String> arrayList = new ArrayList(newlist);
            return arrayList;
        } else {
            return null;
        }
    }

    /**
     * 保存一个最近数据
     */
    private void saveSpData(String str) {
        SharedPreferences userSettings = getActivity().getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = userSettings.edit();
        List<String> newlist = getSpData();
        if (newlist == null) {
            newlist = new ArrayList<>();
        }
        if (newlist.contains(str)) {//已存在，就不保存
            return;
        }
        if (newlist.size() == 5) {
            newlist.add(0, str);//插入到第一个
            newlist.remove(newlist.size());//删除最后一个
        } else {
            newlist.add(0, str);//插入到第一个
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < newlist.size(); i++) {
            if (i == 0) {
                buffer.append(newlist.get(i));
            } else {
                buffer.append("," + newlist.get(i));
            }
        }
        editor.putString("latelyList", buffer.toString());
        editor.commit();
    }

    /**
     * 清除所有最近数据
     */
    private void deleteSpDataAll() {
        SharedPreferences userSettings = getActivity().getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.clear();
        editor.commit();
    }
}
