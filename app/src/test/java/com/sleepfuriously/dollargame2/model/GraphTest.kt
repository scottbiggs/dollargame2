package com.sleepfuriously.dollargame2.model

import org.junit.Test
import com.google.common.truth.Truth.assertThat


/**
 * todo:  add DIRECTED graph tests!!
 */
internal class GraphTest {

    //-----------------
    //  getUniqueNodeId()
    //-----------------
    @Test
    fun getUniqueNodeIdTest() {

        val graph = Graph<Boolean>()

        // first id should be 0
        val firstId = graph.generateUniqueNodeId()
        assertThat(firstId).isEqualTo(0)
        graph.addNode(true, firstId)

        // add to the graph and make sure that the next one is unique
        val secondId = graph.generateUniqueNodeId()
        assertThat(secondId).isNotEqualTo(firstId)
        graph.addNode(true, secondId)

        // try a 3rd
        val thirdId = graph.generateUniqueNodeId()
        assertThat(thirdId).isNotEqualTo(secondId)
        assertThat(thirdId).isNotEqualTo(firstId)
        graph.addNode(false)

        // remove one (middle node), and try again
        graph.removeNode(secondId)
        val fourthId = graph.generateUniqueNodeId()
        assertThat(fourthId).isNotEqualTo(firstId)
        assertThat(fourthId).isNotEqualTo(thirdId)
    }

    @Test
    fun getUniqueEdgeIdTest() {
        val graph = Graph<Boolean>()    // simple graph with true/false data at each node
        val firstNode = graph.addNode(true)
        val secondNode = graph.addNode(true)
        val thirdNode = graph.addNode(true)

        val firstEdge = graph.generateUniqueEdgeId()
        assertThat(firstEdge).isEqualTo(0)      // first is always 0
        graph.addEdge(firstNode, secondNode)

        val secondEdge = graph.addEdge(secondNode, thirdNode)
        assertThat(secondEdge).isNotEqualTo(firstEdge)

        val thirdEdge = graph.addEdge(thirdNode, firstNode)
        assertThat(thirdEdge).isNotEqualTo(firstEdge)
        assertThat(thirdEdge).isNotEqualTo(secondEdge)

        // try removing the middle one
        graph.removeEdge(secondNode, thirdNode)
        val fourthEdge = graph.addEdge(secondNode, thirdNode)
        assertThat(fourthEdge).isNotEqualTo(firstEdge)
        assertThat(fourthEdge).isNotEqualTo(thirdEdge)
    }

    @Test
    fun addNodeTest() {
        val graph = Graph<Boolean>()

        val firstNodeId = graph.addNode(true)
        val second = graph.addNode(true)
        val third = graph.addNode(false)

        assertThat(firstNodeId).isNotEqualTo(second)
        assertThat(firstNodeId).isNotEqualTo(third)
        assertThat(second).isNotEqualTo(third)
    }

    @Test
    fun addEdgeTest() {
        val graph = buildSimpleGraph()

        val nodeList = graph.getAllNodeIds()

        val edge1 = graph.addEdge(nodeList[0], nodeList[1])
        val edge2 = graph.addEdge(nodeList[1], nodeList[2])
        val edge3 = graph.addEdge(nodeList[2], nodeList[0])

        assertThat(edge1).isNotEqualTo(edge2)
        assertThat(edge1).isNotEqualTo(edge3)
        assertThat(edge2).isNotEqualTo(edge3)
    }

    @Test
    fun getEdgeIdTest() {
        val graph = buildSimpleGraph()
        val nodeList = graph.getAllNodeIds()

        // add some edges
        val edge1 = graph.addEdge(nodeList[0], nodeList[1])
        val edge2 = graph.addEdge(nodeList[1], nodeList[2])

        val result1 = graph.getEdgeId(nodeList[0], nodeList[1])
        assertThat(result1).isEqualTo(edge1)

        val result2 = graph.getEdgeId(nodeList[1], nodeList[2])
        assertThat(result2).isEqualTo(edge2)
    }

