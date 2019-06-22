package com.example.myflickrapp;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myflickrapp.api.FlickrPhoto;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;

import java.util.List;

public class FlickrImageAdapter extends RecyclerView.Adapter {

    private static final String TAG = "FlickrImageAdapter";
    private final String mSearchTerm;
    private List<FlickrPhoto> mFlickrPhotoList;
    private FlickrFetchMoreImageListener mFlickrFetchMoreImageListener;
    int mPage = 1;

    public FlickrImageAdapter(List<FlickrPhoto> flickrPhotoList, String searchTerm) {
        mFlickrPhotoList = flickrPhotoList;
        mSearchTerm = searchTerm;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_layout,
                parent, false);
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
        if (i == mFlickrPhotoList.size() - 1 && mFlickrFetchMoreImageListener != null) {
            mFlickrFetchMoreImageListener.onLoadMore(mSearchTerm, ++mPage);
        }
        String url = mFlickrPhotoList.get(i).url_s;
        if (url != null) {
            myViewHolder.imageView.setImageURI(Uri.parse(url));
        }
    }

    @Override
    public int getItemCount() {
        return mFlickrPhotoList.size();
    }

    public void clear() {
        int size = mFlickrPhotoList.size();
        mFlickrPhotoList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void setFetchMoreImageListener(
            FlickrFetchMoreImageListener flickrFetchMoreImageListener) {
        mFlickrFetchMoreImageListener = flickrFetchMoreImageListener;
    }

    public void addList(List<FlickrPhoto> photo) {
        mFlickrPhotoList.addAll(photo);
        notifyDataSetChanged();
    }

    public String getJsonString() {
        return new Gson().toJson(mFlickrPhotoList);
    }

    public void setPage(int page) {
        this.mPage = page;
    }

    public int getPage() {
        return this.mPage;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView imageView;// init the item view's

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
