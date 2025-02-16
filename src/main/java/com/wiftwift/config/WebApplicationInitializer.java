package com.wiftwift.config;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;

public class WebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{SecurityConfig.class, AppConfig.class}; 
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebMvcConfig.class}; 
    }

    @Override
    @NonNull
    protected String[] getServletMappings() {
        return new String[]{"/"}; 
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(new MultipartConfigElement(
            System.getProperty("java.io.tmpdir"),
            10 * 1024 * 1024, // max file size 10MB
            10 * 1024 * 1024, // max request size 10MB
            0 // file size threshold
        ));
    }
}
