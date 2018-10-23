package com.anweshainfo.anwesha_registration;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anweshainfo.anwesha_registration.Adapter.CustomSpinnerAdapter;
import com.anweshainfo.anwesha_registration.Adapter.RVAdapter;
import com.anweshainfo.anwesha_registration.model.Participant;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * Created by manish on 27/10/17.
 */

public class qrscannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    RequestQueue mQueue;
    @BindView(R.id.scanner)
    LinearLayout scannerView;
    @BindView(R.id.spinner_events_name)
    Spinner eventsspinner;
    @BindView(R.id.rv_participants)
    RecyclerView recyclerView;
    private SharedPreferences.Editor isLogged;
    private ZXingScannerView mScannerView;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private ArrayList<String> eventNameList= new ArrayList<>();
    private ArrayList<String> eventIdList = new ArrayList<>();
    private ArrayList<Participant> participants = new ArrayList<>();
    private String mBaseUrl;
    private SharedPreferences mSharedPreferences;
    private RVAdapter rvAdapter;
    private String eventName;
    private String eventId;
    private String paymentRegId = "0";
    private String viewUserId = "view";
    private String mMakepaymentUrl;
    private CustomSpinnerAdapter customSpinnerAdapter;
    private JSONObject jsonObject;
    private boolean isCamActive = false;
    TextInputEditText mInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.event_selector);
        mInputEditText = findViewById(R.id.clstInput);
        setUI();
    }

    private void setUI() {
        ButterKnife.bind(this);
        mBaseUrl = getResources().getString(R.string.url_register);
        //mMakepaymentUrl=getResources().getString(R.string.makePaymentUrl);
        Log.e("This ", "This activity was started .....");
        Log.e("Thissss", "" + eventNameList.size());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLogged = PreferenceManager.getDefaultSharedPreferences(this).edit();
        mQueue = Volley.newRequestQueue(this);

        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);

        checkPermission();


        //extracting the json response
        String response = mSharedPreferences.getString("jsonEventResponse", "");
        try {
            jsonObject = new JSONObject(response);
        } catch (Exception e) {
            Log.e("Error in Json", e.toString());
        }

        eventNameList = filterEventName(jsonObject);
//        eventNameList.add(getString(R.string.view_user_details));

        eventIdList = filterEventid(jsonObject);
//        eventIdList.add(viewUserId);

        eventId = "";

        //set the array adapter
        customSpinnerAdapter = new CustomSpinnerAdapter(this, eventNameList);
        eventsspinner.setAdapter(customSpinnerAdapter);
        eventsspinner.setSelection(mSharedPreferences.getInt("spinnerVal", 0));


        eventsspinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                show(i);
                //setting the value
                eventName = eventNameList.get(i);
                eventId = eventIdList.get(i);
                mSharedPreferences.edit().putInt("spinnerVal", i).apply();

                if(eventId.equals("0")) {
                    mInputEditText.setVisibility(View.VISIBLE);
                } else {
                    mInputEditText.setVisibility(View.INVISIBLE);
                }

                setUpRV();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start the scan
                Scan();
            }
        });

        rvAdapter = new RVAdapter(participants);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setUpRV();
    }

    private void setUpRV() {
        participants.clear();
        rvAdapter.notifyDataSetChanged();



        String postUrl = mBaseUrl+"getReg/" + eventId ;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Response registered:", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 200) {
                                fillRV(jsonObject);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("Error : ", error.toString());
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error fetching registered user",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uID", getuID());
                params.put("val", mSharedPreferences.getString("val", ""));

