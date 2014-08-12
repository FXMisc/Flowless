Flowless
========

Efficient VirtualFlow for JavaFX. VirtualFlow is a layout container that lays out _cells_ in a vertical or horizontal _flow_. The main feature of a _virtual_ flow is that only the currently visible cells are rendered in the scene. You may have a list of thousands of items, but only, say, 30 cells are rendered at any given time.

JavaFX has its own VirtualFlow, which is not part of the public API, but is used, for example, in the implementation of [ListView](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html). It is, however, [not very efficient](https://javafx-jira.kenai.com/browse/RT-35395) when updating the viewport on items change or scroll. **EDIT:** Jonathan Giles created a patch to improve ListView performance in the benchmark below. The patch may be integrated into JDK 8u40.

Here is a comparison of JavaFX's ListView vs. Flowless on a list of 80 items, 25 of which fit into the viewport.

|                     | Flowless (# of cell creations) | ListView in JDK8u20-b21 (# of `updateItem` calls) | ListView in JDK8u40<sup>(</sup>\*<sup>), (</sup>\*\*<sup>)</sup> (# of `updateItem` calls) |
|---------------------|:------------------------------:|:-------------------------------------------------:|:------------------------------------------------------------------------------------------:|
| update an item in the viewport |                   1 | 25                                                | 1                                                                                          |
| update an item outside the viewport |              0 | 25                                                | 0                                                                                          |
| delete an item in the middle of the viewport |     1 | 75                                                | 12                                                                                         |
| add an item in the middle of the viewport |        1 | 75                                                | 12                                                                                         |
| scroll 5 items down |                              5 | 5                                                 | 5                                                                                          |
| scroll 50 items down |                            25 | 75                                                | 25                                                                                         |

<sup>(</sup>\*<sup>)</sup> If the patch gets integrated into 8u40.  
<sup>(</sup>\*\*<sup>)</sup> With fixed cell size.  

Here is the [source code](https://gist.github.com/TomasMikula/1dcee2cc4e5dab421913) of this mini-benchmark.

Use case for Flowless
---------------------

You will benefit from Flowless (compared to ListView) the most if you have many item updates and an expensive [updateItem](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html#updateItem-T-boolean-) method.

Note, however, that Flowless is a low-level layout component and does not provide higher-level features like selection model or inline editing. One can, of course, implement those on top of Flowless.

Additional API
--------------

VirtualFlow in Flowless provides additional public API compared to ListView or VirtualFlow from JavaFX.

**Direct cell access** with [getCell(itemIndex)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#getCell-int-) and [getCellIfVisible(itemIndex)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#getCellIfVisible-int-) methods. This is useful for measurement purposes.

**Hit test** with the [hit(double offset)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#hit-double-) method that converts an offset in primary axis (x for horizontal flow, y for vertical flow) into a cell index and offset relative to the cell, or indicates that the hit is before or beyond the cells.

**Navigate to a subregion of a cell** using the [show(cell, region)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#show-C-javafx.geometry.Bounds-) method. This is a finer grained navigation than just the [show(itemIndex)](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/VirtualFlow.html#show-int-) method.

Conceptual differences from ListView
------------------------------------

**Dumb cells.** This is the most important difference. For Flowless, cells are just [Node](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html)s and don't encapsulate any logic regarding virtual flow. A cell does not even necessarily store the index of the item it is displaying. This allows VirtualFlow to have complete control over when the cells are created and/or updated.

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
* Cells are not expected to change their size _spontaneously_, i.e. without changing the item they are holding. Changes to their min/pref/max size will be ignored.

Include Flowless in your project
--------------------------------

#### Maven coordinates

| Group ID            | Artifact ID | Version |
| :---------:         | :---------: | :-----: |
| org.fxmisc.flowless | flowless    | 0.3     |

#### Gradle example

```groovy
dependencies {
    compile group: 'org.fxmisc.flowless', name: 'flowless', version: '0.3'
}
```

#### Sbt example

```scala
libraryDependencies += "org.fxmisc.flowless" % "flowless" % "0.3"
```

#### Manual download

Download the [0.3 jar](https://github.com/TomasMikula/Flowless/releases/tag/v0.3) and place it on your classpath.

Documentation
-------------

[Javadoc](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/package-summary.html)
