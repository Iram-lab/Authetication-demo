package com.authetication.project.asept;



import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut that matches all repositories, services, and REST controllers.
     */
    @Pointcut("within(com.authetication.project.controller..*) " +
              "|| within(com.authetication.project.service..*) " +
              "|| within(com.authetication.project.repository..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut definition
    }

    /**
     * Advice that logs methods before execution, after execution, and measures execution time.
     */
    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        // Mask passwords or credentials for security before logging input arguments
        String arguments = maskSensitiveData(methodName, joinPoint.getArgs());

        log.info("Enter: {}.{}() with argument[s] = {}", className, methodName, arguments);
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            
            log.info("Exit: {}.{}() executed in {} ms", className, methodName, elapsedTime);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", arguments, className, methodName);
            throw e;
        }
    }

    /**
     * Advice that logs exceptions thrown from any method in the pointcut.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in {}.{}() with cause = '{}' and message = '{}'",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.getCause() != null ? exception.getCause() : "NULL",
                exception.getMessage());
    }

    /**
     * Helper method to prevent logging plain-text passwords during login or registration.
     */
    private String maskSensitiveData(String methodName, Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        if (methodName.toLowerCase().contains("login") || methodName.toLowerCase().contains("register")) {
            return "[PROTECTED_CREDENTIALS_MASKED]";
        }
        return Arrays.toString(args);
    }
}

