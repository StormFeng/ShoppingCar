package com.taobao.shoppingcar;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements UpdateView {

    @BindView(R.id.btn_Edit)
    Button btnEdit;
    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;
    @BindView(R.id.cb_SelectAll)
    SmoothCheckBox cbSelectAll;
    @BindView(R.id.tv_AllMoney)
    TextView tvAllMoney;
    @BindView(R.id.tv_Transport)
    TextView tvTransport;
    @BindView(R.id.btn_Settlement)
    Button btnSettlement;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;

    ExpandableListAdapter adapter;
    StringBuffer stringBuffer;
    GoodBean goodBean;
    boolean isEdit=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        expandableListView.setGroupIndicator(null);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return true;
            }
        });
        AssetManager assetManager = getAssets();
        try {
            InputStream is = assetManager.open("data.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            stringBuffer = new StringBuffer();
            String str = null;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("wqf", stringBuffer.toString());
        Gson gson = new Gson();
        goodBean = gson.fromJson(stringBuffer.toString(), GoodBean.class);
        adapter = new ExpandableListAdapter(this, goodBean);
        adapter.setChangedListener(this);
        expandableListView.setAdapter(adapter);
        for (int i = 0; i < goodBean.getContent().size(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    @Override
    public void update(boolean isAllSelect,int count, int price) {
        btnSettlement.setText("结算("+count+")");
        tvAllMoney.setText("￥"+price);
        cbSelectAll.setChecked(isAllSelect);
    }

    @OnClick({R.id.btn_Edit, R.id.cb_SelectAll})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Edit:
                edit();
                break;
            case R.id.cb_SelectAll:
                selectAll();
                break;
        }
    }

    private void edit(){
        if(isEdit){
            isEdit=false;
            btnEdit.setText("完成");
            for(int i=0;i<goodBean.getContent().size();i++){
                for(int n=0;n<goodBean.getContent().get(i).getGooddetail().size();n++){
                    goodBean.getContent().get(i).getGooddetail().get(n).setIsedit(true);
                }
            }
        }else{
            isEdit=true;
            btnEdit.setText("编辑");
            for(int i=0;i<goodBean.getContent().size();i++){
                for(int n=0;n<goodBean.getContent().get(i).getGooddetail().size();n++){
                    goodBean.getContent().get(i).getGooddetail().get(n).setIsedit(false);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void selectAll(){
        int allCount=goodBean.getAllcount();
        int allMoney=goodBean.getAllmoney();
        if(!cbSelectAll.isChecked()){
            goodBean.setAllSelect(true);
            for(int i=0;i<goodBean.getContent().size();i++){
                goodBean.getContent().get(i).setIsselected(true);
                for(int n=0;n<goodBean.getContent().get(i).getGooddetail().size();n++){
                    if(!goodBean.getContent().get(i).getGooddetail().get(n).isselected()){
                        allCount++;
                        allMoney+=Integer.valueOf(goodBean.getContent().get(i).getGooddetail().get(n).getCount())
                                *Integer.valueOf(goodBean.getContent().get(i).getGooddetail().get(n).getPrice());
                        goodBean.getContent().get(i).getGooddetail().get(n).setIsselected(true);
                    }
                }
            }
        }else{
            goodBean.setAllSelect(false);
            for(int i=0;i<goodBean.getContent().size();i++){
                goodBean.getContent().get(i).setIsselected(false);
                for(int n=0;n<goodBean.getContent().get(i).getGooddetail().size();n++){
                    goodBean.getContent().get(i).getGooddetail().get(n).setIsselected(false);
                }
                allCount=0;
                allMoney=0;
            }
        }
        goodBean.setAllmoney(allMoney);
        goodBean.setAllcount(allCount);
        update(goodBean.isAllSelect(),allCount,allMoney);
        adapter.notifyDataSetChanged();
    }
}
