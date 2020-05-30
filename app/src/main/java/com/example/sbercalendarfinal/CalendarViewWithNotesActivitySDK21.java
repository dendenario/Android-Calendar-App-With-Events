package com.example.sbercalendarfinal;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sbercalendarfinal.data.AlarmService;
import com.example.sbercalendarfinal.data.Event;
import com.example.sbercalendarfinal.uihelpers.CalendarDialog;
import com.example.sbercalendarfinal.uihelpers.CreateEventActivity;
import com.google.gson.Gson;

import org.hugoandrade.calendarviewlib.CalendarView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CalendarViewWithNotesActivitySDK21 extends AppCompatActivity {

    private final static int CREATE_EVENT_REQUEST_CODE = 100;

    private String[] mShortMonths;
    private CalendarView mCalendarView;
    private CalendarDialog mCalendarDialog;

    protected List<Event> mEventList = new ArrayList<>();

   // private static final String URL_TASKS = "http://35.206.104.43/:8080/api/tasks";
    private static final String URL_TASKS = "http://95.131.148.98:8080/api/tasks";
    private static final String URL_TODO = "http://95.131.148.98:8080/api/todo";
    private static final String URL_DATE_TASK = "http://35.206.104.43/:8080/api/tasks/search/findByDate?date=";

    private static AlarmService alarmService;

    private static String login;
    private static String token;
    private static String refresh_token;
//    private static String role;
//    private static String work;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

//
//    private BottomNavigationView bnTabs;


    public static Intent makeIntent(Context context) {
        return new Intent(context, CalendarViewWithNotesActivitySDK21.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShortMonths = new DateFormatSymbols().getShortMonths();

        Bundle args = getIntent().getExtras();
        login = args.get("login").toString();
        token = args.getString("token");
        refresh_token = args.getString("refresh_token");
        initializeUI();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadTODOData();
            }
        });
        thread.start();

        Context context = getApplicationContext();

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("refresh_token",refresh_token);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        10000, alarmIntent);


        registerReceiver(alarmService, new IntentFilter("Alarm"));

        BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();

                refresh_token = b.getString("refresh_token");

                Log.e("Token refreshed", "" + refresh_token);
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("AlarmRefresh"));
    }

    @Override
    public void onBackPressed()
    {
        finishAffinity();
    }

    private void initializeUI() {

        setContentView(R.layout.activity_calendar_view_with_notes_sdk_21);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setOnMonthChangedListener(new CalendarView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(int month, int year) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mShortMonths[month]);
                    getSupportActionBar().setSubtitle(Integer.toString(year));
                }
            }
        });
        mCalendarView.setOnItemClickedListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClicked(List<CalendarView.CalendarObject> calendarObjects,
                                      Calendar previousDate,
                                      Calendar selectedDate) {
                if (calendarObjects.size() != 0) {
                    mCalendarDialog.setSelectedDate(selectedDate);
                    mCalendarDialog.show();
                }
                else {
                    if (diffYMD(previousDate, selectedDate) == 0)
                        createEvent(selectedDate);
                }
            }
        });

        for (Event e : mEventList) {
            mCalendarView.addCalendarObject(parseCalendarObject(e));
        }

        if (getSupportActionBar() != null) {
            int month = mCalendarView.getCurrentDate().get(Calendar.MONTH);
            int year = mCalendarView.getCurrentDate().get(Calendar.YEAR);
            getSupportActionBar().setTitle(mShortMonths[month]);
            getSupportActionBar().setSubtitle(Integer.toString(year));
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent(mCalendarView.getSelectedDate());
            }
        });

        mCalendarDialog = CalendarDialog.Builder.instance(this)
                .setEventList(mEventList)
                .setOnItemClickListener(new CalendarDialog.OnCalendarDialogListener() {
                    @Override
                    public void onEventClick(Event event) {
                        onEventSelected(event);
                    }

                    @Override
                    public void onCreateEvent(Calendar calendar) {
                        createEvent(calendar);
                    }
                })
                .create();
    }

    private void onEventSelected(Event event) {
        Activity context = CalendarViewWithNotesActivitySDK21.this;
        Intent intent = CreateEventActivity.makeIntent(context, event);
        intent.putExtra("token", token);
        intent.putExtra("login", login);
        intent.putExtra("refresh_token", refresh_token);
        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    private void createEvent(Calendar selectedDate) {
        Activity context = CalendarViewWithNotesActivitySDK21.this;
        Intent intent = CreateEventActivity.makeIntent(context, selectedDate);
        intent.putExtra("token", token);
        intent.putExtra("login", login);
        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_calendar_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today: {
                mCalendarView.setSelectedDate(Calendar.getInstance());
                return true;
            }
            case R.id.action_profile:
            {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("login",login);
                intent.putExtra("token", token);
                Gson gson = new Gson();

                String jsonEvents = gson.toJson(mEventList);
                intent.putExtra("events", jsonEvents);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int action = CreateEventActivity.extractActionFromIntent(data);
                Event event = CreateEventActivity.extractEventFromIntent(data);

                switch (action) {
                    case CreateEventActivity.ACTION_CREATE: {
                        mEventList.add(event);
                        mCalendarView.addCalendarObject(parseCalendarObject(event));
                        mCalendarDialog.setEventList(mEventList);
                        break;
                    }
                    case CreateEventActivity.ACTION_EDIT: {
                        Event oldEvent = null;
                        for (Event e : mEventList) {
                            if (Objects.equals(event.getID(), e.getID())) {
                                oldEvent = e;
                                break;
                            }
                        }
                        if (oldEvent != null) {
                            mEventList.remove(oldEvent);
                            mEventList.add(event);

                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));
                            mCalendarView.addCalendarObject(parseCalendarObject(event));
                            mCalendarDialog.setEventList(mEventList);
                        }
                        break;
                    }
                    case CreateEventActivity.ACTION_DELETE: {
                        Event oldEvent = null;
                        for (Event e : mEventList) {
                            if (Objects.equals(event.getID(), e.getID())) {
                                oldEvent = e;
                                break;
                            }
                        }
                        if (oldEvent != null) {
                            mEventList.remove(oldEvent);
                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));

                            mCalendarDialog.setEventList(mEventList);
                        }
                        break;
                    }
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static int diffYMD(Calendar date1, Calendar date2) {
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH))
            return 0;

        return date1.before(date2) ? -1 : 1;
    }

    private static CalendarView.CalendarObject parseCalendarObject(Event event) {
        return new CalendarView.CalendarObject(
                event.getID(),
                event.getDate(),
                event.getColor(),
                event.isCompleted() ? Color.TRANSPARENT : Color.RED);
    }

    private void loadTasksData()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_TASKS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject _embedded = jsonObject.getJSONObject("_embedded");
                            JSONArray array = _embedded.getJSONArray("tasks");
                            //List<Event> list = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                String[] buf = o.getString("date").split("-");
                                String[] buf2 = o.getString("time").split(":");
                                Event event = new Event(
                                        o.getString("id"),
                                        o.getString("name"),
                                        new GregorianCalendar(Integer.parseInt(buf[0]),Integer.parseInt(buf[1])-1,Integer.parseInt(buf[2]),Integer.parseInt(buf2[0]),Integer.parseInt(buf2[1]),Integer.parseInt(buf2[2])),
                                        Color.GRAY,
                                        false,
                                        o.getString("description"));
                                mEventList.add(event);
                                mCalendarView.addCalendarObject(parseCalendarObject(event));
                            }
                           // mCalendarDialog.setEventList(mEventList);
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
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<>();
//                String credentials = login + ":" + password;
//                String auth = "Basic "  + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }


    private void loadTODOData()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_TODO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            //List<Event> list = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                String[] buf = o.getString("date").split("-");
                                String[] buf2 = o.getString("time").split(":");
                                Event event = new Event(
                                        o.getString("id"),
                                        o.getString("name"),
                                        new GregorianCalendar(Integer.parseInt(buf[0]),Integer.parseInt(buf[1])-1,Integer.parseInt(buf[2]),Integer.parseInt(buf2[0]),Integer.parseInt(buf2[1]),Integer.parseInt(buf2[2])),
                                        Color.GRAY,
                                        false,
                                        o.getString("description"));
                                mEventList.add(event);
                                mCalendarView.addCalendarObject(parseCalendarObject(event));
                            }
                            Collections.sort(mEventList); //TODO ХЗ, че будет
                            mCalendarDialog.setEventList(mEventList);
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
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<>();
//                String credentials = login + ":" + password;
//                String auth = "Basic "  + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void refreshToken()
    {

    }
}
