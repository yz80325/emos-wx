package com.yzh.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi(){
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        //先封装APiInfo
        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        apiInfoBuilder.title("EMOS办公管理系统");
        ApiInfo build = apiInfoBuilder.build();
        docket.apiInfo(build);

        //标注哪些类的哪些方法需要添加到Swagger页面
        ApiSelectorBuilder select = docket.select();
        //所有的类
        select.paths(PathSelectors.any());
        //所有有ApiOper的方法
        select.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = select.build();

        //整合JWT单点登录
        ApiKey apiKey = new ApiKey("token", "token", "header");
        List<ApiKey> apiKeyList=new ArrayList<>();
        apiKeyList.add(apiKey);
        docket.securitySchemes(apiKeyList);

        //设置作用域
        AuthorizationScope authorizationScope=new AuthorizationScope("global","accessEverything");
        AuthorizationScope[] scopes={authorizationScope};
        SecurityReference securityReference=new SecurityReference("token",scopes);
        List refList=new ArrayList();
        refList.add(securityReference);
        SecurityContext context=SecurityContext.builder().securityReferences(refList).build();
        List csxList=new ArrayList();
        csxList.add(context);
        docket.securityContexts(csxList);

        return docket;
    }
}
