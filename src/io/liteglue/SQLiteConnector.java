package io.liteglue;

public class SQLiteConnector implements SQLiteConnectionFactory {
  static boolean isLibLoaded = false;

  public SQLiteConnector() {
    if (!isLibLoaded) {
      System.loadLibrary("sqlc-native-driver");
      isLibLoaded = true;
    }
  }

  @Override
  public SQLiteConnection newSQLiteConnection(String filename, int flags) throws java.sql.SQLException {
    return new SQLiteGlueConnection(filename, flags);
  }
}
