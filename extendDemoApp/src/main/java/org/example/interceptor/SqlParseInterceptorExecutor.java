package org.example.interceptor;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
@Intercepts({
//        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
//        @Signature(type = StatementHandler.class,method = "parameterize",args = {Statement.class})
})
@Log4j2
public class SqlParseInterceptorExecutor implements Interceptor {
    CacheOption cacheOption = new CacheOption(128, 1024L);
    SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
    SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", false, new Properties());
    @PostConstruct
    public void init(){
        ParseASTNode parseASTNode = parserEngine.parse("SELECT 1",true);
        SQLStatement sqlStatement = visitorEngine.visit(parseASTNode);
    }

    ObjectMapper mapper = new ObjectMapper();
    private void visitTableAndColumnsBySS(String sql, Set<String> tables, Set<String> columns){

        ParseASTNode parseASTNode = parserEngine.parse(sql, true);

        SQLStatement sqlStatement = visitorEngine.visit(parseASTNode);

        if (sqlStatement instanceof MySQLInsertStatement) {
            MySQLInsertStatement insertStatement = (MySQLInsertStatement) sqlStatement;
// 提取表名
            SimpleTableSegment tableSegment = insertStatement.getTable();
            tables.add(tableSegment.getTableName().getIdentifier().getValue());

            // 提取字段名
            Collection<ColumnSegment> columnSegments = insertStatement.getColumns();
            for (ColumnSegment columnSegment : columnSegments) {

                columns.add(columnSegment.getIdentifier().getValue());
            }
        }
        else if(sqlStatement instanceof MySQLUpdateStatement){
            MySQLUpdateStatement updateStatement = (MySQLUpdateStatement) sqlStatement;

            // 提取表名
            SimpleTableSegment tableSegment = (SimpleTableSegment) updateStatement.getTable();
            tables.add(tableSegment.getTableName().getIdentifier().getValue());

            // 提取字段名
            List<AssignmentSegment> assignments = (List<AssignmentSegment>) updateStatement.getSetAssignment().getAssignments();
            for (AssignmentSegment assignment : assignments) {
                columns.addAll(assignment.getColumns().stream().map(item->item.getIdentifier().getValue()).collect(Collectors.toList()));
            }
        }
    }

    private void visitTableAndColumnsByCalcite(String sql, Set<String> tables, Set<String> columns){
        // 创建 SQL 解析器
        SqlParser parser = SqlParser.create(sql);
        try {
            SqlNode sqlNode = parser.parseQuery(sql);
            // 提取表名和字段名
            TableAndColumnExtractor extractor = new TableAndColumnExtractor();
            sqlNode.accept(extractor);
            columns.addAll(extractor.getColumns());
            tables.addAll(extractor.getTables());
        }
        catch (Exception ex){

        }
    }
    static class TableAndColumnExtractor extends SqlShuttle {
        private Set<String> tables = new HashSet<>();
        private Set<String> columns = new HashSet<>();

        public Set<String> getTables() {
            return tables;
        }

        public Set<String> getColumns() {
            return columns;
        }

//        @Override
//        public SqlNode visit(SqlIdentifier id) {
//            if (id.isSimple()) {
//                columns.add(id.toString());
//            } else {
//                columns.add(id.toString());
//            }
//            return super.visit(id);
//        }

        @Override
        public SqlNode visit(SqlCall call) {
            if(call instanceof SqlInsert){
                SqlInsert insert = (SqlInsert)call;
                SqlNode table = insert.getTargetTable();
                tables.add(table.toString());

            }
            else if(call instanceof SqlUpdate){
                SqlUpdate update = (SqlUpdate) call;
                SqlNode table = update.getTargetTable();
                tables.add(table.toString());
            }

            return super.visit(call);
        }
    }

    private String packageName="com.isoftstone";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();

        Future<Long> result = Executors.newCachedThreadPool().submit(()->{
            if(!Proxy.isProxyClass(target.getClass())){
                long start = System.currentTimeMillis();
                Object[] args = invocation.getArgs();
                MappedStatement ms = (MappedStatement)args[0];
                SqlCommandType commandType = ms.getSqlCommandType();
                if(commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE){
                    Object parameter = args[1];
                    BoundSql boundSql;
                    boundSql = ms.getBoundSql(parameter);
                    String sql = boundSql.getSql();
                    Set<String> table = new HashSet<>();
                    Set<String> column = new HashSet<>();
                    visitTableAndColumnsBySS(sql,table,column);
                    System.out.println(String.format("Executor-->original sql: %s",sql));
                    for(String t : table){
                        System.out.println(String.format("table: %s",t));
                    }
                    if(commandType == SqlCommandType.INSERT) {
                        Map<String, Object> map =
                                mapper.convertValue(parameter, new TypeReference<Map<String, Object>>() {
                                });

                        for (String key : map.keySet()) {
                            System.out.println(String.format("key: %s, value: %s", key, map.get(key)));
                        }
                    }
                    else if(commandType == SqlCommandType.UPDATE){
                        Map<String,Object> map = new HashMap<>();
                        if(parameter instanceof MapperMethod.ParamMap){
                            MapperMethod.ParamMap paramMap =((MapperMethod.ParamMap) parameter);
                            Set<String> keyset = paramMap.keySet();

                            if(null != keyset && !keyset.isEmpty()){
                                for(String k:keyset){
                                    Object obj = paramMap.get(k);
                                    Map<String, Object> tmpMap =
                                            mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
                                            });
                                    map.putAll(tmpMap);
                                }
                            }

                        }
                        for (String key : map.keySet()) {
                            System.out.println(String.format("key: %s, value: %s", key, map.get(key)));
                        }

                    }

                    long end = System.currentTimeMillis();
                    System.out.println(String.format("execute type [%s],Executor-->cost time: %s",commandType,end-start));
                    return (Long)(end-start);
                }

                return (Long)0L;
            }
            return (Long)0L;
        });
        //必须加这个，因为如果是代理过来的，会被调用两次。
        result.get(100,TimeUnit.SECONDS);
        Object object = invocation.proceed();
        return object;
    }
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

}
