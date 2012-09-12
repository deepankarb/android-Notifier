package rickflail.messaging.notifier;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

public class ClearReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ContentValues values = new ContentValues();
		values.put("viewed", 1);
		context.getContentResolver().update(MessageProvider.CONTENT_URI, values, null, null);
	}

}
