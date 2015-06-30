# Android sqlite connector

Java classes with abstract interface layers to provide a simple, easy-to-use Java interface to the andriod-sqlite-native-driver library (may be adapted for other Java environments).

With a simple test Android app included.

Unlicense (public domain).

## Dependencies

Not included:
- `sqlite-native-driver.jar` with native Android libraries, as built in [liteglue / Android-sqlite-native-driver](https://github.com/liteglue/Android-sqlite-native-driver)

Included:
- `src/io/liteglue/SQLiteNative.java` - low-level Java class from  [liteglue / Android-sqlite-native-driver](https://github.com/liteglue/Android-sqlite-native-driver)

## Installation

Include the following `jar` files in your `libs` directory:
- `sqlite-native-driver.jar`, as built in [liteglue / Android-sqlite-native-driver](https://github.com/liteglue/Android-sqlite-native-driver)
- `sqlite-connector.jar`, which is built by simply issuing the `make` command in this project

## Testing

There is a simple test project in the `Android-SQLiteConnectorTest` subdirectory. To test:
- `cd Android-SQLiteConnectorTest`
- copy the `jar` files into the `libs` subdirectory of `Android-SQLiteConnectorTest` as described above
- install it on your emulator or device: `ant debug install`
- run it on your emulator or device

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

