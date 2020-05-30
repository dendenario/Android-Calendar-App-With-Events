package com.example.sbercalendarfinal.uihelpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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
import com.example.sbercalendarfinal.CalendarViewWithNotesActivitySDK21;
import com.example.sbercalendarfinal.R;
import com.example.sbercalendarfinal.SelectColorDialog;
import com.example.sbercalendarfinal.SelectDateAndTimeActivity;
import com.example.sbercalendarfinal.data.Event;
import com.example.sbercalendarfinal.utils.ColorUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CreateEventActivity extends AppCompatActivity {

    private static final String URL_TODO = "http://95.131.148.98:8080/api/todo";

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss");

    public static final int ACTION_DELETE = 1;
    public static final int ACTION_EDIT = 2;
    public static final int ACTION_CREATE = 3;

    private static final String INTENT_EXTRA_ACTION = "intent_extra_action";
    private static final String INTENT_EXTRA_EVENT = "intent_extra_event";
    private static final String INTENT_EXTRA_CALENDAR = "intent_extra_calendar";

    private static final int SET_DATE_AND_TIME_REQUEST_CODE = 200;

    private final static SimpleDateFormat dateFormat
            = new SimpleDateFormat("EEEE, dd/MM    HH:mm", Locale.getDefault());

    private Event mOriginalEvent;

    private Calendar mCalendar;
    private String mTitle;
    private boolean mIsComplete;
    private int mColor;

    private boolean isViewMode = true;

    private EditText mTitleView;
    private EditText mDescView;
    private Switch mIsCompleteCheckBox;
    private TextView mDateTextView;
    private CardView mColorCardView;
    private View mHeader;

    private String token;
    private String login;

    public static String formatDate(GregorianCalendar date) throws ParseException {

        simpleDateFormat.setCalendar(date);
        String dateFormatted = simpleDateFormat.format(date.getTime());
        System.out.println(dateFormatted);
        return dateFormatted;
    }

    public static String formatTime(GregorianCalendar date) throws ParseException {

        simpleTimeFormat.setCalendar(date);
        String dateFormatted = simpleTimeFormat.format(date.getTime());
        return dateFormatted;
    }

    public static Intent makeIntent(Context context, @NonNull Calendar calendar) {
        return new Intent(context, CreateEventActivity.class).putExtra(INTENT_EXTRA_CALENDAR, calendar);
    }

    public static Intent makeIntent(Context context, @NonNull Event event) {
        return new Intent(context, CreateEventActivity.class).putExtra(INTENT_EXTRA_EVENT, event);
    }

    public static Event extractEventFromIntent(Intent intent) {
        return intent.getParcelableExtra(INTENT_EXTRA_EVENT);
    }

    public static int extractActionFromIntent(Intent intent) {
        return intent.getIntExtra(INTENT_EXTRA_ACTION, 0);
    }

    public static Calendar extractCalendarFromIntent(Intent intent) {
        return (Calendar) intent.getSerializableExtra(INTENT_EXTRA_CALENDAR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        extractDataFromIntentAndInitialize();

        initializeUI();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete: {
                delete();
                return true;
            }
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void extractDataFromIntentAndInitialize() {

        mOriginalEvent = extractEventFromIntent(getIntent());

        if (mOriginalEvent == null) {
            mCalendar = extractCalendarFromIntent(getIntent());
            if (mCalendar == null)
                mCalendar = Calendar.getInstance();
            mCalendar.set(Calendar.HOUR_OF_DAY, 8);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mCalendar.set(Calendar.MILLISECOND, 0);
            mColor = ColorUtils.mColors[0];
            mTitle = "";
            mIsComplete = false;
            isViewMode = false;
        }
        else {
            mCalendar = mOriginalEvent.getDate();
            mColor = mOriginalEvent.getColor();
            mTitle = mOriginalEvent.getTitle();
            mIsComplete = mOriginalEvent.isCompleted();
            isViewMode = true;
        }

        Bundle args = getIntent().getExtras();
        if(login==null) {
            login = args.get("login").toString();
        }
        if(token==null) {
            token = args.getString("token");
        }
    }

    private void initializeUI() {
        setContentView(R.layout.activity_create_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mHeader = findViewById(R.id.ll_header);
        mHeader.setVisibility(View.VISIBLE);

        setupToolbar();

        View tvSave = mHeader.findViewById(R.id.tv_save);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        View tvCancel = mHeader.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

                if (mOriginalEvent == null)
                    overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
            }
        });

        mDateTextView = findViewById(R.id.tv_date);
        mDateTextView.setText(dateFormat.format(mCalendar.getTime()));
        mDateTextView.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                Activity context = CreateEventActivity.this;
                Intent intent = SelectDateAndTimeActivity.makeIntent(context, mCalendar);

                startActivityForResult(intent,
                        SET_DATE_AND_TIME_REQUEST_CODE,
                        ActivityOptions.makeSceneTransitionAnimation(context).toBundle());
            }
        });

        mColorCardView = findViewById(R.id.cardView_event_color);
        mColorCardView.setCardBackgroundColor(mColor);
        mColorCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SelectColorDialog.Builder.instance(CreateEventActivity.this)
                        .setSelectedColor(mColor)
                        .setOnColorSelectedListener(new SelectColorDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                mColor = color;
                                mColorCardView.setCardBackgroundColor(mColor);
                            }
                        })
                        .create()
                        .show();
            }
        });
        mTitleView = findViewById(R.id.et_event_title);
        mDescView = findViewById(R.id.et_event_desc);
        mTitleView.setText(mTitle);
        mIsCompleteCheckBox = findViewById(R.id.checkbox_completed);
        mIsCompleteCheckBox.setChecked(mIsComplete);

        if (isViewMode) {
            mIsCompleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setupEditMode();
                    mIsCompleteCheckBox.setOnCheckedChangeListener(null);
                }
            });
            mTitleView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    setupEditMode();
                    mTitleView.setOnFocusChangeListener(null);
                }
            });
        }
        else {
            setupEditMode();
        }
    }

    private void setupEditMode() {
        if (isViewMode) {
            isViewMode = false;
            setupToolbar();
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            if (isViewMode)
                getSupportActionBar().show();
            else
                getSupportActionBar().hide();
        }

        if (mHeader != null) {
            mHeader.setVisibility(isViewMode? View.GONE : View.VISIBLE);
        }
    }

    private void delete() {
        Log.i(getClass().getSimpleName(), "delete");

        deleteTASK(mOriginalEvent.getTitle(), (GregorianCalendar) mOriginalEvent.getDate(), ACTION_DELETE, mOriginalEvent.getID(),mOriginalEvent.getDecription());

    }

    private void save() {

        int action = mOriginalEvent != null ? ACTION_EDIT : ACTION_CREATE;
        String id = mOriginalEvent != null ? mOriginalEvent.getID() : generateID();
        String rawTitle = mTitleView.getText().toString().trim();
        String rawDesc = mDescView.getText().toString().trim();

        mOriginalEvent = new Event(
                id,
                rawTitle.isEmpty() ? null : rawTitle,
                mCalendar,
                mColor,
                mIsCompleteCheckBox.isChecked(),
                rawDesc.isEmpty() ? null : rawDesc
        );
        if(action==ACTION_CREATE) {
            createTASK(mOriginalEvent.getTitle(), (GregorianCalendar) mOriginalEvent.getDate(), action, mOriginalEvent.getID(),mOriginalEvent.getDecription());
        }
        else if(action==ACTION_EDIT)
        {
            editTASK(mOriginalEvent.getTitle(), (GregorianCalendar) mOriginalEvent.getDate(), action, mOriginalEvent.getID(),mOriginalEvent.getDecription());
        }



//        setResult(RESULT_OK, new Intent()
//                .putExtra(INTENT_EXTRA_ACTION, action)
//                .putExtra(INTENT_EXTRA_EVENT, mOriginalEvent));
//        finish();
//
//        if (action == ACTION_CREATE)
//            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_DATE_AND_TIME_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                mCalendar = SelectDateAndTimeActivity.extractCalendarFromIntent(data);
                mDateTextView.setText(dateFormat.format(mCalendar.getTime()));

                setupEditMode();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static String generateID() {
        return Long.toString(System.currentTimeMillis());
    }



    private void createTASK(final String name, final GregorianCalendar taskDate, final int action, final String id, final String description)
    {
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_TODO,
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
                        Toast.makeText(getApplicationContext(), "Не удается отправить данные", Toast.LENGTH_LONG).show();
                    }
                })
        {

            @Override
            public byte[] getBody() throws AuthFailureError
            {

                JSONObject taskType = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                JSONObject user = new JSONObject();
                    try {
                        taskType.put("id", id);
                        user.put("id", 1);
                        String eventDate = null;
                        String eventTime = null;
                        try {
                            eventDate = formatDate(taskDate);
                            eventTime = formatTime(taskDate);
                            jsonObject.put("name", name);
                            jsonObject.put("description", description);
                            jsonObject.put("date", eventDate);
                            jsonObject.put("time", eventTime);
                            jsonObject.put("taskType", taskType);
                            jsonObject.put("user", user);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("CreateEventA:JSONError", e.getMessage());
                }
                    Log.i("Params", jsonObject.toString());
                    return jsonObject.toString().getBytes();
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                if(response!=null) {
                    int status = response.statusCode;
                    Log.i("CrEvAct:Server response", " " + response.statusCode);
                    if (status == 200) {
                        setResult(RESULT_OK, new Intent()
                                .putExtra(INTENT_EXTRA_ACTION, action)
                                .putExtra(INTENT_EXTRA_EVENT, mOriginalEvent));
                        finish();

                        if (action == ACTION_CREATE)
                            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
                        Log.i("CrEvAct:ActivityChange", " Starting " + CalendarViewWithNotesActivitySDK21.class);
                    }
                }
                else
                {
                    Log.e("CrEvAct:Volley", "Null network response");
                }
                return super.parseNetworkResponse(response);
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


    private void deleteTASK(final String name, final GregorianCalendar taskDate, final int action, final String id, final String description)
    {
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE, URL_TODO+"/"+id,
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
                        Toast.makeText(getApplicationContext(), "Не удается отправить данные", Toast.LENGTH_LONG).show();
                    }
                })
        {

//            @Override
//            public byte[] getBody() throws AuthFailureError
//            {
//                JSONObject taskType = new JSONObject();
//                JSONObject jsonObject = new JSONObject();
//                JSONObject user = new JSONObject();
//                try {
//                    taskType.put("id", id);
//                    user.put("id", "inapik");
//                    String eventDate = null;
//                    String eventTime = null;
//                    try {
//                        eventDate = formatDate(taskDate);
//                        eventTime = formatTime(taskDate);
//                    jsonObject.put("name", name);
//                    jsonObject.put("description", description);
//                    jsonObject.put("date", eventDate);
//                    jsonObject.put("time", eventTime);
//                    jsonObject.put("taskType", taskType);
//                    jsonObject.put("user", user);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.e("CreateEventA:JSONError", e.getMessage());
//                }
//                Log.i("Params", jsonObject.toString());
//                return jsonObject.toString().getBytes();
//            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                if(response!=null) {
                    int status = response.statusCode;
                    Log.i("CrEvAct:Server response", " " + response.statusCode);
                    if (status == 200)
                    {
                        setResult(RESULT_OK, new Intent()
                                .putExtra(INTENT_EXTRA_ACTION, ACTION_DELETE)
                                .putExtra(INTENT_EXTRA_EVENT, mOriginalEvent));
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
                        Log.i("CrEvAct:ActivityChange", " Starting " + CalendarViewWithNotesActivitySDK21.class);
                    }
                    if (status == 400)
                    {
                        Toast.makeText(CreateEventActivity.this, "Данные имеют неверную форму", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Log.e("CrEvAct:Volley", "Null network response");
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

    private void editTASK(final String name, final GregorianCalendar taskDate, final int action, final String id, final String description)
    {
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL_TODO+"/"+id,
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
                        Toast.makeText(getApplicationContext(), "Не удается отправить данные", Toast.LENGTH_LONG).show();
                    }
                })
        {

            @Override
            public byte[] getBody() throws AuthFailureError
            {
                JSONObject taskType = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                JSONObject user = new JSONObject();
                try {
                    taskType.put("id", id);
                    user.put("id", 1);
                    String eventDate = null;
                    String eventTime = null;
                    try {
                        eventDate = formatDate(taskDate);
                        eventTime = formatTime(taskDate);
                    jsonObject.put("name", name);
                    jsonObject.put("description", description);
                    jsonObject.put("date", eventDate);
                    jsonObject.put("time", eventTime);
                    jsonObject.put("taskType", taskType);
                    jsonObject.put("user", user);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("CreateEventA:JSONError", e.getMessage());
                }
                Log.i("Params", jsonObject.toString());
                return jsonObject.toString().getBytes();
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // take the statusCode here.
                if(response!=null) {
                    int status = response.statusCode;
                    Log.i("CrEvAct:Server response", " " + response.statusCode);
                    if (status == 200)
                    {
                        setResult(RESULT_OK, new Intent()
                                .putExtra(INTENT_EXTRA_ACTION, action)
                                .putExtra(INTENT_EXTRA_EVENT, mOriginalEvent));
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
                        Log.i("CrEvAct:ActivityChange", " Starting " + CalendarViewWithNotesActivitySDK21.class);
                    }
                    if (status == 400)
                    {
                        Toast.makeText(CreateEventActivity.this, "Данные имеют неверную форму", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Log.e("CrEvAct:Volley", "Null network response");
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
}
