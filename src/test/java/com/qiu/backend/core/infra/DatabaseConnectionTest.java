package com.qiu.backend.core.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testConnection() throws SQLException {
        // 尝试获取连接，如果失败会抛出异常
        try (var connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1000)); // 验证连接是否有效
            System.out.println("数据库连接成功!");
        }
    }
}