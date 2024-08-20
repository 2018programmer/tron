package com.dx.futures.local;

public interface Listener {
    Listener EMPTY = new Listener(){};
    default void onEvent( EventType type, Object data){
    }
}
