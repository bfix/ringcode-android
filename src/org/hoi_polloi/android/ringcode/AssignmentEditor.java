
//*********************************************************************
//*   PGMID.        CREATE AND MAINTAIN NUMBER/CODE ASSIGNMENT.       *
//*   AUTHOR.       BERND R. FIX   >Y<                                *
//*   DATE WRITTEN. 10/08/07.                                         *
//*   COPYRIGHT.    (C) BY BERND R. FIX. ALL RIGHTS RESERVED.         *
//*                 LICENSED MATERIAL - PROGRAM PROPERTY OF THE       *
//*                 AUTHOR. REFER TO COPYRIGHT INSTRUCTIONS.          *
//*   REMARKS.      REVISION HISTORY AT END OF FILE.                  *
//*********************************************************************

package org.hoi_polloi.android.ringcode;

///////////////////////////////////////////////////////////////////////
// Import external declarations

import java.util.Vector;

import org.hoi_polloi.android.ringcode.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Activity to edit a number/code assignment in the repository.</p>
 * <p>The activity can either be used to display an assignment
 * {@link Intent#ACTION_VIEW}, edit parameters {@link Intent#ACTION_EDIT}
 * or create a new assignment {@link Intent#ACTION_INSERT}.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class AssignmentEditor extends Activity implements DialogInterface.OnDismissListener {

	//=================================================================
	/*
	 * Constants:
	 */
	// Identifiers for menu items.
	private static final int REVERT_ID	= Menu.FIRST;
	private static final int DISCARD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID	= Menu.FIRST + 2;
	private static final int PICK_ID	= Menu.FIRST + 3;

	// The different distinct states the activity can be run in.
	private static final int STATE_EDIT		= 0;
	private static final int STATE_INSERT	= 1;

	// Identifiers for specific sub-activities
	private static final int PICK_CONTACT_FROM_LIST	= 1;
	private static final int DIALOG_PICK_NUMBER		= 2;

	//=================================================================
	/*
	 * Attributes:
	 */
	private int		state;			// current activity state (STATE_???)
	private Uri		uri;			// current data specification
	private Cursor	cursor;			// managed repository query

	//=================================================================
	/*
	 * UI elements:
	 */
	// phone number (EditText) and associated contact name (TextView)
	private static EditText	phoneNumber;
	private static String	origPhoneNumber;
	private static TextView	assocName;

	// assigned morse code (EditText)
	private static EditText	code;
	private static String	origCode;

	// current state (active/inactive) (CheckBox)
	private static CheckBox	mode;
	private static Boolean	origMode;

	// saved state (for revert action)
	private static final String ORIG_PHONE = "origPhoneNumber";
	private static final String ORIG_CODE  = "origCode";
	private static final String ORIG_MODE  = "origMode";
	private static boolean keepData = false;

	//=================================================================
	/**
	 * <p>Create activity instance.</p>
	 * @param savedInstanceState Bundle - previous instance data
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);

		// Do some setup based on the action being performed.
		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (Intent.ACTION_EDIT.equals (action)) {
			// Request to edit: set that state, and the data being edited.
			state = STATE_EDIT;
			uri = intent.getData();
		}
		else if (Intent.ACTION_INSERT.equals (action)) {
			// Request to insert: set that state, and create a new entry
			// in the container.
			state = STATE_INSERT;
			uri = getContentResolver().insert (intent.getData(), null);
			if (uri == null) {
				Debug.log ("Failed to insert new entry into " + getIntent().getData());
				finish();
				return;
			}

			// The new entry was created, so assume all will end well and
			// set the result to be returned.
			setResult (RESULT_OK, (new Intent()).setAction (uri.toString()));
		}
		else {
			Debug.log ("Unknown action, exiting");
			finish();
			return;
		}

		// setup layout of editor
		setContentView (R.layout.editor);
		// grab editable views (editor elements)
		phoneNumber = (EditText) findViewById (R.id.te_phone);
		assocName = (TextView) findViewById (R.id.edit_name);
		code = (EditText) findViewById (R.id.te_code);
		mode = (CheckBox) findViewById (R.id.cb_active);

		// perform query
		cursor = managedQuery (uri, Repository.PROJECTION, null, null, null);

		// If an instance of this activity had previously stopped, we can
		// get the original text it started with.
		if (savedInstanceState != null) {
			origPhoneNumber = savedInstanceState.getString (ORIG_PHONE);
			origCode = savedInstanceState.getString (ORIG_CODE);
			origMode = savedInstanceState.getBoolean (ORIG_MODE, true);
		}
	}

	//=================================================================
	/**
	 * <p>Handle click in editor views.</p>
	 * @param view View - clicked element
	 */
	public void onClick (View view) {
		
		switch (view.getId()) {
		
			// play morse code once
			case R.id.btn_play: {
				int volume = Setup.getVolume();
				int speed = Setup.getSpeed();
				Morse beeper = new Morse (speed, volume);
				beeper.sendCode (code.getText().toString());
			} break;
			
			// lookup contact info for telephone number and
			// display associated contact name
			case R.id.te_phone: {
				String num = phoneNumber.getText().toString();
				phoneNumber.setText (num);
				assocName.setText (lookupContact (num));
			} break;

			// toggle active mode
			case R.id.cb_active: {
				//CheckBox cb = (CheckBox) findViewById (R.id.cb_active);
				//boolean on = cb.isChecked();
				//Debug.log ("RB_State is " + (on ? "ON" : "OFF"));
			} break;
		}
	}

	//=================================================================
	/**
	 * <p>Resume activity after pause/stop.</p>
	 */
	@Override
	protected void onResume() {
		super.onResume();

		if (cursor != null) {
			// Make sure we are at the one and only row in the cursor.
			cursor.moveToFirst();

			// Modify our overall title depending on the mode we are running in.
			if (state == STATE_EDIT) {
				setTitle (getText (R.string.title_edit));
			} else if (state == STATE_INSERT) {
				setTitle (getText(R.string.title_create));
			}

			// set data from repository 
			if (keepData)
				// no: keep our current data (not updated yet)
				keepData = false;
			else {
				String text = cursor.getString (Repository.COLUMN_NUMBER);
				if (origPhoneNumber == null)
					origPhoneNumber = text;
				phoneNumber.setTextKeepState (text);
				assocName.setText (lookupContact (text));

				text = cursor.getString (Repository.COLUMN_CODE);
				if (origCode == null)
					origCode = text;
				code.setTextKeepState (text);

				boolean m = (cursor.getInt (Repository.COLUMN_ACTIVE) != 0);
				if (origMode == null)
					origMode = m;
				mode.setChecked (m);
			}
		}
	}

	//=================================================================
	/**
	 * <p>Save current state.</p>
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		// Save away the original values, so we still have it if the activity
		// needs to be killed while paused.
		outState.putString (ORIG_PHONE, origPhoneNumber);
		outState.putString (ORIG_CODE, origCode);
		outState.putBoolean (ORIG_MODE, origMode);
	}

	//=================================================================
	/**
	 * <p>Pause activity.</p>
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// The user is going somewhere else, so make sure their current
		// changes are safely saved away in the provider.  We don't need
		// to do this if only editing.
		if (cursor != null) {
			// get current entry values.
			String pn = phoneNumber.getText().toString();
			String cd = code.getText().toString();
			boolean md = mode.isChecked();

			// If this activity is finished, and there is no data, then we
			// do something a little special: simply delete the entry.
			// Note that we do this both for editing and inserting...  it
			// would be reasonable to only do it when inserting.
			if (isFinishing() && (pn.length() == 0 || cd.length() == 0)) {
				setResult (RESULT_CANCELED);
				deleteEntry ();
			}
			else {
				// Get out updates into the provider.
				ContentValues values = new ContentValues();
				values.put (Repository.NumberCode.NUMBER, pn);
				values.put (Repository.NumberCode.CODE,   cd);
				values.put (Repository.NumberCode.ACTIVE, md ? 1 : 0);
				values.put (Repository.NumberCode.NAME,   lookupContact (pn));

				// Commit all of our changes to persistent storage. When the update completes
				// the content provider will notify the cursor of the change, which will
				// cause the UI to be updated.
				getContentResolver().update (uri, values, null, null);
			}
		}
	}

	//=================================================================
	//	Menu handling
	//=================================================================
	/**
	 * <p>Create menu based on the state of the activity.</p>
	 * @param menu Menu - menu to be assembled
	 * @return boolean - successful operation?
	 */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);

		if (state == STATE_EDIT) {
			// Build the menus that are shown when editing.
			menu.add(0, REVERT_ID, 0, R.string.menu_revert)
				.setShortcut('0', 'r')
				.setIcon(android.R.drawable.ic_menu_revert);
			menu.add(0, DELETE_ID, 0, R.string.menu_delete)
				.setShortcut('1', 'd')
				.setIcon(android.R.drawable.ic_menu_delete);
		}
		else {
			// Build the menus that are shown when inserting.
			menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
				.setShortcut('0', 'd')
				.setIcon(android.R.drawable.ic_menu_delete);
		}

		// we can always pick a contact
		menu.add (0, PICK_ID, 0, R.string.menu_pick)
			.setShortcut('2', 'p')
			.setIcon (android.R.drawable.ic_menu_more);

		return true;
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Handle selection of menu entry.</p>
	 * @param item MenuItem - selected menu item
	 * @return boolean - event handled?
	 */
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {

		// Handle all of the possible menu actions.
		switch (item.getItemId()) {

			// delete current entry
			case DELETE_ID:
				deleteEntry();
				finish();
				break;

			// discard changes to current entry
			case DISCARD_ID:
				cancelEntry();
				break;

			// reset to old values
			case REVERT_ID:
				cancelEntry();
				break;

			// pick contact to set phone number
			case PICK_ID:
				pickContact();
				break;
		}
		// let the base class handle the rest...
		return super.onOptionsItemSelected (item);
	}

	//=================================================================
	//	Entry handling
	//=================================================================
	/**
	 * <p>Take care of canceling work on an entry.  Deletes the entry if we
	 * had created it, otherwise reverts to the original.</p>
	 */
	private final void cancelEntry () {

		if (cursor != null) {
			if (state == STATE_EDIT) {
				// put the original assignment back into the database
				cursor.close();
				cursor = null;
				ContentValues values = new ContentValues();
				values.put (Repository.NumberCode.NUMBER, origPhoneNumber);
				values.put (Repository.NumberCode.CODE,   origCode);
				values.put (Repository.NumberCode.ACTIVE, origMode ? 1 : 0);
				getContentResolver().update (uri, values, null, null);
			}
			else if (state == STATE_INSERT) {
				// We inserted an empty note, make sure to delete it
				deleteEntry ();
			}
		}
		// quit this activity.
		setResult (RESULT_CANCELED);
		finish();
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Take care of deleting a entry.</p>
	 */
	private final void deleteEntry () {

		if (cursor != null) {
			// delete entry in repository.
			cursor.close();
			cursor = null;
			getContentResolver().delete (uri, null, null);

			// clear editor views.
			phoneNumber.setText ("");
			code.setText ("");
			mode.setChecked (false);
		}
	}

	//=================================================================
	//	Contact handling
	//=================================================================
	/**
	 * <p>lookup contact associated with given phone number.</p>
	 * @param number String - phone number
	 * @return String - associated contact name (or null)
	 */
	private String lookupContact (String number) {
		
		Uri contactUri = null;
		Cursor c = null;
		try {
			// check for valid number
			if (number != null && number.length() > 0) {
				// query the repository
				c = getContentResolver().query (
					Uri.withAppendedPath (PhoneLookup.CONTENT_FILTER_URI, Uri.encode (number)),
					new String[] { PhoneLookup.DISPLAY_NAME },
					null, null, null
				);
				// use first match
				if (c.moveToFirst()) {
					int idx = c.getColumnIndex (PhoneLookup.DISPLAY_NAME);
					return c.getString (idx);
				}
			}
		}
		catch (Exception e) {
			Debug.log ("Failed to access contact database: " + contactUri);
		}
		finally {
			// close query on exit
			if (c != null)
				c.close();
		}
		// return "unknown" if no contact was found.
		return getString (R.string.unknown);
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Pick contact from list.</p>
	 */
	private void pickContact () {

		// start "pick" activity.
		Intent intent = new Intent (Intent.ACTION_PICK, Contacts.CONTENT_URI);
		startActivityForResult (intent, PICK_CONTACT_FROM_LIST);
	}
	//-----------------------------------------------------------------
	/*
	 * pick attributes:
	 */
	private Vector<String> numberList = null;
	private String selectedNumber = null;

	//-----------------------------------------------------------------
	/**
	 * <p>Pick activity ended: handle pick result.</p>
	 * @param requestCode int - activity identifier
	 * @param resultCode int - activity result status
	 * @param data Intent - associated data
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult (requestCode, resultCode, data);

		// handle "pick" activity...
		if (requestCode == PICK_CONTACT_FROM_LIST) {
			// if picking was successful...
			if (resultCode == RESULT_OK) {

				// get the selected contact ID and query all phone
				// numbers associated with the contact.
				String id = data.getData().getLastPathSegment();
				Cursor c = getContentResolver().query (
					Phone.CONTENT_URI,
					new String[] { Phone.NUMBER },
					Phone.CONTACT_ID + "=?",
					new String[] { id },
					null
				);
				// assemble list of phone numbers.
				numberList = new Vector<String>();
				try {
					c.moveToFirst();
					do {
						String number = c.getString (0);
						numberList.add (number);
					} while (c.moveToNext());
				}
				catch (Exception e) {
					Debug.log ("Failed: Pick contact number");
				}
				finally {
					// close query on exit
					c.close();
				}

				// handle number list.
				int count = numberList.size();
				if (count == 0) {
					// no number available.
					Toast.makeText (this, R.string.no_phone, Toast.LENGTH_LONG).show();
				}
				else if (count == 1) {
					// use the one and only number
					String number = numberList.firstElement();
					phoneNumber.setText (number);
					String name = lookupContact (number);
					assocName.setText (name);

					// make sure new data is used
					keepData = true;
				}
				else {
					// multiple numbers associated with the selected contact
					//Toast.makeText (this, "Multiple numbers available!", Toast.LENGTH_LONG).show();
					showDialog (DIALOG_PICK_NUMBER);
				}
			}
		}
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Create "pick number" dialog.</p>
	 * @param int id - dialog identifier
	 * @return Dialog - new dialog instance
	 */
	protected Dialog onCreateDialog (int id) {

		Dialog dialog = null;;
		switch (id) {
			case DIALOG_PICK_NUMBER: {
				
				// create new "pick number" dialog...
				int count = numberList.size();
				final CharSequence[] items = new CharSequence [count];
				for (int n = 0; n < count; n++)
					items[n] = numberList.elementAt (n);

				AlertDialog.Builder builder = new AlertDialog.Builder (this);
				builder.setTitle (getText (R.string.pick_num));
				builder.setItems (items, new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int item) {
						selectedNumber = items[item].toString();
					}
				});
				dialog = builder.create();
				dialog.setOnDismissListener (this);
			} break;
		}
		// return dialog instance
		return dialog;
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Handle termination of dialog.</p>
	 * @param dialog DialogInterface - reference to dialog instance
	 */
	public void onDismiss (DialogInterface dialog) {
		
		// use selected number (if available)
		if (selectedNumber != null) {
			phoneNumber.setText (selectedNumber);
			String name = lookupContact (selectedNumber);
			assocName.setText (name);
		}
	}
}
