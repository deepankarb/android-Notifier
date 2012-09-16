package rickflail.messaging.notifier;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessagesOpenHelper extends SQLiteOpenHelper {

	public MessagesOpenHelper(Context context) {
		super(context, "messages", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE messages (_id INTEGER PRIMARY KEY ASC AUTOINCREMENT, title VARCHAR, message VARCHAR, link VARCHAR, key VARCHAR DEFAULT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, viewed INTEGER DEFAULT 0)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		return;
	}

}
