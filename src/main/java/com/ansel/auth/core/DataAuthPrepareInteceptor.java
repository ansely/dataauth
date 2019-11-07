package com.ansel.auth.core;

import com.ansel.auth.parse.PropertiesParser;
import com.ansel.auth.parse.SelectVistorImpl;
import java.sql.Connection;
import java.util.Properties;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/19
 * @Description:
 */
@Intercepts({@Signature(method = "prepare", type = StatementHandler.class, args = {Connection.class,
    Integer.class})})
public class DataAuthPrepareInteceptor implements Interceptor {

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    StatementHandler handler = (StatementHandler) invocation.getTarget();
    MetaObject statementHandler = SystemMetaObject.forObject(handler);
    MappedStatement mappedStatement = (MappedStatement) statementHandler
        .getValue("delegate.mappedStatement");
    SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
    //只处理查询
    if ("SELECT".equals(sqlCommandType.name()) && DataAuthManager.checkIsAuth()) {
      BoundSql boundSql = handler.getBoundSql();
      String sql = boundSql.getSql();
      Statement stmt = CCJSqlParserUtil.parse(sql);
      Select select = (Select) stmt;
      SelectVistorImpl selectVisitor = new SelectVistorImpl();
      select.getSelectBody().accept(selectVisitor);
      statementHandler.setValue("delegate.boundSql.sql", select.toString());
    }
    return invocation.proceed();
  }

  @Override
  public Object plugin(Object o) {
    return Plugin.wrap(o, this);
  }

  @Override
  public void setProperties(Properties properties) {
    PropertiesParser propertiesParser = new PropertiesParser();
    propertiesParser.setProperties(properties);
  }
}
