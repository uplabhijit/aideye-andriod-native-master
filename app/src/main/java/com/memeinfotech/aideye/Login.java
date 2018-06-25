package com.memeinfotech.aideye;

import android.Manifest;
import android.app.AlertDialog;
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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

//32951 new port

public class Login extends AppCompatActivity
{
    RequestQueue requestQueue;
    public EditText phoneText, passText;
    private ProgressDialog progress;
    static final int INTERNET_REQ = 23;
    static final String REQ_TAG = "LOGINACTIVITY";
    public String model,deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String res = pref.getString("store", "");
        boolean status = pref.getBoolean("loginStatus", false);
        String role = pref.getString("role", "");
        System.out.println(res);
        System.out.println(status);
        System.out.println(role);

        if (status)
        {
            System.out.println(role);
            Intent fp;
            fp = new Intent(Login.this, MainActivity.class);
            startActivity(fp);
            finish();
        }
        else
        {
            System.out.println("stay in login page");
        }

        phoneText = (EditText) findViewById(R.id.phoneText);
        passText = (EditText) findViewById(R.id.passText);

        if (isPermissionGranted())
        {
            System.out.println("permission granted");

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            deviceId = telephonyManager.getDeviceId();
            System.out.println("devide id>>>>>>>>>>>>>>>>>" + deviceId);
            Toast.makeText(Login.this, deviceId, Toast.LENGTH_SHORT).show();
        } else {


            System.out.println("permission denied");


        }


        model = Build.MODEL;

        Toast.makeText(Login.this, model, Toast.LENGTH_SHORT).show();

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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

    public void Openregistration(View view) {

        startActivity(new Intent(this, Registration.class));

    }

    public void loginClick(View view) {


        if (phoneText.getText().toString().length() > 0 && passText.getText().toString().length() > 0) {

            progress = new ProgressDialog(this);
            progress.setMessage("please wait...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();


            JSONObject json = new JSONObject();
            JSONObject jsonobj = new JSONObject();

            try {
                json.put("phoneNumber", phoneText.getText().toString());
                json.put("password", passText.getText().toString());
                json.put("os","android");
                json.put("deviceId",deviceId);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url = getResources().getString(R.string.login_url);

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
                                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                                } else {

                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("store", response.toString());
//                                   String roleName = serverResp.getJSONObject("result").getString("role");
//                                   phoneNumber = serverResp.getJSONObject("result").getString("phoneNumber");
//                                  editor.putString("userId", serverResp.getJSONObject("result").getString("_id"));
//                                   editor.putString("role", roleName);
                                    editor.putBoolean("loginStatus", true);
                                    editor.commit();
//                                   String res = pref.getString("store", "");
                                    boolean status = pref.getBoolean("loginStatus", false);
//                                   String role = pref.getString("role", "");
//                                   String userId = pref.getString("userId", "");
//                                   System.out.println("1-----------------------" + userId);
//                                   System.out.println("2-----------------------" + res);
//                                    System.out.println("3-----------------------" + status);

                                    if (status) {
//                                        System.out.println(role);
                                        Intent fp;

                                        fp = new Intent(Login.this, MainActivity.class);
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

            Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show();

        }
    }


    public void forgotpassword(View view) {

        AlertDialog alertDialog = new AlertDialog.Builder(
                Login.this).create();
        alertDialog.setMessage("Coming Soon......");
        alertDialog.show();
    }
}
