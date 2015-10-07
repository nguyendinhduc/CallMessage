package com.modev.common;

import android.content.Context;
import android.util.Log;

public class MySharedPreferences {
    private static final String NAME_CHECK_REGISTER = "CHECK_REGISTER";
    private static final String KEY_REGISTER_MESSAGE = "KEY_REGISTER_MESSAGE";
    public static final String KEY_REGISTER_CALL = "KEY_REGISTER_CALL";
    private Context context;
    public MySharedPreferences ( Context context ) {
        this.context = context;
    }

    public boolean isCheckRegisterBroadcastMessage() {
       return this.context.getSharedPreferences(NAME_CHECK_REGISTER,
                Context.MODE_PRIVATE).getBoolean(KEY_REGISTER_MESSAGE, true);

    }

    public void setCheckRegisterBroadcastMessage( boolean checkRegisterBroadcastMessage) {
        this.context.getSharedPreferences(NAME_CHECK_REGISTER, Context.MODE_PRIVATE).
                edit().putBoolean(KEY_REGISTER_MESSAGE, checkRegisterBroadcastMessage).commit();

    }

    public boolean isCheckRegisterBroadcastCall() {
        return this.context.getSharedPreferences(NAME_CHECK_REGISTER,
                Context.MODE_PRIVATE).getBoolean(KEY_REGISTER_CALL, true);

    }

    public void setCheckRegisterBroadcastCall( boolean checkRegisterBroadcastCall) {
        this.context.getSharedPreferences(NAME_CHECK_REGISTER, Context.MODE_PRIVATE).
                edit().putBoolean(KEY_REGISTER_CALL, checkRegisterBroadcastCall).commit();
    }


}