    @Test
    fun testCloneTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)

        val clone = graph.clone()

        // make sure the lists match
        val origNodes = graph.getAllNodeIds()
        val resultNodes = clone?.getAllNodeIds()
        assertThat(resultNodes).isNotNull()
        assertThat(resultNodes).isEqualTo(origNodes)

        val origEdges = graph.getAllEdges()
        val resultEdges = clone?.getAllEdges()
        assertThat(resultEdges).isNotNull()
        assertThat(resultEdges).isEqualTo(origEdges)
    }

    @Test
    fun getAllAdjacentToTest() {
        val graph = buildSimpleGraph()
        val nodeList = graph.getAllNodeIds()

        // make sure all nodes have no adjacent nodes (not connected yet!)
        val result1 = graph.getAllAdjacentTo(nodeList[0])
        val result2 = graph.getAllAdjacentTo(nodeList[1])
        val result3 = graph.getAllAdjacentTo(nodeList[2])
        assertThat(result1.size).isEqualTo(0)
        assertThat(result2.size).isEqualTo(0)
        assertThat(result3.size).isEqualTo(0)

        // test the circularly connected graph
        addSimpleGraphEdges(graph)
        val result4 = graph.getAllAdjacentTo(nodeList[0])
        val result5 = graph.getAllAdjacentTo(nodeList[1])
        val result6 = graph.getAllAdjacentTo(nodeList[2])

        assertThat(result4.size).isEqualTo(2)
        assertThat(result5.size).isEqualTo(2)
        assertThat(result6.size).isEqualTo(2)

        // the hard part: making sure the right nodes are adjacent
        assertThat(result4.contains(nodeList[1])).isTrue()
        assertThat(result4.contains(nodeList[2])).isTrue()

        assertThat(result5.contains(nodeList[0])).isTrue()
        assertThat(result5.contains(nodeList[2])).isTrue()

        assertThat(result6.contains(nodeList[0])).isTrue()
        assertThat(result6.contains(nodeList[1])).isTrue()
    }

    @Test
    fun getAllNodeIdsTest() {
        val graph = buildSimpleGraph()
        val nodeList = graph.getAllNodeIds()

        assertThat(nodeList.size).isEqualTo(3)
        assertThat(nodeList[0]).isEqualTo(0)
        assertThat(nodeList[1]).isEqualTo(1)
        assertThat(nodeList[2]).isEqualTo(2)
    }

    @Test
    fun getAllEdgeIdsTest() {
        val graph = buildSimpleGraph()
        val nodeList = graph.getAllNodeIds()

        var edgeList = graph.getAllEdgeIds()
        assertThat(edgeList.size).isEqualTo(0)

        addSimpleGraphEdges(graph)
        edgeList = graph.getAllEdgeIds()
        assertThat(edgeList.size).isEqualTo(3)

        graph.removeEdge(nodeList[0], nodeList[1])
        assertThat(edgeList.size).isEqualTo(2)

        graph.removeAllEdges()
        assertThat(edgeList.size).isEqualTo(0)
}

    @Test
    fun getAllNodeDataTest() {
        val graph = buildSimpleGraph()
        val dataList = graph.getAllNodeData()

        assertThat(dataList.size).isEqualTo(3)
        assertThat(dataList[0]).isEqualTo(true)
        assertThat(dataList[1]).isEqualTo(true)
        assertThat(dataList[2]).isEqualTo(false)
    }

    @Test
    fun getNodeDataTest() {
        val graph = buildSimpleGraph()
        val result1 = graph.getNodeData(0)
        val result2 = graph.getNodeData(1)
        val result3 = graph.getNodeData(2)

        assertThat(result1).isTrue()
        assertThat(result2).isTrue()
        assertThat(result3).isFalse()
    }

    @Test
    fun getNodeIdTest() {
        val graph = buildSimpleGraph()
        val resultId1 = graph.getNodeId(true)

        // since there are 2 possible results this is slightly tricky
        val positiveResult = (resultId1 == 0) || (resultId1 == 1)
        assertThat(positiveResult).isTrue()

        val resultId2 = graph.getNodeId(false)
        assertThat(resultId2).isEqualTo(2)
    }

    @Test
    fun isAdjacentTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)

        // add a node
        val newNodeId = graph.addNode(false)

        val nodeList = graph.getAllNodeIds()

        // add this as a spur to the second node
        //
        //        ------\
        //       /       \
        //      v         |
        //      0 -> 1 -> 2
        //           |
        //           v
        //           3
        graph.addEdge(nodeList[1], newNodeId)

        assertThat(graph.isAdjacent(nodeList[0], nodeList[1])).isTrue()
        assertThat(graph.isAdjacent(nodeList[1], nodeList[2])).isTrue()
        assertThat(graph.isAdjacent(nodeList[2], nodeList[0])).isTrue()
        assertThat(graph.isAdjacent(nodeList[1], newNodeId)).isTrue()

        assertThat(graph.isAdjacent(nodeList[0], newNodeId)).isFalse()
        assertThat(graph.isAdjacent(nodeList[2], newNodeId)).isFalse()
        assertThat(graph.isAdjacent(nodeList[3], newNodeId)).isFalse()

        if (graph.mDirected) {
            assertThat(graph.isAdjacent(nodeList[1], nodeList[0])).isFalse()
            assertThat(graph.isAdjacent(nodeList[1], nodeList[2])).isFalse()
            assertThat(graph.isAdjacent(nodeList[0], nodeList[2])).isFalse()
            assertThat(graph.isAdjacent(newNodeId, nodeList[1])).isFalse()
        }
        else {
            assertThat(graph.isAdjacent(nodeList[1], nodeList[0])).isTrue()
            assertThat(graph.isAdjacent(nodeList[1], nodeList[2])).isTrue()
            assertThat(graph.isAdjacent(nodeList[0], nodeList[2])).isTrue()
            assertThat(graph.isAdjacent(newNodeId, nodeList[1])).isTrue()
        }
    }

    @Test
    fun getGenusTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)

        val result1 = graph.getGenus()
        assertThat(result1).isEqualTo(graph.getAllEdges().size - graph.getAllNodeIds().size + 1)

        // try unconnected graph
        graph.addNode(true)
        var threwException = false
        try {
            graph.getGenus()
        }
        catch (e : GraphNotConnectedException) {
            threwException = true
        }
        assertThat(threwException).isTrue()
    }

    @Test
    fun isConnectedTest() {
        val graph = buildSimpleGraph()
        assertThat(graph.isConnected()).isFalse()

        addSimpleGraphEdges(graph)
        assertThat(graph.isConnected()).isTrue()

        val newNodId = graph.addNode(false)
        assertThat(graph.isConnected()).isFalse()

        val nodeList = graph.getAllNodeIds()
        graph.addEdge(nodeList[0], newNodId)

        assertThat(graph.isConnected()).isTrue()
    }

    @Test
    fun numNodesTest() {
        val graph1 = Graph<Boolean>()
        assertThat(graph1.numNodes()).isEqualTo(0)

        val graph2 = buildSimpleGraph()
        assertThat(graph2.numNodes()).isEqualTo(3)

        graph2.addNode(true)
        graph2.addNode(true)
        assertThat(graph2.numNodes()).isEqualTo(5)

        var nodeList = graph2.getAllNodeIds()
        graph2.removeNode(nodeList[0])
        assertThat(graph2.numNodes()).isEqualTo(4)

        nodeList = graph2.getAllNodeIds()
        graph2.removeNode(nodeList[1])
        assertThat(graph2.numNodes()).isEqualTo(3)
    }

    @Test
    fun numEdgesTest() {
        val graph = buildSimpleGraph()
        assertThat(graph.numEdges()).isEqualTo(0)

        addSimpleGraphEdges(graph)
        assertThat(graph.numEdges()).isEqualTo(3)

        // add a node so we have more edges to play with
        graph.addNode(false)
        val nodeList = graph.getAllNodeIds()
        assertThat(graph.numEdges()).isEqualTo(3)

        graph.addEdge(nodeList[2], nodeList[3])
        assertThat(graph.numEdges()).isEqualTo(4)

        graph.removeEdge(nodeList[0], nodeList[1])
        assertThat(graph.numEdges()).isEqualTo(3)

        graph.removeAllEdges()
        assertThat(graph.numEdges()).isEqualTo(0)
    }

    @Test
    fun removeNodeTest() {
        val graph = buildSimpleGraph()
        val nodeList = graph.getAllNodeIds()

        // make sure that the node exists
        assertThat(graph.getNodeData(nodeList[0])).isNotNull()

        // make sure it's removed
        graph.removeNode(nodeList[0])
        assertThat(graph.getNodeData(nodeList[0])).isNull()

        // make sure the others are NOT removed
        assertThat(graph.getNodeData(nodeList[1])).isNotNull()
        assertThat(graph.getNodeData(nodeList[2])).isNotNull()
    }

    @Test
    fun removeAllNodesTest() {
        val graph = buildSimpleGraph()
        assertThat(graph.numNodes()).isEqualTo(3)

        graph.removeAllNodes()
        assertThat(graph.numNodes()).isEqualTo(0)
    }

    @Test
    fun removeEdgesWithNodeTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)
        val nodeList = graph.getAllNodeIds()

        val numEdgesRemoved = graph.removeEdgesWithNode(nodeList[1])
        assertThat(numEdgesRemoved).isEqualTo(2)

        assertThat(graph.getEdgeId(nodeList[2], nodeList[0])).isNotEqualTo(-1)
        assertThat(graph.getEdgeId(nodeList[0], nodeList[1])).isEqualTo(-1)
        assertThat(graph.getEdgeId(nodeList[1], nodeList[2])).isEqualTo(-1)

        if (graph.mDirected == false) {
            assertThat(graph.getEdgeId(nodeList[1], nodeList[0])).isEqualTo(-1)
            assertThat(graph.getEdgeId(nodeList[2], nodeList[1])).isEqualTo(-1)
        }
    }

    @Test
    fun removeEdgeTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)
        val nodeList = graph.getAllNodeIds()

        var removed = graph.removeEdge(nodeList[1], nodeList[2])
        assertThat(removed).isTrue()
        assertThat(graph.numEdges()).isEqualTo(2)

        removed = graph.removeEdge(nodeList[1], nodeList[2])
        assertThat(removed).isFalse()       // can't remove it twice

        removed = graph.removeEdge(nodeList[0], nodeList[1])
        assertThat(removed).isTrue()
        assertThat(graph.numEdges()).isEqualTo(1)

        removed = graph.removeEdge(nodeList[2], nodeList[0])
        assertThat(removed).isTrue()
        assertThat(graph.numEdges()).isEqualTo(0)

        // try removing an edge with nodes that don't exist
        removed = graph.removeEdge(12828, 92)
        assertThat(removed).isFalse()
    }

    @Test
    fun removeAllEdgesTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)

        graph.removeAllEdges()
        assertThat(graph.numEdges()).isEqualTo(0)
    }

    @Test
    fun getEdgeFromIdTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)
        val edgeIdList = graph.getAllEdgeIds()

        // try an bad id
        var edge = graph.getEdgeFromId(666)
        assertThat(edge).isNull()

        val firstEdgeId = edgeIdList.first()
        edge = graph.getEdgeFromId(firstEdgeId)
        assertThat(edge).isNotNull()

        val found = when (edge?.startNodeId) {
            in 0..2 -> true
            else -> false
        }
        assertThat(found).isTrue()
    }

    @Test
    fun getEdgeFromNodesTest() {
        val graph = buildSimpleGraph()
        addSimpleGraphEdges(graph)
        val nodeList = graph.getAllNodeIds()

        var edge = graph.getEdgeFromNodes(nodeList[0], nodeList[1])
        assertThat(edge).isNotNull()
        assertThat(edge?.startNodeId).isEqualTo(nodeList[0])
        assertThat(edge?.endNodeId).isEqualTo(nodeList[1])

        edge = graph.getEdgeFromNodes(nodeList[1], nodeList[2])
        assertThat(edge).isNotNull()
        assertThat(edge?.startNodeId).isEqualTo(nodeList[1])
        assertThat(edge?.endNodeId).isEqualTo(nodeList[2])
    }

    @Test
    fun getAllEdgesTest() {
        val graph = buildSimpleGraph()

        var edgeList = graph.getAllEdges()
        assertThat(edgeList.size).isEqualTo(0)

        addSimpleGraphEdges(graph)
        edgeList = graph.getAllEdges()
        assertThat(edgeList.size).isEqualTo(3)
    }

    @Test
    fun getDirectedTest() {
        val graph = buildSimpleGraph()
        assertThat(graph.mDirected).isFalse()

        val directedGraph = Graph<Boolean>(true)
        assertThat(directedGraph.mDirected).isTrue()
    }


    //---------------------------------------
    //  helper functions
    //---------------------------------------

    /**
     * Makes a graph with 3 nodes and 0 edges
     */
    private fun buildSimpleGraph() : Graph<Boolean> {
        val graph = Graph<Boolean>()    // simple graph -- don't care about the data
        graph.addNode(true)
        graph.addNode(true)
        graph.addNode(false)
        return graph
    }

    /**
     * Adds a circle of edges.  In other words:
     *      0 -> 1,  1 -> 2,  2 -> 3 ...  until n-2 -> n then n -> 0
     *
     * Works completely by side-effect
     */
    private fun addSimpleGraphEdges(graph : Graph<Boolean>) {
        val nodeList = graph.getAllNodeIds()

        for (i in 0 until nodeList.size - 1) {
            graph.addEdge(nodeList[i], nodeList[i + 1])
        }
        graph.addEdge(nodeList[nodeList.size - 1], nodeList[0])
    }

}