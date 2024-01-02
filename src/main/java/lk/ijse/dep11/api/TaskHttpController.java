package lk.ijse.dep11.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.to.TaskTO;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.sql.*;

@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin
public class TaskHttpController {
    private final HikariDataSource pool;

    public TaskHttpController() {
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("1234");
        config.setJdbcUrl("jdbc:postgresql://localhost:3306/dep11_todo_app");
        config.setDriverClassName("org.postgresql.Driver");
        config.addDataSourceProperty("maximumPoolSize", 10);
        pool = new HikariDataSource(config);
    }

    @PreDestroy
    public void destroy() {
        pool.close();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public TaskTO createTask(@RequestBody @Validated TaskTO task) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection
                    .prepareStatement("INSERT INTO task (description, status, email) VALUES (?, FALSE, ?)",
                            Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, task.getDescription());
            stm.setString(2, task.getEmail());
            stm.executeUpdate();
            ResultSet generatedKeys = stm.getGeneratedKeys();
            generatedKeys.next();
            int id = generatedKeys.getInt(1);
            task.setId(id);
            task.setStatus(false);
            return task;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

