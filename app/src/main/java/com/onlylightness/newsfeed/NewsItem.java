package com.onlylightness.newsfeed;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URL;

/**
 * Created by Minsik on 2016-12-31.
 */

public class NewsItem {

    //Item to display
    public Drawable mThumb;
    public String mTitle;
    public String mSummary;

    public MetaData mMeta;

    //TODO : Add Corresponding Hyperlink and MetaData
    //MetaData
    public class MetaData {
        public String mURL;
        MetaData(String url) {
            this.mURL = url;
        }
    }

    public NewsItem(@NonNull String title, @Nullable String summary, @Nullable Drawable thumb, @NonNull String mURL) {
        this.mTitle = title;
        this.mSummary = summary;
        this.mThumb = thumb;
        this.mMeta = new MetaData(mURL);
    }
}
