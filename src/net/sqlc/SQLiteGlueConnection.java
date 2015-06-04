package net.sqlc;

import org.sqlg.SQLiteGlue;

public class SQLiteGlueConnection implements SQLiteConnection {
  public SQLiteGlueConnection(String filename, int flags)
  {
    long handle = SQLiteGlue.sqlg_db_open(filename, flags);
    if (handle < 0) throw new java.lang.IllegalStateException("sqlite3_open_v2() failed with error: " + (-handle));
    this.dbhandle = handle;
  }

  @Override
  public int close() {
    return SQLiteGlue.sqlg_db_close(this.dbhandle);
  }

  @Override
  public SQLiteStatement prepareStatement(String sql) {
    long sh = SQLiteGlue.sqlg_db_prepare_st(this.dbhandle, sql);
    if (sh < 0) throw new java.lang.IllegalStateException("sqlite3_prepare_v2() failed with error: " + (-sh));
    return new SQLGStatement(sh);
  }

  @Override
  public long getLastInsertRowid() {
    return SQLiteGlue.sqlg_db_last_insert_rowid(this.dbhandle);
  }

  @Override
  public int getTotalChanges() {
    return SQLiteGlue.sqlg_db_total_changes(this.dbhandle);
  }

  class SQLGStatement implements SQLiteStatement {
    SQLGStatement(long h) {
      this.sthandle = h;
    }

    @Override
    public int bindDouble(int col, double val) {
      return SQLiteGlue.sqlg_st_bind_double(this.sthandle, col, val);
    }

    @Override
    public int bindInteger(int col, int val) {
      return SQLiteGlue.sqlg_st_bind_int(this.sthandle, col, val);
    }

    @Override
    public int bindLong(int col, long val) {
      return SQLiteGlue.sqlg_st_bind_int64(this.sthandle, col, val);
    }

    @Override
    public int bindTextString(int col, String val) {
      return SQLiteGlue.sqlg_st_bind_text_string(this.sthandle, col, val);
    }

    @Override
    public int step() {
      return SQLiteGlue.sqlg_st_step(this.sthandle);
    }

    @Override
    public int getColumnCount() {
      return SQLiteGlue.sqlg_st_column_count(this.sthandle);
    }

    @Override
    public String getColumnName(int col) {
      return SQLiteGlue.sqlg_st_column_name(this.sthandle, col);
    }

    @Override
    public int getColumnType(int col) {
      return SQLiteGlue.sqlg_st_column_type(this.sthandle, col);
    }

    @Override
    public double getColumnDouble(int col) {
      return SQLiteGlue.sqlg_st_column_double(this.sthandle, col);
    }

    @Override
    public long getColumnLong(int col) {
      return SQLiteGlue.sqlg_st_column_long(this.sthandle, col);
    }

    @Override
    public String getColumnTextString(int col) {
      return SQLiteGlue.sqlg_st_column_text_string(this.sthandle, col);
    }

    @Override
    public int finish() {
      return SQLiteGlue.sqlg_st_finish(this.sthandle);
    }

    private long sthandle;
  }

  private long dbhandle;
}
