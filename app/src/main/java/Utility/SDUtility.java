package Utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec(command).waitFor() == 0);
    }

    // validating email id
    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //TO va;idate event name
    public static boolean isValidStringField(String name, int length) {
        if (name != null && name.length() > length) {
            return true;
        }
        return false;
    }


    //To display error message
    public static void displayExceptionMessage(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // validating password with retype password
    public static boolean isValidPassword(String pass) {
        return pass != null && pass.length() > 4;
    }

    //validating phoneNumber
    public static boolean isValidphoneNumber(String phone) {
        return phone != null && phone.length() > 5;
    }


}
