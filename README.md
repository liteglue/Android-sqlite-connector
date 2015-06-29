# Android SQLiteConnector

Java classes with abstract interface layers to provide a simple, easy-to-use Java interface to sqlite library. Tested with Android, may be adapted for other Java applications.

With a simple test Android app.

Unlicense (public domain).

## Dependencies

Not included:
- `libs/armeabi/libsqlg.so`, as built in [sqlg / SQLiteGlue-core](https://github.com/sqlg/SQLiteGlue-core) (for Android)

Included:
- `src/org/sqlg/SQLiteGlue.java` - low-level Java class that provides 

## Installation

- Include the contents of `src/org/sqlg` (`src/org/sqlg/SQLiteGlue.java`) and `src/net/sqlc` in your source code (or build separately)
- Include the contents of `libs` (`libs/armeabi/libsqlg.so`) which must be built in [sqlg / SQLiteGlue-core](https://github.com/sqlg/SQLiteGlue-core)

## Sample API Usage

**IMPORTANT:** Most of the methods described here will throw `java.sql.SQLException` if the sqlite library reports an error or if they detect a problem with the usage.

### First step

Get a SQLiteConnector factory instance:

```Java
SQLiteConnector myconnector = new SQLiteGlueConnector();
```

### Open a database

```Java
File dbfile = new File(getFilesDir(), "my.db");

SQLiteConnection mydbc = myconnector.newSQLiteConnection(dbfile.getAbsolutePath(),
    SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
```

### Prepare and run a simple statement (with no parameters)

```Java
SQLiteStatement mystatement = mydbc.prepareStatement("CREATE TABLE IF NOT EXISTS mytable (text1 TEXT, num1 INTEGER, num2 INTEGER, real1 REAL)");
mystatement.step();
mystatement.dispose();
```

**IMPORTANT:** Whenever `SQLiteConnection.prepareStatement()` successfully returns a `SQLiteStatement`, it must be cleaned up using its `dispose()` method.

### Prepare and run a statement with parameter values

```Java
SQLiteStatement mystatement = mydbc.prepareStatement("INSERT INTO mytable (text1, num1, num2, real1) VALUES (?,?,?,?)");

mystatement.bindTextNativeString(1, "test");
mystatement.bindInteger(2, 10100);
mystatement.bindLong(3, 0x1230000abcdL);
mystatement.bindDouble(4, 123456.789);

mystatement.step();
mystatement.dispose();
```

### SELECT data and get row result(s)

```Java
SQLiteStatement mystatement = mydbc.prepareStatement("SELECT * FROM mytable;");

boolean keep_going = mystatement.step();
while (keep_going) {
    int colcount = colcount = mystatement.getColumnCount();
    android.util.Log.e("MySQLiteApp", "Row with " + colcount + " columns");

    for (int i=0; i<colcount; ++i) {
        int coltype = mystatement.getColumnType(i);
        switch(coltype) {
        case SQLColumnType.INTEGER:
            android.util.Log.e("MySQLiteApp",
                "Col " + i + " type: INTEGER (long) value: 0x" +
                    java.lang.Long.toHexString(mystatement.getColumnLong(i)));
            break;

        case SQLColumnType.REAL:
            android.util.Log.e("MySQLiteApp",
                "Col " + i + " type: REAL value: " + mystatement.getColumnDouble(i));
            break;

        case SQLColumnType.NULL:
            android.util.Log.e("MySQLiteApp", "Col " + i + " type: NULL (no value)");
            break;

        default:
            android.util.Log.e("MySQLiteApp",
                "Col " + i + " type: " + ((coltype == SQLColumnType.BLOB) ? "BLOB" : "TEXT") +
                    " value: " + mystatement.getColumnTextNativeString(i));
            break;
        }
    }
    keep_going = mystatement.step();
}
mystatement.dispose();
```

### Close the database connection

```Java
mydbc.dispose();

```

## Internals

- Multiple layers with abstract interfaces to make it easier to replace the lower-level SQLiteGlue native database interface

