package com.max.agent;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int SPEECH_REQUEST = 1001;
    private static final int AUDIO_PERMISSION = 1002;
    private static final int CONTACT_PERMISSION = 1003;
    private static final int CALL_PERMISSION = 1004;

    private TextView status;
    private TextView heard;
    private TextToSpeech tts;

    private String pendingCommand = "";
    private String pendingCallNumber = "";
    private String pendingCallName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(new Locale("ur", "PK"));

                if (r == TextToSpeech.LANG_MISSING_DATA ||
                    r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
            }
        });
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 45, 30, 30);
        root.setBackgroundColor(Color.rgb(15, 16, 20));

        TextView title = new TextView(this);
        title.setText("AIRA");
        title.setTextColor(Color.WHITE);
        title.setTextSize(38);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Personal Phone Assistant");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Bolo boss");
        status.setTextColor(Color.WHITE);
        status.setTextSize(24);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 65, 0, 35);

        Button mic = new Button(this);
        mic.setText("🎙  Bolo boss");
        mic.setTextSize(19);
        mic.setOnClickListener(v -> startListening());

        heard = new TextView(this);
        heard.setText("Command ka intezar hai...");
        heard.setTextColor(Color.LTGRAY);
        heard.setTextSize(17);
        heard.setGravity(Gravity.CENTER);
        heard.setPadding(10, 35, 10, 20);

        root.addView(title);
        root.addView(subtitle);
        root.addView(status);
        root.addView(mic);
        root.addView(heard);

        setContentView(root);
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION
            );
            return;
        }

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "ur-PK"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Bolo boss"
        );

        try {
            status.setText("Sun raha hoon...");
            startActivityForResult(intent, SPEECH_REQUEST);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Speech recognition available nahi hai",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != SPEECH_REQUEST ||
                resultCode != RESULT_OK ||
                data == null) {
            status.setText("Bolo boss");
            return;
        }

        ArrayList<String> results =
                data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                );

        if (results == null || results.isEmpty()) {
            speak("Dobara bolo boss");
            return;
        }

        String command = results.get(0).trim();

        heard.setText("You: " + command);

        handleCommand(command.toLowerCase(Locale.ROOT));
    }

    private void handleCommand(String command) {

        if (contains(command, "bolo boss", "بولو باس")) {
            speak("Jee boss, bolo");
            return;
        }

        // CALL
        if (contains(command,
                "call ",
                "call karo",
                "call kar",
                "phone karo",
                "فون کرو",
                "کال کرو")) {

            startCallFlow(command);
            return;
        }

        // WHATSAPP MESSAGE
        if (contains(command,
                "whatsapp message",
                "whatsapp pe message",
                "whatsapp par message",
                "واٹس ایپ میسج",
                "واٹس ایپ پر میسج")) {

            speak("WhatsApp message feature ready hai boss. Abhi chat open karne wala step add hai.");
            openWhatsApp();
            return;
        }

        // YOUTUBE
        if (contains(command,
                "youtube",
                "یوٹیوب")) {

            openApp(
                    "com.google.android.youtube",
                    "YouTube"
            );
            return;
        }

        // WHATSAPP
        if (contains(command,
                "whatsapp",
                "واٹس ایپ")) {

            openApp(
                    "com.whatsapp",
                    "WhatsApp"
            );
            return;
        }

        // CHROME
        if (contains(command,
                "chrome",
                "browser",
                "کروم",
                "براؤزر")) {

            openApp(
                    "com.android.chrome",
                    "Chrome"
            );
            return;
        }

        // CAMERA
        if (contains(command,
                "camera",
                "کیمرا")) {

            try {
                startActivity(
                        new Intent(
                                android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                        )
                );

                speak("Camera khol diya boss");

            } catch (Exception e) {
                speak("Camera nahi khul saka boss");
            }

            return;
        }

        // SETTINGS
        if (contains(command,
                "settings",
                "setting",
                "سیٹنگ")) {

            try {
                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );

                speak("Settings khol di boss");

            } catch (Exception e) {
                speak("Settings nahi khul saki boss");
            }

            return;
        }

        // GOOGLE SEARCH
        if (contains(command,
                "search ",
                "google ",
                "سرچ ",
                "گوگل ")) {

            String query = command
                    .replace("search", "")
                    .replace("google", "")
                    .replace("سرچ", "")
                    .replace("گوگل", "")
                    .trim();

            if (query.isEmpty()) {
                speak("Kya search karun boss?");
                return;
            }

            try {
                Intent search = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                                "https://www.google.com/search?q="
                                        + Uri.encode(query)
                        )
                );

                startActivity(search);
                speak("Search khol diya boss");

            } catch (Exception e) {
                speak("Search nahi khul saka boss");
            }

            return;
        }

        // HELP
        if (contains(command,
                "help",
                "madad",
                "مدد")) {

            speak(
                    "Main apps khol sakti hoon, call kar sakti hoon aur Google search kar sakti hoon boss."
            );
            return;
        }
        speak("Command abhi available nahi hai boss.");
    }

    private void startCallFlow(String command) {

        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            pendingCommand = command;

            requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACT_PERMISSION
            );
            return;
        }

        findContactAndCall(command);
    }

    private void findContactAndCall(String command) {

        String spokenName = command
                .replace("call", "")
                .replace("karo", "")
                .replace("kar", "")
                .replace("phone", "")
                .replace("فون", "")
                .replace("کال", "")
                .replace("کرو", "")
                .trim();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                null
        );

        String foundName = null;
        String foundNumber = null;

        if (cursor != null) {

            while (cursor.moveToNext()) {

                String name = cursor.getString(0);
                String number = cursor.getString(1);

                if (name != null &&
                        spokenName.contains(
                                name.toLowerCase(Locale.ROOT))) {

                    foundName = name;
                    foundNumber = number;
                    break;
                }
            }

            cursor.close();
        }

        if (foundNumber == null) {
            speak("Contact nahi mila boss.");
            return;
        }

        confirmCall(foundName, foundNumber);
    }

    private void confirmCall(String name, String number) {

        new AlertDialog.Builder(this)
                .setTitle("Aira confirmation")
                .setMessage(name + " ko call karna hai?")
                .setNegativeButton(
                        "Cancel",
                        (dialog, which) ->
                                speak("Call cancel kar di boss")
                )
                .setPositiveButton(
                        "Call",
                        (dialog, which) ->
                                makeCall(name, number)
                )
                .show();
    }

    private void makeCall(String name, String number) {

        pendingCallName = name;
        pendingCallNumber = number;

        if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION
            );
            return;
        }

        doCall();
    }

    private void doCall() {

        try {

            Intent call = new Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + pendingCallNumber)
            );

            startActivity(call);

            speak(
                    pendingCallName +
                    " ko call kar raha hoon boss"
            );

        } catch (Exception e) {

            Intent dial = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:" + pendingCallNumber)
            );

            startActivity(dial);
            speak("Dialer khol diya boss");
        }
    }

    private void openWhatsApp() {

        Intent launch =
                getPackageManager()
                        .getLaunchIntentForPackage("com.whatsapp");

        if (launch != null) {
            startActivity(launch);
            speak("WhatsApp khol diya boss");
        } else {
            speak("WhatsApp installed nahi hai boss");
        }
    }

    private void openApp(String packageName, String appName) {

        Intent launch =
                getPackageManager()
                        .getLaunchIntentForPackage(packageName);

        if (launch != null) {

            startActivity(launch);
            speak(appName + " khol diya boss");

        } else {

            speak(
                    appName +
                    " phone mein installed nahi mila boss"
            );
        }
    }

    private boolean contains(
            String command,
            String... words) {

        for (String word : words) {
            if (command.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private void speak(String message) {

        status.setText(message);

        if (tts != null) {
            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "AIRA_RESPONSE"
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == AUDIO_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                startListening();

            } else {
                speak("Microphone permission chahiye boss.");
            }

            return;
        }

        if (requestCode == CONTACT_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                if (!pendingCommand.isEmpty()) {

                    String command = pendingCommand;
                    pendingCommand = "";

                    findContactAndCall(command);
                }

            } else {
                speak("Contacts permission ke baghair contact nahi mil sakta boss.");
            }

            return;
        }

        if (requestCode == CALL_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                doCall();

            } else {
                speak("Call permission deny ho gayi boss.");
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
