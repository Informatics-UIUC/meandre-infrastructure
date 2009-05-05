package org.meandre.core.utils;

import java.util.Calendar;

/** This class contains basic global constants used by the engine.
 *
 * @author Xavier Llor&agrave;
 */
public class Constants {

	/** The engine version */
    public final static String MEANDRE_RELEASE_TAG ="Sara";

    /** The version identifier */
    public final static String MEANDRE_VERSION = "1.4.5";

    /** The meandre qualifier based on the date */
	public static String MEANDRE_QUALIFIER = null;

	static {
		String sYear  = ""+Calendar.getInstance().get(Calendar.YEAR);
		String sMonth = ""+Calendar.getInstance().get(Calendar.MONTH);
		String sDay   = ""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

		if ( sMonth.length()<2 ) sMonth = "0"+sMonth;
		if ( sDay.length()<2 )   sDay = "0"+sDay;

		MEANDRE_QUALIFIER = "v"+sYear+sMonth+sDay;
	}

}
