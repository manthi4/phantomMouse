package com.example.phantomouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BDeviceCardAdapter extends RecyclerView.Adapter<BDeviceCardAdapter.Viewholder> {

    private Context context;
    private ArrayList<Bdevice> bdeviceArrayList;
    private AdapterView.OnItemClickListener listener;

    public BDeviceCardAdapter(Context context, ArrayList<Bdevice> bdeviceArrayList, AdapterView.OnItemClickListener listener) {
        this.context = context;
        this.bdeviceArrayList = bdeviceArrayList;
        this.listener = listener;
    }



    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bdevicecard, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, @SuppressLint("RecyclerView") int position) {
        // to set data to textview and imageview of each card layout
        Bdevice model = bdeviceArrayList.get(position);
        holder.deviceName.setText(model.getDeviceName());
        holder.deviceStatus.setText(model.getConnectionStatus());
        switch (model.connectionState){
            case 0:
                holder.itemView.setBackgroundColor(Color.LTGRAY);
                break;
            case 1:
                holder.itemView.setBackgroundColor(Color.YELLOW);
                break;
            case 2:
                holder.itemView.setBackgroundColor(Color.GREEN);
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AdapterView<?> adapterView, View view, int i, long l
//                view == holder.itemView (they the same)
                listener.onItemClick(null,  view, position, 0 );
            }
        });
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return bdeviceArrayList.size();
    }



    public class Viewholder extends RecyclerView.ViewHolder{
        private TextView deviceName, deviceStatus;
        public Viewholder(View itemView){
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceStatus = itemView.findViewById(R.id.deviceStatus);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    btConnect
//                }
//            });
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String element = ListElementsArrayList.get(i);
//                String deviceName = element.substring(element.indexOf("'") + 1, element.lastIndexOf("'"));
//                btConnect(deviceName);
//            }

        }
    }
}
