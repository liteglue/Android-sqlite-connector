package io.liteglue;

/* package */ interface SQLStatementHandle {
  public int prepare();
  public int bindDouble(int pos, double val);
  public int bindInteger(int pos, int val);
  public int bindLong(int pos, long val);
  public int bindNull(int pos);
  public int bindTextNativeString(int col, String val);
  public int step();
  public int getColumnCount();
  public String getColumnName(int col);
  public double getColumnDouble(int col);
  public int getColumnInteger(int col);
  public long getColumnLong(int col);
  public String getColumnTextNativeString(int col);
  public int getColumnType(int col);
  public int finish();
}
