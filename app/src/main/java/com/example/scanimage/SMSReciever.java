package com.example.scanimage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReciever extends BroadcastReceiver {
    public static MessageListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data=intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage=SmsMessage.createFromPdu((byte[])pdus[i]);
            String message = smsMessage.getMessageBody();
            Log.d("Message",message);
            listener.messageRecieved(message);
        }
    }


    public static void bindListener(MessageListener mlistener){
        listener = mlistener;
    }
}
interface  MessageListener{
    void messageRecieved(String Message);
}
