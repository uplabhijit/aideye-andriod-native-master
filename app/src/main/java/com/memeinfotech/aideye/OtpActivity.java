package com.memeinfotech.aideye;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class OtpActivity extends AppCompatActivity {


    public EditText otp;
    RequestQueue requestQueue;
    private ProgressDialog progress;
    private Bundle bundle;
    static final String REQ_TAG = "OTPACTIVITY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_otp);

        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext())
                .getRequestQueue();


        otp = (EditText) findViewById(R.id.otp);

        bundle = getIntent().getExtras();

    }


    public void verifyotp(View view) {

        System.out.println("otp>>>>>>>>>>>");

        String phoneNumber = bundle.getString("phoneNumber");


        if (otp.getText().toString().length() > 0 && phoneNumber.toString().length() > 0) {

            progress = new ProgressDialog(this);
            progress.setMessage("please wait...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();


            JSONObject json = new JSONObject();

            try {
                json.put("otp", otp.getText().toString());
                json.put("phoneNumber", phoneNumber.toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url = getResources().getString(R.string.json_otp_post_url);

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
                                    Toast.makeText(OtpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                } else {

                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("store", response.toString());

                                    editor.putBoolean("loginStatus", true);
                                    editor.commit();

                                    boolean status = pref.getBoolean("loginStatus", false);


                                    if (status) {
//                                        System.out.println(role);
                                        Intent fp;

                                        fp = new Intent(OtpActivity.this, MainActivity.class);
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


            Toast.makeText(this, "OTP required", Toast.LENGTH_SHORT).show();

        }


    }
}
