package com.fast.framework.config;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fast.common.core.license.interceptor.LicenseCheckInterceptor;
import com.fast.framework.interceptor.RepeatSubmitInterceptor;
import com.fast.framework.utils.Constant;
import com.fast.framework.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fast.common.core.utils.ToolUtil;

/**
 * WebMvc配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Autowired
	private MyInterceptor myInterceptor;

    @Autowired
    private LockHandlerInterceptorAdapter lockHandlerInterceptorAdapter;

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Autowired
    private LicenseCheckInterceptor LicenseCheckInterceptor;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {


	    registry.addResourceHandler("/i18n/**").addResourceLocations("classpath:/i18n/");
        registry.addResourceHandler("/statics/**").addResourceLocations("classpath:/statics/");
        /** 本地文件上传路径 */
        registry.addResourceHandler(Constant.RESOURCE_urlPrefix + "/**").addResourceLocations("file:" + Global.getProfile() + "/");
        //registry.addResourceHandler(Constant.RESOURCE_urlPrefix + "/**").addResourceLocations("file:" + Global.getConifgFile() + "/");
	}

//    /**
//     * web跨域访问配置
//     */
//    @Override
//    public void addCorsMappings(CorsRegistry registry)
//    {
//        // 设置允许跨域的路径
//        registry.addMapping("/**")
//                // 设置允许跨域请求的域名
//                .allowedOrigins("*")
//                // 是否允许证书
//                .allowCredentials(true)
//                // 设置允许的方法
//                .allowedMethods("GET", "POST", "DELETE", "PUT")
//                // 设置允许的header属性
//                .allowedHeaders("*")
//                // 跨域允许时间
//                .maxAge(3600);
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(LicenseCheckInterceptor).addPathPatterns("/**");
    	registry.addInterceptor(myInterceptor).addPathPatterns("/**");
        registry.addInterceptor(lockHandlerInterceptorAdapter).addPathPatterns("/**");
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }
    
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();

		// 生成json时，将所有Long转换成String
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		objectMapper.registerModule(simpleModule);

		jackson2HttpMessageConverter.setObjectMapper(objectMapper);
		converters.add(0, jackson2HttpMessageConverter);
	}
	
	/**
	 * 配置国际化参数
	 */
	@Bean
    public LocaleResolver localeResolver()
    {
        return new NativeLocaleResolver();
    }

	
	protected static class NativeLocaleResolver implements LocaleResolver{
        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            String language = request.getParameter("_lang");
            Locale locale = Locale.SIMPLIFIED_CHINESE;
            if(ToolUtil.isNotEmpty(language)){
                String[] split = language.split("_");
                locale = new Locale(split[0],split[1]);
            }else {
            	//获取客户端Cookie 变换国际化参数
            	Cookie[] Cookies = request.getCookies();  
                for(int i =0;Cookies !=null && i<Cookies.length;i++){  
                    Cookie c = Cookies[i];
                    if(c.getName().equals("_lang")) {
                    	language =  c.getValue();
                    	String[] split = language.split("_");
                        locale = new Locale(split[0],split[1]);
                    }
                }
            }
            return locale;
        }

        @Override
        public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
//        	Cookie c1 = new Cookie("_lang",locale.getLanguage()); 
//        	c1.setMaxAge(60*60*24*7);
//            response.addCookie(c1);
        	
//        	System.out.println("//////////////////////////////////language:");
        }
    }
}
