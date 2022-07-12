package io.codes.datamigration.vo;

public class ColumnMapperVO {
    private String sourceColumn;
    private String destinationColumn;

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getDestinationColumn() {
        return destinationColumn;
    }

    public void setDestinationColumn(String destinationColumn) {
        this.destinationColumn = destinationColumn;
    }
}
