package com.gocubetech.aideye;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.gocubetech.aideye.Constant.MeMeConstant;
import com.gocubetech.aideye.DataHandler.MeMePref;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import Utility.SDUtility;
import co.ceryle.segmentedbutton.SegmentedButton;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private EditText dataEditText;
    public LinearLayout layout1;
    private TextToSpeech tts;
    private boolean isWrite = false;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Button saveButton;
    boolean micEnable = true;
    private ImageView menuImageView;
    private LinearLayout indicator;
    private String voiceSpeed;
    private String voicePitch;
    float ttsSpeed = 1.0f;
    float ttsPitch = 1.1f;
    private TextView poweredByText;
    private SegmentedButton auditoryBttn;
    private SegmentedButton interactivityBttn;
    private SegmentedButtonGroup segmentedButtonGroup;
    private LinearLayout intractiveButtonsContainer;
    public GestureDetector gestureDetector;
    GoogleTranslateMainActivity translator = new GoogleTranslateMainActivity("AIzaSyAWPLjcaA8ortKKHxQmNlhiy9U48vCVB3M");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Make screen full screen
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //detector to detect touch
        gestureDetector = new GestureDetector(this, this);
        dataEditText = findViewById(R.id.editText);
        dataEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        saveButton = findViewById(R.id.save_button);
        menuImageView = findViewById(R.id.menu_imageView);
        indicator = findViewById(R.id.indicator_wrapper);
        poweredByText = findViewById(R.id.powered_by_text);
        indicator.setVisibility(View.INVISIBLE);
        //editTextSizeChanged();
        tts = new TextToSpeech(this, this);
        //Init the tag with pending intent
        initTag();
        intractiveButtonsContainer = findViewById(R.id.audio_int);
        saveButton.setVisibility(View.INVISIBLE);
        dataEditText.setEnabled(false);
        dataEditText.setClickable(false);
        voiceSpeed = MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_SPEED);
        voicePitch = MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_PITCH);
        System.out.println("speed:- " + voicePitch);
        if (voicePitch.length() > 0 && voiceSpeed.length() > 0) {
            System.out.println("Setting configured");
            ttsPitch = Float.parseFloat(voicePitch);
            ttsSpeed = Float.parseFloat(voiceSpeed);
        } else {
            System.out.println("Setting not configured");
            MeMePref.addStringPreference(this, String.valueOf(ttsPitch), MeMeConstant.PREF_VOICE_PITCH);
            MeMePref.addStringPreference(this, String.valueOf(ttsSpeed), MeMeConstant.PREF_VOICE_SPEED);
            MeMePref.addStringPreference(this, "en", MeMeConstant.PREF_VOICE_LANG);
        }
    }

    //touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    //function call to add interactive button in dashboard
    public void addIntractiveButtonLayout() {
        intractiveButtonsContainer.removeAllViews();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View intractiveButtonLayout = mInflater.inflate(R.layout.audio_interactive_button_layout, (ViewGroup) intractiveButtonsContainer, false);
        intractiveButtonsContainer.addView(intractiveButtonLayout);
        segmentedButtonGroup = intractiveButtonLayout.findViewById(R.id.segmentedButtonGroup);
        auditoryBttn = intractiveButtonLayout.findViewById(R.id.auditory_button);
        interactivityBttn = intractiveButtonLayout.findViewById(R.id.interactive_button);
        //function call to segment button
        segmentButtonListenerInit();
        segmentedButtonGroup.setPosition(0, 0);
    }

    //function call to segment button listener
    public void segmentButtonListenerInit() {
        segmentedButtonGroup.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                System.out.println("button position: " + position);
                int buttonVisibility = 0;
                boolean auditoryEnable = false;
                if (position == 0) {
                    //auditory
                    buttonVisibility = View.INVISIBLE;
                    auditoryEnable = false;
                    micEnable = true;
                    dataEditText.setHint(getResources().getString(R.string.data_text_hint_auditory));
                    dataEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                } else {
                    //interactive
                    buttonVisibility = View.VISIBLE;
                    auditoryEnable = true;
                    micEnable = false;
                    dataEditText.setHint(getResources().getString(R.string.data_text_hint_interactive));
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(
                            dataEditText.getApplicationWindowToken(),
                            InputMethodManager.SHOW_FORCED, 0);
                    dataEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                }
                saveButton.setVisibility(buttonVisibility);
                dataEditText.setEnabled(auditoryEnable);
                dataEditText.setClickable(auditoryEnable);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null && mAdapter.isEnabled()) {
            this.mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
        changeLocal(MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
        Locale locale = new Locale(MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
        tts.setLanguage(locale);
        System.out.println("onResume:--------------- " + getIntent().getAction());
        //function call to update text
        updateTexts();
    }

    //function call to update text
    private void updateTexts() {
        addIntractiveButtonLayout();
        String dataTextHintMsg;
        //condition to check mis enable status
        if (micEnable)
            dataTextHintMsg = getResources().getString(R.string.data_text_hint_auditory);
        else
            dataTextHintMsg = getResources().getString(R.string.data_text_hint_interactive);
        ////TODO Create a extranal layout with segmentbutton and reinflate onResume
        String str = getResources().getString(R.string.auditory_button_text);
        dataEditText.setHint(dataTextHintMsg);
        saveButton.setText(getResources().getString(R.string.save_button));
        poweredByText.setText(getResources().getString(R.string.powered_by));
    }
    // To go to subscription activity

    public void subscribePlans(View view) {
        Intent fp;
        fp = new Intent(MainActivity.this, PlansActivity.class);
        startActivity(fp);
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    dataEditText.setText(result.get(0));
                    isWrite = true;
                    speakOut(getResources().getString(R.string.prefix_msg_of_confirm_dialog)
                            + ".  " + result.get(0) + getResources().getString(R.string.sufix_msg_of_confirm_dialog));
                }
                break;
            }
            case 8: {
                initTag();
                break;
            }
        }
    }

    //function call to get status of nfc
    @Override
    public void onInit(int status) {
        System.out.println("onInit tts:  " + status);
        if (status == TextToSpeech.SUCCESS) {
            System.out.println("onInit trace--------------" + MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
            Locale locale = new Locale(MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
            tts.setLanguage(locale);
            checkNfcEnableStatus();
            //speakOut("text to speak init");
            tts.setOnUtteranceCompletedListener(this);
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("OnNewintent action tarce-----------------------------------------: " + intent.getAction());
        System.out.println("OnNewintent: " + isWrite);
        if (isWrite) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //Function call to write tag
            writeTag(tag);
        } else {
            //function call to handle intent
            handleIntent(intent);
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    //function call to init nfc tag
    public void initTag() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        System.out.println("mAdapter: " + mAdapter);
        if (mAdapter != null) {
            System.out.println("mAdapter.isEnabled(): " + mAdapter.isEnabled());
            if (mAdapter.isEnabled()) {
                mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            } else {
                //speakOut("Your device nfc is disable, Please turn it on from nfc setting");
            }
        } else {
            //speakOut("Your device Is not Nfc Supported");
        }
    }

    //function call to save data in interactive mode
    public void saveData(View view) {
        isWrite = true;
        auditoryBttn.setText(R.string.auditorybtntext);
    }

    //function call to handle intent
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    //function call to show more menu option
    public void showMenu(View view) {
        if (tts.isSpeaking()) {
            tts.stop();
        }
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(MainActivity.this, menuImageView);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @SuppressLint("NewApi")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.setting: {
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    }
                    break;
                    case R.id.subscription: {
                        startActivity(new Intent(MainActivity.this, PlansActivity.class));
                    }
                    break;
                    case R.id.logout: {
                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.custom_dialog);
                        dialog.show();
                        Button acceptButton = (Button) dialog.findViewById(R.id.btn_yes);
                        // if yes  button is clicked, open the custom dialog
                        acceptButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putBoolean("loginStatus", false);
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                        Button rejectButton = (Button) dialog.findViewById(R.id.btn_no);
                        // if yes  button is clicked, close the custom dialog
                        rejectButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Close dialog
                                dialog.dismiss();
                            }
                        });
                    }
                    break;
                }
                return true;
            }
        });
        popup.show();
    }

    //function call to change local language
    public void changeLocal(String lang) {
        System.out.println("changeLocal language -----------" + lang);
        Locale locale = new Locale(lang);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onUtteranceCompleted(String s) {
        System.out.println("detect speech stop");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                indicator.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        System.out.println("double tap>>>>>>>>>>>");
        speakToText(null);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        System.out.println("double tap>>>>>>>>>>>");
        return true;
    }

    //To check nfc reader task in background
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            NdefRecord ndefRecord_0 = records[0];
            String inMsg = new String(ndefRecord_0.getPayload());
            System.out.println("Read data output:" + inMsg);
            return inMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (result.length() >= 3) {
                    //Get TagData With Language....
                    String tagdataLang = result.substring(1, 3);
                    String tagDataOnly = result.substring(3, result.length());
                    if (MeMePref.getStringPreference(MainActivity.this, MeMeConstant.PREF_VOICE_LANG).equals((tagdataLang))) {
                        speakOut(result.substring(3));
                        System.out.println("Entering in onpostExecute---------->>>>>>>>>>>>>>>>>>>>>>>>>>>>" + result);
                        dataEditText.setText(result.substring(3));
                        System.out.println("language match -----------------------");
                    } else {
                        System.out.println("language not match -----------------------");
                        translate(tagdataLang, MeMePref.getStringPreference(MainActivity.this, MeMeConstant.PREF_VOICE_LANG), tagDataOnly);
                    }
                } else
                    speakOut(result);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    //Method to Execute AsyncTask of google translation api......
    public void translate(String fromLang, String toLang, String tagData) {
        if (SDUtility.isNetworkAvailable(this)) {
            //if (SDUtility.isInternetAvailable()) {
            try {
                if (isConnected()) {
                    new LanguageTranslation(fromLang, toLang, tagData).execute();
                } else {
                    Toast.makeText(this, R.string.internetnotavamsg, Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.turnoninternetmsg, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec(command).waitFor() == 0);
    }

    //function call to create text message
    public NdefMessage createTextMessage() {
        String content = dataEditText.getText().toString();
        try {
            // Get UTF-8 byte
            byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            byte[] text = content.getBytes("UTF-8"); // Content in UTF-8
            int langSize = lang.length;
            int textLength = text.length;
            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1F));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, new byte[0],
                    payload.toByteArray());
            speakOut(getResources().getString(R.string.data_saved_successfully));
            dataEditText.setText("");
            NdefRecord[] ndefRecords = new NdefRecord[2];
            ndefRecords[0] = record;
            ndefRecords[1] = NdefRecord.createApplicationRecord(getPackageName());
            //NdefMessage ndefMessage = new NdefMessage(ndefRecords);
            return new NdefMessage(ndefRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //function call to write tag
    public void writeTag(Tag tag) {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);
                System.out.println("ndefTag: " + ndefTag);
                if (ndefTag == null) {
                    // Let's try to format the Tag in NDEF
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(createTextMessage());
                        nForm.close();
                        System.out.println("nForm not null: " + nForm);
                    } else {
                        System.out.println("nForm null: " + nForm);
                        speakOut(getResources().getString(R.string.write_error));
                    }
                } else {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(createTextMessage());
                    ndefTag.close();
                }
                isWrite = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.null_tag_msg), Toast.LENGTH_SHORT).show();
        }
    }

    //function call to speak out
    private void speakOut(String speakData) {
        indicator.setVisibility(View.VISIBLE);
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        tts.setPitch(ttsPitch);
        tts.setSpeechRate(ttsSpeed);
        tts.speak(speakData, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    //function call to speak to text
    public void speakToText(View view) {
        if (!micEnable) {
            return;
        } else if (tts.isSpeaking()) {
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.say_something_msg));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), a.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //function call to check nfc enable status
    public void checkNfcEnableStatus() {
        if (mAdapter != null) {
            if (mAdapter.isEnabled()) {
                String wellComeMsg = "";
                Calendar c = Calendar.getInstance();
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                if (timeOfDay >= 0 && timeOfDay < 12) {
                    wellComeMsg = getResources().getString(R.string.good_morning) + ", ";
                } else if (timeOfDay >= 12 && timeOfDay < 16) {
                    wellComeMsg = getResources().getString(R.string.good_afternoon) + ", ";
                } else if (timeOfDay >= 16 && timeOfDay < 21) {
                    wellComeMsg = getResources().getString(R.string.good_evening) + ", ";
                } else if (timeOfDay >= 21 && timeOfDay < 24) {
                    wellComeMsg = getResources().getString(R.string.good_evening) + ", ";
                }
                speakOut(wellComeMsg + getResources().getString(R.string.welcome_msg));
            } else {
                speakOut(getResources().getString(R.string.nfc_disable_msg));
                showNfcTurnOnAlert();
            }
        } else {
            speakOut(getResources().getString(R.string.nfc_not_supported_msg));
        }
    }

    public void showNfcTurnOnAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);
        //Setting message manually and performing action on button click
        builder.setMessage(getResources().getString(R.string.enable_nfc_description_text))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(android.provider.Settings.ACTION_NFC_SETTINGS);
                        startActivityForResult(i, 8);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle(getResources().getString(R.string.enable_nfc_title_text));
        alert.show();
    }

    //Integrating Asynctask to call Google Translate API ........Start..........
    private class LanguageTranslation extends AsyncTask<String, String, String> {
        private ProgressDialog progress = null;

        String fromLang;
        String toLang;
        String tagData;

        LanguageTranslation(String fromLang, String toLang, String tagData) {
            this.fromLang = fromLang;
            this.toLang = toLang;
            this.tagData = tagData;
        }

        protected void onError(Exception ex) {
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                translator = new GoogleTranslateMainActivity("AIzaSyAWPLjcaA8ortKKHxQmNlhiy9U48vCVB3M");
                Thread.sleep(1000);
                System.out.println("input language ------------->>>>>>>>>" + tagData);
                String text = translator.translte(tagData, fromLang, toLang);
                return text;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            //start the progress dialog
            progress = ProgressDialog.show(MainActivity.this, null, getResources().getString(R.string.translating_data));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progress.dismiss();
            System.out.println("translated data:--------------" + result);
            speakOut(result);
            System.out.println("Entering in onpostExecute---------->>>>>>>>>>>>>>>>>>>>>>>>>>>>" + result);
            dataEditText.setText(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
    //Integrating Asynctask to call Google Translate API ........End..........
}
