import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
public class Proccessor {
    public static ArrayList<String> sqls = new ArrayList<>();
    private static final ArrayList<Table> tablesQueue = new ArrayList<>();
    private static String com;


    public static void ReadDB(String db) {
        int i = 0;
        boolean isfin = true;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            // Get the tables
            ResultSet resSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (resSet.next()) new Table(resSet.getString("TABLE_NAME"));

            // Get the views
            resSet = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"});
            while (resSet.next()) new View(resSet.getString(3));


            // Add data to tables
            for (Table table : Table.Tables) {
                ResultSet columns = databaseMetaData.getColumns(null, null, table.getName(), null);
                if (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    String datatype = columns.getString("TYPE_NAME");
                    String isNullable = columns.getString("IS_NULLABLE");
                    /*File f = new File(dbName + ".txt");
                    BufferedReader bf = new BufferedReader(new FileReader(f));
                    String str;

                    while((str=bf.readLine())!=null){
                        String[] sent =str.split(";\n");*/
                    Column column = new Column(colName, isNullable, datatype);
                    table.addColumn(column);
                    ResultSet datas = connection.createStatement().executeQuery("SELECT `" + colName + "` FROM `" + table.getName() + "`");
                    while (datas.next()){
                        if (datatype.equals("INTEGER")){
                            if (datas.getString(1) == null || datas.getString(1).equals("\\N")) column.addData("null");
                            else column.addData(datas.getInt(1));
                        }else if (datatype.equals("BLOB")) column.addData(datas.getBytes(1));
                        else column.addData(datas.getString(1));

                    }
                }
                ResultSet PK = databaseMetaData.getPrimaryKeys(null, null, table.getName());

                if (PK.next()) table.addPrimaryKey(PK.getString("COLUMN_NAME"));

                ResultSet FK = databaseMetaData.getImportedKeys(null, null, table.getName());
                if (FK.next())
                    table.addForeignKey(FK.getString("PKTABLE_NAME"), FK.getString("PKCOLUMN_NAME"), FK.getString("FKTABLE_NAME"), FK.getString("FKCOLUMN_NAME"));

                ResultSet IN = databaseMetaData.getIndexInfo(null, null, table.getName(), false, false);
                if (IN.next()) table.addIndex(IN.getString("COLUMN_NAME"), IN.getString("NON_UNIQUE"),IN.getString("INDEX_NAME"));
            }

            // record view
            for (View view : View.Views){
                ResultSet datas = connection.createStatement().executeQuery("SELECT `sql` FROM `sqlite_master` WHERE `tbl_name` = \"" + view.getName() + "\"");
                while (datas.next()) view.setSql(datas.getString(1));
            }

            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return;
    }

    /**
     * Backup to files
     *
     * @param db        DB name
     * @param backupDB  back up to a .db file?
     * @param backupSql back up to a .sql file?
     * @return sqls in Strings
     */
    public static ArrayList<String> Backup(String db, boolean backupDB, boolean backupSql, boolean backupBat) {
        for (Table table : Table.Tables) tablesQueue.add(table);

        while(tablesQueue.size() > 0) CreateTableRecursively(tablesQueue.get(0));

        InsertDatas();
        if (backupSql) BackupSql(db);
        if (backupDB) BackupDB(db);
        if (backupBat) BackupBat(db);

        for(int i = 0; i <40;i++) System.out.print("\b");

        return sqls;
    }

