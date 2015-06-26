package org.sqlg;

import android.app.Activity;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.sqlg.SQLiteGlue;

import net.sqlc.*;

import java.io.File;

import java.sql.SQLException;

public class SQLiteGlueTest extends Activity
{
  ArrayAdapter<String> resultsAdapter;

  /* package */ void logUnexpectedException(String result, java.lang.Exception ex) {
    android.util.Log.e("SQLiteGlueTest", "UNEXPECTED EXCEPTION IN " + result, ex);
    resultsAdapter.add("UNEXPECTED EXCEPTION IN " + result + " : " + ex);
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    ArrayAdapter<String> r1 =
      resultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    ListView lv1 = (ListView)findViewById(R.id.results);
    lv1.setAdapter(r1);

  //  try {
  //    runTest();
  //  } catch (java.sql.SQLException ex) {
  //    android.util.Log.w("SQLiteGlueTest", "unexpected sql exception", ex);
  //    r1.add("unexpected sql exception" + ex);
  //    return;
  //  } catch (java.lang.Exception ex) {
  //    android.util.Log.w("SQLiteGlueTest", "unexpected exception", ex);
  //    r1.add("unexpected exception: " + ex);
  //    return;
  //  }
  //}

  ///* package */ void runTest() {
    try {

    SQLiteConnector connector = new SQLiteGlueConnector();

    File dbfile = new File(getFilesDir(), "mytest.db");

    SQLiteConnection mydbc = null;

    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (SQLException ex) {
      logUnexpectedException("DB open exception", ex);
      return;
    }

    SQLiteStatement st;

    try {
      st = mydbc.prepareStatement("select upper('How about some ascii text?') as caps");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
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

    String first = st.getColumnTextNativeString(0);

    r1.add(new String("upper: " + first));

    st.dispose();

    try {
      st = mydbc.prepareStatement("drop table if exists tt;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    try {
      st = mydbc.prepareStatement("create table if not exists tt (text1 text, num1 integer, num2 integer, real1 real)");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    /* XXX Brody TODO:
    // test statement error handling (seems to throw exception here):
    try {
      // seems to fail here:
      st = mydbc.prepareStatement("INSERT INTO tt (data, data_num) VALUES (?,?)");

      st.step();
      st.dispose();

      // should not get here:
      android.util.Log.w("SQLiteGlueTest", "ERROR: statement should not have succeeded");
      r1.add("ERROR: statement should not have succeeded");
    } catch (SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception, as expected OK", ex);
      r1.add("prepare statement exception, as expected OK: " + ex);
      // TODO dispose statement??
    }
    */

    try {
      st = mydbc.prepareStatement("INSERT INTO tt (text1, num1, num2, real1) VALUES (?,?,?,?)");

      // should not get here:
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception (not expected)", ex);
      mydbc.dispose();
      return;
    }
    st.bindTextNativeString(1, "test");
    st.bindInteger(2, 10100);
    st.bindLong(3, 0x1230000abcdL);
    st.bindDouble(4, 123456.789);

    boolean sr = st.step();
    while (sr) {
      android.util.Log.i("SQLiteGlueTest", "step next");
      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);
    st.dispose();

    try {
      st = mydbc.prepareStatement("select * from tt;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    sr = st.step();
    while (sr) {
      android.util.Log.i("SQLiteGlueTest", "step next");
      r1.add("step next");

      colcount = st.getColumnCount();
      android.util.Log.i("SQLiteGlueTest", "column count: " + colcount);

      for (int i=0;i<colcount;++i) {
        colname = st.getColumnName(i);
        android.util.Log.i("SQLiteGlueTest", "column " + i + " name: " + colname);
        r1.add("column " + i + " name: " + colname);

        coltype = st.getColumnType(i);
        android.util.Log.i("SQLiteGlueTest", "column " + i + " type: " + coltype);
        r1.add("column " + i + " type: " + coltype);

        String text = st.getColumnTextNativeString(i);
        android.util.Log.i("SQLiteGlueTest", "col " + i + " text " + text);
        r1.add("col " + i + " text " + text);
      }

      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);
    r1.add("last step " + sr);

    st.dispose();

    // XXX TODO fails with SQL error code 5 (SQLITE_BUSY):
    //mydbc.dispose();

    /* XXX TODO:
    // try to reopen database:
    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "DB open exception", ex);
      return;
    }

    // try to cleanup the table:
    try {
      st = mydbc.prepareStatement("drop table if exists tt;");
    } catch (SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    mydbc.dispose();
    */

    } catch (java.lang.Exception ex) {
      logUnexpectedException("unexpected exception", ex);
      return;
    }

  }
}
