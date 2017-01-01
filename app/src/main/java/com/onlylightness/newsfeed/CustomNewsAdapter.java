package com.onlylightness.newsfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minsik on 2016-12-31.
 */

/**
 * NewsItem is manipulated in this class.
 * Also, it handles displaying appropriate views.
 */
public class CustomNewsAdapter extends BaseAdapter implements NewsModify {
    private Context mContext = null;

    //An Array for holding actual NewsItem(w/ all the meta data)
    private ArrayList<NewsItem> mListDataArray = new ArrayList<>();

    public CustomNewsAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mListDataArray.size();
    }

    @Override
    public Object getItem(int index) {
        return mListDataArray.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View newsView, ViewGroup listItemViewGroup) {
        //TODO : Add
        ViewHolder holder;
        if(newsView == null) {
            // Init List Item View if it's null.
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newsView = inflater.inflate(R.layout.news_item, null);

            holder.mImageView = (ImageView)newsView.findViewById(R.id.result_img);
            holder.mTitleView = (TextView)newsView.findViewById(R.id.title_txt);
            holder.mSummaryView = (TextView)newsView.findViewById(R.id.summary_txt);

            //Save Holder as tag Object in newsView
            newsView.setTag(holder);
        } else {
            holder = (ViewHolder)newsView.getTag();
        }

        final NewsItem mData = mListDataArray.get(index);

        if(mData.mThumb != null) {
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.mImageView.setImageDrawable(mData.mThumb);
        } else {
            holder.mImageView.setVisibility(View.GONE);
        }

        holder.mTitleView.setText(mData.mTitle);
        holder.mSummaryView.setText(mData.mSummary);

        return newsView;
    }

    @Override
    public List<NewsItem> getItemArray() {
        return mListDataArray;
    }

    /**
     * Add new NewsItem element to ListView.
     * @param listData
     */
    @Override
    public void addItem(NewsItem listData) {
        mListDataArray.add(listData);
    }

    /**
     * Remove NewsItem element at position.
     * @param pos
     */
    @Override
    public void removeItem(int pos) {
        mListDataArray.remove(pos);
        this.notifyDataSetChanged();
    }

    /**
     * Remove NewsItem element from ListView.
     * @param listData
     */
    @Override
    public void removeItem(NewsItem listData) {
        mListDataArray.remove(listData);
        this.notifyDataSetChanged();
    }

    /**
     * Remove All Items in mListDataArray
     */
    @Override
    public void removeAll() {
        mListDataArray.clear();
        this.notifyDataSetChanged();
    }

    public void sort() {
        //TODO
        this.notifyDataSetChanged();
    }

    /**
     * A class for tagging LIstView elements.
     */
    private class ViewHolder {
        public ImageView mImageView;
        public TextView mTitleView;
        public TextView mSummaryView;
        //URL and other meta data aren't here, because this is just a holder for tagging view UI elements.
    }
}
