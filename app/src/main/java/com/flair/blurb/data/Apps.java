package com.flair.blurb.data;

import java.util.HashMap;

/**
 * Created by sivaram-3911 on 07/01/17.
 */

public class Apps {

    HashMap<String, String> apps;

    public Apps() {
        apps = new HashMap<>();
    }

    synchronized public void changeCateory(String pkgname, String category) {
        apps.put(pkgname, category);
    }

    synchronized public String getCategory(String pkgname) {
        return apps.get(pkgname);
    }
}
