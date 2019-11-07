package com.ansel.auth.parse;



import com.ansel.auth.core.DataAuthContext;
import com.ansel.auth.core.DataAuthException;
import com.ansel.auth.core.DataAuthManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import org.springframework.util.CollectionUtils;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/15
 * @Description: 数据权限sql增强
 */
@Slf4j
public class SelectVistorImpl implements SelectVisitor {

  //是否处理sql标识
  private Boolean isEnhanceSql = false;

  @Override
  public void visit(PlainSelect plainSelect) {
    if(plainSelect.getFromItem()==null){
      return;
    }
    FromVistorImpl fromVistor = new FromVistorImpl();
    plainSelect.getFromItem().accept(fromVistor);
    if (plainSelect.getJoins() != null) {
      plainSelect.getJoins().stream().forEach(j -> {
        j.getRightItem().accept(fromVistor);
      });
    }
    if (CollectionUtils.isEmpty(fromVistor.getTables())) {
      return;
    }
    isEnhanceSql= true;
    Map<String, List<String>> mapDatas = DataAuthManager.get();
    Expression whereExpression = null;
    if (plainSelect.getWhere() != null) {
      whereExpression = plainSelect.getWhere();
    }
    log.debug("Data auth interceptor start..");
    for (Table table : fromVistor.getTables()) {
      log.debug("Data auth interceptor...table:{}", table.getFullyQualifiedName());

      if (CollectionUtils.isEmpty(mapDatas)) {
        throw new DataAuthException(
            "DataAuth Exception:Data permission parameter 'datas'  must not null");
      }
      List<String> authDatas = mapDatas.get(table.getFullyQualifiedName());
      if(CollectionUtils.isEmpty(authDatas)){
        throw new DataAuthException(
            "DataAuth Exception:table "+table.getName()+", Data permission parameter 'datas'  must not null");
      }
      ExpressionList list = new ExpressionList(
          authDatas.stream().map(d -> new StringValue(d))
              .collect(Collectors.toList()));
      Expression enhancedCondition = new InExpression(new Column(table, DataAuthContext.getColumn(table.getFullyQualifiedName())),
          list);
      if (whereExpression == null) {
        whereExpression = enhancedCondition;
        continue;
      }
      AndExpression and = new AndExpression(whereExpression, enhancedCondition);
      whereExpression = and;
    }
    plainSelect.setWhere(whereExpression);
    log.debug("Data auth interceptor end. sql:{}", plainSelect.toString());
  }

  @Override
  public void visit(SetOperationList setOpList) {

  }

  @Override
  public void visit(WithItem withItem) {

  }

  public Boolean getEnhanceSql() {
    return isEnhanceSql;
  }
}
