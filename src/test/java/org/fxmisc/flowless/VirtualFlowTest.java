package org.fxmisc.flowless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Rectangle;

public class VirtualFlowTest extends FlowlessTestBase {

    @Test
    public void idempotentShowTest() {
        // create VirtualFlow with 1 big cell
        Rectangle rect = new Rectangle(500, 500);
        ObservableList<Rectangle> items = FXCollections.singletonObservableList(rect);
        VirtualFlow<Rectangle, Cell<Rectangle, Rectangle>> vf = VirtualFlow.createVertical(items, Cell::wrapNode);
        vf.resize(100, 100); // size of VirtualFlow less than that of the cell
        vf.layout();

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

        WaitForAsyncUtils.waitForFxEvents();
        vf.setLengthOffset( 10 );
        vf.layout();

        assertEquals(-10.0, rect.getBoundsInParent().getMinY(), 0.01);
    }

    @Test
    public void fastVisibleIndexTest() {
        ObservableList<Rectangle> items = FXCollections.observableArrayList();
        for (int i = 0; i < 100; i++) {
            items.add(new Rectangle(500, 100));
        }

        VirtualFlow<Rectangle, Cell<Rectangle, Rectangle>> vf = VirtualFlow.createVertical(items, Cell::wrapNode);
        vf.resize(100, 450); // size of VirtualFlow enough to show several cells
        vf.layout();

        ObservableList<Cell<Rectangle,Rectangle>> visibleCells = vf.visibleCells();
        
        vf.show(0);
        vf.layout();
        assertSame(visibleCells.get(0), vf.getCell(vf.getFirstVisibleIndex()));
        assertSame(visibleCells.get(visibleCells.size() - 1), vf.getCell(vf.getLastVisibleIndex()));
        assertTrue(vf.getFirstVisibleIndex() <= 0 && 0 <= vf.getLastVisibleIndex());
        
        vf.show(50);
        vf.layout();
        assertSame(visibleCells.get(0), vf.getCell(vf.getFirstVisibleIndex()));
        assertSame(visibleCells.get(visibleCells.size() - 1), vf.getCell(vf.getLastVisibleIndex()));
        assertTrue(vf.getFirstVisibleIndex() > 40 && vf.getFirstVisibleIndex() <= 50);
        assertTrue(vf.getLastVisibleIndex() >= 50);

        vf.show(99);
        vf.layout();
        assertSame(visibleCells.get(0), vf.getCell(vf.getFirstVisibleIndex()));
        assertSame(visibleCells.get(visibleCells.size() - 1), vf.getCell(vf.getLastVisibleIndex()));
        assertTrue(vf.getFirstVisibleIndex() > 50 && vf.getFirstVisibleIndex() <= 99);
        assertTrue(vf.getLastVisibleIndex() >= 99);
    }
}
