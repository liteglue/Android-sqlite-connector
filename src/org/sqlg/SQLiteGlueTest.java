package org.sqlg;

import android.app.Activity;
import android.os.Bundle;

import org.sqlg.SQLiteGlue;

import net.sqlc.*;

import java.io.File;

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

    File dbfile = new File(getFilesDir(), "DB.db");

    SQLiteConnection mydbc;

    try {
      mydbc = new SQLiteGlueConnection(dbfile.getAbsolutePath(),
        SQLiteGlue.SQLG_OPEN_READWRITE | SQLiteGlue.SQLG_OPEN_CREATE);
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "DB open exception", ex);
      return;
    }

    SQLiteStatement st;

    try {
      st = mydbc.prepareStatement("select upper('How about ascii text?') as caps");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.close();
      return;
    }

    st.step();

    int colcount = st.getColumnCount();
    android.util.Log.i("SQLiteGlueTest", "column count: " + colcount);

    String colname = st.getColumnName(0);
    android.util.Log.i("SQLiteGlueTest", "column name: " + colname);

    int coltype = st.getColumnType(0);
    android.util.Log.i("SQLiteGlueTest", "column type: " + coltype);

    String first = st.getColumnTextString(0);

    android.util.Log.i("SQLiteGlueTest", "upper: " + first);

    st.finish();

    try {
      st = mydbc.prepareStatement("drop table if exists test_table;");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.close();
      return;
    }
    st.step();
    st.finish();

    // source: https://github.com/pgsqlite/PG-SQLitePlugin-iOS
    try {
      st = mydbc.prepareStatement("CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.close();
      return;
    }
    st.step();
    st.finish();

    try {
      st = mydbc.prepareStatement("INSERT INTO test_table (data, data_num) VALUES (?,?)");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.close();
      return;
    }
    st.bindTextString(1, "test");
    st.bindInteger(2, 100);
    int sr = st.step();
    while (sr == 100) {
      android.util.Log.i("SQLiteGlueTest", "step next");
      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);
    st.finish();

    try {
      st = mydbc.prepareStatement("select * from test_table;");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.close();
      return;
    }

    sr = st.step();
    while (sr == 100) {
      android.util.Log.i("SQLiteGlueTest", "step next");

      colcount = st.getColumnCount();
      android.util.Log.i("SQLiteGlueTest", "column count: " + colcount);

      for (int i=0;i<colcount;++i) {
        colname = st.getColumnName(i);
        android.util.Log.i("SQLiteGlueTest", "column " + i + " name: " + colname);

        coltype = st.getColumnType(i);
        android.util.Log.i("SQLiteGlueTest", "column " + i + " type: " + coltype);

        String text = st.getColumnTextString(i);
        android.util.Log.i("SQLiteGlueTest", "col " + i + " text " + text);
      }

      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);

    st.finish();

    mydbc.close();
  }
}
