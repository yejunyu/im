import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/12/17
 **/
@Ignore
public class JdbcTest {

    @Test
    public void jdbcTest() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:mysql://127.0.0.1:3306/test",
                "root", "123456"
        );
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int update = jdbcTemplate.update("insert into customer(name,age) values ('yy','18')");
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from customer");
        for (Map<String, Object> customer : maps) {
            System.out.println(customer);
        }
    }
}
