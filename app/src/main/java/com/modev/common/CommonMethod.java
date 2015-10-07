package com.modev.common;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.modev.call.ContentContact;

import java.util.ArrayList;

/**
 * Created by ducnd on 03/10/2015.
 */
public class CommonMethod {
    public static CommonMethod instance() {
        return  new CommonMethod();
    }

    public String convertToNumberPhone( String phoneNumber) {
        StringBuffer buffrePhoneNumber = new StringBuffer();
        for (char i : phoneNumber.toCharArray()) {
            if (i >= '0' && i <= '9') {
                buffrePhoneNumber.append(i);
            }
        }
        return new String(buffrePhoneNumber);
    }
    public ArrayList<ContentContact> getContentContact( Context context) {
        ArrayList<ContentContact> contentContacts = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                }, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        if (cursor != null) {
            int indexNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int indexName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String phoneNumber = cursor.getString(indexNumber);
                if ( phoneNumber.charAt(0) == '+' ) {
                    phoneNumber = "0" + phoneNumber.substring(2);
                }
                contentContacts.add(new ContentContact(cursor.getString(indexName),
                        CommonMethod.instance().convertToNumberPhone(phoneNumber)));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return contentContacts;
    }
}
