package com.gocubetech.aideye.Constant;

public class ApiConstant {

    //Base url for aideye
    private final static String aideye_api_prefix = " http://ec2-18-216-89-205.us-east-2.compute.amazonaws.com:32951/";

    //Url used in aideye
    public final static String api_registration_url = aideye_api_prefix + "user";
    public final static String api_login_url = aideye_api_prefix + "user/login";
    public final static String api_verifyotp_url = aideye_api_prefix + "user/verifyotp";
    public final static String api_getplan_url = aideye_api_prefix + "user/plans";

    //url using to translate different language
    public final static String google_transalate_url = "https://translation.googleapis.com/language/translate/v2?key=";
}
