package net.sqlc;

public interface SQLiteStatement {
  public void bindDouble(int col, double val) throws java.sql.SQLException;
  public void bindInteger(int col, int val) throws java.sql.SQLException;
  public void bindLong(int col, long val) throws java.sql.SQLException;
  public void bindTextNativeString(int col, String val) throws java.sql.SQLException;
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
