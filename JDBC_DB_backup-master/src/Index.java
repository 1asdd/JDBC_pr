public class Index {
    private String nUnique;
    private String colName;
    private String indexName;

    public Index(String colName, String nUnique, String indexName) {
        //index
        this.colName = "`" + colName + "`";
        this.nUnique = nUnique.equals("0") ? "" : "UNIQUE ";
        this.indexName = "`" + indexName + "`";
    }

    public void addColumn(String colName) {
        this.colName += ",`" + colName + "`";
    }
    public String getIndexName() {
        return indexName;
    }
    public String getColumnName() {
        return colName;
    }
    public String getNonUnique() {
        return nUnique;
    }
}
