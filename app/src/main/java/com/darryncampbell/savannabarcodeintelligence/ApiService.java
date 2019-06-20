package com.darryncampbell.savannabarcodeintelligence;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("lookup")
    Single<Product> getProductData(@Query("upc") String barcode,
                                   @Header("apikey") String api_key);
}


/*
public interface ApiService {
    @GET("lookup/{barcode}")
    Single<Product> getProductData(@Path("barcode") String theBarcode,
                                    @Query("api_key") String apiKey);
}
*/