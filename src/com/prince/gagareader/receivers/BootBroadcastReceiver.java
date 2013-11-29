package com.prince.gagareader.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prince.gagareader.services.UpdateService;

public class BootBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent arg1) {
		Intent service = new Intent(context,UpdateService.class); 
		context.startService(service); 

	}

}
