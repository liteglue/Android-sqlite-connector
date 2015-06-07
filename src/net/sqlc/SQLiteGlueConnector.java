package net.sqlc;

public class SQLiteGlueConnector implements SQLiteConnector {
  static boolean isLibLoaded = false;

  public SQLiteGlueConnector() {
    if (!isLibLoaded) {
      System.loadLibrary("sqlg");
      isLibLoaded = true;
    }
  }

  @Override
  public SQLiteConnection newSQLiteConnection(String filename, int flags) throws java.sql.SQLException {
    return new SQLiteGlueConnection(filename, flags);
  }
}
