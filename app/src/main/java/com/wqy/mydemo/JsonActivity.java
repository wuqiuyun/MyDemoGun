package com.wqy.mydemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.PatternSyntaxException;

public class JsonActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_banner;
    private TextView tv_json;
    private Button btn_fresh;
    private Button btn_show;
    private ArrayList<String> mWords;
    private String mJson;
    private EditText chats_view;
    private MarqueeTextView marqueeTv;
    private String [] textArrays = new String[]{"this is content No.1","this is content No.2","this is content No.3"};
    private String [] textArrays1 = new String[]{"this is content No.1"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);
        initView();
        darkWord();
    }

    private void initView() {
        tv_banner = findViewById(R.id.textView1);
        tv_json = findViewById(R.id.textView2);
        btn_fresh = findViewById(R.id.buttom1);
        btn_show = findViewById(R.id.buttom2);
        btn_fresh.setOnClickListener(this);
        btn_show.setOnClickListener(this);
        chats_view = (EditText) findViewById(R.id.chats_view);
        marqueeTv = (MarqueeTextView) findViewById(R.id.marqueeTv);

        marqueeTv.setTextArraysAndClickListener(textArrays1, new MarqueeTextViewClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JsonActivity.this,AnotherActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        marqueeTv.releaseResources();
        super.onDestroy();
    }

    private List<String> darkWord() {
        mWords = new ArrayList<>();
        mWords.add("1买买买真爽");
        mWords.add("2云南旅游大坑");
        mWords.add("3丽江导游巨坑");
        mWords.add("4购物团真坑今天天气");
        return mWords;
    }

    private HotWord fromJsonString(String str) {
        HotWord hotWord = new HotWord();
        try {
            JSONObject json = new JSONObject(str);
            hotWord.trans = json.getString("trans");
            String list = json.getString("words");
            hotWord.size = json.getInt("size");
            hotWord.words = Arrays.asList(list.split(","));
            hotWord.name = json.getString("name");
            hotWord.number = json.getInt("number");
            hotWord.state = json.getInt("state");
            Log.e("log", "Object数据对象:" + hotWord.toString());
            return hotWord;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new HotWord();
    }

    private String toJsonString(List<String> list, String trans, int size, HotWord interveneWord) {
        JSONStringer stringer = new JSONStringer();
        try {
            stringer.object();
            Log.e("log", "list to string:" + listToString(list, ","));
            stringer.key("trans").value(trans);
            stringer.key("words").value(listToString(list, ","));
            stringer.key("size").value(size);

            stringer.key("name").value(interveneWord.name);
            stringer.key("number").value(interveneWord.number);
            stringer.key("state").value(interveneWord.state);
            stringer.endObject();
            return stringer.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String listToString(List<String> list, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                stringBuilder.append(list.get(i));
                if (i != list.size() - 1) {
                    stringBuilder.append(separator);
                }
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttom1:
                fresh();

                break;
            case R.id.buttom2:
                Log.e("log", "JSON字符串：" + getHotWord());
                show();
                break;
        }
    }

    private void show() {
        HotWord hotWord = fromJsonString(mJson);
        tv_json.setText(hotWord.toString());
    }

    private void fresh() {
        tv_banner.setText(getBanner());

        chats_view.setSelection(chats_view.getText().length() , chats_view.getText().length());
        chats_view.setMovementMethod(ScrollingMovementMethod.getInstance());
        chats_view.setText(getBanner());
    }


//    private String hotWork() {
//        ++current;
//        mJson = current + "{\"trans\": \"\",\"words\": \"1买买买真爽,2丽江导游真坑,3购物店真坑,4玉龙雪山巨坑\",\"size\": 4,\"InterveneWord\": {\"name\": \"买到就是赚到\",\"number\": 3,\"status\": 0}}";
//    }

    public String getBanner() {
        int i = new Random().nextInt(4);
        return mWords.get(i);
    }

    private String getHotWord() {
        HotWord hotWord = new HotWord();
        hotWord.trans = "trans";
        hotWord.size = 4;
        hotWord.words = darkWord();
        hotWord.name = "剁手";
        hotWord.number = 3;
        hotWord.state = 0;

        Log.e("log", "string list:" + darkWord());
        mJson = toJsonString(darkWord(), hotWord.trans, hotWord.size, hotWord);

        return mJson;
    }
}
