package net.sqlc;

import org.sqlg.SQLiteGlue;

public class SQLGDatabaseHandle implements SQLDatabaseHandle {
  public SQLGDatabaseHandle(String filename, int flags) {
    dbfilename = filename;
    openflags = flags;
  }

  @Override
  public int open() {
    /* check state (should be checked by caller): */
    if (dbfilename == null || dbhandle != 0) return SQLCode.MISUSE;

    long handle = SQLiteGlue.sqlg_db_open(dbfilename, openflags);
    if (handle < 0) return (int)(-handle);

    dbhandle = handle;
    return SQLCode.OK; /* 0 */
  }

  @Override
  public int close() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return SQLCode.MISUSE;

    return SQLiteGlue.sqlg_db_close(this.dbhandle);
  }

  @Override
  public boolean isOpen() {
    return (dbhandle != 0);
  }

  @Override
  public SQLStatementHandle newStatementHandle(String sql) {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return null;

    return new SQLGStatementHandle(sql);
  }

  @Override
  public long getLastInsertRowid() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return -1; /* illegit value */

    return SQLiteGlue.sqlg_db_last_insert_rowid(dbhandle);
  }

  @Override
  public int getTotalChanges() {
    /* check state (should be checked by caller): */
    if (dbhandle == 0) return -1; /* illegit value */

    return SQLiteGlue.sqlg_db_total_changes(dbhandle);
  }

  // XXX TODO make this reusable:
  private class SQLGStatementHandle implements SQLStatementHandle {
    private SQLGStatementHandle(String sql) {
      this.sql = sql;
    }

    @Override
    public int prepare() {
      /* check state (should be checked by caller): */
      if (sql == null || sthandle != 0) return SQLCode.MISUSE;

      long sh = SQLiteGlue.sqlg_db_prepare_st(dbhandle, sql);
      if (sh < 0) return (int)(-sh);

      sthandle = sh;
      return SQLCode.OK; /* 0 */
    }

    @Override
    public int bindDouble(int col, double val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteGlue.sqlg_st_bind_double(this.sthandle, col, val);
    }

    @Override
    public int bindInteger(int col, int val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteGlue.sqlg_st_bind_int(this.sthandle, col, val);
    }

    @Override
    public int bindLong(int col, long val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteGlue.sqlg_st_bind_int64(this.sthandle, col, val);
    }

    @Override
    public int bindTextString(int col, String val) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteGlue.sqlg_st_bind_text_string(this.sthandle, col, val);
    }

    @Override
    public int step() {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      return SQLiteGlue.sqlg_st_step(this.sthandle);
    }

    @Override
    public int getColumnCount() {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteGlue.sqlg_st_column_count(this.sthandle);
    }

    @Override
    public String getColumnName(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return null;

      return SQLiteGlue.sqlg_st_column_name(this.sthandle, col);
    }

    @Override
    public int getColumnType(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteGlue.sqlg_st_column_type(this.sthandle, col);
    }

    @Override
    public double getColumnDouble(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteGlue.sqlg_st_column_double(this.sthandle, col);
    }

    @Override
    public long getColumnLong(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return -1;

      return SQLiteGlue.sqlg_st_column_long(this.sthandle, col);
    }

    @Override
    public String getColumnTextString(int col) {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return null;

      return SQLiteGlue.sqlg_st_column_text_string(this.sthandle, col);
    }

    @Override
    public int finish() {
      /* check state (should be checked by caller): */
      if (sthandle == 0) return SQLCode.MISUSE;

      sql = null;
      sthandle = 0;

      return SQLiteGlue.sqlg_st_finish(sthandle);
    }

    String sql = null;
    private long sthandle = 0;
  }

  String dbfilename = null;
  int openflags = 0;
  private long dbhandle = 0;
}
