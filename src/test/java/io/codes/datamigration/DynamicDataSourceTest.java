package io.codes.datamigration;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;


import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DynamicDataSourceTest {

    @Test
    void testDBConnect() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:test");
        dataSource.setDriverClassName("org.h2.Driver");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        ClassPathResource script = new ClassPathResource("DynamicDataSourceTest.sql");
        EncodedResource encodedScript = new EncodedResource(script, StandardCharsets.UTF_8);
        ScriptUtils.executeSqlScript(dataSource.getConnection(), encodedScript);

        // jdbcTemplate.update("INSERT INTO tbl_user values(1)");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_user", Integer.class);

        assertThat(count).as("测试Count").isGreaterThan(0);

        List<Map<String, Object>> list = jdbcTemplate.query("SELECT * FROM tbl_user", new ColumnMapRowMapper());
        assertThat(list.size()).as("测试返回笔数").isEqualTo(1);
        assertThat(list.get(0).containsKey("id")).as("测试字段名称").isTrue();
    }

    @Test
    void testDataSource() throws SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("Test2Pool");
        dataSource.setMaximumPoolSize(10);
        dataSource.setJdbcUrl("jdbc:h2:mem:test2");
        dataSource.setDriverClassName("org.h2.Driver");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        dataSource.getConnection().setAutoCommit(false);
        jdbcTemplate.execute("CREATE TABLE tbl_user(id integer)");
        dataSource.getConnection().commit();
        dataSource.getConnection().setAutoCommit(true);
        jdbcTemplate.update("INSERT INTO tbl_user values(1)");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_user", Integer.class);

        assertThat(count).as("测试 Hikari Count").isGreaterThan(0);

        List<Map<String, Object>> list = jdbcTemplate.query("SELECT * FROM tbl_user", new ColumnMapRowMapper());
        assertThat(list.size()).as("测试 Hikari 返回笔数").isEqualTo(1);
        assertThat(list.get(0).containsKey("id")).as("测试 Hikari 字段名称").isTrue();
    }
}
