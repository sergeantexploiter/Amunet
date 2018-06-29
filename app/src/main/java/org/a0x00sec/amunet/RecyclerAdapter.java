package org.a0x00sec.amunet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private List<RecyclerJava> recyclerJava;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        Switch aSwitch;
        Context context;

        private String[] permission_identifier;
        private int permission_request_code;

        public MyViewHolder(View view) {
            super(view);
            context = view.getContext();

            aSwitch = view.findViewById(R.id.permission_switch);
            aSwitch.setChecked(false);
        }
    }

    public RecyclerAdapter(List<RecyclerJava> recyclerList) {
        this.recyclerJava = recyclerList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        RecyclerJava data = recyclerJava.get(position);

        String permission_name = data.getPermission_name();
        holder.permission_identifier = data.getPermission_identifier();
        holder.permission_request_code = data.getPermission_request_code();

        holder.aSwitch.setText(permission_name.toUpperCase());

        if(ActivityCompat.checkSelfPermission(holder.context, holder.permission_identifier[0]) != PackageManager.PERMISSION_GRANTED) {
            holder.aSwitch.setChecked(false);
        } else {
            holder.aSwitch.setChecked(true);
        }

        holder.aSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b) {
                    ((Dashboard)holder.context).PermissionRequestHandler(holder.permission_identifier, holder.permission_request_code);
                } else {
                    holder.aSwitch.setChecked(true);
                    Toast.makeText(holder.context, "Action Not Permitted", Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return recyclerJava.size();
    }

}
