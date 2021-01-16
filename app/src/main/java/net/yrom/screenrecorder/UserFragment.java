package net.yrom.screenrecorder;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import me.yokeyword.fragmentation.SupportFragment;

public class UserFragment extends SupportFragment {
    RecyclerView  recyclerView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user,container,false);
        recyclerView=view.findViewById(R.id.recycler);
        loadVaule();
        return view;
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        loadVaule();
    }

    String video_path="";
    ArrayList<Picture> listPictures=new ArrayList<>();
    private void loadVaule() {
        File file = getSavingDir();
        //判断文件夹是否存在，如果不存在就创建一个
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] files = file.listFiles();
        if (files==null||files.length==0)return;
        listPictures = new ArrayList<Picture>();
        for (int i = 0; i < files.length; i++) {
            Picture picture = getVideoThumbnail(files[i].getPath(), 200, 200, MediaStore.Images.Thumbnails.MICRO_KIND);
            picture.setPath(files[i].getPath());
            picture.setName(files[i].getName());
            listPictures.add(picture);
        }
        // listView = (ListView) findViewById(R.id.lv_show);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new VideoAdapter(getContext(),listPictures ));
    }


    //获取视频的缩略图
    private Picture getVideoThumbnail(String videoPath, int width, int height, int kind) {
        // 获取视频的缩略图
        Picture picture=null;
        Bitmap bitmap = null;
        int duration=0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime(-1);
            duration = Integer.parseInt(retriever.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION))/1000;
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
//            System.out.println("w"+bitmap.getWidth());
//            System.out.println("h"+bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        picture=new Picture();
        picture.setBitmap(bitmap);
        picture.setDuration(duration);
        return picture;
    }
    private static File getSavingDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "Screenshots");
    }
    public  class Picture{
        Bitmap Bitmap;
        String path;
        String name;
        int duration;

        public Bitmap getBitmap() {
            return Bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            Bitmap = bitmap;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
}
