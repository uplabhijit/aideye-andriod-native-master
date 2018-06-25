package com.memeinfotech.aideye;

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
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.memeinfotech.aideye.Constant.MeMeConstant;
import com.memeinfotech.aideye.DataHandler.MeMePref;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import Utility.SDUtility;
import co.ceryle.segmentedbutton.SegmentedButton;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private EditText dataEditText;
    private TextToSpeech tts;
    private boolean isWrite = false;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Button saveButton;
    int dataTextHintSize = 40;
    int dataTextSize = 40;
    boolean micEnable = true;
    private ImageView menuImageView;
    private LinearLayout indicator;
    private String voiceSpeed;
    private String voicePitch;
    float ttsSpeed = 1.0f;
    float ttsPitch = 1.1f;
    private Spinner spinner1;
    TextView viewPitch;
    TextView viewSpeed;
    private TextView poweredByText;
    private EditText dialogTestDataEditText;
    private TextView viewPitchLabel;
    private TextView viewSpeedLabel;
    private TextView testVoiceDataLabel;
    private SegmentedButton auditoryBttn;
    private SegmentedButton interactivityBttn;
    private SegmentedButtonGroup segmentedButtonGroup;
    private LinearLayout intractiveButtonsContainer;
    GoogleTranslateMainActivity translator = new GoogleTranslateMainActivity("AIzaSyAWPLjcaA8ortKKHxQmNlhiy9U48vCVB3M");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Make screen full screen
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        dataEditText = findViewById(R.id.editText);
        dataEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        saveButton = findViewById(R.id.save_button);
        menuImageView = findViewById(R.id.menu_imageView);
        indicator = findViewById(R.id.indicator_wrapper);
        poweredByText = findViewById(R.id.powered_by_text);

        indicator.setVisibility(View.INVISIBLE);
        //editTextSizeChanged();
        tts = new TextToSpeech(this, this);
        //tts.setLanguage(Locale.ENGLISH);

        //Init the tag with pending intent
        initTag();

        intractiveButtonsContainer = findViewById(R.id.audio_int);
        saveButton.setVisibility(View.INVISIBLE);
        dataEditText.setEnabled(false);
        dataEditText.setClickable(false);

        voiceSpeed = MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_SPEED);
        voicePitch = MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_PITCH);
        System.out.println("speed:- " + voicePitch);

        if (voicePitch.length() > 0 && voiceSpeed.length() > 0)
        {
            System.out.println("Setting configured");
            ttsPitch = Float.parseFloat(voicePitch);
            ttsSpeed = Float.parseFloat(voiceSpeed);
        } else{
            System.out.println("Setting not configured");
            MeMePref.addStringPreference(this, String.valueOf(ttsPitch), MeMeConstant.PREF_VOICE_PITCH);
            MeMePref.addStringPreference(this, String.valueOf(ttsSpeed), MeMeConstant.PREF_VOICE_SPEED);
            MeMePref.addStringPreference(this, "en", MeMeConstant.PREF_VOICE_LANG);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mAdapter != null)
        {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    public void addIntractiveButtonLayout()
    {
        intractiveButtonsContainer.removeAllViews();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View intractiveButtonLayout = mInflater.inflate(R.layout.audio_interactive_button_layout, (ViewGroup) intractiveButtonsContainer, false);
        intractiveButtonsContainer.addView(intractiveButtonLayout);

        segmentedButtonGroup = intractiveButtonLayout.findViewById(R.id.segmentedButtonGroup);
        auditoryBttn = intractiveButtonLayout.findViewById(R.id.auditory_button);
        interactivityBttn = intractiveButtonLayout.findViewById(R.id.interactive_button);

        segmentButtonListenerInit();

        segmentedButtonGroup.setPosition(0,0);
    }

    public void segmentButtonListenerInit()
    {
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
        //startService();
        //initTag();
        //handleIntent(getIntent());

        System.out.println("onResume:--------------- " + getIntent().getAction());

        updateTexts();
    }

    private void updateTexts() {
        addIntractiveButtonLayout();
        String dataTextHintMsg;

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

                    speakOut(getResources().getString(R.string.prefix_msg_of_confirm_dialog) + result.get(0) + getResources().getString(R.string.sufix_msg_of_confirm_dialog));
                }

                break;
            }
            case 8: {
                initTag();
                break;
            }
        }
    }

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
            writeTag(tag);
        } else {
            handleIntent(intent);
        }

        //Toast.makeText(this, getResources().getString(R.string.tag_data_msg), Toast.LENGTH_SHORT).show();
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

    public void initTag() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        System.out.println("mAdapter: " + mAdapter);

        if (mAdapter != null) {
            System.out.println("mAdapter.isEnabled(): " + mAdapter.isEnabled());

            if (mAdapter.isEnabled()) {
                mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

                //this.mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);

                //speakOut("Your device nfc is enable");
            } else {
                //speakOut("Your device nfc is disable, Please turn it on from nfc setting");
            }
        } else {
            //speakOut("Your device Is not Nfc Supported");
        }
    }

    public void saveData(View view) {
        isWrite = true;
        auditoryBttn.setText("dscgjya");
    }

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

    public void showMenu() {
        startActivity(new Intent(this, SettingActivity.class));
    }

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
                    case R.id.setting:
                    {
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    }
                    break;
                    case R.id.subscription:
                    {
                        startActivity(new Intent(MainActivity.this, PlansActivity.class));
                    }
                    break;
                    case R.id.logout:
                    {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("loginStatus", false);
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    break;
                }
                return true;
            }
        });

        popup.show();
    }

    //Function for Spinner and language select....
    public void spinnerValue(final Dialog dialog) {
        spinner1 = dialog.findViewById(R.id.spinner1);
        // locale code, display name and country
        final ArrayList<String> resources = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.listValues));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        System.out.println("lllllllllll----------------------" + getResources().getStringArray(R.array.listValues).toString());

        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItem(position).equalsIgnoreCase("Select language"))
                    return;

                MeMePref.addIntPreference(MainActivity.this, position, MeMeConstant.PREF_VOICE_SPINNER_POSITION);

                System.out.println("selected language---------" + adapter.getItem(position));
                System.out.println("lang from local" + position);
                spinner1.setSelection(adapter.getPosition(MeMeConstant.PREF_VOICE_LANG));
                System.out.println("memememememeeeeeeee........." + adapter.getItem(position));
                setTtsLang(adapter.getItem(position));

                updateTexts();
                updateSettingTextValue();

            }

            private void updateSettingText() {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner1.setSelection(MeMePref.getIntPreference(this, MeMeConstant.PREF_VOICE_SPINNER_POSITION));
    }

    public void updateSettingTextValue()
    {
        viewSpeedLabel.setText(getResources().getString(R.string.speed_text));
        viewPitchLabel.setText(getResources().getString(R.string.pitc_text));
        testVoiceDataLabel.setText(getResources().getString(R.string.test_voice_data));
        dialogTestDataEditText.setText(getResources().getString(R.string.test_voice_data_content));

    }


    public void changeLocal(String lang) {
        System.out.println("changeLocal language -----------" + lang);
        Locale locale = new Locale(lang);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void setTtsLang(String lang)
    {
        //int  tts.setLanguage(Locale.ENGLISH);
        Locale loc = new Locale("en" );

        switch (lang) {
            case "English":
                loc = new Locale("en");
                break;
            case "Spanish":
                loc = new Locale("es");
                break;
            case "Dutch":
                loc = new Locale("nl");
                break;
            case "Swedish":
                loc = new Locale("sv");
                break;
            case "Hindi":
                loc = new Locale("hi");
                break;
            case "German":
                loc = new Locale("de");
                break;
            case "French":
                loc = new Locale("fr");
                break;
            case "Italian":
                loc = new Locale("it");
                break;
            case "Polish":
                loc = new Locale("pl");
                break;
            case "Greek":
                loc = new Locale("el");
                break;
        }

        System.out.println("language selected is -------" + loc.getLanguage());

        MeMePref.addStringPreference(this, loc.getLanguage(), MeMeConstant.PREF_VOICE_LANG);
        MeMePref.addStringPreference(this, loc.getLanguage(), MeMeConstant.PREF_VOICE_ACCENT_LANG);
        changeLocal(loc.getLanguage());
        tts.setLanguage(loc);
    }


    public int getProgressValue(float floatVal) {
        int settingValue = 0;

        if (floatVal == 0.0f) {
            settingValue = 0;
        } else if (floatVal == 0.1f) {
            settingValue = 1;
        } else if (floatVal == 0.2f) {
            settingValue = 2;
        } else if (floatVal == 0.3f) {
            settingValue = 3;
        } else if (floatVal == 0.4f) {
            settingValue = 4;
        } else if (floatVal == 0.5f) {
            settingValue = 5;
        } else if (floatVal == 0.6f) {
            settingValue = 6;
        } else if (floatVal == 0.7f) {
            settingValue = 7;
        } else if (floatVal == 0.8f) {
            settingValue = 8;
        } else if (floatVal == 0.9f) {
            settingValue = 9;
        } else if (floatVal == 1.0f) {
            settingValue = 10;
        } else if (floatVal == 1.1f) {
            settingValue = 11;
        } else if (floatVal == 1.2f) {
            settingValue = 12;
        } else if (floatVal == 1.3f) {
            settingValue = 13;
        } else if (floatVal == 1.4f) {
            settingValue = 14;
        } else if (floatVal == 1.5f) {
            settingValue = 15;
        } else if (floatVal == 1.6f) {
            settingValue = 16;
        } else if (floatVal == 1.7f) {
            settingValue = 17;
        } else if (floatVal == 1.8f) {
            settingValue = 18;
        } else if (floatVal == 1.9f) {
            settingValue = 19;
        } else {
            settingValue = 20;
        }
        return settingValue;
    }

    //TODO To optimise below switch case
    public float getConvertedValue(int intVal) {
        float settingValue = 0.0f;

        switch (intVal) {
            case 1:
                settingValue = 0.1f;
                break;
            case 2:
                settingValue = 0.2f;
                break;
            case 3:
                settingValue = 0.3f;
                break;
            case 4:
                settingValue = 0.4f;
                break;
            case 5:
                settingValue = 0.5f;
                break;
            case 6:
                settingValue = 0.6f;
                break;
            case 7:
                settingValue = 0.7f;
                break;
            case 8:
                settingValue = 0.8f;
                break;
            case 9:
                settingValue = 0.9f;
                break;
            case 10:
                settingValue = 1.0f;
                break;
            case 11:
                settingValue = 1.1f;
                break;
            case 12:
                settingValue = 1.2f;
                break;
            case 13:
                settingValue = 1.3f;
                break;
            case 14:
                settingValue = 1.4f;
                break;
            case 15:
                settingValue = 1.5f;
                break;
            case 16:
                settingValue = 1.6f;
                break;
            case 17:
                settingValue = 1.7f;
                break;
            case 18:
                settingValue = 1.8f;
                break;
            case 19:
                settingValue = 1.9f;
                break;
            case 20:
                settingValue = 2.0f;
                break;
        }

        return settingValue;
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
        protected void onPostExecute(String result)
        {
            if (result != null) {
                if (result.length() >= 3)
                {
                    //Get TagData With Language....
                    String tagdataLang = result.substring(1, 3);
                    String tagDataOnly = result.substring(3,result.length());
                    if (MeMePref.getStringPreference(MainActivity.this, MeMeConstant.PREF_VOICE_LANG).equals((tagdataLang)))
                    {
                        speakOut(result.substring(3));
                        System.out.println("Entering in onpostExecute---------->>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ result);
                        dataEditText.setText(result.substring(3));
                        System.out.println("language match -----------------------");
                    }
                    else
                    {
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
    public void translate(String fromLang, String toLang, String tagData)
    {
        if (SDUtility.isNetworkAvailable(this))
        {
            //if (SDUtility.isInternetAvailable()) {
            try {
                if (isConnected()) {

                    new LanguageTranslation(fromLang, toLang, tagData).execute();
                }
                else
                {
                    Toast.makeText(this, "Internet not available", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(this, "No internet connection. Trun on internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnected() throws InterruptedException, IOException
    {
        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec (command).waitFor() == 0);
    }

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


    public void editTextSizeChanged() {
        dataEditText.addTextChangedListener(new TextWatcher() {
            boolean hint;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    // no text, hint is visible
                    dataEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, dataTextHintSize);
                } else {
                    dataEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, dataTextSize);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void speakOut(String speakData) {
        indicator.setVisibility(View.VISIBLE);

        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");

        tts.setPitch(ttsPitch);
        tts.setSpeechRate(ttsSpeed);
        tts.speak(speakData, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    public void speakToText(View view) {
        if (!micEnable) {
            return;
        } else if (tts.isSpeaking()) {
            Toast.makeText(this, getResources().getString(R.string.speaking_after_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.say_something_msg));

        try
        {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException a)
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.speach_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    public void startService() {
        startService(new Intent(getBaseContext(), NfcReaderService.class));
    }

    public void checkNfcEnableStatus()
    {
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

    public void showNfcTurnOnAlert()
    {
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

    private void getTagInfo(Intent intent)
    {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        System.out.println("tag data : " + tag.toString());
        Ndef ndef = Ndef.get(tag);
        Toast.makeText(this, getResources().getString(R.string.tag_data_msg), Toast.LENGTH_SHORT).show();
    }

    //Integrating Asynctask to call Google Translate API ........Start..........
    private class LanguageTranslation extends AsyncTask<String, String, String>
    {
        private ProgressDialog progress = null;

        String fromLang;
        String toLang;
        String tagData;

        LanguageTranslation(String fromLang, String toLang, String tagData)
        {
            this.fromLang = fromLang;
            this.toLang =  toLang;
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

                System.out.println("input language ------------->>>>>>>>>"+tagData);
                String text = translator.translte(tagData, fromLang, toLang);
                return  text;
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
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            progress.dismiss();

            System.out.println("translated data:--------------" + result);
            speakOut(result);
            System.out.println("Entering in onpostExecute---------->>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ result);
            dataEditText.setText(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    //Integrating Asynctask to call Google Translate API ........End..........
}
