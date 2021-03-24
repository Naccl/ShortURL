package top.naccl.dwz.service;

public interface UrlService {
	String getLongUrlByShortUrl(String shortURL);

	String saveUrlMap(String shortURL, String longURL);
}
