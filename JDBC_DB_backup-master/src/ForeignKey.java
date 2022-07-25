public class ForeignKey {
    private String table_A;
    private String table_B;
    private String column_A;
    private String column_B;

    public ForeignKey(String ta, String ca, String tb, String cb){
        table_A = ta;
        table_B = tb;
        column_A = ca;
        column_B = cb;
    }

    public String getColumnA() {
        return column_A;
    }

    public String getColumnB() {
        return column_B;
    }

    public String getTableA() {
        return table_A;
    }

    public String getTableB() {
        return table_B;
    }

    public String[] getAll(){
        String[] x = {table_A,column_A,table_B,column_B};
        return x;
    }
}
