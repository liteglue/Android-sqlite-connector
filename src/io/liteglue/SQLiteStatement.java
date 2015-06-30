package io.liteglue;

public interface SQLiteStatement {
  public void bindDouble(int pos, double val) throws java.sql.SQLException;
  public void bindInteger(int pos, int val) throws java.sql.SQLException;
  public void bindLong(int pos, long val) throws java.sql.SQLException;
  public void bindNull(int pos) throws java.sql.SQLException;
  public void bindTextNativeString(int pos, String val) throws java.sql.SQLException;
  public boolean step() throws java.sql.SQLException;
  public int getColumnCount() throws java.sql.SQLException;
  public String getColumnName(int col) throws java.sql.SQLException;
  public double getColumnDouble(int col) throws java.sql.SQLException;
  public int getColumnInteger(int col) throws java.sql.SQLException;
  public long getColumnLong(int col) throws java.sql.SQLException;
  public String getColumnTextNativeString(int col) throws java.sql.SQLException;
  public int getColumnType(int col) throws java.sql.SQLException;
  public void dispose() throws java.sql.SQLException;
}
