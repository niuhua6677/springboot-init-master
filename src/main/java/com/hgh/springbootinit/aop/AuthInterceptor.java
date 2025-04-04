package com.hgh.springbootinit.aop;

import com.hgh.springbootinit.annotation.AuthCheck;
import com.hgh.springbootinit.common.ErrorCode;
import com.hgh.springbootinit.exception.BusinessException;
import com.hgh.springbootinit.model.entity.User;
import com.hgh.springbootinit.model.enums.UserRoleEnum;
import com.hgh.springbootinit.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 * 使用面向切面编程（AOP）来进行权限校验。
 *
 * @author hgh
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService; // 用户服务接口，用于获取当前登录用户的信息

    /**
     * 执行拦截逻辑
     * 当目标方法上有 @AuthCheck 注解时，此方法会被触发。
     *
     * @param joinPoint AOP 的 JoinPoint 对象，代表了执行切入点时的状态
     * @param authCheck 目标方法上的 @AuthCheck 注解实例
     * @return 目标方法的返回值
     * @throws Throwable 如果目标方法抛出异常，此方法也会抛出异常
     */
    @Around("@annotation(authCheck)") // 当方法上有指定的注解时触发此增强器
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {

        // 获取必须的角色标识
        String mustRole = authCheck.mustRole();

        // 获取当前请求属性
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

        // 从请求属性中获取 HttpServletRequest 对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 根据角色标识获取枚举类型 UserRoleEnum
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 如果必须的角色为空（即不需要权限），则直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // 获取当前用户的枚举类型 UserRoleEnum
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 如果当前用户的枚举类型为空，则抛出异常
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 如果当前用户被封号，则直接拒绝请求
        if (UserRoleEnum.BAN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 如果必须的角色是管理员，则进行额外的检查
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            // 如果当前用户不是管理员，则拒绝请求
            if (!UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 如果通过了所有的权限校验，则允许执行目标方法
        return joinPoint.proceed();
    }
}

