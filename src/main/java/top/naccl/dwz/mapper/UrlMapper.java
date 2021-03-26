package top.naccl.dwz.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.naccl.dwz.entity.UrlMap;

/**
 * @Description: 长短链接映射持久层接口
 * @Author: Naccl
 * @Date: 2021-03-22
 */
@Mapper
@Repository
public interface UrlMapper {
	String getLongUrlByShortUrl(String surl);

	int saveUrlMap(UrlMap urlMap);

	int updateUrlViews(String surl);
}
