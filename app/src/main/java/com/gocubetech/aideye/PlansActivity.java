package com.gocubetech.aideye;

import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gocubetech.aideye.Constant.ApiConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlansActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    RequestQueue requestQueue;
    private PlansListAdapter pAdapter;
    private ProgressDialog progress;
    private List<Plan> planList = new ArrayList<>();
    static final String REQ_TAG = "PLANACTIVITY";
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange_400)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext())
                .getRequestQueue();
        bundle = getIntent().getExtras();
        pAdapter = new PlansListAdapter(planList, bundle, PlansActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(pAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*api call to get plan list*/
        getPlanList();
    }

    /*function call to get plan list*/
    private void getPlanList() {
        planList = new ArrayList<>();
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.pleasewaitloadermsg));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        String url = ApiConstant.api_getplan_url;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progress.dismiss();
                        System.out.println(response.toString());
                        try {
                            JSONObject serverResp = new JSONObject(response.toString());
                            System.out.println("success result: " + serverResp);
                            JSONArray jsonArray = serverResp.getJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject planJsonObject = jsonArray.getJSONObject(i);
                                System.out.println("plan details:>>_>>>>>>-----------------------------" + planJsonObject.toString());
                                Plan plan = new Plan(
                                        planJsonObject.getString("name"),
                                        planJsonObject.getString("amount"),
                                        planJsonObject.getString("currency"),
                                        planJsonObject.getString("description"),
                                        planJsonObject.getString("_id"));
                                System.out.println("plan name:>>_>>>>>>-----------------------------" + plan.getName());
                                planList.add(plan);
                            }
                            pAdapter = new PlansListAdapter(planList, bundle, PlansActivity.this);
                            recyclerView.setAdapter(pAdapter);
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
    }

    /*private void getUserById() {
        //To check internet connection
        if (SDUtility.isNetworkAvailable(PlansActivity.this)) {
            try {
                if (SDUtility.isConnected()) {
                    progress = new ProgressDialog(PlansActivity.this);
                    progress.setMessage(getString(R.string.pleasewaitloadermsg));
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                    String userId = pref.getString("userId", "");
                    System.out.println("response>>>>>>>>>>>>>" + userId);
                    //api to login
                    //String url = ApiConstant.api_getUserById_url + userId;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
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
                                            Toast.makeText(PlansActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        } else {
                                            JSONObject activeSubscription = null;
                                            editor = pref.edit();
                                            JSONArray subscriptionarray = response.getJSONObject("result").getJSONArray("subscription");
                                            for (int i = 0; i < subscriptionarray.length(); i++) {
                                                if ((subscriptionarray.getJSONObject(i).getString("Active")).equals("true")) {
                                                    activeSubscription = subscriptionarray.getJSONObject(i);
                                                }
                                            }
                                            editor.putString("activeSubscription", activeSubscription.toString());
                                            editor.commit();
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
                    Toast.makeText(this, R.string.error_net_connection, Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.error_net_connection, Toast.LENGTH_SHORT).show();
        }
    }*/

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public void planDetails(View view) {
        System.out.println("plan details>>>>>>>>>>>>>>>>");
    }
}