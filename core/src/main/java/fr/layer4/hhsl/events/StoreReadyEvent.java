package fr.layer4.hhsl.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.jdbc.core.JdbcTemplate;

public class StoreReadyEvent extends ApplicationEvent {
    public StoreReadyEvent(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}
