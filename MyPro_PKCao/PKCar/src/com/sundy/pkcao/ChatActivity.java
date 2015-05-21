package com.sundy.pkcao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.sundy.pkcao.service.CacheService;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.EmotionEditText;
import com.sundy.pkcao.tools.OperationFileHelper;
import com.sundy.pkcao.tools.xlistview.XListView;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by sundy on 15/4/26.
 */
public class ChatActivity extends _AbstractActivity implements XListView.IXListViewListener {

    private final String TAG = "ChatActivity";
    private XListView listview;
    private String avimConversationID;
    private AVIMConversation conv;
    private EmotionEditText textEdit;
    private Button sendBtn;
    private View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;

    // 自定义消息响应类
    class CustomMessageHandler extends AVIMMessageHandler {
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
            // 新消息到来了。在这里增加你自己的处理代码。
            String msgContent = message.getContent();
            Log.e("sundy", conversation.getConversationId() + " 收到一条新消息：" + msgContent);
            Toast.makeText(ChatActivity.this, "------>" + conversation.getConversationId() + " 收到一条新消息：" + msgContent, Toast.LENGTH_SHORT).show();
        }
    }

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_view);
        AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
        aq = new AQuery(this);
        init();
    }

    private void init() {
        avimConversationID = getIntent().getStringExtra("avimConversationID");

        conv = CacheService.lookupConv(avimConversationID);
        if (conv == null) {
            throw new NullPointerException("conv is null");
        }

        textEdit = (EmotionEditText) aq.id(R.id.textEdit).getView();
        sendBtn = aq.id(R.id.sendBtn).getButton();
        chatEmotionLayout = aq.id(R.id.chatEmotionLayout).getView();
        chatAddLayout = aq.id(R.id.chatAddLayout).getView();

        textEdit.setOnClickListener(onClick);
        sendBtn.setOnClickListener(onClick);

        aq.id(R.id.btnBack).clicked(onClick);

        listview = (XListView) aq.id(R.id.listview).getView();
        listview.setPullRefreshEnable(true);
        listview.setPullLoadEnable(false);
        listview.setXListViewListener(this);
    }

    private void sendMsg() {
        String msg = textEdit.getText().toString().trim();
        if (msg.length() == 0)
            return;
        Log.e("sundy", "----------->msg=" + msg);
        AVIMMessage message = new AVIMMessage();
        message.setContent(msg);
        conv.sendMessage(message, new AVIMConversationCallback() {
            @Override
            public void done(AVException e) {
                Log.e("sundy", "----------->e =" + e);
                if (null != e) {

                } else {

                }
            }
        });
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnBack:
                    finish();
                    break;
                case R.id.textEdit:
                    hideBottomLayoutAndScrollToLast();
                    break;
                case R.id.sendBtn:
                    sendMsg();
                    break;
            }
        }
    };

    private void hideBottomLayoutAndScrollToLast() {
        hideBottomLayout();
        scrollToLast();
    }

    private void hideBottomLayout() {
        hideAddLayout();
        chatEmotionLayout.setVisibility(View.GONE);
    }

    public void scrollToLast() {
        try {
            listview.post(new Runnable() {
                @Override
                public void run() {
                    ListAdapter adapter = listview.getAdapter();
                    if (adapter != null)
                        listview.smoothScrollToPosition(adapter.getCount() - 1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAddLayout() {
        chatAddLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }
}
