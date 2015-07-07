package com.test;

import android.app.Activity;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.liteglue.*;

import java.io.File;

import java.sql.SQLException;

public class SQLiteConnectorTest extends Activity
{
  ArrayAdapter<String> resultsAdapter;

  int errorCount = 0;

  /* package */ void logErrorItem(String result) {
    android.util.Log.e("SQLiteGlueTest", result);
    resultsAdapter.add(result);
  }

  /* package */ void checkBooleanResult(String label, boolean actual, boolean expected) {
    if (expected == actual) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
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

  /* package */ void logUnexpectedException(String result, java.lang.Exception ex) {
    android.util.Log.e("SQLiteGlueTest", "UNEXPECTED EXCEPTION IN " + result, ex);
    resultsAdapter.add("UNEXPECTED EXCEPTION IN " + result + " : " + ex);
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

    SQLiteConnector myconnector = new SQLiteConnector();

    File dbfile = new File(getFilesDir(), "mytest.db");

    SQLiteConnection mydbc = null;

    try {
      mydbc = myconnector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (SQLException ex) {
      logUnexpectedException("DB open exception", ex);
      return;
    }

    SQLiteStatement mystatement;

    try {
      mystatement = mydbc.prepareStatement("SELECT UPPER('How about some ascii text?') AS caps");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    mystatement.step();

    int mycolcount = mystatement.getColumnCount();
    checkIntegerResult("SELECT UPPER() column count: ", mycolcount, 1);

    if (mycolcount > 0) {
      String colname = mystatement.getColumnName(0);
      checkStringResult("SELECT UPPER() caps column name", colname, "caps");

      int coltype = mystatement.getColumnType(0);
      checkIntegerResult("SELECT UPPER() caps column type", coltype, 3);

      String coltext = mystatement.getColumnTextNativeString(0);
      checkStringResult("SELECT UPPER() as caps", coltext, "HOW ABOUT SOME ASCII TEXT?");
    }

    mystatement.dispose();

    try {
      mystatement = mydbc.prepareStatement("DROP TABLE IF EXISTS mytable;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    mystatement.step();
    mystatement.dispose();

    try {
      mystatement = mydbc.prepareStatement("CREATE TABLE IF NOT EXISTS mytable (text1 TEXT, num1 INTEGER, num2 INTEGER, real1 REAL)");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    mystatement.step();
    mystatement.dispose();

    try {
      mystatement = mydbc.prepareStatement("INSERT INTO mytable (text1, num1, num2, real1) VALUES (?,?,?,?)");

      // should not get here:
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception (not expected)", ex);
      mydbc.dispose();
      return;
    }
    mystatement.bindTextNativeString(1, "test");
    mystatement.bindInteger(2, 10100);
    mystatement.bindLong(3, 0x1230000abcdL);
    mystatement.bindDouble(4, 123456.789);

    boolean keep_going = mystatement.step();
    while (keep_going) {
      logError("ERROR: ROW result NOT EXPECTED for INSERT");
      keep_going = mystatement.step();
    }
    mystatement.dispose();

    try {
      mystatement = mydbc.prepareStatement("SELECT * FROM mytable;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    keep_going = mystatement.step();
    checkBooleanResult("SELECT step result (keep_going flag)", keep_going, true);
    if (keep_going) {
      int colcount = mystatement.getColumnCount();
      checkIntegerResult("SELECT column count", colcount, 4);

      if (colcount >= 3) {
        int colid = 0;
        String colname;
        int coltype;
        String coltext;
        int intval;
        long longval;
        double doubleval;

        colname = mystatement.getColumnName(colid);
        checkStringResult("SELECT column " + colid + " name", colname, "text1");

        coltype = mystatement.getColumnType(colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLColumnType.TEXT);

        coltext = mystatement.getColumnTextNativeString(colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "test");

        ++colid;

        colname = mystatement.getColumnName(colid);
        checkStringResult("SELECT column " + colid + " name", colname, "num1");

        coltype = mystatement.getColumnType(colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLColumnType.INTEGER);

        coltext = mystatement.getColumnTextNativeString(colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "10100");
        intval = mystatement.getColumnInteger(colid);
        checkIntegerResult("SELECT column " + colid + " int value", intval, 10100);
        longval = mystatement.getColumnLong(colid);
        checkLongResult("SELECT column " + colid + " long value", longval, 10100);

        ++colid;

        colname = mystatement.getColumnName(colid);
        checkStringResult("SELECT column " + colid + " name", colname, "num2");

        coltype = mystatement.getColumnType(colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLColumnType.INTEGER);

        longval = mystatement.getColumnLong(colid);
        checkLongResult("SELECT column " + colid + " long value", longval, 0x1230000abcdL);

        ++colid;

        colname = mystatement.getColumnName(colid);
        checkStringResult("SELECT column " + colid + " name", colname, "real1");

        coltype = mystatement.getColumnType(colid);
        checkIntegerResult("SELECT column " + colid + " type", coltype, SQLColumnType.REAL);

        coltext = mystatement.getColumnTextNativeString(colid);
        checkStringResult("SELECT column " + colid + " text string", coltext, "123456.789");
        doubleval = mystatement.getColumnDouble(colid);
        checkDoubleResult("SELECT column " + colid + " double (real) value", doubleval, 123456.789);
      }

      keep_going = mystatement.step();
    }
    checkBooleanResult("SELECT step result (keep_going flag)", keep_going, false);

    mystatement.dispose();

    mydbc.dispose();

    // try to reopen database:
    try {
      mydbc = myconnector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (SQLException ex) {
      logUnexpectedException("DB open exception", ex);
      return;
    }

    // try to cleanup the table:
    try {
      mystatement = mydbc.prepareStatement("DROP TABLE IF EXISTS mytable;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    mystatement.step();
    mystatement.dispose();

    mydbc.dispose();

    checkIntegerResult("TEST error count", errorCount, 0);

    } catch (java.lang.Exception ex) {
      logUnexpectedException("unexpected exception", ex);
      return;
    }

  }
}
