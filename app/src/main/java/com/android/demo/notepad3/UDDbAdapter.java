package edu.udel.udmap;

/**
 * Created by Holly on 4/29/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UDDbAdapter {

    public static final String KEY_CODE = "code";
    public static final String KEY_NAME = "name";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "UDDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Create SQL Statement
     */
    private static final String DATABASE_CREATE =
            "create table positions (_id integer primary key autoincrement, "
                    + "code text not null, name text not null, latitude real not null, longitude real not null );";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "positions";
    private static int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public UDDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the data database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public UDDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param code the building code of the data
     * @param name the name of the building
     * @param latitude
     * @param longitude
     * @return rowId or -1 if failed
     */

    public long createData(String code, String name, String latitude, String longitude) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CODE, code);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteData(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all buildings in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllData() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CODE,
                KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the data that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchData(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_CODE, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE}, KEY_ROWID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the data using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of note to update
     * @param code value to set data code to
     * @param name value to set building name to
     * @param latitude value to set latitude to
     * @param longitude value to set longitude to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateData(long rowId, String code, String name, String latitude, String longitude) {
        ContentValues args = new ContentValues();
        args.put(KEY_CODE, code);
        args.put(KEY_NAME, name);
        args.put(KEY_LATITUDE, latitude);
        args.put(KEY_LONGITUDE, longitude);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

}