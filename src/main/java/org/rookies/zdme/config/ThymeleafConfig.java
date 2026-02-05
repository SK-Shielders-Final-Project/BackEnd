package org.rookies.zdme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Bean
    public ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();

        // 핵심: 기존 /upload/ 설정을 무시하고, 표준 경로를 바라보게 함
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");

        // 설정
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // 개발 중엔 캐시 끄기 (수정사항 바로 반영)
        resolver.setCheckExistence(true);

        return resolver;
    }
}