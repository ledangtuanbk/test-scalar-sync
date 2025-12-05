package com.demo.springdemotest;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class SpringdocConfig {

    private final Environment env;
    private final RequestMappingHandlerMapping handlerMapping;

    public SpringdocConfig(Environment env, 
                          @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.env = env;
        this.handlerMapping = handlerMapping;
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            String[] activeProfiles = env.getActiveProfiles();
            Set<String> hiddenPaths = new HashSet<>();
            Set<String> hiddenTags = new HashSet<>();

            // Find all controllers - hide those without annotation or not matching current profiles
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                HandlerMethod handlerMethod = entry.getValue();
                Class<?> beanType = handlerMethod.getBeanType();
                boolean shouldHide = false;

                // Check if controller has @ShowForProfiles annotation
                if (beanType.isAnnotationPresent(ShowForProfiles.class)) {
                    ShowForProfiles annotation = beanType.getAnnotation(ShowForProfiles.class);
                    boolean shouldShow = false;

                    // Check if any of the annotation's profiles match the active profiles
                    for (String profile : annotation.value()) {
                        if (Arrays.asList(activeProfiles).contains(profile)) {
                            shouldShow = true;
                            break;
                        }
                    }

                    // If annotation is present but profile doesn't match, hide it
                    if (!shouldShow) {
                        shouldHide = true;
                    }
                } else {
                    // No annotation -> hide by default
                    shouldHide = true;
                }

                // Collect paths and tags to hide
                if (shouldHide) {
                    RequestMappingInfo mappingInfo = entry.getKey();
                    if (mappingInfo.getPathPatternsCondition() != null) {
                        mappingInfo.getPathPatternsCondition().getPatterns()
                            .forEach(pattern -> hiddenPaths.add(pattern.getPatternString()));
                    } else if (mappingInfo.getPatternsCondition() != null) {
                        hiddenPaths.addAll(mappingInfo.getPatternsCondition().getPatterns());
                    }

                    // Collect tag names from @Tag annotation if present
                    if (beanType.isAnnotationPresent(io.swagger.v3.oas.annotations.tags.Tag.class)) {
                        io.swagger.v3.oas.annotations.tags.Tag tagAnnotation =
                            beanType.getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
                        hiddenTags.add(tagAnnotation.name());
                    }
                }
            }

            // Remove hidden paths from OpenAPI spec
            if (!hiddenPaths.isEmpty()) {
                openApi.getPaths().entrySet().removeIf(entry ->
                    hiddenPaths.stream().anyMatch(hiddenPath ->
                        entry.getKey().equals(hiddenPath) || entry.getKey().startsWith(hiddenPath)
                    )
                );
            }

            // Remove hidden tags from OpenAPI spec
            if (!hiddenTags.isEmpty() && openApi.getTags() != null) {
                openApi.getTags().removeIf(tag -> hiddenTags.contains(tag.getName()));
            }
        };
    }
}
