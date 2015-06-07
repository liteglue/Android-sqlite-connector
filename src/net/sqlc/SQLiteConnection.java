package net.sqlc;

public interface SQLiteConnection {
  public int close();
  public SQLiteStatement prepareStatement(String sql) throws java.sql.SQLException;
  public long getLastInsertRowid();
  public int getTotalChanges();
}
