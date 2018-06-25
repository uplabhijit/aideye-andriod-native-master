package com.memeinfotech.aideye;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Registration extends AppCompatActivity {

    RequestQueue requestQueue;
    public EditText nameText, phoneText, passText, addressText, zipcode;
    public AutoCompleteTextView emailText;
    private ProgressDialog progress;
    static final int INTERNET_REQ = 23;
    static final String REQ_TAG = "VACTIVITY";
    public String deviceId;
    public String model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        getSupportActionBar().hide();

        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext())
                .getRequestQueue();


        nameText = (EditText) findViewById(R.id.nameText);
        emailText = (AutoCompleteTextView) findViewById(R.id.emailText);
        phoneText = (EditText) findViewById(R.id.phoneText);
        passText = (EditText) findViewById(R.id.passText);
        addressText = (EditText) findViewById(R.id.addressText);
        zipcode = (EditText) findViewById(R.id.zipcode);


        if (isPermissionGranted()) {
            System.out.println("permission granted");


            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            deviceId = telephonyManager.getDeviceId();
            System.out.println("devide id>>>>>>>>>>>>>>>>>" + deviceId);
            Toast.makeText(Registration.this, deviceId, Toast.LENGTH_SHORT).show();
        } else {


            System.out.println("permission denied");


        }


        model = Build.MODEL;

        Toast.makeText(Registration.this, model, Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    //do ur specific task after read phone state granted
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void register(View view) {


        emailText.setError(null);
        boolean cancel = false;
        View focusView = null;

        String email = emailText.getText().toString();

        if (!isEmailValid(email)) {
            emailText.setError(getString(R.string.error_invalid_email));
            focusView = emailText;
            cancel = true;
        }


        if (nameText.getText().toString().length() > 0 && emailText.getText().toString().length() > 0 && phoneText.getText().toString().length() > 0 && passText.getText().toString().length() > 0 && addressText.getText().toString().length() > 0 && zipcode.getText().toString().length() > 0) {

            progress = new ProgressDialog(this);
            progress.setMessage("please wait...");
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
                jsonobj.put("model",model);
                jsonobj.put("os","android");
                jsonobj.put("deviceId",deviceId);
                JSONArray deviceInfo = new JSONArray();
                deviceInfo.put(jsonobj);
                json.put("deviceInfo",deviceInfo);
                System.out.println(json);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url = getResources().getString(R.string.json_get_url);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            progress.dismiss();
//                            serverResp.setText("String Response : "+ response.toString());
                            System.out.println(response.toString());
                            try {
                                JSONObject serverResp = new JSONObject(response.toString());
                                System.out.println("success result: " + serverResp);


                                String errorStatus = serverResp.getString("error");
                                if (errorStatus.equals("true")) {
                                    String errorMessage = serverResp.getString("message");
                                    Toast.makeText(Registration.this, errorMessage, Toast.LENGTH_SHORT).show();
                                } else {


                                        Intent fp;

                                        fp = new Intent(Registration.this, OtpActivity.class);
                                        fp.putExtra("phoneNumber",phoneText.getText().toString());
                                        startActivity(fp);


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

            emailText.setError(getString(R.string.error_field_required));
            focusView = emailText;
            cancel = true;


            Toast.makeText(this, "All the fields are required", Toast.LENGTH_SHORT).show();

        }

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
}
