package com.onlylightness.newsfeed;

import java.util.List;

/**
 * Created by Minsik on 2016-12-31.
 */

public interface NewsModify {
    List<NewsItem> getItemArray();
    void addItem(NewsItem listData);
    void removeItem(int position);
    void removeItem(NewsItem listData);
    void removeAll();
}
