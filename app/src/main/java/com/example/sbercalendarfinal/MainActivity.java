package com.example.sbercalendarfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class MainActivity extends AppCompatActivity {

    private Button btnChange;
    private EditText etLogin;
    private EditText etPassword;
    private TextView tvWelcome;



    private boolean logged;

    private String hashLogin;
    private String hashPassword;

    private String login;
    private String password;

    private String errorMessage;

    private SpinKitView spinKitView;

    private static final String URL_SECOND_LOGIN = "http://dev.inapik.ru/login";
    private static final String URL_LOGIN = "http://95.131.148.98:8080/mlogin";
    private static final String URL_TOKEN = "http://95.131.148.98:8080/oauth/token";

    private static String token;
    private static String refresh_token;


    //android:layout_margin="@dimen/fab_margin"

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(Build.VERSION_CODES.CUR_DEVELOPMENT<=Build.VERSION_CODES.LOLLIPOP)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Ошибка версии")
                    .setMessage("Неподходящая версия Android")
                    .setCancelable(false)
                    .setNegativeButton("ОК, закрыть приложение",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finishAffinity();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }



        logged = false;


        btnChange = (Button)findViewById(R.id.btnChange);
        etLogin = (EditText)findViewById(R.id.etLogin);
        etPassword = (EditText)findViewById(R.id.etPassword);
        tvWelcome = (TextView)findViewById(R.id.textView3);

        spinKitView = findViewById(R.id.spin_kit);


        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login = etLogin.getText().toString().trim();
                password = etPassword.getText().toString().trim();

                changeVisibility(true);


                Thread thread = new Thread(){
                    public void run(){
                        SendOAuthRequest();
                    }
                };
                thread.start();

             //   SendAuthorization();
               // SendLoginRequestString();
            }
        });

     //   Executor executor = Executors.newSingleThreadExecutor();

