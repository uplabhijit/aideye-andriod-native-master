package com.memeinfotech.aideye.DataHandler;
import android.content.Context;
import android.content.SharedPreferences;

import com.memeinfotech.aideye.Constant.MeMeConstant;

/**
 * Created by root on 10/10/15.
 */
public class MeMePref
{
    //This method wilL save int value in preference
    public static void addIntPreference(Context context, int value, String key)
    {
        System.out.println("Value in addIntPreference:" + value);
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }


    //This method wilL save int value in preference
    public static void addStringPreference(Context context, String value, String key)
    {
        System.out.println("Value in addIntPreference:" + value);
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    //This method wilL save boolean value in preference
    public static void addBooleanPreference(Context context, boolean value, String key)
    {
        System.out.println("Finl verification code :" + value);
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static int getIntPreference(Context context, String key)
    {
        final SharedPreferences prefs = getGCMPreferences(context);
        return prefs.getInt(key, 0);
    }

    public static String getStringPreference(Context context, String key)
    {
        final SharedPreferences prefs = getGCMPreferences(context);
        return prefs.getString(key, "");
    }

    public static boolean getBooleanPreference(Context context, String key)
    {
        final SharedPreferences prefs = getGCMPreferences(context);
        return prefs.getBoolean(key, false);
    }

    private static SharedPreferences getGCMPreferences(Context context)
    {
        return context.getSharedPreferences(MeMeConstant.MEME_APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}