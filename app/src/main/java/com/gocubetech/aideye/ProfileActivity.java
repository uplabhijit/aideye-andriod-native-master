package com.gocubetech.aideye;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gocubetech.aideye.Constant.ApiConstant;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Utility.SDUtility;
import de.hdodenhof.circleimageview.CircleImageView;
import in.mayanknagwanshi.imagepicker.imageCompression.ImageCompressionListener;
import in.mayanknagwanshi.imagepicker.imagePicker.ImagePicker;
import pub.devrel.easypermissions.EasyPermissions;
public class ProfileActivity extends AppCompatActivity implements SingleUploadBroadcastReceiver.Delegate {

    public EditText nameText, addressText, zipcode, phoneText;
    public AutoCompleteTextView emailText;
    public Button btn_update;
    public CircleImageView personimage, editimage;
    private ProgressDialog progress;
    static final String REQ_TAG = " PROFILEUPDATEACTIVITY";
    public String userId;

    ImagePicker imagePicker;
    private SingleUploadBroadcastReceiver uploadReceiver;
    public String uploadId;
    private String filePath;
    //storage permission code
    /* private static final int STORAGE_PERMISSION_CODE = 123;*/
    private Bitmap selectedImage;
    private ProgressBar profile_imageview_progressbar;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private RequestQueue requestQueue;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        inituielements();
        getProfileData();
        //Requesting storage permission
//        requestStoragePermission();
        //To initialize image picker
        imagePicker = new ImagePicker();
        //To initialize receiver
        uploadReceiver = new SingleUploadBroadcastReceiver();
        //To get request queue
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    private void getProfileData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        try {
            JSONObject serverResp = new JSONObject(pref.getString("store", ""));
            System.out.println(serverResp);
            if (serverResp.getJSONObject("result").has("name")) {
                nameText.setText(serverResp.getJSONObject("result").getString("name"));
            }
            if (serverResp.getJSONObject("result").has("email")) {
                emailText.setText(serverResp.getJSONObject("result").getString("email"));
            }
            if (serverResp.getJSONObject("result").has("phoneNumber")) {
                phoneText.setText(serverResp.getJSONObject("result").getString("phoneNumber"));
            }
            if (serverResp.getJSONObject("result").has("address")) {
                addressText.setText(serverResp.getJSONObject("result").getString("address"));
            }
            if (serverResp.getJSONObject("result").has("zipcode")) {
                zipcode.setText(serverResp.getJSONObject("result").getString("zipcode"));
            }
            if (serverResp.getJSONObject("result").has("imageId")) {
                Boolean value = !serverResp.getJSONObject("result").getString("imageId").equals("");
                if (value) {
                    progress = new ProgressDialog(this);
                    progress.setMessage("Loading...");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    String getImageUrl = ApiConstant.api_downloadimage_url + serverResp.getJSONObject("result").getString("imageId");
                    Glide.with(this)
                            .asBitmap()
                            .load(getImageUrl)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    personimage.setImageBitmap(resource);
                                    progress.dismiss();
                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                    progress.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Image load fails", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            SDUtility.displayExceptionMessage(e.getMessage(), ProfileActivity.this);
        }
        //To make all fiels disable
        nameText.setEnabled(false);
        emailText.setEnabled(false);
        phoneText.setEnabled(false);
        addressText.setEnabled(false);
        zipcode.setEnabled(false);
    }

    private void inituielements() {
        nameText = findViewById(R.id.nameText);
        addressText = findViewById(R.id.addressText);
        zipcode = findViewById(R.id.zipcode);
        phoneText = findViewById(R.id.phoneText);
        personimage = findViewById(R.id.personimage);
        editimage = findViewById(R.id.editimage);
        emailText = findViewById(R.id.emailText);
        btn_update = findViewById(R.id.btn_update);
        profile_imageview_progressbar = findViewById(R.id.profile_imageview_progressbar);
        //add listener to update button
        btn_update.setOnClickListener(view -> {
            //function call to update profile
            updateProfile();
        });
        //add listener to update profile image
        editimage.setOnClickListener(view -> {
            //function call update profile image
            updateProfileImage();
        });
    }

    //Function call to upload profile image
    private void updateProfileImage() {
        if (EasyPermissions.hasPermissions(ProfileActivity.this, galleryPermissions)) {
            imagePicker.withActivity(ProfileActivity.this) //calling from activity
                    .chooseFromGallery(true) //default is true
                    .chooseFromCamera(true) //default is true
                    .withCompression(false) //default is true
                    .start();
        } else {
            EasyPermissions.requestPermissions(ProfileActivity.this, getString(R.string.accessstorage_toastmsg), 101, galleryPermissions);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImagePicker.SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            //Add compression listener if withCompression is set to true
            imagePicker.addOnCompressListener(new ImageCompressionListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onCompressed(String filePath) {//filePath of the compressed image
                    //convert to bitmap easily
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    //To set person image
                    personimage.setImageBitmap(selectedImage);
                }
            });
        }
        //call the method 'getImageFilePath(Intent data)' even if compression is set to false
        filePath = imagePicker.getImageFilePath(data);
        System.out.println("filepath_--------------------" + filePath);
        if (filePath != null) {//filePath will return null if compression is set to true
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            selectedImage = BitmapFactory.decodeFile(filePath, options);
            profile_imageview_progressbar.setVisibility(View.VISIBLE);
            uploadMultipart(filePath);
        }
    }

