package com.example.tjarnav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.example.tjarnav.adapter.InputTipsAdapter;
import com.example.tjarnav.adapter.OnItemClickListener;
import com.example.tjarnav.util.Constants;
import com.example.tjarnav.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class InputTipsActivity extends Activity implements SearchView.OnQueryTextListener, Inputtips.InputtipsListener, View.OnClickListener {
    private SearchView mSearchView;// 搜索输入框
    private ImageView mBack; // 返回键
    private RecyclerView mInputListView; // 提示列表
    private List<Tip> mCurrentTipList; // 提示列表数据
    private InputTipsAdapter mIntipAdapter; // 列表适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_tips);
        initSearchView();
        mInputListView = (RecyclerView) findViewById(R.id.inputtip_list);

        mBack = (ImageView) findViewById(R.id.back);
        mBack.setOnClickListener(this);
    }

    private void initSearchView() {
        mSearchView = (SearchView) findViewById(R.id.keyWord);
        mSearchView.setOnQueryTextListener(this);

        //设置SearchView默认为展开显示
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
    }

    /**
     * 输入提示回调
     *
     * @param tipList
     * @param rCode
     */
    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        Log.d("hgy", "onGetInputtips: tipList=" + tipList.size()+ "," + tipList);

        if (rCode == 1000) {// 正确返回
            boolean hasSnippet = true;
            while(hasSnippet){
                if(tipList.get(0).getPoint() == null){
                    tipList.remove(0);
                }else{
                    hasSnippet = false;
                }
            }
            Log.d("hgy", "onGetInputtips: tipList=" + tipList.size()+ "," + tipList);
            mCurrentTipList = tipList;
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            mInputListView.setLayoutManager(new LinearLayoutManager(this));
            mIntipAdapter = new InputTipsAdapter(getApplicationContext(), mCurrentTipList);
            mIntipAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view) {
                    int position = mInputListView.getChildAdapterPosition(view);
                    Tip tip = mCurrentTipList.get(position);
                    Log.d("hgy", "onItemClick: " + position + "," + tip);
                    Intent intent = new Intent();
                    intent.putExtra(Constants.EXTRA_TIP, tip);
                    setResult(NavActivity.RESULT_CODE_INPUTTIPS, intent);
                    finish();
                }
            });
            mInputListView.setAdapter(mIntipAdapter);
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    /**
     * 按下确认键触发，本例为键盘回车或搜索键
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * 输入字符变化时触发
     *
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (!IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, Constants.DEFAULT_CITY);
            Inputtips inputTips = new Inputtips(InputTipsActivity.this.getApplicationContext(), inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        } else {
            if (mIntipAdapter != null && mCurrentTipList != null) {
                mCurrentTipList.clear();
                mIntipAdapter.notifyDataSetChanged();
            }
        }
        return false;
    }

    // 返回按钮单击事件
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            this.finish();
        }
    }

    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0);
    }
}
