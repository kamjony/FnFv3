package org.lanaeus.fnfv3;

/**
 * Created by KamrulHasan on 3/11/2018.
 */

class Chats {

    private boolean seen;
    private long timestamp;

    public Chats() {
    }

    public Chats(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
