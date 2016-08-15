package com.bitsailer.yauc.api;

import android.content.Context;

import com.bitsailer.yauc.BuildConfig;
import com.bitsailer.yauc.Preferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Unsplash service creator with interceptors for authentication
 * and logging (debug build).
 * Created by Uli Wucherer (u.wucherer@gmail.com) on 26/07/16.
 */

public class UnsplashService {

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create());

    /**
     * Create service for endpoint defined in service interface with
     * base url from UnsplashAPI.URL.
     * With a given authToken it will be injected in Authorization
     * header.
     * @param serviceClass service class to create
     * @return Implementation of service interface
     */
    public static <S> S create(Class<S> serviceClass) {
        return create(serviceClass, null, null);
    }

    public static <S> S create(Class<S> serviceClass, Context context) {
        String authToken = null;
        if (context != null) {
            authToken = Preferences.get(context).getAccessToken();
        }
        return create(serviceClass, authToken, null);
    }

    /**
     * Create service for endpoint defined in service interface.
     * With a given baseUrl it will be used instead of UnsplashAPI.URL.
     * @param serviceClass service class to create
     * @param baseUrl the base url to use
     * @return Implementation of service interface
     */
    public static <S> S createAuth(Class<S> serviceClass, String baseUrl) {
        return create(serviceClass, null, baseUrl);
    }

    private static <S> S create(Class<S> serviceClass, final String authToken, final String baseUrl) {

        httpClient.interceptors().clear();
        /**
         * Authenticate Requests.
         * Possible states of header:
         * - without header when authenticating to {@link com.bitsailer.yauc.api.UnsplashAPI.OAUTH_URL}
         * - with "Client-ID" token
         * - with "Bearer" token when user has signed in
         */
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {

                Request.Builder builder = chain.request().newBuilder();

                // run without auth header during oauth authentication
                if (baseUrl != null && baseUrl.equals(UnsplashAPI.OAUTH_URL)) {
                    return chain.proceed(builder.build());
                }

                builder.removeHeader("Authorization"); // be sure to remove any existing

                // default auth with "Client-id" and unauthorized user
                String auth = String.format("Client-ID %s", BuildConfig.UNSPLASH_CLIENT_ID);
                if (authToken != null) {
                    // with auth token given there is a authenticated user
                    auth = String.format("Bearer %s", authToken);
                }
                builder.addHeader("Authorization", auth);
                return chain.proceed(builder.build());
            }
        });

        // add logging interceptor for debug builds
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            httpClient.addInterceptor(logging);
        }


        if (baseUrl != null) {
            // overwrite base url
            builder.baseUrl(baseUrl);
        } else {
            // default base url
            builder.baseUrl(UnsplashAPI.URL);
        }
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}
