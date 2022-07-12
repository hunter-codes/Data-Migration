package io.codes.datamigration.vo;
import java.util.List;

public class TaskVO {
    private List<DataSourceVO> dataSources;
    private String sourceSql;
    private String destinationTable;
    private List<ColumnMapperVO> columnMappings;

    public List<DataSourceVO> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSourceVO> dataSources) {
        this.dataSources = dataSources;
    }

    public String getSourceSql() {
        return sourceSql;
    }

    public void setSourceSql(String sourceSql) {
        this.sourceSql = sourceSql;
    }

    public String getDestinationTable() {
        return destinationTable;
    }

    public void setDestinationTable(String destinationTable) {
        this.destinationTable = destinationTable;
    }

    public List<ColumnMapperVO> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(List<ColumnMapperVO> columnMappings) {
        this.columnMappings = columnMappings;
    }
}
