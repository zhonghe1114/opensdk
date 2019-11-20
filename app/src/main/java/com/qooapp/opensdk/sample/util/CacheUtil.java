package com.qooapp.opensdk.sample.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CacheUtil {
    private static final String KEY_APPIDS = "appids";

    public static void saveChannel(Context context, String appid) {
        List<String> list = getChannels(context);
        if (!list.contains(appid) && appid != null && !"".equals(appid)) {
            list.add(appid);
            JSONArray jsonArray = new JSONArray();
            for (String id : list) {
                jsonArray.put(id);
            }
            SharedPrefsUtils.setStringPreference(context, KEY_APPIDS, jsonArray.toString());
        }
    }

    public static List<String> getChannels(Context context) {

        List<String> dataList = new ArrayList<>();
        String appids = SharedPrefsUtils.getStringPreference(context, KEY_APPIDS);

        if (appids != null && !"".equals(appids)) {
            try {
                JSONArray dataArray = new JSONArray(appids);
                for (int i = 0; i < dataArray.length(); i++) {
                    String channel = dataArray.getString(i);
                    dataList.add(channel);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }
}
