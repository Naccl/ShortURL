package top.naccl.dwz.service.impl;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.bloomfilter.BloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.naccl.dwz.entity.UrlMap;
import top.naccl.dwz.mapper.UrlMapper;
import top.naccl.dwz.service.UrlService;
import top.naccl.dwz.util.HashUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Description: 长短链接映射业务层实现
 * @Author: Naccl
 * @Date: 2021-03-22
 */
@Service
public class UrlServiceImpl implements UrlService {
	@Autowired
	UrlMapper urlMapper;
	@Autowired
	StringRedisTemplate redisTemplate;
	//自定义长链接防重复字符串
	private static final String DUPLICATE = "*";
	//最近使用的短链接缓存过期时间(分钟)
	private static final long TIMEOUT = 10;
	//创建布隆过滤器
	private static final BitMapBloomFilter FILTER = BloomFilterUtil.createBitMap(10);

	@Override
	public String getLongUrlByShortUrl(String shortURL) {
		//查找Redis中是否有缓存
		String longURL = redisTemplate.opsForValue().get(shortURL);
		if (longURL != null) {
			//有缓存，延迟缓存时间
			redisTemplate.expire(shortURL, TIMEOUT, TimeUnit.MINUTES);
			return longURL;
		}
		//Redis没有缓存，从数据库查找
		longURL = urlMapper.getLongUrlByShortUrl(shortURL);
		if (longURL != null) {
			//数据库有此短链接，添加缓存
			redisTemplate.opsForValue().set(shortURL, longURL, TIMEOUT, TimeUnit.MINUTES);
		}
		return longURL;
	}

	@Override
	public String saveUrlMap(String shortURL, String longURL, String originalURL) {
		//保留长度为1的短链接
		if (shortURL.length() == 1) {
			longURL += DUPLICATE;
			shortURL = saveUrlMap(HashUtils.hashToBase62(longURL), longURL, originalURL);
		}
		//在布隆过滤器中查找是否存在
		else if (FILTER.contains(shortURL)) {
			//存在，从Redis中查找是否有缓存
			String redisLongURL = redisTemplate.opsForValue().get(shortURL);
			if (redisLongURL != null && originalURL.equals(redisLongURL)) {
				//Redis有缓存，重置过期时间
				redisTemplate.expire(shortURL, TIMEOUT, TimeUnit.MINUTES);
				return shortURL;
			}
			//没有缓存，在长链接后加上指定字符串，重新hash
			longURL += DUPLICATE;
			shortURL = saveUrlMap(HashUtils.hashToBase62(longURL), longURL, originalURL);
		} else {
			//不存在，直接存入数据库
			try {
				urlMapper.saveUrlMap(new UrlMap(shortURL, originalURL));
				FILTER.add(shortURL);
				//添加缓存
				redisTemplate.opsForValue().set(shortURL, originalURL, TIMEOUT, TimeUnit.MINUTES);
			} catch (Exception e) {
				if (e instanceof DuplicateKeyException) {
					//数据库已经存在此短链接，则可能是布隆过滤器误判，在长链接后加上指定字符串，重新hash
					longURL += DUPLICATE;
					shortURL = saveUrlMap(HashUtils.hashToBase62(longURL), longURL, originalURL);
				} else {
					throw e;
				}
			}
		}
		return shortURL;
	}

	@Override
	public void updateUrlViews(String shortURL) {
		urlMapper.updateUrlViews(shortURL);
	}
}
