package com.aircandi.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTime {

	public static final String	DATE_NOW_FORMAT_FILENAME			= "yyyyMMdd_HHmmss";
	private static final String	DATE_FORMAT_TIME_SINCE				= "MMM d";
	private static final String	DATE_FORMAT_TIME_SINCE_WITH_YEAR	= "MMM d, yyyy";
	private static final String	TIME_FORMAT_TIME_SINCE				= "h:mm";
	private static final String	AMPM_FORMAT_TIME_SINCE				= "a";

	public static String nowString(String pattern) {
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
		return sdf.format(cal.getTime());
	}

	public static Date nowDate() {
		final Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}

	public static String timeTill(Long dateOldMs, Long dateNewMs) {
		String since = interval(dateOldMs, dateNewMs);
		return since;
	}

	public static String intervalSince(Date dateOld, Date dateNew) {
		String since = interval(dateOld.getTime(), dateNew.getTime());
		if (!since.equals("now")) {
			since += " ago";
		}
		return since;
	}

	public static String timeSince(Long dateOldMs, Long dateNewMs) {
		String since = interval(dateOldMs, dateNewMs);
		if (!since.equals("now")) {
			since += " ago";
		}
		return since;
	}

	@SuppressWarnings("deprecation")
	public static String interval(Long oldDateMs, Long newDateMs) {

		final Date dateOld = new Date(oldDateMs);

		final Long diff = newDateMs - oldDateMs;

		if (diff <= 0) {
			return "now";
		}
		final int seconds = (int) (diff / 1000);
		final int minutes = (int) ((diff / 1000) / 60);
		final int hours = (int) ((diff / 1000) / (60 * 60));
		final int days = (int) ((diff / 1000) / (60 * 60 * 24));

		String interval = "now";
		if (days >= 1) {
			SimpleDateFormat datePart = new SimpleDateFormat(DATE_FORMAT_TIME_SINCE, Locale.US);
			if (dateOld.getYear() != DateTime.nowDate().getYear()) {
				datePart = new SimpleDateFormat(DATE_FORMAT_TIME_SINCE_WITH_YEAR, Locale.US);
				return datePart.format(dateOld.getTime());
			}
			else {
				final SimpleDateFormat timePart = new SimpleDateFormat(TIME_FORMAT_TIME_SINCE, Locale.US);
				final SimpleDateFormat ampmPart = new SimpleDateFormat(AMPM_FORMAT_TIME_SINCE, Locale.US);
				return datePart.format(dateOld.getTime()) + " at "
						+ timePart.format(dateOld.getTime())
						+ ampmPart.format(dateOld.getTime()).toLowerCase(Locale.US);
			}
		}
		else if (hours == 1) /* x hours x minutes ago */
		{
			interval = "1 hour";
		}
		else if (hours > 1) /* x hours x minutes ago */
		{
			interval = String.valueOf(hours) + " hours";
		}
		else if (minutes == 1) /* x hours x minutes ago */
		{
			interval = "1 minute";
		}
		else if (minutes > 1) /* x hours x minutes ago */
		{
			interval = String.valueOf(minutes) + " minutes";
		}
		else if (seconds == 1) /* 1 second ago */
		{
			interval = "1 second";
		}
		else if (seconds > 1) /* x hours x minutes ago */
		{
			interval = String.valueOf(seconds) + " seconds";
		}
		return interval;
	}
}
