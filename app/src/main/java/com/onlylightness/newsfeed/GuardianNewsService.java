package com.onlylightness.newsfeed;

import android.database.Observable;

import com.onlylightness.newsfeed.NewsJSON.SearchJSON;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

/**
 * Created by Minsik on 2017-01-01.
 */

public interface GuardianNewsService {
/*    @GET("search")
    Single<SearchJSON> getSearchResult(@Query("q") String query, @Path("api-key") String key);*/

    @GET("search")
    Call<SearchJSON> getSearchResult(@Query("q") String query, @Query("api-key") String key);
}
