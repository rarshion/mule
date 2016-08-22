/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.db.internal.domain.executor;

import org.mule.extension.db.api.StatementResult;
import org.mule.extension.db.internal.domain.autogeneratedkey.AutoGeneratedKeyStrategy;
import org.mule.extension.db.internal.domain.autogeneratedkey.NoAutoGeneratedKeyStrategy;
import org.mule.extension.db.internal.domain.connection.DbConnection;
import org.mule.extension.db.internal.domain.query.Query;
import org.mule.extension.db.internal.domain.statement.StatementFactory;
import org.mule.extension.db.internal.result.resultset.ListResultSetHandler;
import org.mule.extension.db.internal.result.row.InsensitiveMapRowHandler;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Executes queries that return an update count or an update count and auto
 * generated keys
 */
public class UpdateExecutor extends AbstractSingleQueryExecutor {

  public UpdateExecutor(StatementFactory statementFactory) {
    super(statementFactory);
  }

  @Override
  protected Object doExecuteQuery(DbConnection connection, Statement statement, Query query) throws SQLException {
    return doExecuteQuery(connection, statement, query, new NoAutoGeneratedKeyStrategy());
  }

  @Override
  protected Object doExecuteQuery(DbConnection dbConnection, Statement statement, Query query,
                                  AutoGeneratedKeyStrategy autoGeneratedKeyStrategy)
      throws SQLException {
    try {
      int updateCount = autoGeneratedKeyStrategy.executeUpdate(statement, query.getQueryTemplate());
      StatementResult result;

      if (autoGeneratedKeyStrategy.returnsAutoGeneratedKeys()) {
        ResultSet generatedKeys = statement.getGeneratedKeys();
        ListResultSetHandler listResultSetHandler = new ListResultSetHandler(new InsensitiveMapRowHandler());

        List generatedKeyList = listResultSetHandler.processResultSet(dbConnection, generatedKeys);
        Map<String, BigInteger> keyMap = generatedKeyList.isEmpty() ? null : (Map<String, BigInteger>) generatedKeyList.get(0);
        result = new StatementResult(updateCount, keyMap);
      } else {
        result = new StatementResult(updateCount, null);
      }

      return result;
    } finally {
      statement.close();
    }
  }

}
