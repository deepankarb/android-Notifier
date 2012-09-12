package rickflail.messaging.notifier;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageCursorAdapter extends SimpleCursorAdapter {

	public MessageCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}
	
	@Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewBinder binder = getViewBinder();
        final int count = mTo.length;
        final int[] from = mFrom;
        final int[] to = mTo;
        TextView tv;

    	boolean viewed = (cursor.getInt(5) == 1);

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
            	
            	if (v instanceof TextView) {
            		tv = (TextView)v;
            		
            		if (!viewed) {
            			tv.setTextColor(view.getResources().getColor(R.color.unread));
            		} else {
            			int target;
            			if (to[i] == R.id.text2) target = view.getResources().getColor(android.R.color.secondary_text_dark);
            			else target = view.getResources().getColor(android.R.color.primary_text_dark);
            			tv.setTextColor(target);
            		}
            	}
            	
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, cursor, from[i]);
                }

                if (!bound) {
                    String text = cursor.getString(from[i]);
                    if (text == null || text.equals("")) {
                        text = "";
                    }

                    if (v instanceof TextView) {
                    	tv = (TextView)v;
                        setViewText(tv, text);
                        if (text.equals("")) tv.setVisibility(View.GONE);
                        else tv.setVisibility(View.VISIBLE);
                    } else if (v instanceof ImageView) {
                        setViewImage((ImageView) v, text);
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleCursorAdapter");
                    }
                }
            }
        }
    }

}
