package com.gocubetech.aideye;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class PlansListAdapter extends RecyclerView.Adapter<PlansListAdapter.MyViewHolder> {

    private final Context context;
    private List<Plan> planList;
    private Bundle planData;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final Button viewButton;
        public TextView title, amount, description;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            amount = (TextView) view.findViewById(R.id.amount);
            description = (TextView) view.findViewById(R.id.description);
            viewButton = (Button) view.findViewById(R.id.button_ViewDetails);
        }
    }

    public PlansListAdapter(List<Plan> planList, Bundle planData, Context context) {
        this.planList = planList;
        this.planData = planData;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_plans_list_adapter, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        System.out.println("adaptervcvdgdhdpassengerData>>>>>>>>>>>>........." + planData);
        final Plan plan = planList.get(position);
        holder.title.setText(plan.getName());
        holder.amount.setText(plan.getAmount() + "$");
        holder.description.setText(plan.getDescription());
        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SubscriptionActivity.class);
                Bundle planData = new Bundle();
                planData.putString("amount", plan.getAmount());
                intent.putExtras(planData);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }
}