package top.naccl.dwz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.naccl.dwz.entity.R;
import top.naccl.dwz.service.UrlService;
import top.naccl.dwz.util.HashUtils;
import top.naccl.dwz.util.UrlUtils;

/**
 * @Description:
 * @Author: Naccl
 * @Date: 2021-03-21
 */
@Controller
public class IndexController {
	@Autowired
	UrlService urlService;
	private static String host;

	@Value("${server.host}")
	public void setHost(String host) {
		this.host = host;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/generate")
	@ResponseBody
	public R generateShortURL(@RequestParam String longURL) {
		if (UrlUtils.checkURL(longURL)) {
			if (!longURL.startsWith("http")) {
				longURL = "http://" + longURL;
			}
			String shortURL = urlService.saveUrlMap(HashUtils.hashToBase62(longURL), longURL, longURL);
			return R.ok("请求成功", host + shortURL);
		}
		return R.create(400, "URL有误");
	}

	@GetMapping("/{shortURL}")
	public String redirect(@PathVariable String shortURL) {
		String longURL = urlService.getLongUrlByShortUrl(shortURL);
		if (longURL != null) {
			urlService.updateUrlViews(shortURL);
			//查询到对应的原始链接，302重定向
			return "redirect:" + longURL;
		}
		//没有对应的原始链接，直接返回首页
		return "redirect:/";
	}
}
