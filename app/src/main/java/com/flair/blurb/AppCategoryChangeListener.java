package com.flair.blurb;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by sivaram-3911 on 07/01/17.
 */

public class AppCategoryChangeListener implements ValueEventListener {

    DatabaseReference writeNode;
    String pkgname, category;
    DataChangeNotfier notifier;

    private AppCategoryChangeListener() {}

    public AppCategoryChangeListener(DatabaseReference nodeToWriteOn, String pkgname, String category, DataChangeNotfier notifier) {
        this.writeNode = nodeToWriteOn;
        this.pkgname = pkgname;
        this.category = category;
        this.notifier = notifier;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot == null) {
            writeNode.child(pkgname).setValue(category);
        } else {
            notifier.notifyCategoryChanged(dataSnapshot.getKey(), ((String) dataSnapshot.getValue()));
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

}
