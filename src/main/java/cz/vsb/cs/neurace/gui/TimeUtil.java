package cz.vsb.cs.neurace.gui;

/**
 * 
 */
public class TimeUtil {

        public static String genTime(int time) {
		int ms = (time % 1000);
		int s = (time / 1000) % 60;
		int m = (time / 60000) % 60;
		int h = (time / 3600000);
                if(h == 0)
                    return oo(m) + ":" + oo(s) + "." + ms;
                else
                    return oo(h) + ":" + oo(m) + ":" + oo(s) + "." + ms;
	}

	/**
	 * Změní číslo na řetězec nejméně o dvou znacích.
	 *
	 * @param n
	 * @return
	 */
	private static String oo(int n) {
		if (n < 10) {
			return "0" + n;
		} else {
			return String.valueOf(n);
		}
	}
}
