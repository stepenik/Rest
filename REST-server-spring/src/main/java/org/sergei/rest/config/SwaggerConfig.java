package org.sergei.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.oauth2.provider.token.AccessTokenConverter.CLIENT_ID;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * @author Sergei Visotsky, 2018
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${security.oauth2.resource.accessTokenUri}")
    private String authServer;
    private static final String CLIENT_SECRET = "client_secret";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.sergei.rest.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(securitySchema()))
                .securityContexts(Collections.singletonList(securityContext()))
                .apiInfo(apiInfo());
    }

    /*@Bean
    public Docket customerApiV1() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("customer-api-v1")
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.sergei.rest.controller"))
                .paths(regex("/api/v1/customers*"))
                .build()
                .securitySchemes(Collections.singletonList(securitySchema()))
                .securityContexts(Collections.singletonList(securityContext()))
                .apiInfo(new ApiInfoBuilder().version("1.0").title("Customer API").description("REST API documentation for customers V1.0").build());
    }

    @Bean
    public Docket customerApiV2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("customer-api-v2")
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.sergei.rest.controller"))
                .paths(regex("/api/v2/customers*"))
                .build()
                .securitySchemes(Collections.singletonList(securitySchema()))
                .securityContexts(Collections.singletonList(securityContext()))
                .apiInfo(new ApiInfoBuilder().version("2.0").title("Customer API").description("REST API documentation for customers V2.0").build());
    }*/

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Sergei REST API documentation")
                .description("Methods offered by this REST API")
                .version("1.0")
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .contact(new Contact("", "", "sergei.visotsky@gmail.com"))
                .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .scopeSeparator("")
                .useBasicAuthenticationWithAccessCodeGrant(true)
                .build();
    }

    private SecurityScheme securitySchema() {
        List<AuthorizationScope> authorizationScopeList = new ArrayList<>();
        authorizationScopeList.add(new AuthorizationScope("read", "read all"));
        authorizationScopeList.add(new AuthorizationScope("trust", "trust all"));
        authorizationScopeList.add(new AuthorizationScope("write", "write all"));

        List<GrantType> grantTypes = new ArrayList<>();
        GrantType grantType = new ResourceOwnerPasswordCredentialsGrant(authServer);
        grantTypes.add(grantType);

        return new OAuth("oauth2schema", authorizationScopeList, grantTypes);
    }

    private List<SecurityReference> defaultAuth() {

        final AuthorizationScope[] authorizationScopes = new AuthorizationScope[3];
        authorizationScopes[0] = new AuthorizationScope("read", "read all");
        authorizationScopes[1] = new AuthorizationScope("trust", "trust all");
        authorizationScopes[2] = new AuthorizationScope("write", "write all");

        return Collections.singletonList(new SecurityReference("oauth2schema", authorizationScopes));
    }

    private SecurityContext securityContext() {
        return SecurityContext
                .builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant("/api/**"))
                .build();
    }
}