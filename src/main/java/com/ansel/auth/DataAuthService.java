package com.ansel.auth;

import java.util.List;
import java.util.Map;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/16
 * @Description: 数据权限过滤集合获取接口定义
 */
public interface DataAuthService {

   Map<String,List<String>> authDatas();
}
