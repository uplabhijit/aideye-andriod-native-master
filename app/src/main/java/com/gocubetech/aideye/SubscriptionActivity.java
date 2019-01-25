package com.gocubetech.aideye;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.gocubetech.aideye.Constant.ApiConstant;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class SubscriptionActivity extends AppCompatActivity {

    public TextView title;
    public TextView amount;
    public TextView planDescription;

    //Payment Amount
    private String paymentAmount;

    public static final int PAYPAL_REQUEST_CODE = 123;

    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID_PROD);
    private ProgressDialog progress;
    private RequestQueue requestQueue;
    static final String REQ_TAG = "SUBSCRIPTIONACTIVITY";
    private Plan plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange_400)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        amount = (TextView) findViewById(R.id.amount);
        planDescription= findViewById(R.id.planDescription);
        title = findViewById(R.id.title);
        plan = (Plan) getIntent().getSerializableExtra("plan_obj");
        title.setText(plan.getName());
        amount.setText("$" + plan.getAmount());
        planDescription.setText(plan.getDescription());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    //function call to start payment using paypal
    public void paymentNow(View view) {
        //Getting the amount from editText
        paymentAmount = plan.getAmount();
        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), "USD", "Plan Amount",
                PayPalPayment.PAYMENT_INTENT_SALE);
        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);
        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {
            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        final String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentExample", paymentDetails);
                        progress = new ProgressDialog(this);
                        progress.setMessage(getString(R.string.pleasewaitloadermsg));
                        progress.setCancelable(false); // disable       dismiss by tapping outside of the dialog
                        progress.show();
                        JSONObject json = new JSONObject();
                        try {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                            String response = pref.getString("store", "");
                            System.out.println("response>>>>>>>>>>>>>" + response);
                            JSONObject serverresponse = new JSONObject(response);
                            json.put("phoneNumber", serverresponse.getJSONObject("result").getString("phoneNumber"));
                            json.put("planId", plan.getPlanId());
                            json.put("planName", plan.getName());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //api to subscription update
                        String url = ApiConstant.api_updateSubscription_url;
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, json,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        progress.dismiss();
                                        System.out.println(response.toString());
                                        try {
                                            JSONObject serverResp = new JSONObject(response.toString());
                                            String errorStatus = serverResp.getString("error");
                                            System.out.println("success result: " + serverResp);
                                            if (errorStatus.equals("true")) {
                                                String errorMessage = serverResp.getString("message");
                                                Toast.makeText(SubscriptionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            } else {
                                                JSONObject activeSubscription = null;
                                                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                                SharedPreferences.Editor editor = pref.edit();
                                                JSONArray subscriptionarray = response.getJSONObject("result").getJSONArray("subscription");
                                                for (int i = 0; i < subscriptionarray.length(); i++) {
                                                    if ((subscriptionarray.getJSONObject(i).getString("Active")).equals("true")) {
                                                        activeSubscription = subscriptionarray.getJSONObject(i);
                                                    }
                                                }

                                                editor.putString("activeSubscription", activeSubscription.toString());
                                                editor.commit();

                                                SubscriptionActivity.this.finish();
                                                Toast.makeText(SubscriptionActivity.this, "Plan Subscribed Successfully!!!", Toast.LENGTH_SHORT).show();
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
                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                        System.out.println("an extremely unlikely failure occurred:");
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
                System.out.println("user canceled");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
                System.out.println("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}

    //Starting a new activity for the payment details and also putting the payment details with intent
  /*  startActivity(new Intent(SubscriptionActivity.this, ConfirmationActivity.class)
                                                    .putExtra("PaymentDetails", paymentDetails)
                                                            .putExtra("PaymentAmount", paymentAmount));
*/