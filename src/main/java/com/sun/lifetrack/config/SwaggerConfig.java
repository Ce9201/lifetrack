package com.sun.lifetrack.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lifetrack API 文档")
                        .description("睡眠追踪系统接口文档")
                        .version("1.0")
                        .contact(new Contact()
                                .name("sun")
                                .email("1595330024@qq.com")));
    }
}
