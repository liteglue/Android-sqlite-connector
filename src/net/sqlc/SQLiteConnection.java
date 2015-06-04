package net.sqlc;

public interface SQLiteConnection {
  public int close();
  public SQLiteStatement prepareStatement(String sql);
  public long getLastInsertRowid();
  public int getTotalChanges();
}
