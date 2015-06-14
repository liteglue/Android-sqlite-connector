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

  /* package */ void checkLongResult(String label, long actual, long expected) {
    if (expected == actual) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void checkDoubleResult(String label, double actual, double expected) {
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

      String coltext = SQLiteGlue.sqlg_st_column_text_string(st, 0);
      checkStringResult("SELECT UPPER() as caps", coltext, "HOW ABOUT SOME ASCII TEXT?");
    }

    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "drop table if exists tt;");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }
    SQLiteGlue.sqlg_st_step(st);
    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "create table if not exists tt (text1 text, num1 integer, num2 integer, real1 real)");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }
    SQLiteGlue.sqlg_st_step(st);
    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "INSERT INTO tt (text1, num1, num2, real1) VALUES (?,?,?,?)");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }
    SQLiteGlue.sqlg_st_bind_text_string(st, 1, "test");
    SQLiteGlue.sqlg_st_bind_int(st, 2, 10100);
    SQLiteGlue.sqlg_st_bind_int64(st, 3, 0x1230000abcdL);
    SQLiteGlue.sqlg_st_bind_double(st, 4, 123456.789);
    int sr = SQLiteGlue.sqlg_st_step(st);
    while (sr == 100) {
      logError("ERROR: ROW result NOT EXPECTED for INSERT");
      sr = SQLiteGlue.sqlg_st_step(st);
    }
    checkIntegerResult("INSERT last step result", sr, 101);
    SQLiteGlue.sqlg_st_finish(st);

    st = SQLiteGlue.sqlg_db_prepare_st(mydb, "select * from tt;");
    if (st < 0) {
      logError("prepare statement error: " + -st);
      SQLiteGlue.sqlg_db_close(mydb);
      return;
    }

    sr = SQLiteGlue.sqlg_st_step(st);
    checkIntegerResult("SELECT step result", sr, 100);
    if (sr == 100) {
      colcount = SQLiteGlue.sqlg_st_column_count(st);
      checkIntegerResult("SELECT column count", colcount, 4);

      if (colcount >= 3) {
        int colid = 0;
        String colname;
        int coltype;
        String coltext;
        long longval;
        double doubleval;

        colname = SQLiteGlue.sqlg_st_column_name(st, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "text1");

        coltype = SQLiteGlue.sqlg_st_column_type(st, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, 3); /* SQLITE_TEXT */

        coltext = SQLiteGlue.sqlg_st_column_text_string(st, colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "test");

        ++colid;

        colname = SQLiteGlue.sqlg_st_column_name(st, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "num1");

        coltype = SQLiteGlue.sqlg_st_column_type(st, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, 1); /* SQLITE_INTEGER */

        coltext = SQLiteGlue.sqlg_st_column_text_string(st, colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "10100");
        longval = SQLiteGlue.sqlg_st_column_long(st, colid);
        checkLongResult("SELECT column " + colid + " long value", longval, 10100);

        ++colid;

        colname = SQLiteGlue.sqlg_st_column_name(st, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "num2");

        coltype = SQLiteGlue.sqlg_st_column_type(st, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, 1); /* SQLITE_INTEGER */

        longval = SQLiteGlue.sqlg_st_column_long(st, colid);
        checkLongResult("SELECT column " + colid + " long value", longval, 0x1230000abcdL);

        ++colid;

        colname = SQLiteGlue.sqlg_st_column_name(st, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "real1");

        coltype = SQLiteGlue.sqlg_st_column_type(st, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, 2); /* SQLITE_FLOAT */

        coltext = SQLiteGlue.sqlg_st_column_text_string(st, colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "123456.789");
        doubleval = SQLiteGlue.sqlg_st_column_double(st, colid);
        checkDoubleResult("SELECT column " + colid + " double (real) value", doubleval, 123456.789);
      }

      sr = SQLiteGlue.sqlg_st_step(st);
    }
    checkIntegerResult("SELECT next/last step result", sr, 101);

    SQLiteGlue.sqlg_st_finish(st);

    SQLiteGlue.sqlg_db_close(mydb);

    checkIntegerResult("TEST error count", errorCount, 0);
  }
}
