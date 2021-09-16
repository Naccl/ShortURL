package top.naccl.dwz.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.naccl.dwz.annotation.AccessLimit;
import top.naccl.dwz.entity.R;
import top.naccl.dwz.util.IpAddressUtils;
import top.naccl.dwz.util.JacksonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 访问控制拦截器
 * @Author: Naccl
 * @Date: 2021-09-16
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
	@Autowired
	StringRedisTemplate redisTemplate;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
			//方法上没有访问控制的注解，直接通过
			if (accessLimit == null) {
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			String ip = IpAddressUtils.getIpAddress(request);
			String method = request.getMethod();
			String requestURI = request.getRequestURI();

			String redisKey = ip + ":" + method + ":" + requestURI;
			Object redisResult = redisTemplate.opsForValue().get(redisKey);
			Integer count = JacksonUtils.convertValue(redisResult, Integer.class);
			if (count == null) {
				//在规定周期内第一次访问，存入redis
				redisTemplate.opsForValue().increment(redisKey, 1);
				redisTemplate.expire(redisKey, seconds, TimeUnit.SECONDS);
			} else {
				if (count >= maxCount) {
					//超出访问限制次数
					response.setContentType("application/json;charset=utf-8");
					PrintWriter out = response.getWriter();
					R result = R.create(403, accessLimit.msg());
					out.write(JacksonUtils.writeValueAsString(result));
					out.flush();
					out.close();
					return false;
				} else {
					//没超出访问限制次数
					redisTemplate.opsForValue().increment(redisKey, 1);
				}
			}
		}
		return true;
	}
}
