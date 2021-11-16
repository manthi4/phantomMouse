package com.google.mediapipe.apps.basic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
// import com.google.mediapipe.apps.basic.R;
// import android.R;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Sc3class extends Fragment {

    private BleMouse mouse;
    final ArrayList<Bdevice> ListCardsArrayList = new ArrayList<>();

    public Sc3class(BleMouse mouse){
        super();
        this.mouse = mouse;

    }

    // public setMouse(BleMouse mouse){
    //     this.mouse = mouse;
    // }

    private void updateDeviceList() {
        ListCardsArrayList.clear();
        ListCardsArrayList.addAll(mouse.listDevices());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.screen3, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        RecyclerView recV = getView().findViewById(R.id.recview);
        BDeviceCardAdapter cardAdapter = new BDeviceCardAdapter(getActivity(), ListCardsArrayList, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bdevice element = ListCardsArrayList.get(i);
                mouse.btConnect(element.deviceName);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recV.setLayoutManager(linearLayoutManager);
        recV.setAdapter(cardAdapter);
        updateDeviceList();
        Button refresh = getView().findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDeviceList();
                cardAdapter.notifyDataSetChanged();
            }
        });
    }
}
