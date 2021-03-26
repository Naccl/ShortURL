package top.naccl.dwz.service;

import org.springframework.scheduling.annotation.Async;

public interface UrlService {
	String getLongUrlByShortUrl(String shortURL);

	String saveUrlMap(String shortURL, String longURL, String originalURL);

	@Async
	void updateUrlViews(String shortURL);
}
