import java.util.ArrayList;
public class View {

    public static ArrayList<View> Views = new ArrayList<>();

    private String name;
    private String sql;

    public View(String name){
        this.name = name;
        Views.add(this);
    }

    public String getName() {
        return name;
    }

    public void setSql(String sql){
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
