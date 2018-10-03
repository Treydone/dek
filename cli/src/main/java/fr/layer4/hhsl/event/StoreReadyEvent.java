package fr.layer4.hhsl.event;

import org.springframework.context.ApplicationEvent;

public class StoreReadyEvent extends ApplicationEvent {
    public StoreReadyEvent(Object source) {
        super(source);
    }
}
