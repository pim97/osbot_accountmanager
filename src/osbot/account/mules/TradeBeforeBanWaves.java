package osbot.account.mules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import osbot.database.DatabaseUtilities;

public class TradeBeforeBanWaves {

	public static void trade2() {
		String timeToStart = "15:20:00";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		SimpleDateFormat formatOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		Date dateToStart = null;

		Date tomorrow = new Date();
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));

		try {
			dateToStart = format.parse(formatOnlyDay.format(now) + " at " + timeToStart);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			Date atTheMoment = new Date();
			long diff = dateToStart.getTime() - atTheMoment.getTime();

			System.out.println("diff 1: " + diff);
			if (diff < 0 && diff > -600_000) {

				DatabaseUtilities.updateAtASpecificTimeToMule();
				c.setTime(tomorrow);
				c.add(Calendar.DATE, 1);
				tomorrow = c.getTime();
				try {
					dateToStart = format.parse(formatOnlyDay.format(tomorrow) + " at " + timeToStart);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("end: " + diff);
			}

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void trade1() {

		String timeToStart = "07:20:00";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		SimpleDateFormat formatOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		Date dateToStart = null;

		Date tomorrow = new Date();
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));

		try {
			dateToStart = format.parse(formatOnlyDay.format(now) + " at " + timeToStart);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			Date atTheMoment = new Date();
			long diff = dateToStart.getTime() - atTheMoment.getTime();

			System.out.println("diff 1: " + diff);
			if (diff < 0 && diff > -600_000) {

				DatabaseUtilities.updateAtASpecificTimeToMule();
				c.setTime(tomorrow);
				c.add(Calendar.DATE, 1);
				tomorrow = c.getTime();
				try {
					dateToStart = format.parse(formatOnlyDay.format(tomorrow) + " at " + timeToStart);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("end: " + diff);
			}

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
