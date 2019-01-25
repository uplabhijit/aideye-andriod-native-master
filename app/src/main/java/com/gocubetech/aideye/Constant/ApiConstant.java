package com.gocubetech.aideye.Constant;

public class ApiConstant {
    //Base url for aideye
    //private final static String aideye_api_prefix = " http://ec2-18-216-89-205.us-east-2.compute.amazonaws.com:32951/";
    private final static String aideye_api_prefix = "https://api.aideye.org/";

    //Url used in aideye
    public final static String api_registration_url = aideye_api_prefix + "user";
    public final static String api_login_url = aideye_api_prefix + "user/login";
    public final static String api_verifyotp_url = aideye_api_prefix + "user/verifyotp";
    public final static String api_getplan_url = aideye_api_prefix + "user/plans";
    public final static String api_logout_url=aideye_api_prefix+"user/logout";
    public final static String api_updateSubscription_url=aideye_api_prefix+"user/changeSubscription";
    public final static String api_verifyPhoneNumbr_url = aideye_api_prefix+"user/forgotPassword";
    public final static String api_uploadimage_url = aideye_api_prefix+"file";
    public final static String api_downloadimage_url = aideye_api_prefix+"file?file=";
    //url using to translate different language
    public final static String google_transalate_url = "https://translation.googleapis.com/language/translate/v2?key=";

    public final static String api_resetpassword_url= aideye_api_prefix+"user/resetpassword";
    public final static String api_updateuser_url=aideye_api_prefix+"user/updateUser?id=";
}
