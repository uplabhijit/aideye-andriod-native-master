package com.gocubetech.aideye;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gocubetech.aideye.Constant.ApiConstant;
import com.gocubetech.aideye.Constant.MeMeConstant;
import com.gocubetech.aideye.DataHandler.MeMePref;
import com.rilixtech.Country;
import com.rilixtech.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Utility.SDUtility;

public class ForgotPasswordActivity extends AppCompatActivity {
    public CustomViewPager viewPager;
    private CountryCodePicker ccp;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    public View view;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    public EditText emailText, otpText, newpassword, confirm_password;
    private ProgressDialog progress;
    RequestQueue requestQueue;
    public Button btn_verifyEmail, btn_verifyOtp, btn_resetPassword;
    static final String REQ_TAG = "VERIFYEMAILACTIVITY";
    public int mCurrentFragmentPosition;
    private String countryCodeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_welcome);
        //To init UI elements
        initUIElements();
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.email_for_forgotpasss,
                R.layout.otp_for_forgotpass_view_pagger,
                R.layout.confirm_pass_layout_for_view_pagger
        };
        // adding bottom dots
        addBottomDots(0);
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        //To set paging enable or not
        viewPager.setPagingEnabled(false);
//
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(-1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    System.out.println("Not set");
                }
            }
        });
        ;
    }

   /* private void initcountrycodespinner() {
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.textview_spinner_ke_liye, getResources().getStringArray(R.array.listValues));
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.textview_spinner_ke_liye, getResources().getStringArray(R.array.country_code_array));
        adapter.setDropDownViewResource(R.layout.custo_spinner_dropdwon_item);
        if (adapter != null) {
            spinner1.setAdapter(adapter);
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter.getItem(position).equalsIgnoreCase("Select Country Code"))
                        return;
                    System.out.println("selected language---------" + adapter.getItem(position));
                    spinner1.setSelection(adapter.getPosition(MeMeConstant.PREF_VOICE_LANG));
                    System.out.println("memememememeeeeeeee........." + adapter.getItem(position));
                    spinnerValue = adapter.getItem(position);
                    //To set language in adapter
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        spinner1.setSelection(MeMePref.getIntPreference(this, MeMeConstant.PREF_VOICE_SPINNER_POSITION));
    }*/

    //To init UI elements
    private void initUIElements() {
        viewPager = (CustomViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);
    }

    //To add bottom dots
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    //	viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            mCurrentFragmentPosition = position;
            //To add bottom dots
            addBottomDots(position);
            view.setTag("myview" + position);
            View views = (View) viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                /* btnNext.setText(getString(R.string.start));*/
                btnSkip.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
                newpassword = views.findViewById(R.id.newpassword);
                confirm_password = views.findViewById(R.id.confirm_password);
                btn_resetPassword = views.findViewById(R.id.btn_resetPassword);
                btn_resetPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //function call to reset password
                        resetPassword(null);
                    }
                });
            } else if (position == 0) {
                btnSkip.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
            } else {
                // still pages are left
                /*btnNext.setText(getString(R.string.next));*/
                btnNext.setVisibility(View.GONE);
                btnSkip.setVisibility(View.VISIBLE);
                otpText = views.findViewById(R.id.otpText);
                btn_verifyOtp = views.findViewById(R.id.btn_verifyOtp);
                btn_verifyOtp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //function call to verify otp
                        verifyOtp(null);
                    }
                });
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            System.out.println("position" + position);
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (position == 0) {
                btnSkip.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
            }
            view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            view.setTag("myview" + position);
            View views = (View) viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());
            System.out.println("asdfghjkloijhgf" + view);
            if (position == 0) {
                ccp = views.findViewById(R.id.ccp);
                btnSkip.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
                emailText = views.findViewById(R.id.emailText);
                btn_verifyEmail = views.findViewById(R.id.btn_verifyEmail);
                btn_verifyEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Function call to verify email
                        verifyEmail(null);
                    }
                });
                ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
                    @Override
                    public void onCountrySelected(Country country) {
                       /* Toast.makeText(ForgotPasswordActivity.this, "Updated " + country.getPhoneCode(), Toast.LENGTH_SHORT).show();*/
                        countryCodeValue = "+" + country.getPhoneCode();
                    }
                });
            }
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    //To verify email
    public void verifyEmail(View view) {
        //To check internet connection
        if (SDUtility.isNetworkAvailable(ForgotPasswordActivity.this)) {
            try {
                if (SDUtility.isConnected()) {
                    if (SDUtility.isValidphoneNumber(emailText.getText().toString())) {
                        progress = new ProgressDialog(this);
                        //TODO: replace with string
                        progress.setMessage(getResources().getString(R.string.verify_email_loader));
                        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                        progress.show();
                        JSONObject json = new JSONObject();
                        try {
                            json.put("phoneNumber", emailText.getText().toString());
                            json.put("code", countryCodeValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("json" + json);
                        //To get url to verify email
                        String url = ApiConstant.api_verifyPhoneNumbr_url;
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
                                                Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            } else {
                                                //To set view pager to item
                                                viewPager.setCurrentItem(1);
                                            }
                                        } catch (JSONException e) {
                                            // TODO Auto-generated catch block
                                            progress.dismiss();
                                            e.printStackTrace();
                                            System.out.println("Error getting response || Error msg:-" + e.getMessage());
                                            displayExceptionMessage(e.getMessage());
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progress.dismiss();
                                System.out.println("Error getting response");
                                System.out.println("Error getting response || Error msg:-" + error.getMessage());
                                displayExceptionMessage(error.getMessage());
                            }
                        });
                        jsonObjectRequest.setTag(REQ_TAG);
                        requestQueue.add(jsonObjectRequest);
                    } else {
                        emailText.setError("Invalid PhoneNumber");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                displayExceptionMessage(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                displayExceptionMessage(e.getMessage());
            }
        }
    }

    //To display error message
    public void displayExceptionMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //Function call to verify otp
    public void verifyOtp(View view) {
        //To check internet connection
        if (SDUtility.isNetworkAvailable(ForgotPasswordActivity.this)) {
            try {
                if (SDUtility.isConnected()) {
                    if (otpText.getText().toString().length() > 0) {
                        progress = new ProgressDialog(this);
                        //TODO: replace with string
                        progress.setMessage(getResources().getString(R.string.verify_email_loader));
                        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                        progress.show();
                        JSONObject json = new JSONObject();
                        try {
                            json.put("phoneNumber", emailText.getText().toString());
                            json.put("otp", otpText.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //To get verify otp url
                        String url = ApiConstant.api_verifyotp_url;
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
                                                Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            } else {
                                                viewPager.setCurrentItem(2);
                                            }
                                        } catch (JSONException e) {
                                            // TODO Auto-generated catch block
                                            progress.dismiss();
                                            e.printStackTrace();
                                            System.out.println("Error getting response || Error msg:-" + e.getMessage());
                                            displayExceptionMessage(e.getMessage());
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progress.dismiss();
                                System.out.println("Error getting response");
                                System.out.println("Error getting response || Error msg:-" + error.getMessage());
                                displayExceptionMessage(error.getMessage());
                            }
                        });
                        jsonObjectRequest.setTag(REQ_TAG);
                        requestQueue.add(jsonObjectRequest);
                    } else {
                        Toast.makeText(this, "Please enter otp", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                displayExceptionMessage(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                displayExceptionMessage(e.getMessage());
            }
        }
    }

    //function call to reset password
    public void resetPassword(View view) {
        //To check inyternet connection
        if (SDUtility.isNetworkAvailable(ForgotPasswordActivity.this)) {
            try {
                if (SDUtility.isConnected()) {
                    if (isValidPassword(newpassword.getText().toString()) && isValidPassword(confirm_password.getText().toString())) {
                        if (newpassword.getText().toString().equals(confirm_password.getText().toString())) {
                            progress = new ProgressDialog(this);
                            //TODO: replace with string
                            progress.setMessage(getResources().getString(R.string.reset_password_loader));
                            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                            progress.show();
                            JSONObject json = new JSONObject();
                            try {
                                json.put("phoneNumber", emailText.getText().toString());
                                json.put("newPassword", newpassword.getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //To get password reset url
                            String url = ApiConstant.api_resetpassword_url;
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
                                                    Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    finish();
                                                }
                                            } catch (JSONException e) {
                                                // TODO Auto-generated catch block
                                                progress.dismiss();
                                                e.printStackTrace();
                                                System.out.println("Error getting response || Error msg:-" + e.getMessage());
                                                displayExceptionMessage(e.getMessage());
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progress.dismiss();
                                    System.out.println("Error getting response");
                                    System.out.println("Error getting response || Error msg:-" + error.getMessage());
                                    displayExceptionMessage(error.getMessage());
                                }
                            });
                            jsonObjectRequest.setTag(REQ_TAG);
                            requestQueue.add(jsonObjectRequest);
                        } else {
                            Toast.makeText(this, "Both the passsword Should be same", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        newpassword.setError("Invalid Password");
                        confirm_password.setError("Invalid Password");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                displayExceptionMessage(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                displayExceptionMessage(e.getMessage());
            }
        }
    }

    // validating password with retype password
    public boolean isValidPassword(String pass) {
        if (pass != null && pass.length() > 4) {
            return true;
        }
        return false;
    }
}

