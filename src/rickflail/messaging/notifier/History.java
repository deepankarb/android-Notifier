package rickflail.messaging.notifier;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

import com.google.android.gcm.GCMRegistrar;

public class History extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	SimpleCursorAdapter mAdapter;
	SharedPreferences prefs;
	LoaderManager manager;
	
	ListView messageList;
	TextView emptyView;
	
	Intent settingsIntent;
	
	AlertDialog clearConfirm;
	
	private final BroadcastReceiver messageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    		History.this.getSupportLoaderManager().restartLoader(0, null, History.this);
    		this.abortBroadcast();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_history);
        
        settingsIntent = new Intent(this, Settings.class);
        
		GCMRegistrar.checkDevice(this);
		
		String[] fromColumns = { "title", "message" };
		int[] toViews = { R.id.text1, R.id.text2 };
		
		mAdapter = new MessageCursorAdapter(this, R.layout.simple_list_item_2, null, fromColumns, toViews, 0) {
			public boolean isEnabled(int position) {
				return false;
			}
		};
		
		emptyView = new TextView(this);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
		emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
		emptyView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.padding_large), 0, 0);
		emptyView.setTextSize(18);
		emptyView.setText(getString(R.string.loading));
		
		messageList = (ListView) findViewById(R.id.messageList);
		messageList.setAdapter(mAdapter);
		messageList.setVerticalFadingEdgeEnabled(true);
		messageList.setClickable(false);
		messageList.setEmptyView(emptyView);
		
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(emptyView);
		
		getSupportLoaderManager().initLoader(0, null, this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to clear?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getContentResolver().delete(MessageProvider.CONTENT_URI, null, null);
	        	History.this.getSupportLoaderManager().restartLoader(0, null, History.this);
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		clearConfirm = builder.create();
    }
    
    @Override
    public void onPause() {
    	this.unregisterReceiver(messageReceiver);
    	
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.cancelAll();
    	
    	IntentFilter filter = new IntentFilter(getString(R.string.update_intent));
    	filter.setPriority(1);
    	this.registerReceiver(messageReceiver, filter);
    	
    	if (!Registration.IsRegistered(this)) {
    		Registration.RegisterAsync(this, false);
    	}
		
		getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_history, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clear) {
        	clearConfirm.show();
        } else if (item.getItemId() == R.id.menu_settings) {
        	startActivity(settingsIntent);
        	return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	return new CursorLoader(this, MessageProvider.CONTENT_URI, null, null, null, "timestamp DESC");
    }
    
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	mAdapter.swapCursor(data);
    	messageList.smoothScrollToPosition(0);
    	
    	if (data.getCount() == 0) {
    		emptyView.setText(getString(R.string.no_messages));
    	} else {
	    	ContentValues values = new ContentValues();
			values.put("viewed", 1);
			getContentResolver().update(MessageProvider.CONTENT_URI, values, null, null);
    	}
    }
    
    public void onLoaderReset(Loader<Cursor> loader) {
    	emptyView.setText(getString(R.string.loading));
    	mAdapter.swapCursor(null);
    }
    
}
