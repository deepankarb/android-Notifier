package rickflail.messaging.notifier;

import android.net.Uri;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UpdateReceiver extends BroadcastReceiver {
	
	private static final int NOTIFIER_ID = 1;
	SharedPreferences prefs;

	@Override
	public void onReceive(Context context, Intent intent) {
		String title = intent.getStringExtra("title");
		String message = intent.getStringExtra("message");
		String link = intent.getStringExtra("link");
		Boolean silent = intent.getBooleanExtra("silent", false);
		int viewed = intent.getIntExtra("viewed", 0);
		
		if (silent && viewed == 1) return;
		
		MessagesOpenHelper helper = new MessagesOpenHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT COUNT(*) AS unviewedCount FROM messages WHERE viewed = 0", null);
		c.moveToFirst();
		int unviewedCount = c.getInt(0);
		
		prefs = context.getSharedPreferences(context.getString(R.string.prefs), Context.MODE_PRIVATE);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.notification_icon;
		long when = System.currentTimeMillis();
		
		Context appContext = context.getApplicationContext();
		
		PendingIntent contentIntent;
		if (unviewedCount < 2 && link != null && !link.equals("")) {
			Uri linkUri = Uri.parse(link);
			Intent linkIntent = new Intent(Intent.ACTION_VIEW, linkUri);
			contentIntent = PendingIntent.getActivity(context,  0, linkIntent, 0);
		} else {		
			Intent notificationIntent = new Intent(context, History.class);
			contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		}
		
		Builder builder = new Builder(appContext);
		
		builder.setSmallIcon(icon)
			.setContentTitle(title)
			.setContentText(message)
			.setWhen(when)
			.setContentIntent(contentIntent);
		
		if (!silent) {
			builder.setTicker(title);
		}
		
		if (unviewedCount > 1) {
			builder.setContentInfo(unviewedCount + " unread");
			if (unviewedCount > 2) builder.setSmallIcon(R.drawable.notification_icon_x3);
			else builder.setSmallIcon(R.drawable.notification_icon_x2);
		}
		
		int defaults = 0;
		
		if (prefs.getBoolean("flashLED", true) && !silent) {
			defaults |= Notification.DEFAULT_LIGHTS;
		}
		if (prefs.getBoolean("vibrate", true) && !silent) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (prefs.getBoolean("useSound", true) && !silent) {
			String sound = prefs.getString("sound", "");
			if (!sound.equals("")) {
				builder.setSound(Uri.parse(sound));
			} else {
				defaults |= Notification.DEFAULT_SOUND;
			}
		}
		
		if (defaults != 0) {
			builder.setDefaults(defaults);
		}
		
		if (prefs.getBoolean("viewedOnClear", true)) {
			Intent clearIntent = new Intent(context.getString(R.string.clear_intent));
			PendingIntent deleteIntent = PendingIntent.getBroadcast(context,  0, clearIntent, 0);
			builder.setDeleteIntent(deleteIntent);
		}

		Notification notification = builder.getNotification();
		
		if (unviewedCount < 2 && link != null && !link.equals("")) {
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		
		notificationManager.notify(NOTIFIER_ID, notification);
	}

}
