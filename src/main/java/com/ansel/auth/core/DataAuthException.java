package com.ansel.auth.core;


/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/16
 * @Description: 数据权限异常
 */
public class DataAuthException extends RuntimeException {

  public DataAuthException() {
    super();
  }

  public DataAuthException(String message) {
    super(message);
  }

}
