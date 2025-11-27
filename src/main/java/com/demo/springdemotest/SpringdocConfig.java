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
            Set<Class<?>> hiddenControllers = new HashSet<>();

            // Find all controllers that should be hidden for current profiles
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for (HandlerMethod handlerMethod : handlerMethods.values()) {
                Class<?> beanType = handlerMethod.getBeanType();
                if (beanType.isAnnotationPresent(HiddenForProfiles.class)) {
                    HiddenForProfiles annotation = beanType.getAnnotation(HiddenForProfiles.class);
                    for (String profile : annotation.value()) {
                        if (Arrays.asList(activeProfiles).contains(profile)) {
                            hiddenControllers.add(beanType);
                            break;
                        }
                    }
                }
            }

            // Remove paths that belong to hidden controllers
            if (!hiddenControllers.isEmpty()) {
                openApi.getPaths().entrySet().removeIf(entry -> {
                    return entry.getValue().readOperations().stream().anyMatch(operation -> {
                        if (operation.getTags() != null) {
                            for (String tag : operation.getTags()) {
                                for (Class<?> hiddenController : hiddenControllers) {
                                    String controllerName = hiddenController.getSimpleName().replace("Controller", "");
                                    if (tag.equalsIgnoreCase(controllerName) || 
                                        tag.equalsIgnoreCase(hiddenController.getSimpleName())) {
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    });
                });
            }
        };
    }
}
