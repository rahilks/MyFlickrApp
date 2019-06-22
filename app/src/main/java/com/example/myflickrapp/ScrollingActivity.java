package com.example.myflickrapp;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myflickrapp.api.FlickrPhoto;
import com.example.myflickrapp.api.FlickrSearchResult;
import com.example.myflickrapp.api.FlickrService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScrollingActivity extends AppCompatActivity {

    private static final String TAG = "ScrollingActivity";
    private AppCompatEditText mSearchEditText;
    private FlickrService mFlickrService;
    private RecyclerView mRecyclerView;
    private TextView mProgressTextView;
    private ProgressBar mSearchProgressBar;
    private FlickrImageAdapter mFlickrImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSearchEditText = findViewById(R.id.searchEditText);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("ScrollingActivity", "onEditor");
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performFlickrSearch(v.getText().toString());

                    return true;
                }
                return false;
            }
        });

        mRecyclerView = findViewById(R.id.imageRecyclerView);
        mSearchProgressBar = findViewById(R.id.searchProgressBar);
        mProgressTextView = findViewById(R.id.textViewProgress);

        mSearchProgressBar.setIndeterminate(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? 6 : 3);
        mRecyclerView.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.flickr.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mFlickrService = retrofit.create(FlickrService.class);
        if (savedInstanceState != null) {
            int firstVisibleItemPosition = savedInstanceState.getInt("firstVisibleItemPosition");
            int page = savedInstanceState.getInt("page");
            String jsonImageData = savedInstanceState.getString("imageData");
            String searchStr = savedInstanceState.getString("searchStr");
            mSearchEditText.setText(searchStr);
            mFlickrImageAdapter = new FlickrImageAdapter(
                    (List<FlickrPhoto>) new Gson().fromJson(jsonImageData,
                            new TypeToken<List<FlickrPhoto>>() {
                            }.getType()), searchStr);
            mFlickrImageAdapter.setPage(page);
            mFlickrImageAdapter.setFetchMoreImageListener(mFlickrFetchMoreImageListener);
            mRecyclerView.setAdapter(mFlickrImageAdapter);
            mRecyclerView.scrollToPosition(firstVisibleItemPosition);
        }
    }

    private void performFlickrSearch(final String searchText) {
        Log.d(TAG, "searchText " + searchText);
        mSearchEditText.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        if (mFlickrImageAdapter != null) {
            mFlickrImageAdapter.clear();
        }
        mSearchProgressBar.setVisibility(View.VISIBLE);
        mProgressTextView.setVisibility(View.VISIBLE);
        final Call<FlickrSearchResult> flickrSearch = mFlickrService.search(searchText,
                FlickrService.METHOD, FlickrService.API_KEY, FlickrService.EXTRAS,
                FlickrService.FORMAT, FlickrService.NO_JSON_CALLBACK, 1);
        flickrSearch.enqueue(
                new Callback<FlickrSearchResult>() {
                    @Override
                    public void onResponse(Call<FlickrSearchResult> call,
                            Response<FlickrSearchResult> response) {
                        Log.d(TAG, "onResp called " + response.toString());
                        FlickrSearchResult flickrSearchResult = response.body();
                        if (flickrSearchResult != null) {
                            Log.d(TAG, "Photo array size "
                                    + flickrSearchResult.photos.photo.size());
                            mSearchProgressBar.setVisibility(View.GONE);
                            mProgressTextView.setVisibility(View.GONE);
                            mFlickrImageAdapter = new FlickrImageAdapter(
                                    flickrSearchResult.photos.photo, searchText);
                            mFlickrImageAdapter.setFetchMoreImageListener(
                                    mFlickrFetchMoreImageListener);

                            mRecyclerView.setAdapter(mFlickrImageAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<FlickrSearchResult> call, Throwable t) {
                        Log.e(TAG, "failure", t);
                    }
                });
    }

    private FlickrFetchMoreImageListener mFlickrFetchMoreImageListener =
            new FlickrFetchMoreImageListener() {
                @Override
                public void onLoadMore(String searchText, int pageNeeded) {
                    final Call<FlickrSearchResult> flickrSearch = mFlickrService.search(searchText,
                            FlickrService.METHOD, FlickrService.API_KEY, FlickrService.EXTRAS,
                            FlickrService.FORMAT, FlickrService.NO_JSON_CALLBACK, pageNeeded);
                    flickrSearch.enqueue(
                            new Callback<FlickrSearchResult>() {
                                @Override
                                public void onResponse(Call<FlickrSearchResult> call,
                                        Response<FlickrSearchResult> response) {
                                    Log.d(TAG, "onResp called " + response.toString());
                                    FlickrSearchResult flickrSearchResult = response.body();
                                    if (flickrSearchResult != null) {
                                        Log.d(TAG, "Photo array size "
                                                + flickrSearchResult.photos.photo.size());
                                        mFlickrImageAdapter.addList(
                                                flickrSearchResult.photos.photo);
                                    }
                                }

                                @Override
                                public void onFailure(Call<FlickrSearchResult> call, Throwable t) {
                                    Log.e(TAG, "failure", t);
                                }
                            });
                }
            };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt("firstVisibleItemPosition",
                ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        savedInstanceState.putString("imageData",
                ((FlickrImageAdapter) mRecyclerView.getAdapter()).getJsonString());// get current
        // recycle view position here.
        savedInstanceState.putString("searchStr", mSearchEditText.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
