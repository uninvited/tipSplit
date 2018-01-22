package com.miplot.tipsplit;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainApplication extends Application {
    private static final String TAG = "Application";
    private static final String CURRENT_USER_KEY = "current_user_key;";

    private FirebaseFirestore store;
    private SharedPreferences sharedPreferences;
    private User curUser;

    @Override
    public void onCreate() {
        super.onCreate();
        store = FirebaseFirestore.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        curUser = getCurUser();
        if (curUser != null) {
            Log.e(TAG, "Current user: " + curUser.getLogin() + ", " + curUser.getName());
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public FirebaseFirestore getStore() {
        return store;
    }

    public User getCurUser() {
        String userStr = sharedPreferences.getString(CURRENT_USER_KEY, null);
        if (userStr != null) {
            return deserializeUser(userStr);
        }
        return null;
    }

    public void saveCurUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_USER_KEY, serializeUser(user));
        editor.apply();
        editor.commit();
    }

    public void forgetCurUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(CURRENT_USER_KEY);
        editor.apply();
        editor.commit();
    }

    private String serializeUser(User user) {
        ObjectOutputStream objectOut = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            objectOut = new ObjectOutputStream(bos);
            objectOut.writeObject(user);
            return Base64.encodeToString(bos.toByteArray(),0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    // do nowt
                }
            }
        }
        return null;
    }

    private User deserializeUser(String s) {
        ObjectInputStream objectIn = null;
        try {
            byte[] bytes = Base64.decode(s,0);
            objectIn = new ObjectInputStream( new ByteArrayInputStream(bytes) );
            return (User)objectIn.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                    // do nowt
                }
            }
        }
        return null;
    }

}
