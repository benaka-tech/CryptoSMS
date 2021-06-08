package com.mini_project.cryptosms;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import com.scottyab.aescrypt.AESCrypt;



public class Dashboard extends AppCompatActivity{
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText input;
    SmsManager smsManager = SmsManager.getDefault();
    String secretKey = "password";

    private static Dashboard inst;

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    public Dashboard() throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
    }

    public static Dashboard instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = (ListView) findViewById(R.id.messages);
        input = (EditText) findViewById(R.id.input);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {
            try {
                refreshSmsInbox();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }


    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onSendClick(View view) throws GeneralSecurityException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();

        } else {
            String encrpyted = AESCrypt.encrypt(secretKey,input.getText().toString());
            //ArrayList<String> parts = smsManager.divideMessage(encryptedMessage);
            System.out.println(encrpyted);
            smsManager.sendTextMessage("+919986011018", null, encrpyted, null, null);
            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
        }
    }
    public void updateInbox(final String smsBody, String address) throws GeneralSecurityException {
        String display="";
        System.out.println(smsBody);
        String decryptedMessage = AESCrypt.decrypt(secretKey,smsBody);
        //System.out.println(decryptedMessage);
        display=address+decryptedMessage;
        arrayAdapter.insert(display, 0);
        arrayAdapter.notifyDataSetChanged();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                try {
                    refreshSmsInbox();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }



    }

    public void refreshSmsInbox() throws GeneralSecurityException {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        System.out.println(indexBody);
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;

        arrayAdapter.clear();
        do {
            String decryptedMessage = AESCrypt.decrypt(secretKey,smsInboxCursor.getString(indexBody));
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" +decryptedMessage + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
//messages.setSelection(arrayAdapter.getCount() - 1);
    }



}

