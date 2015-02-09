package com.example.micyeung.sunshine.app;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache.Entry;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NetworkManager {

    private static NetworkManager instance;

    private static final int DEFAULT_DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100 MB

    public static NetworkManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new NetworkManager(ctx);
        }
        return instance;
    }

    /**
     * we separate different queue here for different purpose. for example, the queue for image access
     * need more cache space to increasing the cache hit rate.
     * */
    private RequestQueue mQueue;
    private RequestQueue mImageAccessQueue;

    private NetworkManager(Context context) {
        File cacheDir = new File(context.getCacheDir(), "volley");
        Network network = new BasicNetwork(new HurlStack());
        mImageAccessQueue = new RequestQueue(new DiskBasedCache(cacheDir, DEFAULT_DISK_CACHE_SIZE), network);
        mImageAccessQueue.start();

        mQueue = Volley.newRequestQueue(context);
    }

    public String syncRequest(final RequestBuilder<String> params) throws InterruptedException, ExecutionException {
        RequestFuture<String> future = RequestFuture.newFuture();
        future.setRequest(request(new StringRequest(params.method, params.url, future, future)));
        return future.get();
    }

    public Bitmap syncImageRequest(String url, int maxWidth, int maxHeight,
                                   Config decodeConfig) throws InterruptedException, ExecutionException {
        RequestFuture<Bitmap> future = RequestFuture.newFuture();
        ImageRequest req = new ImageRequest(url, future, maxWidth, maxHeight, decodeConfig, future);
        future.setRequest(requestImage(req));
        return future.get();
    }

    public Request<?> requestImage(Request<?> request) {
        return mImageAccessQueue.add(request);
    }

    public Request<?> request(Request<?> request) {
        return mQueue.add(request);
    }

    public void cancel(Object tag) {
        mQueue.cancelAll(tag);
    }

    public Entry getCacheEntry(String key) {
        return mQueue.getCache().get(key);
    }

    public static class StringRequestBuilder extends RequestBuilder<String> {
        @Override
        public Request<String> build() {
            CBlueStringRequest req = new CBlueStringRequest(method, url, callback, fallback);
            if (TextUtils.isEmpty(tag) == false) {
                req.setTag(tag);
            }
            if (parameters != null) {
                req.setParams(parameters);
            }
            return req;
        }
    }

    public static class CBlueStringRequest extends StringRequest {
        private Map<String, String> mParams = null;

        public CBlueStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        public CBlueStringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
            super(url, listener, errorListener);
        }

        public void setParams(Map<String, String> params) {
            mParams = params;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return mParams;
        }
    }

    public abstract static class RequestBuilder<T> {
        protected Map<String, String> parameters;
        String stringPart;
        File file;
        String url;
        Listener<T> callback;
        ErrorListener fallback;
        String tag;
        int method = Request.Method.GET;

        public RequestBuilder<T> tag(String tag) {
            this.tag = tag;
            return this;
        }
        public RequestBuilder<T> callback(Listener<T> callback) {
            this.callback = callback;
            return this;
        }
        public RequestBuilder<T> fallback(ErrorListener fallback) {
            this.fallback = fallback;
            return this;
        }
        public RequestBuilder<T> method(int method) {
            this.method = method;
            return this;
        }
        public RequestBuilder<T> url(String url) {
            this.url = url;
            return this;
        }
        public RequestBuilder<T> file(File file) {
            this.file = file;
            return this;
        }
        public RequestBuilder<T> stringPart(String stringPart) {
            this.stringPart = stringPart;
            return this;
        }
        public RequestBuilder<T> parameters(Map<String, String> param) {
            this.parameters = param;
            return this;
        }

        public abstract Request<T> build();
    }
}