//      BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
//                .setTitle("Fingerprint Authentication")
//                .setSubtitle("Subtitle")
//                .setDescription("Description")
//                .setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }).build();
//        BiometricPrompt.
    }



    private void ShowMessage()
    {
        Thread thread = new Thread(){
            public void run(){
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(errorMessage!=null) {
                            if (!errorMessage.equals("")) {
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        };
        thread.start();
    }


    private void CalculateHash()
    {
        new Thread(new Runnable() {
            public void run() {
                SecureRandom secureRandom = new SecureRandom();
                byte[] salt = new byte[16];
                secureRandom.nextBytes(salt);
                KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
                KeySpec spec1 = new PBEKeySpec(login.toCharArray(), salt, 65536, 128);
                try {
                    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                    hashLogin = new String(factory.generateSecret(spec).getEncoded());
                    hashPassword = new String(factory.generateSecret(spec1).getEncoded());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MainAct:Crypto Error", " " + e.getMessage());
                    errorMessage = "Не удается обработать пароль";
                    ShowMessage();
                }
            }
        }).start();
    }

    private void SendOAuthRequest()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_TOKEN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changeVisibility(false);

                if (error instanceof AuthFailureError && error!=null && error.networkResponse!=null) {
                    error.printStackTrace();
                    if(error.networkResponse.statusCode == 401) {

                        errorMessage = "Неправильный логин или пароль";
                        ShowMessage();
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
                        ShowMessage();
                    }
                }
                else
                {
                    Log.e("MainAct:Network status",
                            "NULL RESPONSE");
                    errorMessage = "Не удается подключиться к серверу";
                    ShowMessage();
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
                        token=jsonObject.getString("access_token");
                        refresh_token=jsonObject.getString("refresh_token");
                        Context context = MainActivity.this;
                        Intent intent = CalendarViewWithNotesActivitySDK21.makeIntent(context);
                        intent.putExtra("login", login);
                        intent.putExtra("token", token);
                        intent.putExtra("refresh_token", refresh_token);
                        startActivity(intent);
                        Log.i("MainAct:ActivityChange", " Started " + CalendarViewWithNotesActivitySDK21.class);
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
                String credentials = login + ":" + password;
                String auth = "Basic "  + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                headers.put("Content-type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("client_id", login);
                postMap.put("grant_type", "password");
                postMap.put("username", login);
                postMap.put("password", password);
                return postMap;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void changeVisibility(final boolean vis)
    {
        Thread thread = new Thread(){
            public void run(){
                runOnUiThread(new Runnable() {
                    public void run() {
                     if(vis)
                     {
                         spinKitView.setVisibility(View.VISIBLE);
                         Log.i("SpinKit", "Visibility true");
                     }
                     else
                     {
                         spinKitView.setVisibility(View.INVISIBLE);
                         Log.i("SpinKit", "Visibility false");
                     }
                    }
                });
            }
        };
        thread.start();
    }
}


//    private void SendLoginRequestString()
//    {
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (error instanceof AuthFailureError) {
//                    error.printStackTrace();
//                    if(error.networkResponse.statusCode == 401) {
//
//                        errorMessage = "Неправильный логин или пароль";
//                        ShowMessage();
//                        Log.e("MainAct:AuthError", String.valueOf(error.networkResponse.statusCode));
//                    }
//                    else
//                    {
//                        Log.e("MainAct:Network status",
//                                " "
//                                        + error.networkResponse.statusCode
//                                        + " - "
//                                        + "Не удается подключиться к серверу");
//                        errorMessage = "Не удается подключиться к серверу";
//                        ShowMessage();
//                    }
//                }
//            }
//        }){
//            @Override
//            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                // take the statusCode here.
//                int status = response.statusCode;
//                Log.i("MainAct:Server response", " " + response.statusCode);
//                if (status == 202) {
//                    com.github.ybq.android.spinkit.SpinKitView spin_kit = findViewById(R.id.spin_kit);
//                    spin_kit.setVisibility(View.VISIBLE);
//                    Context context = MainActivity.this;
//                    Intent intent = CalendarViewWithNotesActivitySDK21.makeIntent(context);
//                    intent.putExtra("login",login);
//                    intent.putExtra("password", password);
//
//                    startActivity(intent);
//                    Log.i("MainAct:ActivityChange", " Started " + CalendarViewWithNotesActivitySDK21.class);
//                }
//
//                return super.parseNetworkResponse(response);
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError
//            {
//                Map<String, String> postMap = new HashMap<>();
//                postMap.put("username", login);
//                postMap.put("password", password);
//                return postMap;
//            }
//        };
//        requestQueue.add(stringRequest);
//    }

//<android.support.design.widget.AppBarLayout
//        android:layout_width="match_parent"
//        android:layout_height="84dp"
//        app:theme="@style/AppTheme.AppBarOverlay"
//        app:elevation="0dp">
//
//<android.support.v7.widget.Toolbar
//        android:id="@+id/toolbar"
//        android:layout_width="match_parent"
//        android:layout_height="84dp"
//        android:theme="@style/AppTheme.AppBarOverlay"
//        app:popupTheme="@style/AppTheme.PopupOverlay"
//        app:subtitleTextAppearance="@style/AppTheme.SubTitleText"
//        app:titleTextAppearance="@style/AppTheme.TitleText" />
//
//</android.support.design.widget.AppBarLayout>
//
//<org.hugoandrade.calendarviewlib.CalendarView
//        android:id="@+id/calendarView"
//        android:layout_marginTop="84dp"
//        android:layout_marginBottom="60dp"
//        android:layout_width="match_parent"
//        android:layout_height="match_parent"
//        tools:layout_behavior="@string/appbar_scrolling_view_behavior"
//        app:content_background_color="#fff"
//        app:week_header_background_color="#fff"
//        app:week_header_text_color="?attr/colorPrimary"
//        app:current_day_circle_enable="true"
//        app:month_header_show="false"
//        app:week_header_movable="false" />
//
//<android.support.design.widget.FloatingActionButton
//        android:id="@+id/fab"
//        android:layout_width="wrap_content"
//        android:layout_height="wrap_content"
//        android:layout_gravity="bottom|end"
//        android:layout_marginBottom="60dp"
//        android:layout_marginRight="16dp"
//        android:tint="#fff"
//        app:elevation="0dp"
//        app:srcCompat="@android:drawable/ic_input_add" />