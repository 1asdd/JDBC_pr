import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String args[])
    {
        String DBname = "LSH";
        boolean backup_DB = true;
        boolean backup_Sql = true;
        boolean backup_Bat = true;
        boolean isExist=true;
        File f = new File(DBname + ".txt");
        //BufferedReader bf = new BufferedReader(new FileReader(f));
       /*      System.out.println("enter a database name: ");
        Scanner s = new Scanner(System.in);
        String dbName = s.nextLine();
        DbUser myDbUser = null;
        myDbUser = new DbUser(dbName + ".db");
        PrintStream ps = System.out;*/
        if (args.length != 0) DBname = args[0];
        if (args.length > 1) backup_DB = args[1].equals("true") ? true : false;
        if (args.length > 2) backup_Sql = args[2].equals("true") ? true : false;
        if (args.length > 3) backup_Bat = args[3].equals("true") ? true : false;
/*      ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(dbName + ".txt")), true);
        System.setOut(ps);
        */
        // check whether exist
        if (new File(DBname + ".db").exists())
            Proccessor.ReadDB(DBname);
        //    System.out.println(isExist);
        else
            System.out.println(DBname + ".db not exist.");
            isExist=false;
        //    System.out.println(isExist);
        // return sqls and print
        ArrayList<String> sqls = Proccessor.Backup(DBname + "_backup",backup_DB,backup_Sql,backup_Bat);

        for (String sql: sqls)
            System.out.println(sql + ";");
        //    System.out.println(isExist);
    }
}