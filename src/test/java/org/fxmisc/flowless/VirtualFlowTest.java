package org.fxmisc.flowless;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.shape.Rectangle;

import org.junit.BeforeClass;
import org.junit.Test;

public class VirtualFlowTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JFXPanel();
    }

    @Test
    public void idempotentShowTest() {
        // create VirtualFlow with 1 big cell
        Rectangle rect = new Rectangle(500, 500);
        ObservableList<Rectangle> items = FXCollections.singletonObservableList(rect);
        VirtualFlow<Rectangle, Cell<Rectangle, Rectangle>> vf = VirtualFlow.createVertical(items, Cell::wrapNode);
        vf.resize(100, 100); // size of VirtualFlow less than that of the cell
        vf.layout();

        vf.show(110.0);
        vf.show(110.0);
        vf.layout();
        assertEquals(-10.0, rect.getBoundsInParent().getMinY(), 0.01);
    }

    @Test
    public void idempotentSetLengthOffsetTest() {
        // create VirtualFlow with 1 big cell
        Rectangle rect = new Rectangle(500, 500);
        ObservableList<Rectangle> items = FXCollections.singletonObservableList(rect);
        VirtualFlow<Rectangle, Cell<Rectangle, Rectangle>> vf = VirtualFlow.createVertical(items, Cell::wrapNode);
        vf.resize(100, 100); // size of VirtualFlow less than that of the cell
        vf.layout();

        vf.setLengthOffset(10.0);
        vf.setLengthOffset(10.0);
        vf.layout();
        assertEquals(-10.0, rect.getBoundsInParent().getMinY(), 0.01);
    }

}
