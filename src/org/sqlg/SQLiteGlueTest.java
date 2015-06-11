package org.sqlg;

import android.app.Activity;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.sqlg.SQLiteGlue;

import java.io.File;

public class SQLiteGlueTest extends Activity
{
  static
  {
    System.loadLibrary("sqlg");
  }

  ArrayAdapter<String> resultsAdapter;

  int errorCount = 0;

  /* package */ void logErrorItem(String result) {
    android.util.Log.e("SQLiteGlueTest", result);
    resultsAdapter.add(result);
  }

  /* package */ void checkIntegerResult(String label, int actual, int expected) {
    if (expected == actual) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void checkStringResult(String label, String actual, String expected) {
    if (expected.equals(actual)) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void logResult(String result) {
    android.util.Log.i("SQLiteGlueTest", result);
    resultsAdapter.add(result);
  }

  /* package */ void logError(String result) {
    logErrorItem(result);
    ++errorCount;
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    resultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    ListView lv1 = (ListView)findViewById(R.id.results);
    lv1.setAdapter(resultsAdapter);

    File dbfile = new File(getFilesDir(), "DB.db");

    long mydb = SQLiteGlue.sqlg_db_open(dbfile.getAbsolutePath(),
      SQLiteGlue.SQLG_OPEN_READWRITE | SQLiteGlue.SQLG_OPEN_CREATE);

    if (mydb < 0) {
      logError("DB open error: " + -mydb);
      return;
    }

    long st = SQLiteGlue.sqlg_db_prepare_st(mydb, "select upper('How about some ascii text?') as caps");

    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }

    SQLiteGlue.sqlg_st_step(st);

    int colcount = SQLiteGlue.sqlg_st_column_count(st);
    checkIntegerResult("SELECT UPPER() column count: ", colcount, 1);

    if (colcount > 0) {
      String colname = SQLiteGlue.sqlg_st_column_name(st, 0);
      checkStringResult("SELECT UPPER() caps column name", colname, "caps");

      int coltype = SQLiteGlue.sqlg_st_column_type(st, 0);
      checkIntegerResult("SELECT UPPER() caps column type", coltype, 3);

      String coltext = SQLiteGlue.sqlg_st_column_text(st, 0);
      checkStringResult("SELECT UPPER() as caps", coltext, "HOW ABOUT SOME ASCII TEXT?");
    }

    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "drop table if exists test_table;");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }
    SQLiteGlue.sqlg_st_step(st);
    SQLiteGlue.sqlg_st_finish(st);

    // source: https://github.com/davibe/Phonegap-SQLitePlugin
    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }
    SQLiteGlue.sqlg_st_step(st);
    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "INSERT INTO test_table (data, data_num) VALUES (?,?)");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }
    SQLiteGlue.sqlg_st_bind_text(st, 1, "test");
    SQLiteGlue.sqlg_st_bind_int(st, 2, 100);
    int sr = SQLiteGlue.sqlg_st_step(st);
    while (sr == 100) {
      logError("ERROR: ROW result NOT EXPECTED for INSERT");
      sr = SQLiteGlue.sqlg_st_step(st);
    }
    checkIntegerResult("INSERT last step result", sr, 101);
    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "select * from test_table;");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }

    sr = SQLiteGlue.sqlg_st_step(st);
    checkIntegerResult("SELECT step result", sr, 100);
    if (sr == 100) {
      colcount = SQLiteGlue.sqlg_st_column_count(st);
      checkIntegerResult("SELECT column count", colcount, 3);

      if (colcount >= 3) {
        String colname = SQLiteGlue.sqlg_st_column_name(st, 0);
        checkStringResult("SELECT column 0 name", colname, "id");

        int coltype = SQLiteGlue.sqlg_st_column_type(st, 0);
        checkIntegerResult("SELECT column 0 type", coltype, 1);

        String text = SQLiteGlue.sqlg_st_column_text(st, 0);
        checkStringResult("SELECT column 0 text", text, "1");

        colname = SQLiteGlue.sqlg_st_column_name(st, 1);
        checkStringResult("SELECT column 1 name", colname, "data");

        coltype = SQLiteGlue.sqlg_st_column_type(st, 1);
        checkIntegerResult("SELECT column 1 type", coltype, 3);

        text = SQLiteGlue.sqlg_st_column_text(st, 1);
        checkStringResult("SELECT column 1 text", text, "test");

        colname = SQLiteGlue.sqlg_st_column_name(st, 2);
        checkStringResult("SELECT column 2 name", colname, "data_num");

        coltype = SQLiteGlue.sqlg_st_column_type(st, 2);
        checkIntegerResult("SELECT column 2 type", coltype, 1);

        text = SQLiteGlue.sqlg_st_column_text(st, 2);
        checkStringResult("SELECT column 2 text", text, "100");
      }

      sr = SQLiteGlue.sqlg_st_step(st);
    }
    checkIntegerResult("SELECT next/last step result", sr, 101);

    SQLiteGlue.sqlg_st_finish(st);

    SQLiteGlue.sqlg_db_close(mydb);

    checkIntegerResult("TEST error count", errorCount, 0);
  }
}
