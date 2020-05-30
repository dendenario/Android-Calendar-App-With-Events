package com.example.sbercalendarfinal;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sbercalendarfinal.data.Event;
import com.example.sbercalendarfinal.uihelpers.ListItem;
import com.example.sbercalendarfinal.uihelpers.ProfileDialog;
import com.example.sbercalendarfinal.uihelpers.RvAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity implements ProfileDialog.ProfileDialogListener {

    private static final String URL_PROFILE = "http://95.131.148.98:8080/api/profile";
    private static final String URL_PROFILE_IMAGE = "http://95.131.148.98:8080/api/profile/logo";

    private static String token;
    private static String login;
    private static String name;
    private static String surname;
    private static String role;
    private static String work;

    private static String editedName;
    private static String editedWork;
    private static String editedSurname;

    protected ArrayList<Event> mEventList = new ArrayList<>();

    private TextView tvNameSurname;
    private TextView tvWork;
    private TextView tvCompleted;
    private TextView tvUncompleted;

    private ImageView image_profile;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private List<ListItem> listItems = new ArrayList<>();


    private Button btnEdit;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
//        Toolbar toolbar = findViewById(R.id.tbProfile);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("Профиль");
        Bundle args = getIntent().getExtras();
        token = args.getString("token");
        login = args !=null ? args.getString("login") : null;

        String jsonEvents = args.getString("events");

        Gson gson = new Gson();
        Type type = new TypeToken<List<Event>>(){}.getType();
        mEventList = gson.fromJson(jsonEvents, type);
            Log.i("Intent Args", "Events loaded");

        loadPROFILEData();

        btnEdit=findViewById(R.id.btnEdit);
        tvNameSurname = findViewById(R.id.tvNameSurname);
        tvWork = findViewById(R.id.tvWork);

        tvCompleted = findViewById(R.id.tvCompleted);
        tvUncompleted = findViewById(R.id.tvUncompleted);

        image_profile = findViewById(R.id.image_profile);

        loadIMAGE();

        recyclerView = findViewById(R.id.rvEvents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        for(Event e : mEventList)
        {
            ListItem listItem = new ListItem(e.getTitle(),e.getDecription());
            listItems.add(listItem);
        }

        adapter = new RvAdapter(listItems, this);

        recyclerView.setAdapter(adapter);



        int completedCount=0;
        int unCompletedCount=0;
        for(Event e : mEventList)
        {
            if(e.isCompleted())
            {
                completedCount++;
            }
            else
            {
                unCompletedCount++;
            }
        }
        tvCompleted.setText(String.valueOf(completedCount));
        tvUncompleted.setText(String.valueOf(unCompletedCount));

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }

    public void openDialog()
    {
        ProfileDialog profileDialog = new ProfileDialog();
        profileDialog.show(getSupportFragmentManager(), "profile dialog");

    }

    private void loadPROFILEData()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject user = jsonObject.getJSONObject("user");
                            tvNameSurname.setText(String.format("%s %s", user.getString("firstName"), user.getString("lastName")));
                            tvWork.setText(user.getString("email"));
                            Log.i("JSONProfile", jsonObject.toString());
                        }
                        catch (JSONException e)
                        {
                            Log.e("JSONError", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if(error!=null && error.networkResponse!=null) {
                            Log.e("Volley Error", error.getMessage() + " _ " + error.networkResponse.statusCode);
                        }
                        else
                        {
                            Log.e("Volley Error", "NULL RESPONSE");
                        }
                        Toast.makeText(getApplicationContext(), "Не удается получить данные", Toast.LENGTH_LONG).show();
                    }
                })
        {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                if(response!=null) {
                    int status = response.statusCode;
                    Log.i("ProfAct:Server response", " " + response.statusCode);
                    if (status == 200)
                    {
                        Log.i("ProfAct", "profile edited");
                    }
                    if (status == 400)
                    {
                        Toast.makeText(getApplicationContext(), "Неверный запрос", Toast.LENGTH_LONG).show();
                    }
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void editProfile()
    {
        final RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
        final StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error!=null && error.networkResponse!=null) {
                            Log.e("Volley Error", error.getMessage() + " _ " + error.networkResponse.statusCode);
                        }
                        else
                        {
                            Log.e("Volley Error", "NULL RESPONSE");
                        }
                        Toast.makeText(ProfileActivity.this, "Не удается отправить данные", Toast.LENGTH_LONG).show();
                    }
                })
        {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                if(response!=null) {
                    int status = response.statusCode;
                    Log.i("ProfAct:Server response", " " + response.statusCode);
                    if (status == 200)
                    {
                        Log.i("ProfAct", "profile edited");
                    }
                    if (status == 400)
                    {
                        Toast.makeText(getApplicationContext(), "Неверный запрос", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Log.e("ProfileAct:Volley", "Null network response");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(CreateEventActivity.this, "Нет связи с сервером", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                    });
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", editedName);

                    jsonObject.put("surname", editedSurname);
                    jsonObject.put("work", editedWork);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return super.getBody();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-type", getBodyContentType());
                return headers;
            }

        };
        requestQueue.add(stringRequest);
    }

    private void loadIMAGE()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ImageRequest imageRequest = new ImageRequest(URL_PROFILE_IMAGE,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        image_profile.setImageBitmap(response);
                    }
                },
                0,
                0,
                null,
                null,
                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }



        })
        {

            @Override
            protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                if (response != null) {
                    int status = response.statusCode;
                    Log.i("ProfAct:Server response", " " + response.statusCode);
                    if (status == 200) {
                        Log.i("ProfileAct", "image downloaded" );
                    }
                } else {
                    Log.e("ProfileAct:Volley", "Null network response");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(CreateEventActivity.this, "Нет связи с сервером", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                    });
                }
                return super.parseNetworkResponse(response);
            }


            @Override
            public String getBodyContentType() {
                return "application/json";
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-type", getBodyContentType());
                return headers;
            }
        };

        requestQueue.add(imageRequest);

    }


    @Override
    public void applyTexts(String name, String surname, String work) {
        editedName = name;
        editedSurname = surname;
        editedWork = work;
    }
}
