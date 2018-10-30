package com.gocubetech.aideye;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.MenuItem;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.gocubetech.aideye.Constant.MeMeConstant;
import com.gocubetech.aideye.DataHandler.MeMePref;

import java.util.HashMap;
import java.util.Locale;

public class SettingActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private Spinner spinner1;
    private EditText testDataEditText;
    private TextView viewPitchLabel;
    private TextView viewPitch;
    private TextView viewSpeedLabel;
    private TextView testVoiceDataLabel;
    private TextView viewSpeed;
    private String voiceSpeed;
    private String voicePitch;
    float ttsSpeed = 1.0f;
    float ttsPitch = 1.1f;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
//        getSupportActionBar().hide();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.Orange)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tts = new TextToSpeech(this, this);
        //function call to select language from spinner
        initLanguageSpinner();
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
        SeekBar speedSeekBar = findViewById(R.id.speed_seekbar);
        SeekBar pitchSeekBar = findViewById(R.id.pitch_seekbar);
        testDataEditText = findViewById(R.id.test_voice_editext);
        viewPitchLabel = findViewById(R.id.pitch_text_label);
        viewPitch = findViewById(R.id.view_pitch_min);
        viewSpeedLabel = findViewById(R.id.speed_text_label);
        testVoiceDataLabel = findViewById(R.id.test_voice_data_label);
        viewSpeed = findViewById(R.id.view_speed_min);
        speedSeekBar.setProgress(getProgressValue(ttsSpeed));
        pitchSeekBar.setProgress(getProgressValue(ttsPitch));
        viewSpeed.setText(String.valueOf(getProgressValue(ttsSpeed) * 5));
        viewPitch.setText(String.valueOf(getProgressValue(ttsPitch) * 5));
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int configShowValue = i * 5;
                viewSpeed.setText(String.valueOf(configShowValue));
                float settingValue = getConvertedValue(i);
                MeMePref.addStringPreference(SettingActivity.this, String.valueOf(settingValue), MeMeConstant.PREF_VOICE_SPEED);
                ttsSpeed = settingValue;
                //tts.setSpeechRate(ttsSpeed);
                if (tts.isSpeaking()) {
                    tts.stop();
                }
                speakOut(testDataEditText.getText().toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int configShowValue = i * 5;
                viewPitch.setText(String.valueOf(configShowValue));
                float settingValue = getConvertedValue(i);
                MeMePref.addStringPreference(SettingActivity.this, String.valueOf(settingValue), MeMeConstant.PREF_VOICE_PITCH);
                ttsPitch = settingValue;
                //tts.setPitch(ttsPitch);
                if (tts.isSpeaking()) {
                    tts.stop();
                }
                //function call to speak out test data text
                speakOut(testDataEditText.getText().toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    //function call to select language from spinner
    public void initLanguageSpinner() {
        spinner1 = findViewById(R.id.spinner1);
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.textview_spinner_ke_liye, getResources().getStringArray(R.array.listValues));
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.textview_spinner_ke_liye, getResources().getStringArray(R.array.listValues));
        adapter.setDropDownViewResource(R.layout.custo_spinner_dropdwon_item);
        if (adapter != null) {
            spinner1.setAdapter(adapter);
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter.getItem(position).equalsIgnoreCase("Select language"))
                        return;
                    MeMePref.addIntPreference(SettingActivity.this, position, MeMeConstant.PREF_VOICE_SPINNER_POSITION);
                    System.out.println("selected language---------" + adapter.getItem(position));
                    System.out.println("lang from local" + position);
                    spinner1.setSelection(adapter.getPosition(MeMeConstant.PREF_VOICE_LANG));
                    System.out.println("memememememeeeeeeee........." + adapter.getItem(position));
                    //To set language in adapter
                    setTtsLang(adapter.getItem(position));
                    updateSettingTextValue();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        spinner1.setSelection(MeMePref.getIntPreference(this, MeMeConstant.PREF_VOICE_SPINNER_POSITION));
    }

    //function call to update setting text value
    public void updateSettingTextValue() {
        viewSpeedLabel.setText(getResources().getString(R.string.speed_text));
        viewPitchLabel.setText(getResources().getString(R.string.pitc_text));
        testVoiceDataLabel.setText(getResources().getString(R.string.test_voice_data));
        testDataEditText.setText(getResources().getString(R.string.test_voice_data_content));
    }

    //function call to set language
    public void setTtsLang(String lang) {
        //int  tts.setLanguage(Locale.ENGLISH);
        Locale loc = new Locale("en");
        ///TODO place in utility class
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
        //function call to change language
        changeLocal(loc.getLanguage());
        tts.setLanguage(loc);
    }

    //function call to change language
    public void changeLocal(String lang) {
        System.out.println("changeLocal language -----------" + lang);
        Locale locale = new Locale(lang);
        Resources res = getBaseContext().getResources();
        Configuration config = res.getConfiguration();
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    public int getProgressValue(float floatVal) {
        int settingValue;
        settingValue = (int) (floatVal * 10);
        System.out.println(settingValue);

        /*if (floatVal == 0.0f) {
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
        }*/
        return settingValue;
    }

    public float getConvertedValue(int intVal) {
        float settingValue;
        if (intVal != 0) {
            Float fX = new Float(intVal);
            settingValue = fX.floatValue() / 10;
        } else {
            settingValue = 0.0f;
        }
     /*   switch (intVal) {
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
        }*/
        return settingValue;
    }

    //function call to speak out data text
    private void speakOut(String speakData) {
        HashMap<String, String> myHashAlarm = new HashMap<>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        tts.setPitch(ttsPitch);
        tts.setSpeechRate(ttsSpeed);
        tts.speak(speakData, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    @Override
    public void onInit(int status) {
        System.out.println("onInit tts:  " + status);
        if (status == TextToSpeech.SUCCESS) {
            System.out.println("onInit trace--------------" + MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
            Locale locale = new Locale(MeMePref.getStringPreference(this, MeMeConstant.PREF_VOICE_LANG));
            tts.setLanguage(locale);
            //speakOut("text to speak init");
            tts.setOnUtteranceCompletedListener(this);
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    public void onUtteranceCompleted(String s) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}

