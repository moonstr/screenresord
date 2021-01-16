package net.yrom.screenrecorder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter {
    public Context context;
    public List<UserFragment.Picture> datas;
    public VideoAdapter(Context context,List<UserFragment.Picture> datas){
        this.context=context;
        this.datas=datas;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewXbanner = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new ViewoHolder(viewXbanner);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewoHolder viewoHolder= (ViewoHolder) holder;
        Glide.with(context).load(datas.get(position).getPath()).into(viewoHolder.imageView);
        viewoHolder.name.setText(datas.get(position).getName());
        viewoHolder.time.setText(secondsToFormat(datas.get(position).duration+""));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
    public  class ViewoHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name;
        TextView time;
        public ViewoHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image);
            name=itemView.findViewById(R.id.name);
            time=itemView.findViewById(R.id.time);
        }
    }
    public static String secondsToFormat(String secString){
        Integer date = Integer.parseInt(secString);
        if (date<60) {
            return date+"秒";
        }else if (date>60&&date<3600) {
            int m = date/60;
            int s = date%60;
            return m+"分"+s+"秒";
        }else {
            int h = date/3600;
            int m = (date%3600)/60;
            int s = (date%3600)%60;
            return h+"小时"+m+"分"+s+"秒";
        }
    }
}
