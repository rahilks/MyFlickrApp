package com.example.myflickrapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FlickrService {

    String METHOD = "flickr.photos.search";
    String API_KEY = "675894853ae8ec6c242fa4c077bcf4a0";
    String EXTRAS = "url_s";
    String FORMAT = "json";
    int NO_JSON_CALLBACK = 1;

    @GET("services/rest")
    Call<FlickrSearchResult> search(@Query("text") String query, @Query("method") String method,
            @Query("api_key") String apiKey,
            @Query("extras") String extras, @Query("format") String format,
            @Query("nojsoncallback") int nojsoncallback, @Query("page") int page);
}