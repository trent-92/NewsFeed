package com.onlylightness.newsfeed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;
import com.onlylightness.newsfeed.NewsJSON.Result;
import com.onlylightness.newsfeed.NewsJSON.SearchJSON;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private static String BaseURL = "https://content.guardianapis.com";
    private static String APIKey = "";              // FIXME : Get Your Own API Key from Guardian Open-Platform.

    private ListView mListView;

    //Used for custom ListView
    private CustomNewsAdapter mNewsAdapter;

    private EditText mQueryText;

    private GuardianNewsService mRESTConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupRxObjects();
        setupListView();
    }

    private void setupRxObjects() {
        mQueryText = (EditText) findViewById(R.id.query_text);
        mRESTConnection = setupRetrofit();
        Observable
                .create((Subscriber<? super String> subscriber) ->
                        mQueryText.addTextChangedListener(onTextChangedCallback(subscriber)))      //this runs on SubscribeOn thread only once. returns query string when it has been changed.
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(1000, TimeUnit.MILLISECONDS)                                             //data passes when it has not been emitted for 1 seconds.
                //this runs on computation Scheduler.
                .observeOn(Schedulers.io())                                                         //run RESTful map operation on Schedulers.io thread pool.
                .map(RESTQueryFunc1())                                                              //RESTful map operation that queries the string on GoogleCustomSearchAPI
                .observeOn(AndroidSchedulers.mainThread())                                          //perform update of ListView in MainThread.
                .subscribe(newsDisplaySubscriber());                                                //Subscriber that displays NewsItems received in ListView.
    }

    private GuardianNewsService setupRetrofit() {
        HttpLoggingInterceptor intercepter = new HttpLoggingInterceptor();
        intercepter.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(intercepter).build();

        final Retrofit retrofit = new Retrofit.Builder()
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BaseURL)
                .build();
        return retrofit.create(GuardianNewsService.class);
    }

    /**
     * A Subscriber that performs a certain action when NewsItems have been emitted to it.
     *
     * @return
     */
    private Subscriber<ArrayList<NewsItem>> newsDisplaySubscriber() {
        return new Subscriber<ArrayList<NewsItem>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ArrayList<NewsItem> newsItems) {
                mNewsAdapter.removeAll();
                Stream.of(newsItems).forEach(item -> mNewsAdapter.addItem(item));
            }
        };
    }

    /**
     * Callback when EditText content has been changed.
     *
     * @param subscriber
     * @return
     */
    private TextWatcher onTextChangedCallback(Subscriber subscriber) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String initString = editable.toString();
                if (initString.contains("\n")) {
                    final String newString = initString.replaceAll("\n", "");             // Remove Enters from text.
                    mQueryText.setText(newString);
                    subscriber.onNext(newString);
                    return;
                }
                subscriber.onNext(initString);
            }
        };         // run this code when Observable is subscribed.
    }

    /**
     * A Func1 for Google Custom Search with RestAPI.
     *
     * @return
     */
    private Func1<String, ArrayList<NewsItem>> RESTQueryFunc1(){
        //TODO : Query REST API for ArrayList to output.
        //TODO : Parse JSON response with GSON to create ArrayList.

        return s -> {
            Log.d(TAG, "RestQuery Fired");
            Log.d(TAG, "Thread Name : " + Thread.currentThread().getName());

            if(APIKey.equals("")) {
                Log.e(TAG, "Get Your Own API Key from Guardian Open-Platform.");
                Log.e(TAG, "This is a Test Output.");
                final ArrayList<NewsItem> returnArrayList = new ArrayList<>();
                returnArrayList.add(createTestItem());
                return returnArrayList;
            }

            //This Apparently blocks the process..
            final Call<SearchJSON> callJson = mRESTConnection.getSearchResult(s, APIKey);
            SearchJSON searchResult = null;

            Log.d(TAG, "Is Excuted : " + String.valueOf(callJson.isExecuted()));

            try {
                searchResult = callJson.execute().body();
                Log.d(TAG, "Is Excuted : " + String.valueOf(callJson.isExecuted()));
                Log.d(TAG, searchResult.getResponse().getResults().toString());
            } catch (IOException e) {
                Log.e(TAG, "API ERROR");
                e.printStackTrace();
                ArrayList<NewsItem> returnList = new ArrayList<>();
                returnList.add(createTestItem());
                return returnList;
            }

            //Return an ArrayList
            return Stream.of(searchResult.getResponse().getResults())
                    .map(result -> new NewsItem(result.getWebTitle(), null, null, result.getWebUrl()))
                    .collect(Collectors.toCollection(ArrayList<NewsItem>::new));
        };
    }

    /**
     * Sets up the ListView for items.
     */
    private void setupListView() {
        /**
         * Set CustomListView and Adapter.
         */
        mListView = (ListView) findViewById(R.id.search_result);
        mNewsAdapter = new CustomNewsAdapter(this);
        mListView.setAdapter(mNewsAdapter);

        /**
         * Add testItem for testing.
         */
//        NewsItem testItem = createTestItem();
//        mNewsAdapter.addItem(testItem);

        /**
         * Set ListView Listeners.
         */
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem data = mNewsAdapter.getItemArray().get(position);
            final Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.mMeta.mURL));
            startActivity(webIntent);
        });

        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            NewsItem data = mNewsAdapter.getItemArray().get(position);
            //TODO Show the URL in a Toast / floating view
            Log.d(TAG, data.mMeta.mURL);
            return true;        // Because of this, click is not fired when long click is done.
        });
    }

    private NewsItem createTestItem() {
        Drawable testDrawable = getResources().getDrawable(R.drawable.trump_news);
        String testTitle = getString(R.string.test_title);
        String testSummary = getString(R.string.test_summary);

        String testURL = getString(R.string.test_url);

        return new NewsItem(testTitle, testSummary, testDrawable, testURL);
    }
}