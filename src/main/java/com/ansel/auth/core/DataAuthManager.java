package com.ansel.auth.core;

import java.util.List;
import java.util.Map;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/16
 * @Description: 数据权限控制线程变量
 */
public class DataAuthManager {

  private final static ThreadLocal<Map<String,List<String>>> threadLocal = new InheritableThreadLocal<>();


  public static void set(Map<String,List<String>> map){
    threadLocal.set(map);
  }


  public static Map<String,List<String>> get(){
    return threadLocal.get();
  }

  public static boolean checkIsAuth(){
    if(threadLocal.get()==null || threadLocal.get().isEmpty()){
      return false;
    }
    return true;
  }
  public static void remove(){
      threadLocal.remove();
  }
}
