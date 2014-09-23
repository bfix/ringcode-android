
//*********************************************************************
//*   PGMID.        CONNECTOR BETWEEN ASSIGNMENT DB AND LIST VIEW.    *
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

import org.hoi_polloi.android.ringcode.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Connector between assignment database and list view.</p>
 * <p>Update (post-process) generated item view based on item logic.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class AssignmentAdapter extends SimpleCursorAdapter {
	
	//=================================================================
	/**
	 * <p>Constructor: Instantiate a new adapter.</p>
	 * @param parent ListActivity - associated list view
	 * @param cur Cursor - associate managed database query
	 */
	public AssignmentAdapter (ListActivity parent, Cursor cur) {
		super (
			parent, R.layout.list_item, cur,
			// columns for item display
			new String[] {
				Repository.NumberCode.NUMBER,
				Repository.NumberCode.NAME,
				Repository.NumberCode.CODE
			},
			// associated view elements (identifiers)
			new int[] {
				R.id.txt_number,
				R.id.txt_name,
				R.id.txt_code
			}
		);
	}
	
	//=================================================================
	/**
	 * <p>Post-process item view based on item logic.</p>
	 * @param index int - database index for display
	 * @param cell View - generated view (from base class)
	 * @param parent ViewGroup - associated view group
	 * @return View - item view
	 */
	@Override
	public View getView (int index, View cell, ViewGroup parent) {

		// let the base class generate a view instance we can work on
		View v = super.getView (index, cell, parent);

		// get database entry (item for display)
		Cursor c = (Cursor) getItem (index);
		
		// switch list icon based on item state (active/inactive)
		ImageView ui_icon = (ImageView) v.findViewById (R.id.icon);
		if (c.getInt (Repository.COLUMN_ACTIVE) != 0)
			ui_icon.setImageResource (R.drawable.list_mode_on);
		else
			ui_icon.setImageResource (R.drawable.list_mode_off);
		
		return v;
	}
}
