package io.liteglue;

public class SQLiteConnector implements SQLiteConnectionFactory {
  static boolean isLibLoaded = false;

  public SQLiteConnector() {
    if (!isLibLoaded) {
      System.loadLibrary("sqlc-native-driver");

      if (SQLiteNative.sqlc_api_version_check(SQLiteNative.SQLC_API_VERSION) != SQLCode.OK) {
        throw new RuntimeException("native library version mismatch");
      }

      isLibLoaded = true;
    }
  }

  @Override
  public SQLiteConnection newSQLiteConnection(String filename, int flags) throws java.sql.SQLException {
    return new SQLiteGlueConnection(filename, flags);
  }
}
