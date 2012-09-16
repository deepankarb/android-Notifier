package rickflail.messaging.notifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class Settings extends Activity {

	SharedPreferences prefs;
	
	LinearLayout settings;
	
	CheckBox cbFlashLED;
	CheckBox cbVibrate;
	CheckBox cbUseSound;
	CheckBox cbViewedOnClear;
	CheckBox cbImmediateLink;
	Button btnReregister;
	Button btnChooseSound;
	
	SharedPreferences.Editor editor;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
	}
	
	@Override
	public void onResume() {
		super.onResume();
        
        prefs = getSharedPreferences(getString(R.string.prefs), Context.MODE_PRIVATE);
        
        Boolean flashLED = prefs.getBoolean("flashLED", true);
        Boolean vibrate = prefs.getBoolean("vibrate", true);
        Boolean useSound = prefs.getBoolean("useSound", true);
        Boolean viewedOnClear = prefs.getBoolean("viewedOnClear", true);
        Boolean immediateLink = prefs.getBoolean("immediateLink", false);
        
        cbFlashLED = (CheckBox) findViewById(R.id.checkBoxFlashLED);
        cbFlashLED.setChecked(flashLED);
        cbFlashLED.setOnCheckedChangeListener(toggleCB("flashLED"));
        
        cbVibrate = (CheckBox) findViewById(R.id.checkBoxVibrate);
        cbVibrate.setChecked(vibrate);
        cbVibrate.setOnCheckedChangeListener(toggleCB("vibrate"));
        
        cbUseSound = (CheckBox) findViewById(R.id.checkBoxUseSound);
        cbUseSound.setChecked(useSound);
        cbUseSound.setOnCheckedChangeListener(toggleCB("useSound"));
        
        btnChooseSound = (Button) findViewById(R.id.buttonChooseSound);
        btnChooseSound.setOnClickListener(chooseSound(this));
        
        cbViewedOnClear = (CheckBox) findViewById(R.id.checkBoxViewedOnClear);
        cbViewedOnClear.setChecked(viewedOnClear);
        cbViewedOnClear.setOnCheckedChangeListener(toggleCB("viewedOnClear"));
        
        cbImmediateLink = (CheckBox) findViewById(R.id.checkBoxImmediateLink);
        cbImmediateLink.setChecked(immediateLink);
        cbImmediateLink.setOnCheckedChangeListener(toggleCB("immediateLink"));
        
        btnReregister = (Button) findViewById(R.id.buttonReregister);
        btnReregister.setOnClickListener(reregister(this));
        
        editor = prefs.edit();
	}
	
	@Override
    public void onPause() {
    	super.onPause();
    }
	
	public OnCheckedChangeListener toggleCB(final String pref) {
    	return new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				editor.putBoolean(pref, isChecked);
				editor.commit();
			}
		};
    }
	
	public OnClickListener reregister(final Context context) {
		return new View.OnClickListener() {
			public void onClick(View view) {
				Toast.makeText(context, "Attempting Registration...", Toast.LENGTH_SHORT).show();
				Registration.RegisterAsync(context, true);
			}
		};
	}
	
	public OnClickListener chooseSound(final Context context) {
		return new View.OnClickListener() {
			public void onClick(View view) {
		        String sound = prefs.getString("sound", "");
				
				Intent soundIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				
				soundIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
				soundIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select");
				soundIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
				
				if (!sound.equals("")) {
					soundIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sound));
				}
				
				startActivityForResult(soundIntent, 0);
			}
		};
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		
		Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
		if (uri == null) return;
		
		String sound = uri.toString();
		editor.putString("sound", sound);
		editor.commit();
	}

}
