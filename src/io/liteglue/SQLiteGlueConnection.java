package io.liteglue;

/* package */ class SQLiteGlueConnection implements SQLiteConnection {
  public SQLiteGlueConnection(String filename, int flags) throws java.sql.SQLException
  {
    /* check param(s): */
    if (filename == null) throw new java.sql.SQLException("null argument", "failed", SQLCode.MISUSE);

    SQLDatabaseHandle mydb = new SQLGDatabaseHandle(filename, flags);
    int rc = mydb.open();

    if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_open_v2 failure: " + db.getLastErrorMessage(), "failure", rc);
    this.db = mydb;
  }

  @Override
  public void dispose() throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    int rc = db.close();
    if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_close failure: " + db.getLastErrorMessage(), "failure", rc);
    db = null;
  }

  @Override
  public void keyNativeString(String key) throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    int rc = db.keyNativeString(key);
    if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_key failure: " + db.getLastErrorMessage(), "failure", rc);
  }

  @Override
  public SQLiteStatement prepareStatement(String sql) throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    /* check param(s): */
    if (sql == null) throw new java.sql.SQLException("null argument", "failed", SQLCode.MISUSE);

    SQLGStatement st = new SQLGStatement(sql);
    int rc = st.prepare();
    if (rc != SQLCode.OK) {
      throw new java.sql.SQLException("sqlite3_prepare_v2 failure: " + db.getLastErrorMessage(), "failure", rc);
    }

    return st;
  }

  @Override
  public long getLastInsertRowid() throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    return db.getLastInsertRowid();
  }

  @Override
  public int getTotalChanges() throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    return db.getTotalChanges();
  }

  // XXX TODO make this reusable:
  private class SQLGStatement implements SQLiteStatement {
    SQLGStatement(String sql) {
      this.sql = sql;
      this.sthandle = db.newStatementHandle(sql);
    }

    int prepare() {
      // TBD check state here?
      return sthandle.prepare();
    }

    @Override
    public void bindDouble(int pos, double val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindDouble(pos, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_bind_double failure: " + db.getLastErrorMessage(), "failure", rc);
    }

    @Override
    public void bindInteger(int pos, int val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindInteger(pos, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_bind_int failure: " + db.getLastErrorMessage(), "failure", rc);
    }

    @Override
    public void bindLong(int pos, long val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindLong(pos, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_bind_int64 (long) failure: " + db.getLastErrorMessage(), "failure", rc);
    }

    @Override
    public void bindNull(int pos) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindNull(pos);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_bind_null failure: " + db.getLastErrorMessage(), "failure", rc);
    }

    @Override
    public void bindTextNativeString(int pos, String val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      /* check param(s): */
      if (val == null) throw new java.sql.SQLException("null argument", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindTextNativeString(pos, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_bind_text failure: " + db.getLastErrorMessage(), "failure", rc);
    }

    @Override
    public boolean step() throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.step();
      if (rc != SQLCode.OK && rc != SQLCode.ROW && rc != SQLCode.DONE) {
        throw new java.sql.SQLException("sqlite3_step failure: " + db.getLastErrorMessage(), "failure", rc);
      }

      hasRow = (rc == SQLCode.ROW);
      if (hasRow) {
        columnCount = sthandle.getColumnCount();
      } else columnCount = 0;

      return hasRow;
    }

    @Override
    public int getColumnCount() throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return columnCount;
    }

    @Override
    public String getColumnName(int col) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);
      if (col < 0 || col >= columnCount) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return sthandle.getColumnName(col);
    }

    @Override
    public int getColumnType(int col) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);
      if (col < 0 || col >= columnCount) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return sthandle.getColumnType(col);
    }

    @Override
    public double getColumnDouble(int col) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);
      if (col < 0 || col >= columnCount) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return sthandle.getColumnDouble(col);
    }

    @Override
    public int getColumnInteger(int col) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);
      if (col < 0 || col >= columnCount) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return sthandle.getColumnInteger(col);
    }

    @Override
    public long getColumnLong(int col) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);
      if (col < 0 || col >= columnCount) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return sthandle.getColumnLong(col);
    }

    @Override
    public String getColumnTextNativeString(int col) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);
      if (!hasRow) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);
      if (col < 0 || col >= columnCount) throw new java.sql.SQLException("no result available", "failed", SQLCode.MISUSE);

      return sthandle.getColumnTextNativeString(col);
    }

    @Override
    public void dispose() throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      /* NOTE: no need to check the return code in this case. */
      sthandle.finish();
      sthandle = null;
    }

    private SQLStatementHandle sthandle = null;
    private String sql = null;
    private boolean hasRow = false;
    private int columnCount = 0;
  }

  private SQLDatabaseHandle db = null;
}
