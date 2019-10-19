package com.ansel.auth.core;



import com.ansel.auth.parse.PropertiesParser;
import com.ansel.auth.parse.SelectVistorImpl;
import java.sql.SQLException;
import java.util.Properties;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @Author: Ansel.yuan
 * @Date: 2019/10/15
 * @Description: ibatis-sql拦截器--处理数据权限sql-for select * from table
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Intercepts(
    {
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
            Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
            Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
    }
)
public class DataAuthInterceptor implements Interceptor {


  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    //检查是否需要对sql进行数据权限过滤增强
    if(!DataAuthManager.checkIsAuth()){
      return invocation.proceed();
    }
    //进行sql增强逻辑
    Object[] args = invocation.getArgs();
    MappedStatement ms = (MappedStatement) args[0];
    Object parameter = args[1];
    BoundSql boundSql;
    //由于逻辑关系，只会进入一次
    if (args.length == 4) {
      boundSql = ms.getBoundSql(parameter);
    } else {
      boundSql = (BoundSql) args[5];
    }
    String sql = boundSql.getSql();
    Statement stmt = CCJSqlParserUtil.parse(sql);
    Select select = (Select) stmt;
    SelectVistorImpl selectVisitor = new SelectVistorImpl();
    select.getSelectBody().accept(selectVisitor);
    if(selectVisitor.getEnhanceSql()){
      resetSql2Invocation(args,ms,parameter,select.toString());
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


  private void resetSql2Invocation(Object[] args,MappedStatement statement, Object parameterObject,String sql) throws SQLException {
    BoundSql boundSql = statement.getBoundSql(parameterObject);
    MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
    MetaObject msObject = MetaObject
        .forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
            new DefaultReflectorFactory());
    msObject.setValue("sqlSource.boundSql.sql", sql);
    args[0] = newStatement;
  }

  private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
    MappedStatement.Builder builder =
        new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource,
            ms.getSqlCommandType());
    builder.resource(ms.getResource());
    builder.fetchSize(ms.getFetchSize());
    builder.statementType(ms.getStatementType());
    builder.keyGenerator(ms.getKeyGenerator());
    if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
      StringBuilder keyProperties = new StringBuilder();
      for (String keyProperty : ms.getKeyProperties()) {
        keyProperties.append(keyProperty).append(",");
      }
      keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
      builder.keyProperty(keyProperties.toString());
    }
    builder.timeout(ms.getTimeout());
    builder.parameterMap(ms.getParameterMap());
    builder.resultMaps(ms.getResultMaps());
    builder.resultSetType(ms.getResultSetType());
    builder.cache(ms.getCache());
    builder.flushCacheRequired(ms.isFlushCacheRequired());
    builder.useCache(ms.isUseCache());
    return builder.build();
  }

  /** 内部辅助类，包装sq*/
  class BoundSqlSqlSource implements SqlSource {

    private BoundSql boundSql;

    public BoundSqlSqlSource(BoundSql boundSql) {
      this.boundSql = boundSql;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
      return boundSql;
    }
  }

}
