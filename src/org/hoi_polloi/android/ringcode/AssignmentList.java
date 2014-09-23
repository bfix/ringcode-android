
//*********************************************************************
//*   PGMID.        DISPLAY LIST OF ASSIGNMENTS IN REPOSITORY.        *
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

import org.hoi_polloi.android.ringcode.R;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Display list of number/code assignments as stored in the
 * repository.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class AssignmentList extends ListActivity {

	//=================================================================
	/*
	 * Menu constants
	 */
	public static final int MENU_ITEM_DELETE	= Menu.FIRST;		// delete selected entry
	public static final int MENU_ITEM_PLAY		= Menu.FIRST + 1;	// play code of selected entry
	public static final int MENU_ITEM_TOGGLE	= Menu.FIRST + 2;	// Toggle "active" state
	public static final int MENU_ITEM_INSERT	= Menu.FIRST + 3;	// insert new item (no selection required)

	//-----------------------------------------------------------------
	/**
	 * <p>menu title ("number" column of selected entry)</p>
	 */
	private static final int COLUMN_INDEX_NUMBER = 1;

	//=================================================================
	/**
	 * <p>Show list of number/code assignments.</p>
	 * @param savedInstanceState Bundle - activity state data
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setDefaultKeyMode (DEFAULT_KEYS_SHORTCUT);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (intent.getData() == null)
			intent.setData (Repository.NumberCode.CONTENT_URI);

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener (this);

		// Perform a managed query.
		Cursor cursor = managedQuery (intent.getData(), Repository.PROJECTION, null, null, Repository.DEFAULT_SORT_ORDER);

		// Used to map notes entries from the database to views
		setListAdapter (new AssignmentAdapter (this, cursor));
	}

	//=================================================================
	/**
	 * <p>List entry is selected.</p>
	 * @param l ListView - associated view
	 * @param v View - associated item view
	 * @param position int - position of selected entry
	 * @param id long - row id of selection in repository
	 */
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id) {

		Uri uri = ContentUris.withAppendedId (getIntent().getData(), id);
		String action = getIntent().getAction();

		if (Intent.ACTION_PICK.equals (action) || Intent.ACTION_GET_CONTENT.equals (action)) {
			// The caller is waiting for us to return an entry selected by the user.
			setResult (RESULT_OK, new Intent().setData (uri));
		}
		else {
			// Launch activity to view/edit the currently selected item
			startActivity (new Intent(Intent.ACTION_EDIT, uri));
		}
	}

	//=================================================================
	//	Menu handling
	//=================================================================
	/**
	 * <p>Create options menu.</p>
	 * @param menu Menu - associated menu
	 * @return boolean - successful operation?
	 */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu (menu);

		// default menu entry: insert new assignment
		menu.add (Menu.NONE, MENU_ITEM_INSERT, Menu.NONE, R.string.menu_insert)
			.setShortcut('3', 'a')
			.setIcon (android.R.drawable.ic_menu_add);

		// report success.
		return true;
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Build options menu.</p>
	 * @param menu Menu - associated menu
	 * @return boolean - successful operation?
	 */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// check for available (selected) entries...
		if (getListAdapter().getCount() > 0) {
			// get selected entry
			Uri uri = ContentUris.withAppendedId (getIntent().getData(), getSelectedItemId());

			// build menu for selected entry:
			MenuItem[] items = new MenuItem [1];
			Intent[] ops = new Intent [1];
			ops[0] = new Intent (Intent.ACTION_EDIT, uri);

			Intent intent = new Intent (null, uri);
			intent.addCategory (Intent.CATEGORY_ALTERNATIVE);
			menu.addIntentOptions (Menu.CATEGORY_ALTERNATIVE, 0, 0, null, ops, intent, 0, items);

			// set shortcuts and icons for menu entries.
			if (items[0] != null) {
				items[0].setShortcut ('1', 'e');
				items[0].setIcon (android.R.drawable.ic_menu_edit);
			}
		}
		else
			// no additional menu entries if no entries are available
			menu.removeGroup (Menu.CATEGORY_ALTERNATIVE);

		// report success
		return true;
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Handle selection of option.</p>
	 * @param item MenuItem - selected menu item
	 * @return boolean
	 */
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {

		// handle our options (INSERT)
		switch (item.getItemId()) {
			case MENU_ITEM_INSERT:
				Intent intent = new Intent (Intent.ACTION_INSERT, getIntent().getData());
				startActivity (intent);
				return true;
		}
		// get it handled...
		return super.onOptionsItemSelected(item);
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Create context menu.</p>
	 * @param menu ContextMenu
	 * @param view View
	 * @param menuInfo ContextMenuInfo
	 */
	@Override
	public void onCreateContextMenu (ContextMenu menu, View view, ContextMenuInfo menuInfo) {

		// cast to list-specific menu info
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		}
		catch (ClassCastException e) {
			Debug.log ("bad menuInfo: " + e);
			return;
		}

		// setup the menu
		Cursor cursor = (Cursor) getListAdapter().getItem (info.position);
		if (cursor == null)
			return;
		menu.setHeaderTitle (cursor.getString (COLUMN_INDEX_NUMBER));
		menu.add (Menu.NONE, MENU_ITEM_DELETE, Menu.NONE, R.string.menu_delete);
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Context menu entry is selected...</p>
	 * @param item MenuItem
	 * @return boolean - selection handled?
	 */
	@Override
	public boolean onContextItemSelected (MenuItem item) {

		// cast to list-specific menu info
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		}
		catch (ClassCastException e) {
			Debug.log ("bad menuInfo: " + e);
			return false;
		}

		// handle selected menu entry
		switch (item.getItemId()) {
			case MENU_ITEM_DELETE: {
				Uri entry = ContentUris.withAppendedId (getIntent().getData(), info.id);
				getContentResolver().delete (entry, null, null);
				return true;
			}
		}
		// selection not handled.
		return false;
	}
}
