package net.sqlc;

public interface SQLiteConnector {
  public SQLiteConnection newSQLiteConnection(String filename, int flags) throws java.sql.SQLException;
}
