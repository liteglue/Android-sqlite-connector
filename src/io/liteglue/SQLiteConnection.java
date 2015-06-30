package io.liteglue;

public interface SQLiteConnection {
  public void dispose() throws java.sql.SQLException;
  public void keyNativeString(String key) throws java.sql.SQLException;
  public SQLiteStatement prepareStatement(String sql) throws java.sql.SQLException;
  public long getLastInsertRowid() throws java.sql.SQLException;
  public int getTotalChanges() throws java.sql.SQLException;
}
