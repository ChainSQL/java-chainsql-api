package com.peersafe.base.client.pubsub;

public abstract class CallbackContext {
    public void execute(Runnable runnable) {
        runnable.run();
    }
    // TODO: perhaps this interface/class/concept
    // can be expanded (and subsequently renamed .. CallbackContext ??)
    // to automatically remove an ContextedCallback
    // Perhaps `once` can be reimplemented in these terms too ;)
    public boolean shouldExecute() {
        return true;
    }
    /**
     * shouldRemove
     * @return return value.
     */
    public boolean shouldRemove() {
        return false;
    }
}
