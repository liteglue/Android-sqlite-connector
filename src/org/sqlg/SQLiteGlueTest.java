package org.sqlg;

import android.app.Activity;
import android.os.Bundle;

import org.sqlg.SQLiteGlue;

public class SQLiteGlueTest extends Activity
{
  static
  {
    System.loadLibrary("sqlg");
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    long db1 = SQLiteGlue.sqlg_db_open(getFilesDir() + "DB.db", SQLiteGlue.SQLG_OPEN_READWRITE | SQLiteGlue.SQLG_OPEN_CREATE);

if (db1 < 0) {
  android.util.Log.w("SQLiteGlueTest", "DB open error: " + -db1);
return;
}

long s1 = SQLiteGlue.sqlg_db_prepare_st(db1, "select upper('Chris1')");
SQLiteGlue.sqlg_st_step(s1);

String u1 = SQLiteGlue.sqlg_st_column_text(s1, 0);

  android.util.Log.i("SQLiteGlueTest", "upper: " + u1);

SQLiteGlue.sqlg_st_finish(s1);
SQLiteGlue.sqlg_db_close(db1);

  }
}
