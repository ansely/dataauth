package com.ansel.auth.parse;

import com.ansel.auth.core.DataAuthContext;
import com.ansel.auth.core.DataAuthException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.util.StringUtils;


/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/18
 * @Description:
 */
public class PropertiesParser {

  public void setProperties(Properties properties){
    String defaultColoumn =properties.getProperty("defaultColoumn");
    String mapProperties = properties.getProperty("mapProperties");
    if(StringUtils.isEmpty(mapProperties)){
      throw new DataAuthException("DataAuthInterceptor Initialization failure,properties:mapProperties   must not null");
    }
    String[] tableColounms = mapProperties.split(";");
    Map<String,String> map = new HashMap<>();
    for(String str : tableColounms){
      if(str.contains(".")){
        String[] tcs = str.split("\\.");
        map.put(tcs[0],tcs[1]);
        continue;
      }
      if(StringUtils.isEmpty(defaultColoumn)){
        throw new DataAuthException("DataAuthInterceptor Initialization failure,properties:defaultColoumn   must not null");
      }
      map.put(str,defaultColoumn);
    }
    DataAuthContext.initData(map);
  }
}
