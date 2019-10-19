package com.ansel.auth.aspect;

import com.ansel.auth.DataAuth;
import com.ansel.auth.DataAuthService;
import com.ansel.auth.core.DataAuthException;
import com.ansel.auth.core.DataAuthManager;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/1
 * @Description: 数据权限控制
 */
@Aspect
@Component
public class DataAuthAspect {

  @Autowired
  private DataAuthService dataAuthService;

  @Around(value = "@annotation(com.ansel.auth.DataAuth)")
  public Object doAuth(ProceedingJoinPoint pjp) throws Throwable {
    Signature sig = pjp.getSignature();
    if (!(sig instanceof MethodSignature)) {
      throw new IllegalArgumentException("该注解只能用于方法");
    }
    Map<String,List<String>> mapAuth = dataAuthService.authDatas();
    if(CollectionUtils.isEmpty(mapAuth)){
      throw new DataAuthException("mapAuth must not null.");
    }
    DataAuthManager.set(mapAuth);
    return pjp.proceed();
  }

  @AfterReturning(pointcut = "@annotation(com.ansel.auth.DataAuth)")
  public void doRemove() {
    DataAuthManager.remove();
  }
}
