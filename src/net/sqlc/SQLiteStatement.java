package net.sqlc;

public interface SQLiteStatement {
  public int bindDouble(int col, double val);
  public int bindInteger(int col, int val);
  public int bindLong(int col, long val);
  public int bindText(int col, String val);
  public int step();
  public int getColumnCount();
  public String getColumnName(int col);
  public String getColumnText(int col);
  public int getColumnType(int col);
  public int finish();
}
