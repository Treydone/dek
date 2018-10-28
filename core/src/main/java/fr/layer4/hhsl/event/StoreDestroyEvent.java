package fr.layer4.hhsl.event;

import org.springframework.context.ApplicationEvent;

public class StoreDestroyEvent extends ApplicationEvent {
    public StoreDestroyEvent(Object source) {
        super(source);
    }
}