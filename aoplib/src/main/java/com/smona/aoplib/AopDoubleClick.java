package com.smona.aoplib;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

public class AopDoubleClick {
    @Pointcut("execution(* onClick(..))")
    public void methodPointcut() {
    }

    @Around("methodPointcut()")
    public void onDoubleClickListener(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Log.e("motianhu", "proceedingJoinPoint: " + proceedingJoinPoint);
        if (!InterceptDoubleClick.isDoubleClick()) {
            proceedingJoinPoint.proceed();
        }
    }
}
