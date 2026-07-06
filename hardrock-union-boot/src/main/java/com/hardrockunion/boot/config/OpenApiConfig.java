package com.hardrockunion.boot.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(LoginUser.class);
    }

    @Bean
    public OpenAPI hardrockOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Hardrock Union API")
                .description("Hardrock Union platform and solution APIs")
                .version("1.0.0")
                .contact(new Contact().name("Hardrock Union")));
    }

    @Bean
    public GroupedOpenApi platformApi() {
        return GroupedOpenApi.builder()
            .group("platform")
            .pathsToMatch(
                "/api/platform/**",
                "/api/iam/**",
                "/api/{appCode}/auth/**",
                "/api/tenant-registry/**",
                "/api/{appCode}/tenant-registry/**",
                "/api/{appCode}/tenants/**",
                "/api/{appCode}/tenant-join-requests/**",
                "/api/{appCode}/onboarding/**",
                "/api/{appCode}/permissions/**",
                "/api/{appCode}/roles/**",
                "/api/{appCode}/users/**",
                "/api/{appCode}/departments/**"
            )
            .build();
    }

    @Bean
    public GroupedOpenApi wsgmApi() {
        return GroupedOpenApi.builder()
            .group("wsgm")
            .pathsToMatch("/api/wsgm/**")
            .build();
    }

    @Bean
    public GroupedOpenApi pmhubApi() {
        return GroupedOpenApi.builder()
            .group("pmhub")
            .pathsToMatch("/api/pmhub/**")
            .build();
    }

    @Bean
    public GroupedOpenApi primeloadMarketplaceApi() {
        return GroupedOpenApi.builder()
            .group("primeload-marketplace")
            .pathsToMatch("/api/primeload-marketplace/**")
            .build();
    }

    @Bean
    public GroupedOpenApi primeloadLogisticsApi() {
        return GroupedOpenApi.builder()
            .group("primeload-logistics")
            .pathsToMatch("/api/primeload-logistics/**")
            .build();
    }

    @Bean
    public GroupedOpenApi primeloadCloudWarehouseApi() {
        return GroupedOpenApi.builder()
            .group("primeload-cloud-warehouse")
            .pathsToMatch("/api/primeload-cloud-warehouse/**")
            .build();
    }

    @Bean
    public GroupedOpenApi primeloadDeliveryApi() {
        return GroupedOpenApi.builder()
            .group("primeload-delivery")
            .pathsToMatch("/api/primeload-delivery/**")
            .build();
    }

    @Bean
    public GroupedOpenApi healthApi() {
        return GroupedOpenApi.builder()
            .group("health")
            .pathsToMatch("/api/health")
            .build();
    }
}
