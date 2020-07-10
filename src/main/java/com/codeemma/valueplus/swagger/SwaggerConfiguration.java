package com.codeemma.valueplus.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket documentation() {
        return new Docket(DocumentationType.SPRING_WEB)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.codeemma"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(appInfo())
                .securitySchemes(Arrays.asList(apiKey()));
    }

    private ApiKey apiKey() {
        return new ApiKey("authkey", "Authorization", "header");
    }

    private ApiInfo appInfo() {
        return new ApiInfoBuilder().title("REST API")
                .description("The rest api demo for book rating").termsOfServiceUrl("")
                .contact(new Contact("Emmanuel Olayinka", "", "eakinolayinka@gmail.com"))
                .license("Apache License Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .version("0.0.1")
                .build();
    }
}
