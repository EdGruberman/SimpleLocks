package edgruberman.bukkit.simplelocks.messaging;

import edgruberman.bukkit.simplelocks.messaging.messages.Confirmation;

public interface Recipients {

    public abstract Confirmation send(Message message);

}
