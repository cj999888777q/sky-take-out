package com.sky.ascept;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     *
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillPointcut() {
    }


    @Before("AutoFillPointcut()")
    public void autoFill(JoinPoint joinPoint)
    {
        log.info("进行公共字段填充");
        //获取被拦截的方法上数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autofill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autofill.value();//获取数据库操作类型
        //获取被拦截方法上的实体对象--参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        Object entiy = args[0];
        //准备赋值的数值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT) {
            try
            {
                Method setCreateTime = entiy.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entiy.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entiy.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entiy.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射赋值
                setCreateTime.invoke(entiy,now);
                setUpdateTime.invoke(entiy,now);
                setCreateUser.invoke(entiy,currentId);
                setUpdateUser.invoke(entiy,currentId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (operationType == OperationType.UPDATE) {
            try
            {
                Method setUpdateTime = entiy.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entiy.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射赋值
                setUpdateTime.invoke(entiy,now);
                setUpdateUser.invoke(entiy,currentId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
