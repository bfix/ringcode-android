
//*********************************************************************
//*   PGMID.        NUMBER/CODE ASSIGNMENT REPOSITORY.                *
//*   AUTHOR.       BERND R. FIX   >Y<                                *
//*   DATE WRITTEN. 10/08/07.                                         *
//*   COPYRIGHT.    (C) BY BERND R. FIX. ALL RIGHTS RESERVED.         *
//*                 LICENSED MATERIAL - PROGRAM PROPERTY OF THE       *
//*                 AUTHOR. REFER TO COPYRIGHT INSTRUCTIONS.          *
//*   REMARKS.      REVISION HISTORY AT END OF FILE.                  *
//*********************************************************************

package org.hoi_polloi.android.ringcode;

///////////////////////////////////////////////////////////////////////
//Import external declarations

import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Repository for number/code assignments.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class Repository extends ContentProvider {

	//=================================================================
	/**
	 * <p>Inner class for repository entries.</p>
	 */
	public static final class NumberCode implements BaseColumns {

		//-------------------------------------------------------------
		// Repository identifier / URI
		//-------------------------------------------------------------

		/**
		 * <p>Repository identifier.</p>
		 */
		public static final String AUTHORITY = "ch.jeejah.android.ringcode.RingCode";
		/**
		 * <p>The content:// style URL for this table</p>
		 */
		public static final Uri CONTENT_URI = Uri.parse ("content://" + AUTHORITY + "/ringcode");


		//-------------------------------------------------------------
		//	MIME types
		//-------------------------------------------------------------
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of number/code assignments.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ringcode.entry";
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single number/code entry.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ringcode.entry";

		//-------------------------------------------------------------
		//	Entry data (columns)
		//-------------------------------------------------------------
		/**
		 * The telephone number of assignment
		 * <P>Type: TEXT</P>
		 */
		public static final String NUMBER = "number";
		/**
		 * The assigned code sequence
		 * <P>Type: TEXT</P>
		 */
		public static final String CODE = "code";
		/**
		 * Flag is assignment is active
		 * <P>Type: INTEGER</P>
		 */
		public static final String ACTIVE = "active";
		/**
		 * The name associated with the phone number
		 * <P>Type: TEXT</P>
		 */
		public static final String NAME = "name";

		//-------------------------------------------------------------
		/**
		 * <p>Do not allow instantiation outside of repository.</p>
		 */
		private NumberCode () {}
	}

	//=================================================================
	/**
	 * <p>Repository fields (columns) for query<p>
	 */
	public static final String[] PROJECTION = new String[] {
		Repository.NumberCode._ID,			// 0 - entry identifier
		Repository.NumberCode.NUMBER,		// 1 - phone number
		Repository.NumberCode.CODE,			// 2 - assigned morse code
		Repository.NumberCode.ACTIVE,		// 3 - active flag
		Repository.NumberCode.NAME,			// 4 - associated name
	};

	// column identifiers
	public static final int	COLUMN_NUMBER		= 1;
	public static final int	COLUMN_CODE			= 2;
	public static final int	COLUMN_ACTIVE		= 3;
	public static final int	COLUMN_NAME			= 4;

	/**
	 * The default sort order for this table
	 */
	public static final String DEFAULT_SORT_ORDER = "number DESC";

	// database parameters
	private static final String	DATABASE_NAME		= "ringcode.db";
	private static final int	DATABASE_VERSION	= 3;
	private static final String	TABLE_NAME			= "assignments";

	private static final int NUMBERCODE		= 1;
	private static final int NUMBERCODE_ID	= 2;

	private static HashMap<String, String> numbercodePrjMap;

	private static final UriMatcher uriMatcher;

	//=================================================================
	/**
	 * <p>Inner class for database access and management.</p>
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		//-------------------------------------------------------------
		/**
		 * <p>Instantiate helper class.</p>
		 * @param context Context - associated context
		 */
		DatabaseHelper (Context context) {
			super (context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		//-------------------------------------------------------------
		/**
		 * <p>Handle creation of database.</p>
		 * @param db SQLiteDatabase - database instance
		 */
		@Override
		public void onCreate (SQLiteDatabase db) {
			db.execSQL(
				"CREATE TABLE " + TABLE_NAME + " ("
					+ NumberCode._ID + " INTEGER PRIMARY KEY,"
					+ NumberCode.NUMBER + " TEXT,"
					+ NumberCode.CODE + " TEXT,"
					+ NumberCode.ACTIVE + " INTEGER,"
					+ NumberCode.NAME + " TEXT"
				+ ");");
		}

		//-------------------------------------------------------------
		/**
		 * <p>Handle database upgrade.</p> 
		 * @param db SQLiteDatabase - database instance
		 * @param oldVersion int - old database version
		 * @param newVersion int - new database version
		 */
		@Override
		public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			// drop old tables
			db.execSQL ("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
			// create new tables
			onCreate (db);
		}
	}

	//=================================================================
	/**
	 * <p>Database helper instance.</p>
	 */
	private DatabaseHelper dbHelper;

	//=================================================================
	/**
	 * <p>Constructor: Instantiate a new repository.</p>
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper (getContext());
		return true;
	}

	//=================================================================
	/**
	 * <p>Query repository for matching entries.</p>
	 * @param uri Uri - data identifier
	 * @param projection String[] - columns for query
	 * @param selection String - SQL WHERE clause
	 * @param selectionArgs Strring[] - selection parameters (prepared selection)
	 * @param sortOrder String - SORT clause
	 */
	@Override
	public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// get query builder
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables (TABLE_NAME);

		// prepare query
		switch (uriMatcher.match (uri)) {
			case NUMBERCODE:
				qb.setProjectionMap (numbercodePrjMap);
				break;

			case NUMBERCODE_ID:
				qb.setProjectionMap (numbercodePrjMap);
				qb.appendWhere (NumberCode._ID + "=" + uri.getPathSegments().get(1));
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty (sortOrder)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = qb.query (db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		c.setNotificationUri (getContext().getContentResolver(), uri);
		return c;
	}

	//=================================================================
	/**
	 * <p>Insert new entry into repository.</p>
	 * @param uri Uri - data identifier
	 * @param initialValues ContentValues - entry data
	 */
	@Override
	public Uri insert (Uri uri, ContentValues initialValues) {

		// Validate the requested uri
		if (uriMatcher.match (uri) != NUMBERCODE)
			throw new IllegalArgumentException("Unknown URI " + uri);

		// clone initial values.
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues (initialValues);
		} else {
			values = new ContentValues();
		}

		// set default values for missing data
		if (values.containsKey (NumberCode.NUMBER) == false)
			values.put (NumberCode.NUMBER, "");
		if (values.containsKey (NumberCode.CODE) == false)
			values.put (NumberCode.CODE, "");
		if (values.containsKey (NumberCode.ACTIVE) == false)
			values.put (NumberCode.ACTIVE, 0);
		if (values.containsKey (NumberCode.NAME) == false)
			values.put (NumberCode.NAME, "");

		// perform insert operation
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert (TABLE_NAME, NumberCode.CODE, values);
		if (rowId > 0) {
			Uri uriEntry = ContentUris.withAppendedId (NumberCode.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange (uriEntry, null);
			return uriEntry;
		}

		// failed to insert entry...
		throw new SQLException ("Failed to insert row into " + uri);
	}

	//=================================================================
	/**
	 * <p>Delete entry from repository.</p>
	 * @param uri Uri - data identifier
	 * @param where String - SQL WHERE clause
	 * @param whereArgs String[] - WHERE values (prepared clause)
	 */
	@Override
	public int delete (Uri uri, String where, String[] whereArgs) {

		// perform deletion
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
			case NUMBERCODE:
				count = db.delete (TABLE_NAME, where, whereArgs);
				break;

			case NUMBERCODE_ID:
				String id = uri.getPathSegments().get(1);
				count = db.delete (TABLE_NAME, NumberCode._ID + "=" + id
						+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// notify changed repository content.
		getContext().getContentResolver().notifyChange (uri, null);
		return count;
	}

	//=================================================================
	/**
	 * <p>Update entry in repository.</p>
	 * @param uri Uri - data identifier
	 * @param values ContentValues - entry data
	 * @param where String - SQL WHERE clause
	 * @param whereArgs String[] - WHERE values (prepared clause)
	 */
	@Override
	public int update (Uri uri, ContentValues values, String where, String[] whereArgs) {

		// perform update operation
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match (uri)) {
			case NUMBERCODE:
				count = db.update (TABLE_NAME, values, where, whereArgs);
				break;

			case NUMBERCODE_ID:
				String id = uri.getPathSegments().get(1);
				count = db.update (TABLE_NAME, values, NumberCode._ID + "=" + id
						+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// notify changed repository content.
		getContext().getContentResolver().notifyChange (uri, null);
		return count;
	}

	//=================================================================
	/**
	 * <p>Get MIME type for given URI.</p>
	 */
	@Override
	public String getType (Uri uri) {

		switch (uriMatcher.match (uri)) {
			case NUMBERCODE:
				return NumberCode.CONTENT_TYPE;

			case NUMBERCODE_ID:
				return NumberCode.CONTENT_ITEM_TYPE;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	//=================================================================
	/**
	 * <p>Static initialization of repository parameters.</p>
	 */
	static {
		
		uriMatcher = new UriMatcher (UriMatcher.NO_MATCH);
		uriMatcher.addURI (NumberCode.AUTHORITY, "ringcode", NUMBERCODE);
		uriMatcher.addURI (NumberCode.AUTHORITY, "ringcode/#", NUMBERCODE_ID);

		numbercodePrjMap = new HashMap<String, String>();
		numbercodePrjMap.put (NumberCode._ID,		NumberCode._ID);
		numbercodePrjMap.put (NumberCode.NUMBER,	NumberCode.NUMBER);
		numbercodePrjMap.put (NumberCode.CODE,		NumberCode.CODE);
		numbercodePrjMap.put (NumberCode.ACTIVE,	NumberCode.ACTIVE);
		numbercodePrjMap.put (NumberCode.NAME,		NumberCode.NAME);
	}
}
