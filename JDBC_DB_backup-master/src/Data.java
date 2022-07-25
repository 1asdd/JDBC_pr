import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
public class Data {
    public static int picNum = 0;
    private char c;
    private byte[] b;
    private float f;
    private double d;
    private int i;
    private String s;




    public Data(String x){
        if (x != null) s = x.replace("\"","'");
    }

    public Data(double x){
        d = x;
    }
    public Data(byte[] x){
        b = x;
    }
    public Data(float x){
        f = x;
    }
    public Data(char x){
        c = x;
    }
    public Data(int x){
        i = x;
    }


    public String getB() {
        String str = "0x";
        for (byte by : b) str += Integer.toHexString( by & 0xFF);
/*
        try {
             File f = new File(dbName + ".txt");
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String str;

            while((str=bf.readLine())!=null){
                String[] sent =str.split(";\n");*/
        return str;
    }

    public char getC() {
        return c;
    }

    public double getD() {
        return d;
    }

    public float getF() {
        return f;
    }

    public int getI() {
        return i;
    }

    public String getS() {
        return s;
    }
}
