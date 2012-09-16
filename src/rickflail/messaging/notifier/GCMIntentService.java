package rickflail.messaging.notifier;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String link = extras.getString("link");
        String linkText = "";
        String key = extras.getString("key");
        if (key != null && key.equals("")) key = null;
        String silentStr = extras.getString("silent");
        Boolean silent = (silentStr != null && !silentStr.equals("") && !silentStr.equalsIgnoreCase("false"));
        
        if (link != null && !link.equals("")) {
        	Pattern linkPattern = Pattern.compile("^\\[([^\\]]+)\\]\\(([^\\) ]+).*\\)$");
        	Matcher linkMatcher = linkPattern.matcher(link);
        	if (linkMatcher.matches()) {
        		linkText = linkMatcher.group(1);
        		link = linkMatcher.group(2);
        	}
        }
        
        try {
        	URL u = new URL(link);
            u.toURI();
        } catch(Exception ex) {
        	link = "";
        }
        
        int viewed = 0;
        
        ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("message", message);
		values.put("link", link);
		values.put("linkText", linkText);
        
        if (key != null) {
        	values.put("key", key);
        	
        	String where = "key = '" + key + "'";
        	
        	if (silent) {
        		String[] projection = { "*" };
        		Cursor c = getContentResolver().query(MessageProvider.CONTENT_URI, projection, where, null, null);
        		
        		if (c.getCount() > 0) {
	        		c.moveToFirst();
	        		viewed = c.getInt(7);
	        		
	        		values.put("viewed", viewed);
        		}
        	}
        	
        	getContentResolver().delete(MessageProvider.CONTENT_URI, where, null);
        }
        
        getContentResolver().insert(MessageProvider.CONTENT_URI, values);
        
    	Intent update = new Intent(getString(R.string.update_intent));
    	update.putExtra("title", title);
    	update.putExtra("message", message);
    	update.putExtra("link", link);
    	update.putExtra("linkText", linkText);
    	update.putExtra("silent", silent);
    	update.putExtra("viewed", viewed);
    	this.sendOrderedBroadcast(update, getString(R.string.update_permission));
    }
    
}
