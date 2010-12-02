package tinyos.yeti.refactoring.utilities;

import java.util.Collection;

public class StringUtil {
	public static String joinString(Collection<?> d, String delimiter) {
		StringBuffer ret = new StringBuffer();
		boolean first = true;
		for (Object o : d) {
			if (o == null) {
				o = "null";
			}
			if (first) {
				first = false;
			} else {
				ret.append(delimiter);
			}
			ret.append(o.toString());
		}
		return ret.toString();
	}

}
