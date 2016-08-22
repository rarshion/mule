/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.db.internal.domain.autogeneratedkey;

import static java.sql.Statement.NO_GENERATED_KEYS;
import org.mule.runtime.module.db.internal.domain.connection.DbConnection;
import org.mule.runtime.module.db.internal.domain.query.QueryTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class NoAutoGeneratedKeyStrategy implements AutoGeneratedKeyStrategy {

  public NoAutoGeneratedKeyStrategy() {
    super();
  }

  @Override
  public boolean returnsAutoGeneratedKeys() {
    return false;
  }

  @Override
  public PreparedStatement prepareStatement(DbConnection connection, QueryTemplate queryTemplate) throws SQLException {
    return connection.prepareStatement(queryTemplate.getSqlText());
  }

  @Override
  public boolean execute(Statement statement, QueryTemplate queryTemplate) throws SQLException {
    if (statement instanceof PreparedStatement) {
      return ((PreparedStatement) statement).execute();
    } else {
      return statement.execute(queryTemplate.getSqlText(), NO_GENERATED_KEYS);
    }
  }

  @Override
  public int executeUpdate(Statement statement, QueryTemplate queryTemplate) throws SQLException {
    if (statement instanceof PreparedStatement) {
      return ((PreparedStatement) statement).executeUpdate();
    } else {
      return statement.executeUpdate(queryTemplate.getSqlText(), NO_GENERATED_KEYS);
    }
  }
}
