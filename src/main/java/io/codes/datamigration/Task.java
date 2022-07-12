package io.codes.datamigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codes.datamigration.vo.DataSourceVO;
import io.codes.datamigration.vo.TaskVO;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Task {
    final private Map<String, DataSource> dataSources = new HashMap<>();
    final private Map<String, JdbcTemplate> jdbcTemplateMap = new HashMap<>();
    private String sourceSql;
    private String destinationTable;
    private List<ColumnMapper> columnMappings;

    public Task() {
    }

    public Task(TaskVO vo) {
        setProperties(vo);
    }

    public Task(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            TaskVO vo = mapper.readValue(jsonString, TaskVO.class);
            setProperties(vo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public Map<String, JdbcTemplate> getJdbcTemplateMap() {
        return jdbcTemplateMap;
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

    public List<ColumnMapper> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(List<ColumnMapper> columnMappings) {
        this.columnMappings = columnMappings;
    }

    public DataSource getDataSource(String name) {
        return this.getDataSources().get(name);
    }

    public JdbcTemplate getTemplate(String name) {
        return this.getJdbcTemplateMap().get(name);
    }

    public void addDataSource(DataSourceVO vo) {
        DriverManagerDataSource ds = new DriverManagerDataSource(vo.getJdbcUrl());
        ds.setDriverClassName(vo.getDriverClassName());
        ds.setUrl(vo.getJdbcUrl());
        ds.setUsername(vo.getUserName());
        ds.setPassword(vo.getPassword());
        dataSources.put(vo.getName(), ds);

        this.getDataSources().put(vo.getName(), ds);
        this.getJdbcTemplateMap().put(vo.getName(), new JdbcTemplate(ds));
    }
    public void setProperties(TaskVO vo) {
        sourceSql = vo.getSourceSql();
        destinationTable = vo.getDestinationTable();
        columnMappings = vo.getColumnMappings().stream().map(ColumnMapper::new).collect(Collectors.toList());
        vo.getDataSources().forEach(this::addDataSource);
    }

    public String generateSyncSql() {
        return "INSERT INTO " + this.getDestinationTable() +
                "(" +
                this.getColumnMappings().stream().map(ColumnMapper::getDestinationColumn).collect(Collectors.joining(",")) +
                ") VALUES(" +
                this.getColumnMappings().stream().map(m -> "?").collect(Collectors.joining(",")) +
                ")";
    }

    public void sync() {
        String sql = generateSyncSql();

        JdbcTemplate sourceTemplate = jdbcTemplateMap.get("source");
        List<Map<String, Object>> list = sourceTemplate.query(this.getSourceSql(), new ColumnMapRowMapper());

        JdbcTemplate destTemplate = jdbcTemplateMap.get("dest");
        for (Map<String, Object> row : list) {
            destTemplate.update(sql, ps -> {
                int index = 1;
                for (ColumnMapper mapper : this.getColumnMappings()) {
                    String sourceColumn = mapper.getSourceColumn();
                    ps.setObject(index, row.get(sourceColumn));
                    index++;
                }
            });
        }
    }


}
