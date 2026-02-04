package org.rookies.zdme.config; // 👈 본인 패키지 경로로 수정 필수

// [중요] 아래 Import들이 빠지면 에러가 납니다.
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter; // 필수
import org.springframework.http.converter.HttpMessageConverter;         // 필수
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // 필수

// SnakeYAML 관련 Import
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

// Java 기본 유틸
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class BoilerplateConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // [Trap] 개발자 몰래 '위험한 YAML 컨버터'를 등록
        converters.add(new SnakeYamlHttpMessageConverter());
        System.out.println("😈 [Boilerplate] Hidden Unsafe YAML Parser Activated.");
    }

    // 내부 클래스: 위험한 설정을 가진 컨버터
    public static class SnakeYamlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

        public SnakeYamlHttpMessageConverter() {
            // application/x-yaml 미디어 타입을 처리하겠다고 선언
            super(MediaType.parseMediaType("application/x-yaml"));
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            // 모든 클래스 타입을 지원한다고 거짓말
            return true;
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
                throws IOException, HttpMessageNotReadableException {

            // 🔥 [핵심 취약점] SnakeYAML 2.x의 보안 기능 무력화
            LoaderOptions options = new LoaderOptions();
            options.setTagInspector(tag -> true); // "모든 태그(클래스)를 허용해라" --> 이 설정 때문에 위험해짐

            // ⚠️ 여기서 공격자가 원하는 객체가 생성(new)되고 실행됨
            Yaml yaml = new Yaml(new Constructor(Object.class, options));
            return yaml.load(new InputStreamReader(inputMessage.getBody(), StandardCharsets.UTF_8));
        }

        @Override
        protected void writeInternal(Object o, HttpOutputMessage outputMessage)
                throws IOException, HttpMessageNotWritableException {
            // 응답을 줄 일은 없으므로 비워둠 (공격 성공 여부는 에러나 로그로 확인)
        }
    }
}