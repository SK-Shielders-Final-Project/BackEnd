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


//// 보안 적용 코드
//package org.rookies.zdme.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpInputMessage;
//import org.springframework.http.HttpOutputMessage;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.AbstractHttpMessageConverter;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.http.converter.HttpMessageNotWritableException;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import org.yaml.snakeyaml.LoaderOptions;
//import org.yaml.snakeyaml.Yaml;
//import org.yaml.snakeyaml.constructor.Constructor;
//// import org.yaml.snakeyaml.constructor.SafeConstructor; // 더 강력한 보안이 필요하면 사용
//
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@Configuration
//public class BoilerplateConfig implements WebMvcConfigurer {
//
//    @Override
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//        converters.add(new SnakeYamlHttpMessageConverter());
//        System.out.println("🛡️ [SecureConfig] Safe YAML Parser Activated.");
//    }
//
//    public static class SnakeYamlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
//
//        public SnakeYamlHttpMessageConverter() {
//            super(MediaType.parseMediaType("application/x-yaml"));
//        }
//
//        @Override
//        protected boolean supports(Class<?> clazz) {
//            return true;
//        }
//
//        @Override
//        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
//                throws IOException, HttpMessageNotReadableException {
//
//            // [보안 조치 1] LoaderOptions 기본값 사용
//            // SnakeYAML 2.x부터는 기본적으로 Global Tag(!!)를 허용하지 않습니다.
//            // setTagInspector(tag -> true) <-- 이 위험한 코드를 삭제했습니다.
//            LoaderOptions options = new LoaderOptions();
//
//            // [보안 조치 2] 타입 안전성 강화 (Type Safety)
//            // Constructor(Object.class) 대신, 컨트롤러가 요청한 구체적인 DTO 클래스(clazz)를 지정합니다.
//            // 이렇게 하면 공격자가 엉뚱한 ScriptEngineManager를 생성하려 해도 타입 불일치로 막힙니다.
//            Constructor constructor = new Constructor(clazz, options);
//
//            // [참고] 만약 DTO 매핑 없이 순수 데이터(Map, List)만 받는다면 아래처럼 SafeConstructor를 쓰세요.
//            // Constructor constructor = new SafeConstructor(options);
//
//            Yaml yaml = new Yaml(constructor);
//
//            return yaml.load(new InputStreamReader(inputMessage.getBody(), StandardCharsets.UTF_8));
//        }
//
//        @Override
//        protected void writeInternal(Object o, HttpOutputMessage outputMessage)
//                throws IOException, HttpMessageNotWritableException {
//            // 쓰기 로직 생략
//        }
//    }
//}