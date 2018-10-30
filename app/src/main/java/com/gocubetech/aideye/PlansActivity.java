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
    static final String REQ_TAG = " PLANACTIVITY";
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
        pAdapter = new PlansListAdapter(planList, bundle, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(pAdapter);
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
                                        planJsonObject.getString("description"));
                                System.out.println("plan name:>>_>>>>>>-----------------------------" + plan.getName());
                                planList.add(plan);
                            }
                            pAdapter.notifyDataSetChanged();
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

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public void planDetails(View view) {
        System.out.println("plan details>>>>>>>>>>>>>>>>");
    }
}