    //To upload multi part image
    private void uploadMultipart(String filePath) {
        System.out.println("ffcgvbhjbhkbj" + filePath);
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            String uploadUrl = ApiConstant.api_uploadimage_url;
            System.out.println("upload url" + uploadUrl);
            uploadReceiver.setDelegate(this);
            uploadReceiver.setUploadID(uploadId);
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, uploadUrl)
                    .addHeader("Content-Type", "multipart/form-data")
                    .addFileToUpload(filePath, "file") //Adding file
                    .addParameter("name", "profileImage") //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            profile_imageview_progressbar.setVisibility(View.GONE);
        }
    }

    //function call to update profile
    private void updateProfile() {
        //function call to get session id
        getUserIdAndSessionId();
        //To check internet connection
        if (SDUtility.isNetworkAvailable(this)) {
            try {
                if (SDUtility.isConnected()) {
                    progress = new ProgressDialog(this);
                    progress.setMessage(getString(R.string.pleasewait_text));
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    JSONObject profilejson = new JSONObject();
                    try {
                        profilejson.put("name", nameText.getText().toString());
                        profilejson.put("address", addressText.getText().toString());
                        profilejson.put("zipcode", zipcode.getText().toString());
                        if (uploadId != null) {
                            profilejson.put("imageId", uploadId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SDUtility.displayExceptionMessage(e.getMessage(), ProfileActivity.this);
                    }
                    //To get url for create event
                    String url = ApiConstant.api_updateuser_url + userId;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, profilejson,
                            response -> {
                                progress.dismiss();
                                try {
                                    JSONObject serverResp = new JSONObject(response.toString());
                                    System.out.println("success result: " + serverResp);
                                    String errorStatus = serverResp.getString("error");
                                    if (errorStatus.equals("true")) {
                                        String errorMessage = serverResp.getString("message");
                                        Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    } else {
                                        System.out.println("successfully>>>>>>>");
                                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("store", response.toString());
                                        editor.commit();
                                        //to get updated profile data
                                        getProfileData();
                                        btn_update.setVisibility(View.INVISIBLE);
                                        editimage.setVisibility(View.INVISIBLE);
                                        menu.getItem(0).setVisible(true);
                                        menu.getItem(1).setVisible(false);

                                    }
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }, error -> {
                        progress.dismiss();
                        System.out.println("Error getting response");
                        SDUtility.displayExceptionMessage(error.getMessage(), ProfileActivity.this);
                    }) {    //this is the part, that adds the header to the request
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put("content-type", "application/json");
                            return params;
                        }
                    };
                    jsonObjectRequest.setTag(REQ_TAG);
                    requestQueue.add(jsonObjectRequest);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                SDUtility.displayExceptionMessage(e.getMessage(), ProfileActivity.this);
            }
        }
    }

    private void getUserIdAndSessionId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        try {
            JSONObject serverResp = new JSONObject(pref.getString("store", ""));
            userId = serverResp.getJSONObject("result").getString("_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_popup, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            // do something
            /*btn_update.setVisibility(View.VISIBLE);
            editimage.setVisibility(View.VISIBLE);
            nameText.setEnabled(true);
            addressText.setEnabled(true);
            zipcode.setEnabled(true);*/
            showHideUiElements(View.VISIBLE, true);
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(true);
        } else if (id == android.R.id.home) {
            ProfileActivity.this.finish();
            return true;
        } else {
            showHideUiElements(View.GONE, false);
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(false);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHideUiElements(int visibility, boolean isVisible) {
        btn_update.setVisibility(visibility);
        editimage.setVisibility(visibility);
        nameText.setEnabled(isVisible);
        addressText.setEnabled(isVisible);
        zipcode.setEnabled(isVisible);
    }

    @Override
    public void onProgress(int progr) {
        Log.d("PROGRESS", "progress = " + progr);
        progress = new ProgressDialog(this);
        profile_imageview_progressbar.setVisibility(View.VISIBLE);
        progress.setProgress(progr);
    }

    @Override
    public void onProgress(long uploadedBytes, long totalBytes) {
        // your code here
        Log.d("On Progress", String.valueOf(uploadedBytes));
    }

    @Override
    public void onError(Exception exception) {
        System.out.println("");
        // your code here
        Log.d("On error", String.valueOf(exception));
        profile_imageview_progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onCompleted(int serverResponseCode, byte[] serverResponseBody) {
        progress.dismiss();
        String str = new String(serverResponseBody);
        System.out.println(str);
        try {
            JSONObject serverResp = new JSONObject(str);
            if (serverResp.getString("error").equals("true")) {
                String errorMessage = serverResp.getString("message");
                Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                personimage.setImageBitmap(selectedImage);
                profile_imageview_progressbar.setVisibility(View.GONE);
                uploadId = serverResp.getJSONObject("upload").getString("_id");

            }
        } catch (JSONException e) {
            e.printStackTrace();
            SDUtility.displayExceptionMessage(e.getMessage(), ProfileActivity.this);
        }
    }

    @Override
    public void onCancelled() {
        System.out.println(("On cancelled"));
        profile_imageview_progressbar.setVisibility(View.GONE);
    }
}
