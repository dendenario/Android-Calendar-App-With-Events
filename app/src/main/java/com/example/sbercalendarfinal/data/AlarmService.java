package com.example.sbercalendarfinal.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AlarmService extends BroadcastReceiver {



    private String refresh_token;
    private String refresh_token_new;
    private String token;
    private static final String URL_TOKEN = "http://95.131.148.98:8080/oauth/token";
    private String errorMessage;

    private void sendRefreshRequest(Context context)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_TOKEN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof AuthFailureError && error!=null && error.networkResponse!=null) {
                    error.printStackTrace();
                    if(error.networkResponse.statusCode == 401) {

                        errorMessage = "Неправильный логин или пароль";
                        Log.e("MainAct:AuthError", String.valueOf(error.networkResponse.statusCode) + " oAuth");
                    }
                    else
                    {
                        Log.e("MainAct:Network status",
                                " "
                                        + error.networkResponse.statusCode
                                        + " - "
                                        + "Не удается подключиться к серверу");
                        errorMessage = "Не удается подключиться к серверу";
                    }
                }
                else
                {
                    Log.e("MainAct:Network status",
                            "NULL RESPONSE");
                    errorMessage = "Не удается подключиться к серверу";
                }
            }
        }){
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                int status = response.statusCode;

                Log.i("MainAct:Server response", " " + response.statusCode);
                if (status == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(response.data));
                        token=jsonObject.getString("token");
                        refresh_token_new = token=jsonObject.getString("refresh_token");
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                        Log.e("MainActivity:JSONError", e.getMessage());
                    }
                }

                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("grant_type", "refresh_token");
                postMap.put("refresh_token", refresh_token);
                return postMap;
            }
        };
        requestQueue.add(stringRequest);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        sendRefreshRequest(context);

        Intent i = new Intent("AlarmRefresh");
        i.putExtra("refresh_token", refresh_token);

        context.sendBroadcast(i);
    }
}
