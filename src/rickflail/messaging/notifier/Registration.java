package rickflail.messaging.notifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class Registration {
	static final String PATH = "http://flails.net/gcm/register.php";
	static final String APP_GCM_ID = "680972498738";
	static RegisterAsyncTask asyncTask;
	static Boolean taskWasForced = false;
	static Context originalContext;
	
	static boolean IsRegistered(Context context) {
		String regId = GCMRegistrar.getRegistrationId(context);
		boolean onServer = GCMRegistrar.isRegisteredOnServer(context);
		return (regId != "" && onServer);
	}

	static void Register(Context context, Boolean force) {
		taskWasForced = force;
		originalContext = context;
		
		if (force || !IsRegistered(context)) {
			String regId = GCMRegistrar.getRegistrationId(context);
			if (!force && regId != "") {
				RegisterOnDeveloperServer(context, regId);
			} else {
				GCMRegistrar.setRegisteredOnServer(context, false);
				try {
					GCMRegistrar.register(context, APP_GCM_ID);
				} catch (Exception ex) {
					OnRegistrationFail(context);
				}
			}
		}
	}
	
	static void OnRegistrationFail(Context context) {
		if (!Activity.class.isInstance(originalContext)) return;
		
		Activity a = (Activity) originalContext;
		a.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(originalContext, originalContext.getString(R.string.app_name) +  ": Registration Failed", Toast.LENGTH_LONG).show();
			}
		});
	}
	
	static void OnRegistrationSuccess(Context context) {
		if (!taskWasForced) return;
		if (!Activity.class.isInstance(originalContext)) return;
		
		Activity a = (Activity) originalContext;
		a.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(originalContext, originalContext.getString(R.string.app_name) +  ": Registration Successful", Toast.LENGTH_SHORT).show();
			}
		});		
	}
	
	static void RegisterOnDeveloperServer(Context context, String registrationId) {
		for (int i = 0; i < 3; i++) {
			boolean success = post(context, registrationId);
			if (success) {
				GCMRegistrar.setRegisteredOnServer(context, true);
				OnRegistrationSuccess(context);
				return;
			} else {
				try {
					Thread.sleep(2000);
				} catch (Exception ex) {
					break;
				}
				
			}
		}
		OnRegistrationFail(context);
	}
	
	protected static boolean post(Context context, String regId) {
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			HttpPost httpPost = new HttpPost(PATH);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("auth", "rickflail"));
	        nameValuePairs.add(new BasicNameValuePair("name", GetGoogleName(context)));
	        nameValuePairs.add(new BasicNameValuePair("id", regId));
	        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
			String responseBody = httpClient.execute(httpPost, responseHandler);
			
			return responseBody.trim().equals("1");
		} catch (Exception e) {
			return false;
		}
	}
	
	static class RegisterAsyncTask extends AsyncTask<Context, Void, Boolean> {
		Context c;
		Boolean force;
		
		public RegisterAsyncTask(Boolean forceIt) {
			super();
			force = forceIt;
		}
		
		@Override
		protected Boolean doInBackground(Context... params) {
			if (params.length < 1) return false;
			
			c = params[0];
			
			Registration.Register(c, force);
			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				Registration.OnRegistrationFail(c);
			}
		}
		
		@Override
		protected void onCancelled() { }
	}
	
	static void RegisterAsync(Context context, Boolean force) {
		if (asyncTask != null) {
			asyncTask.cancel(false);
		}
		asyncTask = new RegisterAsyncTask(force);
		asyncTask.execute(context);
	}
	
	static String GetGoogleName(Context context) {
		AccountManager manager = AccountManager.get(context); 
	    Account[] accounts = manager.getAccountsByType("com.google");
	    Account account = accounts[0];
	    
	    if (account.name.indexOf("@") != -1) {
	    	String[] parts = account.name.split("@");
	    	return parts[0];
	    } else {
	    	return account.name;
	    }
	}

}
