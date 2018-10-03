package fr.layer4.hhsl.event;

import org.springframework.context.ApplicationEvent;

public class UnlockedEvent extends ApplicationEvent {
    public UnlockedEvent(Object source) {
        super(source);
    }
}
