package fr.layer4.hhsl.event;

import org.springframework.context.ApplicationEvent;

public class LockedEvent extends ApplicationEvent {
    public LockedEvent(Object source) {
        super(source);
    }
}
