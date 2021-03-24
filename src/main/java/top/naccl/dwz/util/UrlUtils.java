package top.naccl.dwz.util;

import java.util.regex.Pattern;

/**
 * @Description: URL校验
 * @Author: Naccl
 * @Date: 2021-03-24
 */
public class UrlUtils {
	private static final Pattern URL_REG = Pattern.compile("^(((ht|f)tps?):\\/\\/)?[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?$");

	public static boolean checkURL(String url) {
		return URL_REG.matcher(url).matches();
	}
}
