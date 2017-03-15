package com.cnlive.libs.adapter;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cnlive.libs.PlayerActivity;
import com.cnlive.libs.R;
import com.cnlive.libs.bean.VideoBean;
import com.cnlive.libs.player.MyMediaPlayer;
import com.facebook.drawee.view.SimpleDraweeView;


/**
 * Created by Mr.hou on 2017/2/3.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context mContext;
    private VideoBean videoBean;

    public MyAdapter(Context mContext, VideoBean videoBean) {
        this.mContext = mContext;
        this.videoBean = videoBean;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listview_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.image.setImageURI(videoBean.getItems().get(position).getPic_url());
        holder.describe.setText(videoBean.getItems().get(position).getContent());

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, PlayerActivity.class);
                intent.putExtra("path", videoBean.getItems().get(position).getHigh_url());
                intent.putExtra("title",videoBean.getItems().get(position).getContent());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoBean.getItems().size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView image;
        TextView describe;
        MyMediaPlayer videoView;
        public MyViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            describe = (TextView) itemView.findViewById(R.id.describe);
            videoView= (MyMediaPlayer) itemView.findViewById(R.id.videoView);
        }
    }
    private onItemClick click;

    public void setOnItemClick(onItemClick click) {
        this.click = click;
    }

    public static interface onItemClick {
        void onclick(int position);
    }
}
