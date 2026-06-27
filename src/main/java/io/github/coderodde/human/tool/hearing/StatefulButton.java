package io.github.coderodde.human.tool.hearing;

import javafx.scene.control.Button;

/**
 * This class implements a button with state information.
 */
public final class StatefulButton extends Button {
    
    public enum State {
        START,
        STOP,
        CONTINUE;
    }
    
    private State state = State.START;
    
    public StatefulButton() {
        super("Start");
    }
    
    public State getState() {
        return state;
    }
    
    public void click() {
        switch (state) {
            case START:
                setText("Pause");
                state = State.CONTINUE;
                break;
                
            case CONTINUE:
                setText("Continue");
                state = State.STOP;
                break;
                
            case STOP:
                setText("Pause");
                state = State.CONTINUE;
                break;
                
            default:
                throw new EnumConstantNotPresentException(State.class, 
                                                          state.name());
        }
    }
}
