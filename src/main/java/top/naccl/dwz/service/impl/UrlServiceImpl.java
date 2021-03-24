package top.naccl.dwz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import top.naccl.dwz.entity.UrlMap;
import top.naccl.dwz.mapper.UrlMapper;
import top.naccl.dwz.service.UrlService;
import top.naccl.dwz.util.HashUtils;

import java.util.Date;

/**
 * @Description: 长短链接映射业务层实现
 * @Author: Naccl
 * @Date: 2021-03-22
 */
@Service
public class UrlServiceImpl implements UrlService {
	@Autowired
	UrlMapper urlMapper;
	private static final String DUPLICATE = " *";

	@Override
	public String getLongUrlByShortUrl(String shortURL) {
		return urlMapper.getLongUrlByShortUrl(shortURL).replace(DUPLICATE, "");
	}

	@Override
	public String saveUrlMap(String shortURL, String longURL) {
		//在布隆过滤器中查找是否存在
		if (judgeExist()) {
			//存在，在长链接后加上指定字符串，重新hash
			longURL += DUPLICATE;
			shortURL = HashUtils.hashToBase62(longURL);
			shortURL = saveUrlMap(shortURL, longURL);
			return shortURL;
		} else {
			//不存在，直接存入数据库
			try {
				urlMapper.saveUrlMap(new UrlMap(shortURL, longURL, new Date()));
			} catch (Exception e) {
				if (e instanceof DuplicateKeyException) {
					//数据库已经存在此短链接，则可能是布隆过滤器误判，在长链接后加上指定字符串，重新hash
					longURL += DUPLICATE;
					shortURL = HashUtils.hashToBase62(longURL);
					shortURL = saveUrlMap(shortURL, longURL);
					return shortURL;
				} else {
					throw e;
				}
			}
		}
		return shortURL;
	}

	public boolean judgeExist() {
		return false;
	}
}
