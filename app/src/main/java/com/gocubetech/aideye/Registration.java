package com.gocubetech.aideye;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gocubetech.aideye.Constant.ApiConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Registration extends AppCompatActivity {

    RequestQueue requestQueue;
    public EditText nameText, phoneText, passText, addressText, zipcode;
    public AutoCompleteTextView emailText;
    private ProgressDialog progress;
    static final String REQ_TAG = "VACTIVITY";
    public String deviceId;
    public String model;
    private JSONObject activeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        getSupportActionBar().hide();
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext())
                .getRequestQueue();
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        passText = findViewById(R.id.passText);
        addressText = findViewById(R.id.addressText);
        zipcode = findViewById(R.id.zipcode);
        passText.setOnKeyListener(onSoftKeyboardDonePress);
        if (isPermissionGranted()) {
            System.out.println("permission granted");
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            /*To get deviceId*/
            deviceId = telephonyManager.getDeviceId();
        } else {
            System.out.println("permission denied");
        }

        /*to get model number*/
        model = Build.MODEL;
        String os = "android";
        System.out.println("os>>>>>>>>>>>>>>>>>" + os);
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permission granted");
                    //do ur specific task after read phone state granted
                } else {
                    Toast.makeText(getApplicationContext(), R.string.permissiondeniedmsg, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /*function call to register*/
    public void register(View view) {
        if (nameText.getText().toString().length() > 0 && emailText.getText().toString().length() > 0 && phoneText.getText().toString().length() > 0 && passText.getText().toString().length() > 0 && addressText.getText().toString().length() > 0 && zipcode.getText().toString().length() > 0) {
            progress = new ProgressDialog(this);
            progress.setMessage(getString(R.string.pleasewaitloadermsg));
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
            JSONObject json = new JSONObject();
            JSONObject jsonobj = new JSONObject();
            try {
                json.put("name", nameText.getText().toString());
                json.put("email", emailText.getText().toString());
                json.put("phoneNumber", phoneText.getText().toString());
                json.put("password", passText.getText().toString());
                json.put("address", addressText.getText().toString());
                json.put("zipcode", zipcode.getText().toString());
                jsonobj.put("model", model);
                jsonobj.put("os", "android");
                jsonobj.put("deviceId", deviceId);
                JSONArray deviceInfo = new JSONArray();
                deviceInfo.put(jsonobj);
                json.put("deviceInfo", deviceInfo);
                System.out.println(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("device Id---------------" + json);
            String url = ApiConstant.api_registration_url;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progress.dismiss();
                            System.out.println(response.toString());
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                System.out.println("success result: " + serverResp);
                                String errorStatus = serverResp.getString("error");
                                if (errorStatus.equals("true")) {
                                    String errorMessage = serverResp.getString("message");
                                    Toast.makeText(Registration.this, errorMessage, Toast.LENGTH_SHORT).show();
                                } else {

                                    JSONArray subscriptionarray = response.getJSONObject("result").getJSONArray("subscription");
                                    for (int i = 0; i < subscriptionarray.length(); i++) {
                                        if ((subscriptionarray.getJSONObject(i).getString("Active")).equals("true")) {
                                            activeSubscription = subscriptionarray.getJSONObject(i);
                                        }
                                    }

                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("store", response.toString());
                                    editor.putBoolean("loginStatus", true);
                                    editor.putString("userId", response.getJSONObject("result").getString("_id"));
                                    editor.putString("activeSubscription", activeSubscription.toString());

                                    editor.commit();
                                    boolean status = pref.getBoolean("loginStatus", false);
                                    if (status) {
                                        Intent fp;
                                        fp = new Intent(Registration.this, MainActivity.class);
                                        startActivity(fp);
                                        finish();
                                    }
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                progress.dismiss();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.dismiss();
                    System.out.println("Error getting response");
                }
            });
            jsonObjectRequest.setTag(REQ_TAG);
            requestQueue.add(jsonObjectRequest);
        } else {
            Toast.makeText(this, R.string.allfieldreqmsg, Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnKeyListener onSoftKeyboardDonePress = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                register(null);
            }
            return false;
        }
    };
}
