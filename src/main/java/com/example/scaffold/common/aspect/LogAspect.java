package com.example.scaffold.common.aspect;

import com.example.scaffold.common.annotation.Log;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        String desc = logAnnotation.value().isEmpty() ? methodName : logAnnotation.value();

        log.info("[{}] 请求参数: {}", desc, Arrays.toString(joinPoint.getArgs()));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] 响应结果: {}, 耗时: {}ms", desc, result, elapsed);
            return result;
        } catch (Throwable e) {
            log.error("[{}] 异常: {}", desc, e.getMessage(), e);
            throw e;
        }
    }
}
