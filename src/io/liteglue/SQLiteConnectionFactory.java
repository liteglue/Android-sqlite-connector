package io.liteglue;

public interface SQLiteConnectionFactory {
  public SQLiteConnection newSQLiteConnection(String filename, int flags) throws java.sql.SQLException;
}
