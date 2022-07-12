package io.codes.datamigration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TaskTest {

    private Task task;

    @BeforeAll
    void setup() throws IOException, SQLException {
        try (InputStream stream = this.getClass().getResourceAsStream("/test.json")) {
            String jsonString = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            task = new Task(jsonString);
        }

        JdbcTemplate destTemplate = task.getTemplate("dest");
        Objects.requireNonNull(destTemplate.getDataSource()).getConnection().setAutoCommit(false);
        destTemplate.execute("CREATE TABLE TBL_EMPLOYEES(emp_no varchar(100), name varchar(100), display_name varchar(100), primary key(emp_no))");
        destTemplate.getDataSource().getConnection().commit();
        destTemplate.getDataSource().getConnection().setAutoCommit(true);

        JdbcTemplate sourceTemplate = task.getTemplate("source");
        Objects.requireNonNull(sourceTemplate.getDataSource()).getConnection().setAutoCommit(false);
        sourceTemplate.execute("CREATE TABLE RESAK(resak001 varchar(100), resak002 varchar(100), resak003 varchar(100), primary key(resak001))");
        sourceTemplate.getDataSource().getConnection().commit();
        sourceTemplate.getDataSource().getConnection().setAutoCommit(true);
        for(int i = 1; i <= 10; i++) {
            String index = String.format("%2d", i);
            sourceTemplate.update("INSERT INTO RESAK VALUES(?,?,?)", ps -> {
                ps.setString(1, index);
                ps.setString(2, "NAME_" + index);
                ps.setString(3, "DISPLAY_NAME_" + index);
            });
        }
    }


    @Test
    void generateSyncSql() {
        String expected = "INSERT INTO TBL_EMPLOYEES(emp_no,name,display_name) VALUES(?,?,?)";
        String actually = task.generateSyncSql();
        assertThat(actually).as("测试产生SQL").isEqualTo(expected);
    }

    @Test
    void sync() {
        task.sync();

        List<Map<String, Object>> list = task.getTemplate("dest").query("SELECT * FROM TBL_EMPLOYEES", new ColumnMapRowMapper());
        assertThat(list.size()).isEqualTo(10);
        for(int i = 1; i <= 10; i++) {
            String index = String.format("%2d", i);
            Map<String, Object> row = list.get(i - 1);
            assertThat(row.get("emp_no").toString()).isEqualTo(index);
            assertThat(row.get("name").toString()).isEqualTo("NAME_" + index);
            assertThat(row.get("display_name").toString()).isEqualTo("DISPLAY_NAME_" + index);
        }
    }
}