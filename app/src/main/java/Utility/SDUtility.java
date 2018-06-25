package Utility;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by sourav on 18/06/18.
 */

public class SDUtility
{
    //Check to make sure it is "connected" to a network:

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    //Check to make sure it is "connected" to a network:

    public static boolean isInternetAvailable()
    {
        try
        {
            final InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        }
        catch (UnknownHostException e)
        {
            // Log error
        }

        return false;
    }

}
