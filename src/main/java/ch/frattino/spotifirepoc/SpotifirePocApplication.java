package ch.frattino.spotifirepoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpotifirePocApplication implements CommandLineRunner {

	private static Logger LOG = LoggerFactory.getLogger(SpotifirePocApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpotifirePocApplication.class, args);
	}

	@Override
	public void run(String... args) {
		LOG.info("Run the various unit tests.");
	}
}
