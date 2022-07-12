package io.codes.datamigration;

import io.codes.datamigration.vo.ColumnMapperVO;
import org.springframework.beans.BeanUtils;

public class ColumnMapper {
    private String sourceColumn;
    private String destinationColumn;

    public ColumnMapper() {

    }

    public ColumnMapper(ColumnMapperVO vo) {
        this.sourceColumn = vo.getSourceColumn();
        this.destinationColumn = vo.getDestinationColumn();
    }

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
