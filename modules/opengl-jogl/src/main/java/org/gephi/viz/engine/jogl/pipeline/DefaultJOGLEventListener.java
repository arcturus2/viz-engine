package org.gephi.viz.engine.jogl.pipeline;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.util.actions.InputActionsProcessor;
import org.joml.Vector2f;

/**
 *
 * @author Eduardo Ramos
 */
public class DefaultJOGLEventListener implements InputListener<JOGLRenderingTarget, NEWTEvent> {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final InputActionsProcessor inputActionsProcessor;

    private static final short MOUSE_LEFT_BUTTON = MouseEvent.BUTTON1;
    private static final short MOUSE_WHEEL_BUTTON = MouseEvent.BUTTON2;
    private static final short MOUSE_RIGHT_BUTTON = MouseEvent.BUTTON3;
    private boolean mouseRightButtonPresed = false;
    private boolean mouseLeftButtonPresed = false;

    public DefaultJOGLEventListener(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        this.engine = engine;
        this.inputActionsProcessor = new InputActionsProcessor(engine);
    }

    private MouseEvent lastMovedPosition = null;

    @Override
    public void frameStart() {
        lastMovedPosition = null;
    }

    @Override
    public void frameEnd() {
        if (lastMovedPosition != null) {
            //TODO: move to independent selection input listener
            final Vector2f worldCoords = engine.screenCoordinatesToWorldCoordinates(lastMovedPosition.getX(), lastMovedPosition.getY());

            inputActionsProcessor.selectNodesUnderPosition(worldCoords);
        }
    }

    @Override
    public boolean processEvent(NEWTEvent event) {
        if (event instanceof KeyEvent) {
            return false;
        } else if (event instanceof MouseEvent) {
            final MouseEvent mouseEvent = (MouseEvent) event;

            switch (event.getEventType()) {
                case MouseEvent.EVENT_MOUSE_CLICKED:
                    return this.mouseClicked(mouseEvent);
                case MouseEvent.EVENT_MOUSE_DRAGGED:
                    return this.mouseDragged(mouseEvent);
                case MouseEvent.EVENT_MOUSE_MOVED:
                    return this.mouseMoved(mouseEvent);
                case MouseEvent.EVENT_MOUSE_PRESSED:
                    return this.mousePressed(mouseEvent);
                case MouseEvent.EVENT_MOUSE_RELEASED:
                    return this.mouseReleased(mouseEvent);
                case MouseEvent.EVENT_MOUSE_WHEEL_MOVED:
                    return this.mouseWheelMoved(mouseEvent);
                case MouseEvent.EVENT_MOUSE_ENTERED:
                case MouseEvent.EVENT_MOUSE_EXITED:
                default:
                    return false;
            }
        }

        return false;
    }

    public boolean mouseClicked(MouseEvent e) {
        boolean leftClick = e.getClickCount() == 1 && e.getButton() == MOUSE_LEFT_BUTTON;
        boolean doubleLeftClick = e.getClickCount() == 2 && e.getButton() == MOUSE_LEFT_BUTTON;
        boolean doubleRightClick = e.getClickCount() == 2 && e.getButton() == MOUSE_RIGHT_BUTTON;
        boolean wheelClick = e.getButton() == MOUSE_WHEEL_BUTTON;

        final int x = e.getX();
        final int y = e.getY();

        if (wheelClick) {
            inputActionsProcessor.processCenterOnGraphEvent();
            return true;
        } else if (doubleLeftClick) {
            //Zoom in:
            inputActionsProcessor.processZoomEvent(10, x, y);
            return true;
        } else if (doubleRightClick) {
            //Zoom out:
            inputActionsProcessor.processZoomEvent(-10, x, y);
            return true;
        } else if (leftClick) {
            //TODO: move to independent selection input listener
            final Vector2f worldCoords = engine.screenCoordinatesToWorldCoordinates(x, y);
            System.out.println(String.format(
                    "Click on %s %s = %s, %s", x, y, worldCoords.x, worldCoords.y
            ));

            return true;
        }

        return false;
    }

    public boolean mousePressed(MouseEvent e) {
        if (e.getButton() == MOUSE_LEFT_BUTTON) {
            mouseLeftButtonPresed = true;
        }

        if (e.getButton() == MOUSE_RIGHT_BUTTON) {
            mouseRightButtonPresed = true;
        }

        lastX = e.getX();
        lastY = e.getY();

        return false;
    }

    public boolean mouseReleased(MouseEvent e) {
        if (e.getButton() == MOUSE_LEFT_BUTTON) {
            mouseLeftButtonPresed = false;
        }

        if (e.getButton() == MOUSE_RIGHT_BUTTON) {
            mouseRightButtonPresed = false;
        }

        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        lastMovedPosition = e;

        return true;
    }

    public boolean mouseDragged(MouseEvent e) {
        try {
            if (mouseLeftButtonPresed && mouseRightButtonPresed) {
                //Zoom in/on the screen center with both buttons pressed and vertical movement:
                double zoomQuantity = (lastY - e.getY()) / 7f;//Divide by some number so zoom is not too fast
                inputActionsProcessor.processZoomEvent(zoomQuantity, engine.getWidth() / 2, engine.getHeight() / 2);
                return true;
            } else if (mouseLeftButtonPresed || mouseRightButtonPresed) {
                inputActionsProcessor.processCameraMoveEvent(e.getX() - lastX, e.getY() - lastY);
                return true;
            }
        } finally {
            lastX = e.getX();
            lastY = e.getY();
        }

        return false;
    }

    public boolean mouseWheelMoved(MouseEvent e) {
        float[] rotation = e.getRotation();
        float verticalRotation = rotation[1] * e.getRotationScale();

        inputActionsProcessor.processZoomEvent(verticalRotation, e.getX(), e.getY());

        return true;
    }

    private int lastX;
    private int lastY;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getCategory() {
        return "default";
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return true;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }
}
