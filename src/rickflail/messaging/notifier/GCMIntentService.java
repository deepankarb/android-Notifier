package rickflail.messaging.notifier;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
        super(Registration.APP_GCM_ID);
    }
	
	@Override
    protected void onRegistered(Context context, String registrationId) {
        Registration.RegisterOnDeveloperServer(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) { }
    
    @Override
    public void onError(Context context, String errorId) {
    	Registration.OnRegistrationFail(context);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
    	Bundle extras = intent.getExtras();
    	if (extras == null) return;
    	
        String title = extras.getString("title");
        String message = extras.getString("message");
        String key = extras.getString("key");
        if (key != null && key.equals("")) key = null;
        String silentStr = extras.getString("silent");
        Boolean silent = (silentStr != null && !silentStr.equals("") && !silentStr.equalsIgnoreCase("false"));
        
        int viewed = 0;
        
        ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("message", message);
        
        if (key != null) {
        	values.put("key", key);
        	
        	String where = "key = '" + key + "'";
        	
        	if (silent) {
        		String[] projection = { "*" };
        		Cursor c = getContentResolver().query(MessageProvider.CONTENT_URI, projection, where, null, null);
        		
        		if (c.getCount() > 0) {
	        		c.moveToFirst();
	        		viewed = c.getInt(5);
	        		
	        		values.put("viewed", viewed);
        		}
        	}
        	
        	getContentResolver().delete(MessageProvider.CONTENT_URI, where, null);
        }
        
        getContentResolver().insert(MessageProvider.CONTENT_URI, values);
        
    	Intent update = new Intent(getString(R.string.update_intent));
    	update.putExtra("title", title);
    	update.putExtra("message", message);
    	update.putExtra("silent", silent);
    	update.putExtra("viewed", viewed);
    	this.sendOrderedBroadcast(update, getString(R.string.update_permission));
    }
    
}
