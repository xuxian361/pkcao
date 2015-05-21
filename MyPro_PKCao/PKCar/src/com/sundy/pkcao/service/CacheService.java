package com.sundy.pkcao.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by sundy on 15/5/20.
 */
public class CacheService {
    private static Map<String, AVIMConversation> cachedConvs = new HashMap<String, AVIMConversation>();
    private static Map<String, AVUser> cachedUsers = new HashMap<String, AVUser>();
    private static List<String> friendIds = new ArrayList<String>();
    private static AVIMConversation curConv;

    public static AVUser lookupUser(String userId) {
        return cachedUsers.get(userId);
    }

    public static void registerUser(AVUser user) {
        cachedUsers.put(user.getObjectId(), user);
    }

    public static void registerUsers(List<AVUser> users) {
        for (AVUser user : users) {
            registerUser(user);
        }
    }

    public static AVIMConversation lookupConv(String convid) {
        return cachedConvs.get(convid);
    }

    public static void registerConvs(List<AVIMConversation> convs) {
        for (AVIMConversation conv : convs) {
            registerConv(conv);
        }
    }

    public static void registerConv(AVIMConversation conv) {
        cachedConvs.put(conv.getConversationId(), conv);
    }

    public static void registerConvIfNone(AVIMConversation conv) {
        if (lookupConv(conv.getConversationId()) == null) {
            registerConv(conv);
        }
    }

    public static List<String> getFriendIds() {
        return friendIds;
    }

    public static void setFriendIds(List<String> friendIds) {
        CacheService.friendIds = Collections.unmodifiableList(friendIds);
    }

    public static AVIMConversation getCurConv() {
        return curConv;
    }

    public static void setCurConv(AVIMConversation curConv) {
        CacheService.curConv = curConv;
    }

    public static boolean isCurConvid(String convid) {
        return curConv != null && curConv.getConversationId().equals(convid);
    }

    public static boolean isCurConv(AVIMConversation conv) {
        if (getCurConv() != null && getCurConv().getConversationId().equals(conv.getConversationId())) {
            return true;
        } else {
            return false;
        }
    }

}
