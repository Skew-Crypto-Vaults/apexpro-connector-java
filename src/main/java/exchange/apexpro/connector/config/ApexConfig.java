package exchange.apexpro.connector.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
@Slf4j
public class ApexConfig {
    private Properties properties;
    public ApexConfig() {
        this.properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                log.error("Sorry, unable to find application.properties");
              throw  new RuntimeException("Sorry, unable to load default token for global context");
            }
            this.properties.load(input);
        } catch (IOException ex) {
            log.error("Sorry, unable to load application.properties",ex);
        }
    }
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

}
