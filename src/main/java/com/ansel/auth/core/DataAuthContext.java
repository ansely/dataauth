package com.ansel.auth.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/16
 * @Description:
 */
@Data
@Builder
public class DataAuthContext {


  protected final static Map<String,String>  P_MAP = new HashMap<>();

  public static void initData(Map<String,String> map){
    if(!CollectionUtils.isEmpty(P_MAP)){
     throw new DataAuthException("DataAuthContext already initialized");
    }
    Iterator<Entry<String,String>> mapEntryIt = map.entrySet().iterator();
    while(mapEntryIt.hasNext()){
      Entry<String,String> entry = mapEntryIt.next();
      P_MAP.put(entry.getKey(),entry.getValue());
    }
  }

  public static String getColumn(String tablename){
    return P_MAP.get(tablename);
  }

  public static boolean checkTable(String tableName){
    return  P_MAP.containsKey(tableName);
  }

  public static Set<String> getTables(){
    return P_MAP.keySet();
  }
}
