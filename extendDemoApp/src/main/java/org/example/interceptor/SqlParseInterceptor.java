package org.example.interceptor;

import lombok.extern.log4j.Log4j2;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;


//@Component
@Intercepts({
//        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class}),
//        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
//        @Signature(type = StatementHandler.class,method = "parameterize",args = {Statement.class})
})
@Log4j2
public class SqlParseInterceptor implements Interceptor {

    private void visitTableAndColumnsBySS(String sql, Set<String> tables, Set<String> columns){
        CacheOption cacheOption = new CacheOption(128, 1024L);
        SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
        ParseASTNode parseASTNode = parserEngine.parse(sql, false);
        SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", false, new Properties());
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

        @Override
        public SqlNode visit(SqlIdentifier id) {
            if (id.isSimple()) {
                columns.add(id.toString());
            } else {
                columns.add(id.toString());
            }
            return super.visit(id);
        }

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

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        //必须加这个，因为如果是代理过来的，会被调用两次。
        if(!Proxy.isProxyClass(target.getClass())){
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            if(Proxy.isProxyClass(invocation.getArgs()[0].getClass())){

                PreparedStatementLogger preparedStatementLogger =((PreparedStatementLogger)Proxy.getInvocationHandler(invocation.getArgs()[0]));


            }


            String sql = statementHandler.getBoundSql().getSql();
            String methodName = invocation.getMethod().getName();

            Set<String> tables = new HashSet<>();
            Set<String> columns = new HashSet<>();

            visitTableAndColumnsBySS(sql, tables,columns);

            if(!tables.isEmpty()){
                Iterator<String> t = tables.iterator();
                System.out.println("tables:");
                while (t.hasNext()){
                    System.out.println(t.next());
                }
            }
            if(!columns.isEmpty()){
                System.out.println("columns:");
                Iterator<String> c = columns.iterator();
                while (c.hasNext()){
                    System.out.println(c.next());
                }
            }


            System.out.println(String.format("StatementHandler-->Original SQL: %s-->%s\n",methodName,sql));
        }


        Object object = invocation.proceed();
        return object;
    }
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

}
