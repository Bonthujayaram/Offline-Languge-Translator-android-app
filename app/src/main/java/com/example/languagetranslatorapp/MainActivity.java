package com.example.languagetranslatorapp;

import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu", "Telugu", "Albanian", "Croatian", "Danish", "Dutch", "Esperanto", "Estonian", "Finnish", "French", "Afrikaans", "German", "Greek", "Gujarati", "Haitian_Creole", "Hebrew", "Hungarian", "Icelandic", "Indonesian", "Irish", "Italian", "Japanese", "Kannada", "Korean", "Latvian", "Lithuanian", "macedonian", "Malay", "Maltese", "Marathi", "Norwegian", "Persian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Spanish", "Swahili", "Swedish", "Tamil", "Turkish", "Ukrainian", "Vietnamese"};
    String[] toLanguages = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu", "Telugu", "Albanian", "Croatian", "Danish", "Dutch", "Esperanto", "Estonian", "Finnish", "French", "Afrikaans", "German", "Greek", "Gujarati", "Haitian_Creole", "Hebrew", "Hungarian", "Icelandic", "Indonesian", "Irish", "Italian", "Japanese", "Kannada", "Korean", "Latvian", "Lithuanian", "macedonian", "Malay", "Maltese", "Marathi", "Norwegian", "Persian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Spanish", "Swahili", "Swedish", "Tamil", "Turkish", "Ukrainian", "Vietnamese"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    String fromLanguageCode = "", toLanguageCode = "";

    private ImageView swapImage;

    private TextToSpeech textToSpeech;
    private static final String UTTERANCE_ID = "TranslateUtterance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEdtSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslated);
        Button copyBtn = findViewById(R.id.idBtnCopy);
        swapImage = findViewById(R.id.idSwapImage);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            Log.d("MainActivity", "Latitude: " + latitude + " Longitude: " + longitude);
        }

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        // Initialize translation options and clients for all available language pairs
        List<Translator> translators = new ArrayList<>();

        for (String fromLanguage : fromLanguages) {
            for (String toLanguage : toLanguages) {
                if (!fromLanguage.equals("From") && !toLanguage.equals("To") && !fromLanguage.equals(toLanguage)) {
                    TranslatorOptions options = new TranslatorOptions.Builder()
                            .setSourceLanguage(getLanguageCode(fromLanguage))
                            .setTargetLanguage(getLanguageCode(toLanguage))
                            .build();

                    Translator translator = Translation.getClient(options);
                    translators.add(translator);
                }
            }
        }

        // Download the language models for all available language pairs
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        for (Translator translator : translators) {
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to download language model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguageCode.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select target language", Toast.LENGTH_SHORT).show();
                } else {
                    String sourceText = sourceEdt.getText().toString();
                    List<String> sentences = tokenizeSentences(sourceText);
                    translateSentences(sentences, fromLanguageCode, toLanguageCode);
                }
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert to Text");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String translatedText = translatedTV.getText().toString();
                if (!translatedText.isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Translated Text", translatedText);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Translated text copied to clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No translated text to copy", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button swapButton = findViewById(R.id.swapButton);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int fromPosition = fromSpinner.getSelectedItemPosition();
                int toPosition = toSpinner.getSelectedItemPosition();

                // Swap the spinner selections
                fromSpinner.setSelection(toPosition);
                toSpinner.setSelection(fromPosition);

                // Load the rotation animation from the XML resource
                Animation rotateAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.spin_animation);

                // Set the animation on the swap image (assuming 'swapImage' is an ImageView)
                swapImage.startAnimation(rotateAnimation);
            }
        });

        Button speakerButton = findViewById(R.id.speakerButton);
        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakTranslatedText();
            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int langResult = textToSpeech.setLanguage(Locale.US);
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TextToSpeech", "Language not supported.");
                    }
                } else {
                    Log.e("TextToSpeech", "Initialization failed.");
                }
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceEdt.setText(result.get(0));
        }
    }

    private List<String> tokenizeSentences(String text) {
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.getDefault());
        iterator.setText(text);

        int start = iterator.first();
        int end;
        List<String> sentences = new ArrayList<>();

        while ((end = iterator.next()) != BreakIterator.DONE) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
            start = end;
        }

        return sentences;
    }

    private void translateSentences(List<String> sentences, String fromLanguage, String toLanguage) {
        translatedTV.setText("Translating...");

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(fromLanguage)
                .setTargetLanguage(toLanguage)
                .build();

        Translator translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                StringBuilder translatedText = new StringBuilder();
                for (String sentence : sentences) {
                    translator.translate(sentence).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            translatedText.append(s).append(" ");
                            if (translatedText.length() >= sentences.size()) {
                                translatedTV.setText(translatedText.toString().trim());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to Translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to download language model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakTranslatedText() {
        String textToRead = translatedTV.getText().toString();
        if (!textToRead.isEmpty()) {
            if (textToSpeech != null) {
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            }
        } else {
            Toast.makeText(MainActivity.this, "No text to read.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private String getLanguageCode(String language) {
        String languageCode = "";
        switch (language) {
            case "English":
                languageCode = TranslateLanguage.ENGLISH;
                break;
            case "Telugu":
                languageCode = TranslateLanguage.TELUGU;
                break;
            case "Hindi":
                languageCode = TranslateLanguage.HINDI;
                break;
            case "Afrikaans":
                languageCode = TranslateLanguage.AFRIKAANS;
                break;
            case "Urdu":
                languageCode = TranslateLanguage.URDU;
                break;
            case "Bengali":
                languageCode = TranslateLanguage.BENGALI;
                break;
            case "Arabic":
                languageCode = TranslateLanguage.ARABIC;
                break;
            case "Belarusian":
                languageCode = TranslateLanguage.BELARUSIAN;
                break;
            case "Catalan":
                languageCode = TranslateLanguage.CATALAN;
                break;
            case "Czech":
                languageCode = TranslateLanguage.CZECH;
                break;
            case "Welsh":
                languageCode = TranslateLanguage.WELSH;
                break;
            case "Bulgarian":
                languageCode = TranslateLanguage.BULGARIAN;
                break;
            case "Albanian":
                languageCode = TranslateLanguage.ALBANIAN;
                break;
            case "Chinese":
                languageCode = TranslateLanguage.CHINESE;
                break;
            case "Croatian":
                languageCode = TranslateLanguage.CROATIAN;
                break;
            case "Danish":
                languageCode = TranslateLanguage.DANISH;
                break;
            case "Dutch":
                languageCode = TranslateLanguage.DUTCH;
                break;
            case "Esperanto":
                languageCode = TranslateLanguage.ESPERANTO;
                break;
            case "Estonian":
                languageCode = TranslateLanguage.ESTONIAN;
                break;
            case "Finnish":
                languageCode = TranslateLanguage.FINNISH;
                break;
            case "French":
                languageCode = TranslateLanguage.FRENCH;
                break;
            case "Georgian":
                languageCode = TranslateLanguage.GEORGIAN;
                break;
            case "German":
                languageCode = TranslateLanguage.GERMAN;
                break;
            case "Greek":
                languageCode = TranslateLanguage.GREEK;
                break;
            case "Gujarati":
                languageCode = TranslateLanguage.GUJARATI;
                break;
            case "Haitian_Creole":
                languageCode = TranslateLanguage.HAITIAN_CREOLE;
                break;
            case "Hebrew":
                languageCode = TranslateLanguage.HEBREW;
                break;
            case "Hungarian":
                languageCode = TranslateLanguage.HUNGARIAN;
                break;
            case "Icelandic":
                languageCode = TranslateLanguage.ICELANDIC;
                break;
            case "Indonesian":
                languageCode = TranslateLanguage.INDONESIAN;
                break;
            case "Irish":
                languageCode = TranslateLanguage.IRISH;
                break;
            case "Italian":
                languageCode = TranslateLanguage.ITALIAN;
                break;
            case "Japanese":
                languageCode = TranslateLanguage.JAPANESE;
                break;
            case "Kannada":
                languageCode = TranslateLanguage.KANNADA;
                break;
            case "Korean":
                languageCode = TranslateLanguage.KOREAN;
                break;
            case "Latvian":
                languageCode = TranslateLanguage.LATVIAN;
                break;
            case "Lithuanian":
                languageCode = TranslateLanguage.LITHUANIAN;
                break;
            case "macedonian":
                languageCode = TranslateLanguage.MACEDONIAN;
                break;
            case "Malay":
                languageCode = TranslateLanguage.MALAY;
                break;
            case "Maltese":
                languageCode = TranslateLanguage.MALTESE;
                break;
            case "Marathi":
                languageCode = TranslateLanguage.MARATHI;
                break;
            case "Norwegian":
                languageCode = TranslateLanguage.NORWEGIAN;
                break;
            case "Persian":
                languageCode = TranslateLanguage.PERSIAN;
                break;
            case "Polish":
                languageCode = TranslateLanguage.POLISH;
                break;
            case "Portuguese":
                languageCode = TranslateLanguage.PORTUGUESE;
                break;
            case "Romanian":
                languageCode = TranslateLanguage.ROMANIAN;
                break;
            case "Russian":
                languageCode = TranslateLanguage.RUSSIAN;
                break;
            case "Slovak":
                languageCode = TranslateLanguage.SLOVAK;
                break;
            case "Slovenian":
                languageCode = TranslateLanguage.SLOVENIAN;
                break;
            case "Spanish":
                languageCode = TranslateLanguage.SPANISH;
                break;
            case "Swahili":
                languageCode = TranslateLanguage.SWAHILI;
                break;
            case "Swedish":
                languageCode = TranslateLanguage.SWEDISH;
                break;
            case "Tamil":
                languageCode = TranslateLanguage.TAMIL;
                break;
            case "Turkish":
                languageCode = TranslateLanguage.TURKISH;
                break;
            case "Ukrainian":
                languageCode = TranslateLanguage.UKRAINIAN;
                break;
            case "Vietnamese":
                languageCode = TranslateLanguage.VIETNAMESE;
                break;
        }
        return languageCode;
    }
}