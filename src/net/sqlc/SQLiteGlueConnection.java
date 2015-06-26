package net.sqlc;

//import org.sqlg.SQLiteGlue;

public class SQLiteGlueConnection implements SQLiteConnection {
  public SQLiteGlueConnection(String filename, int flags) throws java.sql.SQLException
  {
    /* check param(s): */
    if (filename == null) throw new java.sql.SQLException("null argument", "failed", SQLCode.MISUSE);

    SQLDatabaseHandle mydb = new SQLGDatabaseHandle(filename, flags);
    int rc = mydb.open();

    if (rc != SQLCode.OK) throw new java.sql.SQLException("open failed with error: " + rc, "failed", rc);
    this.db = mydb;
  }

  @Override
  public void dispose() throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    int rc = db.close();
    if (rc != SQLCode.OK) throw new java.sql.SQLException("dispose failed with error: " + rc, "failed", rc);
    db = null;
  }

  @Override
  public void keyNativeString(String key) throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    int rc = db.keyNativeString(key);
    if (rc != SQLCode.OK) throw new java.sql.SQLException("sqlite3_key failed with error: " + rc, "failed", rc);
  }

  @Override
  public SQLiteStatement prepareStatement(String sql) throws java.sql.SQLException {
    /* check state: */
    if (db == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

    /* check param(s): */
    if (sql == null) throw new java.sql.SQLException("null argument", "failed", SQLCode.MISUSE);

    SQLGStatement st = new SQLGStatement(sql);
    int rc = st.prepare();
    if (rc != SQLCode.OK) throw new java.sql.SQLException("prepare statement failed with error: " + rc, "failed", rc);

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
    public void bindDouble(int col, double val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindDouble(col, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("bindDouble failed with error: " + rc, "failed", rc);
    }

    @Override
    public void bindInteger(int col, int val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindInteger(col, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("bindInteger failed with error: " + rc, "failed", rc);
    }

    @Override
    public void bindLong(int col, long val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindLong(col, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("bindLong failed with error: " + rc, "failed", rc);
    }

    @Override
    public void bindTextNativeString(int col, String val) throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      /* check param(s): */
      if (val == null) throw new java.sql.SQLException("null argument", "failed", SQLCode.MISUSE);

      int rc = sthandle.bindTextNativeString(col, val);
      if (rc != SQLCode.OK) throw new java.sql.SQLException("bindTextNativeString failed with error: " + rc, "failed", rc);
    }

    @Override
    public boolean step() throws java.sql.SQLException {
      /* check state: */
      if (sthandle == null) throw new java.sql.SQLException("already disposed", "failed", SQLCode.MISUSE);

      int rc = sthandle.step();
      if (rc != SQLCode.OK && rc != SQLCode.ROW && rc != SQLCode.DONE) throw new java.sql.SQLException("step failed with error: " + rc, "failed", rc);

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

      int rc = sthandle.finish();
      if (rc != SQLCode.OK) throw new java.sql.SQLException("finish failed with error: " + rc, "failed", rc);
      sthandle = null;
    }

    private SQLStatementHandle sthandle = null;
    private String sql = null;
    private boolean hasRow = false;
    private int columnCount = 0;
  }

  private SQLDatabaseHandle db = null;
}
