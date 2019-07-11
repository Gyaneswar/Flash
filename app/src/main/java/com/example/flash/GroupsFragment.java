package com.example.flash;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> groupNames=new ArrayList<>();

    private DatabaseReference rootRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        groupFragmentView=inflater.inflate(R.layout.fragment_groups, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        initializeFields();
        retrieveGroups();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName=parent.getItemAtPosition(position).toString();
                Intent intent=new Intent(getContext(),GroupChatActivity.class);
                intent.putExtra("groupName",currentGroupName);
                startActivity(intent);


            }
        });

        return groupFragmentView;
    }

    private void retrieveGroups() {
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();

                while(iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                groupNames.clear();
                groupNames.addAll(set);
                mArrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        mListView=groupFragmentView.findViewById(R.id.listView);
        mArrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,groupNames);
        mListView.setAdapter(mArrayAdapter);
    }


}
