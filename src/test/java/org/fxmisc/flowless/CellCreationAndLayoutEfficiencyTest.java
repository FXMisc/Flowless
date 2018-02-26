package org.fxmisc.flowless;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CellCreationAndLayoutEfficiencyTest extends FlowlessTestBase {

    private StackPane stackPane;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        stackPane = new StackPane();
        // 25 cells (each 16px high) fit into the viewport
        stage.setScene(new Scene(stackPane, 200, 400));
        stage.show();
    }

    @Test
    public void test() {
        Counter cellCreations = new Counter();
        Counter cellLayouts = new Counter();
        ObservableList<String> items = FXCollections.observableArrayList();
        for(int i = 0; i < 20; ++i) {
            items.addAll("red", "green", "blue", "purple");
        }
        VirtualFlow<String, ?> flow = VirtualFlow.createVertical(
                items,
                color -> {
                    cellCreations.inc();
                    Region reg = new Region() {
                        @Override
                        protected void layoutChildren() {
                            cellLayouts.inc();
                            super.layoutChildren();
                        }
                    };
                    reg.setPrefHeight(16.0);
                    reg.setStyle("-fx-background-color: " + color);
                    return Cell.wrapNode(reg);
                });

        interact(() -> stackPane.getChildren().add(flow));
        cellCreations.reset();
        cellLayouts.reset();

        // update an item in the viewport
        interact(() -> items.set(10, "yellow"));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals(1, cellLayouts.getAndReset());

        // update an item outside the viewport
        interact(() -> items.set(30, "yellow"));
        assertEquals(0, cellCreations.getAndReset());
        assertEquals(0, cellLayouts.getAndReset());

        // delete an item in the middle of the viewport
        interact(() -> items.remove(12));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals(1, cellLayouts.getAndReset());

        // add an item in the middle of the viewport
        interact(() -> items.add(12, "yellow"));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals(1, cellLayouts.getAndReset());

        // scroll 5 items down
        interact(() -> flow.showAsFirst(5));
        assertEquals(5, cellCreations.getAndReset());
        assertEquals(5, cellLayouts.getAndReset());

        // scroll 50 items down (only 25 fit into the viewport)
        interact(() -> flow.showAsFirst(55));
        assertEquals(25, cellCreations.getAndReset());
        assertEquals(25, cellLayouts.getAndReset());
    }
}