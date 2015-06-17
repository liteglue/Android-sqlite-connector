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

    File dbfile = new File(this.getFilesDir(), "DB.db");

    long dbhandle = SQLiteGlue.sqlg_db_open(dbfile.getAbsolutePath(),
      SQLiteGlue.SQLG_OPEN_READWRITE | SQLiteGlue.SQLG_OPEN_CREATE);

    if (dbhandle < 0) {
      logError("DB open error: " + -dbhandle);
      return;
    }

    long sthandle = SQLiteGlue.sqlg_db_prepare_st(dbhandle, "SELECT UPPER('How about some ascii text?') AS caps");

    if (sthandle < 0) {
      logError("prepare statement error: " + -sthandle);
      SQLiteGlue.sqlg_db_close(dbhandle);
      return;
    }

    SQLiteGlue.sqlg_st_step(sthandle);

    int colcount1 = SQLiteGlue.sqlg_st_column_count(sthandle);
    checkIntegerResult("SELECT UPPER() column count: ", colcount1, 1);

    if (colcount1 > 0) {
      String colname = SQLiteGlue.sqlg_st_column_name(sthandle, 0);
      checkStringResult("SELECT UPPER() caps column name", colname, "caps");

      int coltype = SQLiteGlue.sqlg_st_column_type(sthandle, 0);
      checkIntegerResult("SELECT UPPER() caps column type", coltype, 3);

      String coltext = SQLiteGlue.sqlg_st_column_text_native(sthandle, 0);
      checkStringResult("SELECT UPPER() as caps", coltext, "HOW ABOUT SOME ASCII TEXT?");
    }

    SQLiteGlue.sqlg_st_finish(sthandle);

    sthandle = SQLiteGlue.sqlg_db_prepare_st(dbhandle, "DROP TABLE IF EXISTS mytable;");
    if (sthandle < 0) {
      logError("prepare statement error: " + -sthandle);
      SQLiteGlue.sqlg_db_close(dbhandle);
      return;
    }
    SQLiteGlue.sqlg_st_step(sthandle);
    SQLiteGlue.sqlg_st_finish(sthandle);

    sthandle = SQLiteGlue.sqlg_db_prepare_st(dbhandle, "CREATE TABLE mytable (text1 TEXT, num1 INTEGER, num2 INTEGER, real1 REAL)");
    if (sthandle < 0) {
      logError("prepare statement error: " + -sthandle);
      SQLiteGlue.sqlg_db_close(dbhandle);
      return;
    }
    int stresult1 = SQLiteGlue.sqlg_st_step(sthandle);
    checkIntegerResult("CREATE TABLE step result", stresult1, SQLiteGlue.SQLG_RESULT_DONE);
    SQLiteGlue.sqlg_st_finish(sthandle);

    sthandle = SQLiteGlue.sqlg_db_prepare_st(dbhandle, "INSERT INTO mytable (text1, num1, num2, real1) VALUES (?,?,?,?)");
    if (sthandle < 0) {
      logError("prepare statement error: " + -sthandle);
      SQLiteGlue.sqlg_db_close(dbhandle);
      return;
    }
    SQLiteGlue.sqlg_st_bind_text_native(sthandle, 1, "test");
    SQLiteGlue.sqlg_st_bind_int(sthandle, 2, 10100);
    SQLiteGlue.sqlg_st_bind_long(sthandle, 3, 0x1230000abcdL);
    SQLiteGlue.sqlg_st_bind_double(sthandle, 4, 123456.789);
    stresult1 = SQLiteGlue.sqlg_st_step(sthandle);
    while (stresult1 == SQLiteGlue.SQLG_RESULT_ROW) {
      logError("ERROR: ROW result NOT EXPECTED for INSERT");
      stresult1 = SQLiteGlue.sqlg_st_step(sthandle);
    }
    checkIntegerResult("INSERT last step result", stresult1, SQLiteGlue.SQLG_RESULT_DONE);
    SQLiteGlue.sqlg_st_finish(sthandle);

    sthandle = SQLiteGlue.sqlg_db_prepare_st(dbhandle, "SELECT text1, num1, num2, real1 FROM mytable;");
    if (sthandle < 0) {
      logError("prepare statement error: " + -sthandle);
      SQLiteGlue.sqlg_db_close(dbhandle);
      return;
    }

    stresult1 = SQLiteGlue.sqlg_st_step(sthandle);
    checkIntegerResult("SELECT step result", stresult1, SQLiteGlue.SQLG_RESULT_ROW);
    if (stresult1 == SQLiteGlue.SQLG_RESULT_ROW) {
      colcount1 = SQLiteGlue.sqlg_st_column_count(sthandle);
      checkIntegerResult("SELECT column count", colcount1, 4);

      if (colcount1 >= 3) {
        int colid = 0;
        String colname;
        int coltype;
        String coltext;
        int intval;
        long longval;
        double doubleval;

        colname = SQLiteGlue.sqlg_st_column_name(sthandle, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "text1");

        coltype = SQLiteGlue.sqlg_st_column_type(sthandle, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLiteGlue.SQLG_TEXT);

        coltext = SQLiteGlue.sqlg_st_column_text_native(sthandle, colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "test");

        ++colid;

        colname = SQLiteGlue.sqlg_st_column_name(sthandle, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "num1");

        coltype = SQLiteGlue.sqlg_st_column_type(sthandle, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLiteGlue.SQLG_INTEGER);

        coltext = SQLiteGlue.sqlg_st_column_text_native(sthandle, colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "10100");
        intval = SQLiteGlue.sqlg_st_column_int(sthandle, colid);
        checkIntegerResult("SELECT column " + colid + " int value", intval, 10100);
        longval = SQLiteGlue.sqlg_st_column_long(sthandle, colid);
        checkLongResult("SELECT column " + colid + " long value", longval, 10100);

        ++colid;

        colname = SQLiteGlue.sqlg_st_column_name(sthandle, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "num2");

        coltype = SQLiteGlue.sqlg_st_column_type(sthandle, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLiteGlue.SQLG_INTEGER);

        longval = SQLiteGlue.sqlg_st_column_long(sthandle, colid);
        checkLongResult("SELECT column " + colid + " long value", longval, 0x1230000abcdL);

        ++colid;

        colname = SQLiteGlue.sqlg_st_column_name(sthandle, colid);
        checkStringResult("SELECT column " + colid + " name", colname, "real1");

        coltype = SQLiteGlue.sqlg_st_column_type(sthandle, colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLiteGlue.SQLG_FLOAT);

        coltext = SQLiteGlue.sqlg_st_column_text_native(sthandle, colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "123456.789");
        doubleval = SQLiteGlue.sqlg_st_column_double(sthandle, colid);
        checkDoubleResult("SELECT column " + colid + " double (real) value", doubleval, 123456.789);
      }

      stresult1 = SQLiteGlue.sqlg_st_step(sthandle);
    }
    checkIntegerResult("SELECT next/last step result", stresult1, SQLiteGlue.SQLG_RESULT_DONE);

    SQLiteGlue.sqlg_st_finish(sthandle);

    SQLiteGlue.sqlg_db_close(dbhandle);

    checkIntegerResult("TEST error count", errorCount, 0);
  }
}
