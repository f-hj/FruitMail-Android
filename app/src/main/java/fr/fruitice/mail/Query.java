package fr.fruitice.mail;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by florian on 25/12/2016.
 */

public class Query {
    private Context mContext;
    private String token;

    private String dUri = "https://mail-2.fruitice.fr";

    Query(Context context) {
        this.mContext = context;
    }
    Query(Context context, String token) {
        this.mContext = context;
        this.token = token;
        Log.d("Query", "Init with token: " + this.token);
    }

    public void result(String data) {
    }
    public void error(VolleyError error) {
        if (error.networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                // Show timeout error message
                Toast.makeText(mContext,
                        "Oops. Timeout error!",
                        Toast.LENGTH_LONG).show();
            } else if (error.getClass().equals(NoConnectionError.class)) {
                Toast.makeText(mContext,
                        "Oops. No connection!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    void get(String url) {

        url = dUri + url;

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(mContext);

// Request a string response from the provided URL.
        final String finalUrl = url;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        result(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Query Error", finalUrl);
                        error.printStackTrace();
                        error(error);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Token", getToken());
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void post(String url, final String data) {
        Log.d("QueryPost", url);
        url = dUri + url;

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(mContext);

// Request a string response from the provided URL.
        final String finalUrl = url;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        result(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Query Error", finalUrl);
                        error.printStackTrace();
                        error(error);
                    }
                }
        ){
            @Override
            public byte[] getBody() throws AuthFailureError {
                return data.getBytes();
            };

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map headers = new HashMap();
                headers.put("Token", getToken());
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Don't make retry for POST (beacause it send multiple mails...) and upper timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        5000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String getToken() {
        if (this.token == null) {
            Log.d("Query", "Token is null");
            SharedPreferences sharedPref = mContext.getSharedPreferences("fruitmail", MODE_PRIVATE);
            this.token = sharedPref.getString("token", null);
        }
        return this.token;
    }
}
