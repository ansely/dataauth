package com.ansel.auth.parse;

import com.ansel.auth.core.DataAuthContext;
import com.ansel.auth.core.DataAuthManager;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/15
 * @Description:
 */
public class FromVistorImpl implements FromItemVisitor {

  private List<Table> tables;


  @Override
  public void visit(Table tableName) {
    //判断是否有需要数据权限注入的table
   if(DataAuthManager.get()==null || DataAuthManager.get().isEmpty()){
     return;
   }else{
     if(tables==null){
       tables = new ArrayList<>();
     }
     if(DataAuthContext.checkTable(tableName.getFullyQualifiedName())){
       tables.add(tableName);
     }
   }
  }

  @Override
  public void visit(SubSelect subSelect) {

  }

  @Override
  public void visit(SubJoin subjoin) {
    System.out.println(subjoin);
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    System.out.println(lateralSubSelect);
  }

  @Override
  public void visit(ValuesList valuesList) {

  }

  @Override
  public void visit(TableFunction tableFunction) {

  }

  @Override
  public void visit(ParenthesisFromItem aThis) {

  }

  public List<Table> getTables() {
    return tables;
  }
}
