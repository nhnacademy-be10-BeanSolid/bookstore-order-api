package com.nhnacademy.bookstoreorderapi.common.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Order-API Swagger")
                .description("BeanSolid-BookStore의 주문에 관한 REST API")
                .version("1.0.0");
    }

    @Bean
    @ConditionalOnMissingBean(ModelConverter.class)
    public ModelConverter modelConverter() {
        return (type, context, chain) -> {
            if (type.getType() instanceof Class) {
                Class<?> cls = (Class<?>) type.getType();
                
                // Spring 관련 클래스들을 제외
                if (cls.getName().startsWith("org.springframework")) {
                    return null;
                }
                
                // ApplicationContext, BeanFactory 등 Spring 컨테이너 관련 클래스 제외
                if (cls.getName().contains("ApplicationContext") ||
                    cls.getName().contains("BeanFactory") ||
                    cls.getName().contains("ConfigurableApplicationContext") ||
                    cls.getName().contains("WebApplicationContext")) {
                    return null;
                }
            }
            
            return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
        };
    }
}
