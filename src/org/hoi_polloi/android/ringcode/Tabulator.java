
//*********************************************************************
//*   PGMID.        MAIN/START-UP TABULATED ACTIVITY.                 *
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
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Main (start-up) activity with tabulated views.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class Tabulator extends TabActivity {

	//=================================================================
	/**
	 * <p>Constructor: Instantiate start-up activity.</p>
	 * @param savedInstanceState Bundle - saved activity state
	 */
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setContentView (R.layout.main);

		Resources res = getResources();
		TabHost tabHost = getTabHost();

		// create assignment list view
		Intent intent = new Intent().setClass (this, AssignmentList.class);
		TabHost.TabSpec spec = tabHost.newTabSpec ("list")
			.setIndicator ("Assignments", res.getDrawable (R.drawable.tab_list))
			.setContent (intent);
		tabHost.addTab (spec);

		// create configuration view
		intent = new Intent().setClass (this, Setup.class);
		spec = tabHost.newTabSpec ("config")
			.setIndicator ("Config", res.getDrawable(R.drawable.tab_config))
			.setContent (intent);
		tabHost.addTab (spec);

		//tabHost.setCurrentTab (1);
	}
}
