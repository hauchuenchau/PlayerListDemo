package com.cnlive.libs.bean;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Administrator on 2016/11/8.
 * 解析
 */

public class ParseVideoBean {
    private List<VideoBean> items;

    public List<VideoBean> getItems() {
        return items;
    }

    public void setItems(List<VideoBean> items) {
        this.items = items;
    }

    public static List<VideoBean> parseData(String json)
    {
        return  new Gson().fromJson(json,ParseVideoBean.class).getItems();
    }
}
