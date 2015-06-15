package org.sqlg;

import android.app.Activity;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.sqlg.SQLiteGlue;

import net.sqlc.*;

import java.io.File;

public class SQLiteGlueTest extends Activity
{
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    ArrayAdapter<String> r1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    ListView lv1 = (ListView)findViewById(R.id.results);
    lv1.setAdapter(r1);
    r1.add(new String("test string 1"));

  //  runTest();
  //}

  // /* package */ void runTest() {

    try {

    SQLiteConnector connector = new SQLiteGlueConnector();

    File dbfile = new File(getFilesDir(), "DB.db");

    SQLiteConnection mydbc;

    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "DB open exception", ex);
      return;
    }

    SQLiteStatement st;

    try {
      st = mydbc.prepareStatement("select upper('How about some ascii text?') as caps");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    st.step();

    int colcount = st.getColumnCount();
    r1.add(new String("column count: " + colcount));
    android.util.Log.i("SQLiteGlueTest", "column count: " + colcount);

    String colname = st.getColumnName(0);
    r1.add(new String("column name: " + colname));
    android.util.Log.i("SQLiteGlueTest", "column name: " + colname);

    int coltype = st.getColumnType(0);
    android.util.Log.i("SQLiteGlueTest", "column type: " + coltype);

    String first = st.getColumnTextString(0);

    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));
    r1.add(new String("upper: " + first));

    st.dispose();

    try {
      st = mydbc.prepareStatement("drop table if exists test_table;");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    // source: https://github.com/pgsqlite/PG-SQLitePlugin-iOS
    try {
      st = mydbc.prepareStatement("CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    try {
      st = mydbc.prepareStatement("INSERT INTO test_table (data, data_num) VALUES (?,?)");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.bindTextString(1, "test");
    st.bindInteger(2, 100);
    boolean sr = st.step();
    while (sr) {
      android.util.Log.i("SQLiteGlueTest", "step next");
      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);
    st.dispose();

    try {
      st = mydbc.prepareStatement("select * from test_table;");
    } catch (java.lang.Exception ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    sr = st.step();
    while (sr) {
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

    st.dispose();

    mydbc.dispose();

    } catch (java.sql.SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "sql exception", ex);
      return;
    }
  }
}
