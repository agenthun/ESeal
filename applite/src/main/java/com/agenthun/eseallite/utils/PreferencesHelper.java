package com.agenthun.eseallite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @project SmartSwitch
 * @authors agenthun
 * @date 2016/12/14 04:58.
 */

public class PreferencesHelper {

    private static final String USER_PREFERENCES = "userPreferences";
    private static final String PREFERENCE_USERNAME = USER_PREFERENCES + ".username";
    private static final String PREFERENCE_PASSWORD = USER_PREFERENCES + ".password";

    private static final String PREFERENCE_TOKEN = USER_PREFERENCES + ".token";

    public PreferencesHelper() {
    }

    public static void writeUserInfoToPreferences(Context context, User user) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(PREFERENCE_USERNAME, user.getUsername());
        editor.putString(PREFERENCE_PASSWORD, user.getPassword());
        editor.apply();
    }

    public static void writeTokenToPreferences(Context context, String token) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(PREFERENCE_TOKEN, token);
        editor.apply();
    }

    public static User getUser(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final String username = preferences.getString(PREFERENCE_USERNAME, null);
        final String password = preferences.getString(PREFERENCE_PASSWORD, null);
        if (username == null || password == null) {
            return null;
        }
        return new User(username, password);
    }

    public static String getTOKEN(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final String token = preferences.getString(PREFERENCE_TOKEN, "null");
        return token;
    }

    public static void signOut(Context context, boolean isSavePreferences) {
        if (!isSavePreferences) {
            SharedPreferences.Editor editor = getEditor(context);
            editor.remove(PREFERENCE_USERNAME);
            editor.remove(PREFERENCE_PASSWORD);
            editor.apply();
        }
    }

    public static void clearUser(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(PREFERENCE_USERNAME);
        editor.remove(PREFERENCE_PASSWORD);
        editor.apply();
    }

    public static void clearTOKEN(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(PREFERENCE_TOKEN);
        editor.apply();
    }

    public static boolean isSignedIn(Context context) {
        final SharedPreferences preferences = getSharedPreferences(context);
        return preferences.contains(PREFERENCE_USERNAME) && preferences.contains(PREFERENCE_PASSWORD);
    }

    public static boolean isInputDataValid(CharSequence username, CharSequence password) {
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static class User implements Parcelable {
        private final String mUsername;
        private final String mPassword;

        public User(String mUsername, String mPassword) {
            this.mUsername = mUsername;
            this.mPassword = mPassword;
        }

        protected User(Parcel in) {
            mUsername = in.readString();
            mPassword = in.readString();
        }

        public String getUsername() {
            return mUsername;
        }

        public String getPassword() {
            return mPassword;
        }

        public static final Creator<User> CREATOR = new Creator<User>() {
            @Override
            public User createFromParcel(Parcel in) {
                return new User(in);
            }

            @Override
            public User[] newArray(int size) {
                return new User[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mUsername);
            parcel.writeString(mPassword);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            if (mUsername != user.mUsername) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return mUsername.hashCode();
        }
    }
}
