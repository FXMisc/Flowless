Flowless
========

Please note that as from version **0.6.5** Flowless requires Java 9 or newer.

Efficient VirtualFlow for JavaFX. VirtualFlow is a layout container that lays out _cells_ in a vertical or horizontal _flow_. The main feature of a _virtual_ flow is that only the currently visible cells are rendered in the scene. You may have a list of thousands of items, but only, say, 30 cells are rendered at any given time.

JavaFX has its own VirtualFlow, which is not part of the public API, but is used, for example, in the implementation of [ListView](https://api.javafx.dev/javafx.controls/javafx/scene/control/ListView.html). It is, however, [not very efficient](https://bugs.openjdk.java.net/browse/JDK-8091726) when updating the viewport on items changed or scroll.

Here is a comparison of JavaFX's ListView vs. Flowless on a list of 80 items, 25 of which fit into the viewport.

|                                              | Flowless (# of cell creations / # of cell layouts) | JDK 8u40 ListView (# of `updateItem` calls / # of cell layouts) | JDK 16 ListView with fixed cell size (# of `updateItem` calls / # of cell layouts) |
|----------------------------------------------|:-----:|:-----:|:-----:|
| update an item in the viewport               |   1/1 | 1/1   | 1/1   |
| update an item outside the viewport          |   0/0 | 0/0   | 0/0   |
| delete an item in the middle of the viewport |   1/1 | 38/25 | 13/13 |
| add an item in the middle of the viewport    |   1/1 | 38/25 | 13/13 |
| scroll 5 items down                          |   5/5 | 5/5   | 5/5   |
| scroll 50 items down                         | 25/25 | 50/25 | 25/25 |


Here is the [source code](https://gist.github.com/Jugen/2d392fd72ebec9db3c5d2aca1f8f5eb5) of this mini-benchmark.

Use case for Flowless
---------------------

You will benefit from Flowless (compared to ListView) the most if you have many add/delete items occuring in the viewport and an expensive [updateItem](https://api.javafx.dev/javafx.controls/javafx/scene/control/Cell.html#updateItem(T,boolean)) method.

Note, however, that Flowless is a low-level layout component and does not provide higher-level features like selection model or inline editing. One can, of course, implement those on top of Flowless.

Additional API
--------------

VirtualFlow in Flowless provides additional public API compared to ListView or VirtualFlow from JavaFX.

**Direct cell access** with [getCell(itemIndex)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#getCell-int-) and [getCellIfVisible(itemIndex)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#getCellIfVisible-int-) methods. This is useful for measurement purposes.

**List of currently visible cells** via [visibleCells()](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#visibleCells--).

**Hit test** with the [hit(double x, double y)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#hit-double-double-) method that converts viewport coordinates into a cell index and coordinates relative to the cell, or indicates that the hit is before or beyond the cells.

**Navigate to a subregion of a cell** using the [show(itemIndex, region)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#show-int-javafx.geometry.Bounds-) method. This is a finer grained navigation than just the [show(itemIndex)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#show-int-) method.

**Scroll:** [scrollX(deltaX)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#scrollX-double-) and [scrollY(deltaY)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#scrollY-double-) methods.

**Gravity:** You can create VirtualFlow where cells stick to the bottom (vertical flow) or right (horizontal flow) when there are not enough items to fill the viewport.

Conceptual differences from ListView
------------------------------------

**Dumb cells.** This is the most important difference. For Flowless, cells are just [Node](https://api.javafx.dev/javafx.graphics/javafx/scene/Node.html)s and don't encapsulate any logic regarding virtual flow. A cell does not even necessarily store the index of the item it is displaying. This allows VirtualFlow to have complete control over when the cells are created and/or updated.

**Cell reuse is opt-in,** not forced. Cells are not reused by default. A new cell is created for each item. This simplifies cell implementation and does not impair performance if reusing a cell would be about as expensive as creating a new one (i.e. `updateItem` would be expensive).

**No empty cells.** There are no _empty cells_ used to fill the viewport when there are too few items. A cell is either displaying an item, or is not displayed at all.

Assumptions about cells
-----------------------

As noted before, for the purposes of virtual flow in Flowless the cells are just `Node`s. You are not forced to inherit from `ListCell`. However, they are expected to behave according to the following rules:

* Cells of a vertical virtual flow should properly implement methods
  * `computeMinWidth(-1)`
  * `computePrefWidth(-1)`
  * `computePrefHeight(width)`
* Cells of a horizontal virtual flow should properly implement methods
  * `computeMinHeight(-1)`
  * `computePrefHeight(-1)`
  * `computePrefWidth(height)`

Include Flowless in your project
--------------------------------

#### Maven coordinates

| Group ID            | Artifact ID | Version |
| :---------:         | :---------: | :-----: |
| org.fxmisc.flowless | flowless    | 0.7.0   |

#### Gradle example

```groovy
dependencies {
    implementation group: 'org.fxmisc.flowless', name: 'flowless', version: '0.7.0'
}
```

#### Sbt example

```scala
libraryDependencies += "org.fxmisc.flowless" % "flowless" % "0.7.0"
```

#### Manual download

Download the [0.7.0 jar](https://github.com/FXMisc/Flowless/releases/tag/v0.7.0) and place it on your classpath.

Documentation
-------------

[Javadoc](http://fxmisc.github.io/flowless/javadoc/0.7.0/org/fxmisc/flowless/package-summary.html)