//                Log.e("USERID : ", getuID());
//                Log.e("AUTHKEY : ", mSharedPreferences.getString("key", ""));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    private void fillRV(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        participants.clear();
        Log.e("ResponseX1",jsonArray.length()+"") ;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject j1 = jsonArray.getJSONObject(i);
            participants.add(new Participant(j1.getString("name"), j1.getString("regID")));
        }
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_log, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                Toast.makeText(this, "Logging Out", Toast.LENGTH_LONG).show();
                isLogged.putBoolean("isloggedIn", false);
                isLogged.apply();
                isLogged.commit();
                mSharedPreferences.edit().clear().apply();
                Intent intent = new Intent(qrscannerActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("savedInstanceEvent", eventNameList);
        outState.putStringArrayList("savedInstanceID", eventIdList);

    }



    private ArrayList<String> filterEventName(JSONObject jsonObject) {
        try {

            ArrayList<String> eventName = new ArrayList<>();
            JSONArray event_name_list = jsonObject.getJSONArray("data");
            for(int i=0;i<event_name_list.length();++i) {
                JSONObject j1 = event_name_list.getJSONObject(i);
                eventName.add(j1.getString("name"));
            }
            return eventName;

        } catch (JSONException e) {
            Log.e("QRActivityclass ", " Error in parsing json event " + e.getMessage());
        }
        return null;
    }


    private ArrayList<String> filterEventid(JSONObject jsonObject) {
        try {

            ArrayList<String> eventId = new ArrayList<>();
            JSONArray event_name_list = jsonObject.getJSONArray("data");
            for(int i=0;i<event_name_list.length();++i) {
                JSONObject j1 = event_name_list.getJSONObject(i);
                eventId.add(j1.getString("id"));
            }
            return eventId;

        } catch (JSONException e) {
            Log.e("qrScannerAcitivity ", " Error in parsing json id " + e.getMessage());
        }

        return null;
    }

    /**
     * Helper method to show the value which is selected
     */
    public void show(int i) {
        Toast.makeText(this, eventNameList.get(i), Toast.LENGTH_LONG).show();
    }

    public void Scan() {
        isCamActive = true;
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.setAutoFocus(true);

    }

    @Override
    public void onBackPressed() {
        if (isCamActive) {
            mScannerView.stopCamera();
            isCamActive = false;
//            mScannerView = null;
//            this.finish();
//            Intent intent = new Intent(this, qrscannerActivity.class);
//            startActivity(intent);
            setContentView(R.layout.event_selector);
            setUI();
        } else
            super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        if(eventId.equals("0")) {
            String clst_id  = mInputEditText.getText().toString().trim();
            Log.e("cslst", mInputEditText.getText().toString().trim());
            pairCelestaID(clst_id, rawResult.getText());
        } else if(eventId.equals("1")) {
            checkin(rawResult.getText());
        } else  if(eventId.equals("2")) {
            checkout(rawResult.getText());
        } else {
            setReg(rawResult.getText());
        }

        //TODO check in checkout and pair
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // starts the scanning back up again
                        mScannerView.resumeCameraPreview(qrscannerActivity.this);
                    }
                });
            }
        }).start();

    }

    //check whether there is permission
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //We don't need an explanation because this will definitely require camera access to scan
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "This is done", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error");
                    builder.setMessage("Unable to Start camera.\n Go to Settings\n->App->Anwesha2k17-Registration->Permission\n" +
                            "Turn on Camera there");
                    AlertDialog alert1 = builder.create();
                    alert1.show();
                    this.finish();
                }
            }
        }
    }

    private  void pairCelestaID(String clstID, String qrHash) {
        String postUrl = getString(R.string.url_register) + "pair/"+clstID.trim()+"/"+qrHash.trim();
        makePost(postUrl);
    }


    private void checkin(String qrhash) {
        String postUrl = getString(R.string.url_register)+"checkin/"+qrhash ;
        makePost(postUrl);
    }

    private  void checkout(String qrhash) {
        String postUrl = getString(R.string.url_register)+"checkout/"+qrhash ;
        makePost(postUrl);
    }

    private void setReg(String qrhash) {
        String postUrl = mBaseUrl+"setReg/" + eventId+"/"+qrhash ;
        makePost(postUrl);
    }

    private void makePost(String postUrl) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("pairResponse", response);
                        try {
                            JSONObject jsonObject  = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            switch (status) {
                                case 200:
                                    Toast.makeText(getApplicationContext(), "Scan successful", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(qrscannerActivity.this, reg_result.class);
                                    startActivity(intent);
                                    break;

                                default:
                                    Toast.makeText(getApplicationContext(), "Error .Please try again later", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                         Log.v("Error : ", error.toString());
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error while process", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uID", getuID());
                params.put("val", mSharedPreferences.getString("val", ""));

//                Log.e("USERID : ", getuID());
//                Log.e("AUTHKEY : ", mSharedPreferences.getString("key", ""));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }
    /**
     * @params result returns the uID(CELESTAID) of the
     * the id which is celesta ID registration of id
     */

    private String getuID() {
        String uID = mSharedPreferences.getString("uID", null);
        return uID;
    }


}