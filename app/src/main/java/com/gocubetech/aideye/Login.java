package com.gocubetech.aideye;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gocubetech.aideye.Constant.ApiConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//32951 new port

public class Login extends AppCompatActivity implements View.OnClickListener {

    RequestQueue requestQueue;
    public EditText phoneText, passText;
    private ProgressDialog progress;
    private Button login;
    static final String REQ_TAG = "LOGINACTIVITY";
    public String model, deviceId;
    private JSONObject activeSubscription;
    public VideoView videoView;
    int mVideoCurrentPosition;
    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        mMediaPlayer = new MediaPlayer();
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        boolean status = pref.getBoolean("loginStatus", false);
        String role = pref.getString("role", "");
        phoneText = findViewById(R.id.phoneText);
        passText = findViewById(R.id.passText);
        login = findViewById(R.id.btn_Login);
        login.setOnClickListener(this);
        passText.setOnKeyListener(onSoftKeyboardDonePress);
        if (status) {
            System.out.println(role);
            Intent fp;
            fp = new Intent(Login.this, MainActivity.class);
            startActivity(fp);
            finish();
        } else {
            System.out.println("stay in login page");
        }
        if (isPermissionGranted()) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            deviceId = telephonyManager.getDeviceId();
        } else {
            System.out.println("permission denied");
        }
        model = Build.MODEL;
        String os = "android";
        System.out.println("os>>>>>>>>>>>>>>>>>" + os);

        //addVideoView();
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            return true;
        }


    }

   /* @Override
    protected void onPause() {
        super.onPause();

        mVideoCurrentPosition = mMediaPlayer.getCurrentPosition();
        videoView.pause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }*/

    /*private void addVideoView() {
        System.out.println("enter in video view");

        videoView = (VideoView) findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.bg_video);
        System.out.println("uri??????????????????"+uri);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mMediaPlayer = mediaPlayer;
                mMediaPlayer.setLooping(true);

                if(mVideoCurrentPosition != 0){
                    mMediaPlayer.seekTo(mVideoCurrentPosition);
                    mMediaPlayer.start();
                }
            }
        });

    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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

    //function call to open registration page
    public void Openregistration(View view) {
        startActivity(new Intent(this, Registration.class));
    }

    //function call to login
    public void onClick(View view) {
        if (phoneText.getText().toString().length() > 0 && passText.getText().toString().length() > 0) {
            progress = new ProgressDialog(this);
            progress.setMessage(getString(R.string.pleasewaitloadermsg));
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
            JSONObject json = new JSONObject();
            try {
                json.put("phoneNumber", phoneText.getText().toString());
                json.put("password", passText.getText().toString());
                json.put("os", "android");
                json.put("deviceId", deviceId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("login json"+json);
            //api to login
            String url = ApiConstant.api_login_url;
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
                                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.msgforwrongemailpass, Toast.LENGTH_SHORT).show();
        }
    }

    //function call to forgot password
    public void forgotPassword(View view) {
        /*Toast.makeText(this, R.string.comingsoonmsg, Toast.LENGTH_SHORT).show();*/
        Intent fp;

        fp = new Intent(Login.this, ForgotPasswordActivity.class);
        startActivity(fp);
    }

    private View.OnKeyListener onSoftKeyboardDonePress = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                onClick(null);
            }
            return false;
        }
    };
}