    /**
     *  Create instructions and add in.

     */
    public static void CreateTableRecursively(Table table) {
        // check foreign keys
        for (ForeignKey fk : table.getForeignKeys()){
            // search by String name
            for (Table fkTable : tablesQueue){
                if (fkTable.getName().equals(fk.getTableA()) && !table.getName().equals(fk.getTableA())){
                    // recursively create the table which has foreign keys of it.
                    CreateTableRecursively(fkTable);
                    break;
                }
            }
        }

        String sql = "CREATE TABLE `" + table.getName() + "`(";
        for (Column column : table.getColumns())
            sql += "`" + column.getColumnName() + "` "
                    + column.getDatatype()
                    + (column.getIsNullable().equals("N") ? " NOT NULL," : ",");


        if (table.getPrimaryKeys().size() != 0) {
            sql += "PRIMARY KEY (";
            for (String str : table.getPrimaryKeys()) sql += str + ",";
            sql = sql.substring(0, sql.length() - 1) + "),";
        }

        for (ForeignKey fk : table.getForeignKeys())
            sql += "FOREIGN KEY (" +
                    "`" + fk.getColumnB() + "`" +
                    ") REFERENCES " +
                    "`" + fk.getTableA() + "`(" +
                    "`" + fk.getColumnA() + "`),";

        sql = sql.substring(0, sql.length() - 1) + ")";

        sqls.add("DROP TABLE IF EXISTS `" + table.getName()+ "`");

        sqls.add(sql);

        if (table.getIndexes() != null)
            sqls.add("CREATE " + table.getIndexes().getNonUnique() + "INDEX "
                    + table.getIndexes().getIndexName() + " ON `"
                    + table.getName()
                    + "` (" + table.getIndexes().getColumnName() + " ASC)");

        tablesQueue.remove(table);
    }

    /**
     * Generate INSERT instructions and add them in sqls.
     */

    public static void InsertDatas() {
        float progress = 0;
        for (Table table : Table.Tables) {
            for (int i = 0; i < table.getColumns().get(0).getDatas().size(); i++) {
                String sql = "INSERT INTO `" + table.getName() + "` VALUES(";
                for (Column column : table.getColumns()) {
                    if (column.getDatatype().startsWith("VARCHAR")) sql += "\"" + column.getDatas().get(i).getS() + "\",";
                    else if (column.getDatatype().startsWith("BLOB")) sql += "\"" + column.getDatas().get(i).getB() +"\",";
                    else if (column.getDatatype().startsWith("INTEGER")) sql += column.getDatas().get(i).getI() + ",";
                    else sql += "\"" + column.getDatas().get(i).getS() + "\",";
                }
                sql = sql.substring(0, sql.length() - 1) + ")";
                sqls.add(sql);
            }
            for(int i = 0; i <40;i++) System.out.print("\b");
            System.out.print("##\tINSERT DATA: " + String.format("%-2f2", 100 * progress++ / Table.Tables.size()) + "%\t##");
        }

        // create view
        for (View view : View.Views) sqls.add(view.getSql());
    }

    public static void BackupDB(String db) {
        // delete backup if exist
        new File(db + ".db").delete();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            assert connection != null;
            Statement statement = connection.createStatement();
            float progress = 0;
            for (String sql : sqls) {
                statement.executeUpdate(sql);
                for(int i = 0; i <40;i++) System.out.print("\b");
                System.out.print("##\tBACKUP DB:   " + String.format("%-2f2", 100 * progress++ / sqls.size()) + "%\t##");
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void BackupSql(String db) {
        new File(db + ".sql").delete();
        File file = new File(db + ".sql");

        try {
            OutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(com + "\n");
            float progress = 0;
            /*
        try {
             File f = new File(dbName + ".txt");
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String str;

            while((str=bf.readLine())!=null){
                String[] sent =str.split(";\n");*/
            for (String sql : sqls) {
                outputStreamWriter.append(sql + ";\n");
                for(int i = 0; i <40;i++) System.out.print("\b");
                System.out.print("##\tBACKUP SQL:  " + String.format("%-2f2", 100 * progress++ / sqls.size()) + "%\t##");
            }
            outputStreamWriter.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param db
     */
    public static void BackupBat(String db) {
        new File("do.bat").delete();
        File do_file = new File("do.bat");
        new File(db + ".bat").delete();
        File db_file = new File(db + ".bat");

        try {
            OutputStream outputStream = new FileOutputStream(do_file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append("For /L %%i in (1,1,10) do (sqlite3 "+ db + ".db<"+ db + ".bat)");
            outputStreamWriter.close();
            outputStream.close();

            outputStream = new FileOutputStream(db_file);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            float progress = 0;
            for (String sql : sqls) {
                outputStreamWriter.append(sql + ";\n");
                for(int i = 0; i <40;i++) System.out.print("\b");
                System.out.print("##\tBACKUP BAT:  " + String.format("%-2f2", 100 * progress++ / sqls.size()) + "%\t##");
            }
            outputStreamWriter.close();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
