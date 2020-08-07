package com.smona.aoplib;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AopDoubleClick {
    @Pointcut("execution(* onClick(..))")
    //@Pointcut("execution(* android.view.View.OnClickListener.onClick(..))")
    public void methodPointcut() {
    }

    @Around("methodPointcut()")
    public void onDoubleClick(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Log.e("motianhu", "proceedingJoinPoint: " + proceedingJoinPoint);
        if (!InterceptDoubleClick.isDoubleClick()) {
            Log.e("motianhu", "not double click: ");
            proceedingJoinPoint.proceed();
        }
    }
}
