package com.modev.common;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.modev.call.ContentContact;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MyService extends Service implements TextToSpeech.OnInitListener {
    private static final String TAG = "MyService";
    private BroadcastMyService broadcastMyService = null;
    private BroadcastCall broadcastCall = null;
    private BroadcastMessage broadcastMessage = null;
    private TextToSpeech textToSpeech;
    private static boolean checkStartTextToSpeech = false;
    volatile static private boolean checkLoopTextToSpeechCall = false;
    public static boolean checkRegisterBroadcastMessage = true;
    public static boolean checkRegisterBroadcastCall = true;
    private ArrayList<ContentContact> contentContacts;
    private TelephonyManager tmgr;
    private MyPhoneStateListener phoneListener;
    private MySharedPreferences mySharedPreferences;

    private Object iTelephony = null;


    private PackageManager packageManager;
    //    private BroadcastSpeechToText broadcastSpeechToText = null;
    private AudioManager mamanager;
    private int mStreamVolume;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent = null;
    private boolean mIsListening = false;
    private Method m3;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate...");
        mamanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
//        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
//        mamanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
//        amanager.setStreamMute(AudioManager.STREAM_RING, true);
//        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
//        mStreamVolume = mamanager.getStreamVolume(AudioManager.STREAM_MUSIC); // getting system volume into var for later un-muting
//        mamanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0); // setting system volume to zero, muting

        mySharedPreferences = new MySharedPreferences(this);
        mamanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mamanager.setStreamVolume(AudioManager.STREAM_MUSIC, mamanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        contentContacts = CommonMethod.instance().getContentContact(this);
//        intitTextToSpeech();
        tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneListener = new MyPhoneStateListener();
        tmgr.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.i(TAG, "sersion phone: " + Build.VERSION.SDK_INT);

        try {
            Method m1 = tmgr.getClass().getDeclaredMethod("getITelephony");
            m1.setAccessible(true);

            iTelephony = m1.invoke(tmgr);
            m3 = iTelephony.getClass().getDeclaredMethod("endCall");
            Log.i(TAG, "onCreate_ok listner call");

//            Method m2 = iTelephony.getClass().getDeclaredMethod("silenceRinger");
//            Method m3 = iTelephony.getClass().getDeclaredMethod("endCall");
        } catch (NoSuchMethodException e) {
            Log.i(TAG, "message error NoSuchMethodException: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.i(TAG, "message error IllegalAccessException: " + e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.i(TAG, "message error InvocationTargetException: " + e.getMessage());
            e.printStackTrace();
        }
        registerBroadcastMyService();
    }

    private void intitTextToSpeech() {
        textToSpeech = null;
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");
        checkRegisterBroadcastCall = mySharedPreferences.isCheckRegisterBroadcastCall();
        if (checkRegisterBroadcastCall) registerBroadcastCall();
        else unregisterBroadcastCall();
        checkRegisterBroadcastMessage = mySharedPreferences.isCheckRegisterBroadcastMessage();
        if (checkRegisterBroadcastMessage) registerBroadcastCall();
        else unregisterBroadCastMessage();
        return START_STICKY;
    }

    private void initComponent() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
//        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "onInit ");
        if (status == TextToSpeech.SUCCESS && textToSpeech != null) {
            textToSpeech.setLanguage(Locale.ENGLISH);
            Log.i(TAG, "Local root: " + Locale.getDefault().getCountry());
            checkStartTextToSpeech = true;
            Log.i(TAG, "onInit SUCCESS");
        }
    }

    private class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived ");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    Log.i(TAG, "ERROR_AUDIO");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    Log.i(TAG, "ERROR_CLIENT");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    Log.i(TAG, "ERROR_NETWORK");
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    Log.i(TAG, "ERROR_NETWORK_TIMEOUT");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    Log.i(TAG, "ERROR_INSUFFICIENT_PERMISSIONS");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    Log.i(TAG, "ERROR_NO_MATCH");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    Log.i(TAG, "ERROR_RECOGNIZER_BUSY");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    Log.i(TAG, "ERROR_SERVER");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    Log.i(TAG, "ERROR_SPEECH_TIMEOUT");
                    break;
            }
            Log.d(TAG, "error = " + error);
            initComponent();
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.i(TAG, "onEvent: " + eventType);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i(TAG, "onPartialResults");
            mIsListening = false;
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
//            mamanager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
        }

        @Override
        public synchronized void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.i(TAG, "result:");
            for (String i : matches) {
                Log.i(TAG, "onResults content: " + i);
            }
            if (matches.contains("no") || matches.contains("No") || matches.contains("nO") || matches.contains("NO")) {
//                try {
//
//                    m3.invoke(iTelephony);
//
//                } catch (IllegalAccessException e) {
//                    Log.i(TAG, "onResults IllegalAccessException: " + e.getMessage());
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    Log.i(TAG, "onResults InvocationTargetException: " + e.getMessage());
//                    e.printStackTrace();
//                }
//                ITelephony phoneMgr=ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                try {
                    Class clazz = Class.forName(tmgr.getClass().getName());
                    Method method = clazz.getDeclaredMethod("getITelephony");
                    method.setAccessible(true);
                    ITelephony telephonyService = (ITelephony) method.invoke(tmgr);
                    telephonyService.endCall();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }


            } else if (matches.contains("yes") || matches.contains("Yes") || matches.contains("yes") || matches.contains("YES")) {
//                try {
//                    Class clazz = Class.forName(tmgr.getClass().getName());
//                    Method method = clazz.getDeclaredMethod("getITelephony");
//                    method.setAccessible(true);
//                    ITelephony telephonyService = (ITelephony) method.invoke(tmgr);
//                    telephonyService.answerRingingCall();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
                PhoneUtil.answerRingingCall(MyService.this);
            } else {
                initComponent();
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }


        }

        @Override
        public void onRmsChanged(float rmsdB) {
//            Log.i(TAG, "onRmsChanged: " + rmsdB);
        }
    }


    private void registerBroadcastCall() {
        if (broadcastCall == null) {
            broadcastCall = new BroadcastCall();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonVL.ACTION_INCOMMINGCALL);
            intentFilter.addAction(CommonVL.ACTION_END_CALL);
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            registerReceiver(broadcastCall, intentFilter);
        }
        if (textToSpeech == null || !checkStartTextToSpeech) {
            intitTextToSpeech();
        }
        initComponent();
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

    }

    private void unregisterBroadcastCall() {
        if (broadcastCall != null) {
            unregisterReceiver(broadcastCall);
            broadcastCall = null;
        }
        tmgr.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
    }


    private class BroadcastCall extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            switch (intent.getAction()) {
                case "android.intent.action.PHONE_STATE":
                    break;
            }
        }
    }

    private void registerBroadCastMessage() {
        if (broadcastMessage == null) {
            broadcastMessage = new BroadcastMessage();
            IntentFilter intentFilter = new IntentFilter();

            registerReceiver(broadcastMessage, intentFilter);
        }
    }

    private void unregisterBroadCastMessage() {
        if (broadcastMessage != null) {
            unregisterReceiver(broadcastMessage);
            broadcastMessage = null;
        }
    }

    private class BroadcastMessage extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

            }
        }
    }


    private void registerBroadcastMyService() {
        if (broadcastMyService == null) {
            broadcastMyService = new BroadcastMyService();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonVL.REGISTER_BROADCAST_CALL);
            intentFilter.addAction(CommonVL.REGISTER_BROADCAST_MESSAGE);
            intentFilter.addAction(CommonVL.UNREGISTER_BROADCAST_CALL);
            intentFilter.addAction(CommonVL.UNREGISTER_BROADCAST_MESSAGE);
            registerReceiver(broadcastMyService, intentFilter);
        }
    }

    private void unregisterMyBroadcastService() {
        if (broadcastMyService != null) {
            unregisterReceiver(broadcastMyService);
            broadcastMyService = null;
        }
        unregisterBroadCastMessage();
        unregisterBroadcastCall();
    }

    private class BroadcastMyService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CommonVL.REGISTER_BROADCAST_CALL:
                    registerBroadcastCall();
                    break;
                case CommonVL.UNREGISTER_BROADCAST_CALL:
                    unregisterBroadcastCall();
                    break;
                case CommonVL.REGISTER_BROADCAST_MESSAGE:
                    registerBroadCastMessage();
                    break;
                case CommonVL.UNREGISTER_BROADCAST_MESSAGE:
                    unregisterBroadCastMessage();
                    break;
            }
        }
    }

    private void startTalking(final String content) {
//        if (checkStartTextToSpeech && checkRegisterBroadcastCall) {
//            checkLoopTextToSpeechCall = true;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (checkLoopTextToSpeechCall) {
//                        while (textToSpeech.isSpeaking()) {
//                            SystemClock.sleep(100);
//                        }
//                        if (textToSpeech != null && checkStartTextToSpeech)
//                            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
//                        SystemClock.sleep(500);
//                    }
//                    textToSpeech.shutdown();
//                    checkStartTextToSpeech = false;
//                    intitTextToSpeech();
//                }
//            }).start();
//        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public synchronized void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    String content;
                    if (incomingNumber.charAt(0) == '+') {
                        incomingNumber = "0" + incomingNumber.substring(2);
                    }
                    incomingNumber = CommonMethod.instance().convertToNumberPhone(incomingNumber);
                    if (contentContacts.contains(new ContentContact(null, incomingNumber))) {
                        String name = contentContacts.get((contentContacts.indexOf(new ContentContact(null, incomingNumber)))).getName();
                        content = name + " calling";
                    } else {
                        content = incomingNumber + " calling";
                    }
                    Log.i(TAG, "content: " + content);
                    MyService.this.startTalking(content);
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    checkLoopTextToSpeechCall = false;
                    textToSpeech.stop();
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

//    private static void answerRingingCallWithReflect(Context context) {
//
//        try {
//
//            Object telephonyObject = getTelephonyObject(context);
//
//            if (null != telephonyObject) {
//
//                Class telephonyClass = telephonyObject.getClass();
//
//                Method endCallMethod = telephonyClass.getMethod("answerRingingCall");
//
//                endCallMethod.setAccessible(true);
//
//
//                endCallMethod.invoke(telephonyObject);
//
//                // ITelephony iTelephony = (ITelephony) telephonyObject;
//
//                // iTelephony.answerRingingCall();
//
//            }
//
//        } catch (SecurityException e) {
//
//            e.printStackTrace();
//
//        } catch (IllegalArgumentException e) {
//
//            e.printStackTrace();
//
//        } catch (IllegalAccessException e) {
//
//            e.printStackTrace();
//
//        } catch (InvocationTargetException e) {
//
//            e.printStackTrace();
//
//        } catch (NoSuchMethodException e) {
//
//            e.printStackTrace();
//
//        }
//
//
//    }
//
//
//    private static void answerRingingCallWithBroadcast(Context context) {
//        AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//
//        //判断是否插上了耳机
//
//        boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
//
//        if (!isWiredHeadsetOn) {
//
//            Intent headsetPluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
//
//            headsetPluggedIntent.putExtra("state", 1);
//
//            headsetPluggedIntent.putExtra("microphone", 0);
//
//            headsetPluggedIntent.putExtra("name", "");
//
//            context.sendBroadcast(headsetPluggedIntent);
//
//
//            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//
//            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
//
//            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
//
//            context.sendOrderedBroadcast(meidaButtonIntent, null);
//
//
//            Intent headsetUnpluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
//
//            headsetUnpluggedIntent.putExtra("state", 0);
//
//            headsetUnpluggedIntent.putExtra("microphone", 0);
//
//            headsetUnpluggedIntent.putExtra("name", "");
//
//            context.sendBroadcast(headsetUnpluggedIntent);
//
//
//        } else {
//
//            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//
//            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
//
//            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
//
//            context.sendOrderedBroadcast(meidaButtonIntent, null);
//
//        }
//
//    }
//
//
//    /**
//     * 138
//     * 接听电话
//     * 139
//     *
//     * @param context 140
//     */
//
//    public static void answerRingingCall(Context context) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {  //2.3或2.3以上系统
//
//            answerRingingCallWithBroadcast(context);
//
//        } else {
//
//            answerRingingCallWithReflect(context);
//
//        }
//
//    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        unregisterMyBroadcastService();
        mamanager.setMode(AudioManager.MODE_NORMAL);
        super.onDestroy();
    }
}
