
package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we
 * do not implement full support for SQL as a usual ContentProvider does. We
 * re-purpose ContentProvider's interface to use it as a key-value table. Please
 * read:
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * before you start to get yourself familiarized with ContentProvider. There are
 * two methods you need to implement---insert() and query(). Others are optional
 * and will not be tested.
 * 
 * @author stevko
 */
public class GroupMessengerProvider extends ContentProvider {

    Context fileContext;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have
         * two columns (a key column and a value column) and one row that
         * contains the actual (key, value) pair to be inserted. For actual
         * storage, you can use any option. If you know how to use SQL, then you
         * can use SQLite. But this is not a requirement. You can use other
         * storage options, such as the internal storage option that I used in
         * PA1. If you want to use that option, please take a look at the code
         * for PA1.
         */
        String filename = values.getAsString("key");
        String row = values.getAsString("value");
        FileOutputStream outputStream;

        try {
            outputStream = fileContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(row.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it
        // here.
        fileContext = getContext();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return
         * a Cursor object with the right format. If the formatting is not
         * correct, then it is not going to work. If you use SQLite, whatever is
         * returned from SQLite is a Cursor object. However, you still need to
         * be careful because the formatting might still be incorrect. If you
         * use a file storage option, then it is your job to build a Cursor *
         * object. I recommend building a MatrixCursor described at:
         * http://developer
         * .android.com/reference/android/database/MatrixCursor.html
         */
        Context fileContext = getContext();
        FileInputStream inputStream;
        String value = null;
        try {
            inputStream = fileContext.openFileInput(selection);
            int avail = inputStream.available();
            byte[] buffer = new byte[avail];
            inputStream.read(buffer, 0, avail);
            value = new String(buffer, "UTF-8");
            inputStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String[] columns = {
                "key", "value"
        };
        MatrixCursor mCursor = new MatrixCursor(columns);
        RowBuilder rb = mCursor.newRow();
        rb.add("key", selection);
        rb.add("value", value);
        Log.v("query", selection);
        return mCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
}
