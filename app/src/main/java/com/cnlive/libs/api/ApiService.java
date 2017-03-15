package com.cnlive.libs.api;

import com.cnlive.libs.bean.VideoBean;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by Mr.hou on 2017/3/1.
 */

public interface ApiService {
    //   http://m2.qiushibaike.com/article/list/video?page=2&count=30&readarticles=[115762484,115762135,115764350,115761463,115760316,115764445,115763537,115758684]&rqcnt=17&r=804df97a1459411164081

    @GET("/article/list/video?page=2&count=30&readarticles=[115762484,115762135,115764350,115761463,115760316," +
            "115764445,115763537,115758684]&rqcnt=17&r=804df97a1459411164081")
    Observable<VideoBean> getCarBean();
}
