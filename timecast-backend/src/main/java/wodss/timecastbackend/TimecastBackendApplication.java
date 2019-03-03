package wodss.timecastbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import wodss.timecastbackend.util.ModelMapper;

import static javafx.scene.input.KeyCode.R;

@SpringBootApplication
public class TimecastBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimecastBackendApplication.class, args);
	}
}
