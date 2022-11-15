package com.sleepfuriously.dollargame2.model

import android.util.Log

/**
 * Library for directed and undirected graphs.
 * Note that this uses lists for its data, so it may
 * not be as fast as it could be.
 *
 *	USAGE:
 *		- When instantiating, provide a Node type to fill
 *		  in the generic T. This will be whatever data you want
 *		  to be held in a node.  I HIGHLY recommend that you override
 *		  the toString() method for this class.  It's used by the
 *		  Graph class' .toString() method.
 *
 *		- Also define whether it's directed (default is undirected).
 *
 *		- Add the nodes, supplying a unique id for that node.
 *
 *		- Add edges.  Use a weight if desired.
 *
 *		- Use the graph as you like.
 */
class Graph<T>(
    /** tells whether this is a directed graph or undirected (default) */
    val mDirected : Boolean = false
) {

    //---------------------------
    //  data
    //---------------------------

    /**
     * Holds the nodes.
     *
     * In this case, a node is a key-value pair of an id and any data
     * associated with it.
     */
    private val mNodes = HashMap<Int, T>()

    /**
     * Holds all the edges.
     *
     * Note that for an undirected graph, there wil be just one edge:
     * startNode and endNode are the same things for them.
     */
    private val mEdges = HashMap<Int, Edge>()


    //---------------------------
    //  functions
    //---------------------------

    /**
     * Returns an id that is guaranteed to be unique from any node already
     * in the Graph.
     *
     * O(n)
     */
    fun generateUniqueNodeId() : Int {
        var id = 0
        while (mNodes.get(id) != null) {
            id++
        }
        return id
    }

    /**
     * Returns an id that is guaranteed to be unique from any edge
     * already in the graph.
     *
     * O(n)
     */
    fun generateUniqueEdgeId() : Int {
        var id = 0
        while (mEdges.get(id) != null) {
            id++
        }
        return id
    }


    /**
     * Add a new node to this graph
     *
     * @param   data    The data to store in this node.
     *
     * @param   _id     A unique id for this node.  Defaults to a brand-new unique id.
     *
     * @return  The id for this node (useful if it was generated).
     *
     * NOTE:  Does not check for duplicates.  You've been warned!!!
     */
    fun addNode(data : T, _id : Int? = null) : Int {

        var id : Int
        if (_id == null) {
            id = generateUniqueNodeId()
        }
        else {
            id = _id
        }

        mNodes.put(id, data)
        return id
    }


    /**
     * Adds an edge to this class.  Does not allow duplicate edges!
     *
     * O(n)
     *
     * @param   startNodeId     The id of the first node (obviously you could use either
     *                          as the start for non-directed graphs).
     *
     * @param   endNodeId       Ending edge id.
     *
     * @param   weight          Weight of this edge.  Defaults to 0
     *
     * @return  The ID of this edge
     *
     */
    fun addEdge(startNodeId : Int, endNodeId : Int, weight: Int = 0) : Int {

        // test for duplicates
        if (getEdgeId(startNodeId, endNodeId) != -1) {
            Log.e(TAG, "Tried to add a duplicate edge!")
            return -1
        }

        val id = generateUniqueEdgeId()
        mEdges.put(id, Edge(startNodeId, endNodeId, weight))
        return id
    }


    /**
     * Private util to simplify a few things.  Just adds an edge with the given id.
     */
    private fun addEdge (id : Int, edge : Edge) {
        mEdges.put(id, edge)
    }


    /**
     * @return      the id of the edge with the given start and end nodes.
     *              -1 if not found.
     *
     * Useful to see if an edge exists.
     *
     * Preconditions:
     *      Relies on [mDirected] to determine if direction matters
     *
     * O(n)
     */
    fun getEdgeId(startNodeId: Int, endNodeId: Int) : Int {

        // loop through the edges
        mEdges.forEach() { it ->
            if ((startNodeId == it.value.startNodeId) && (endNodeId == it.value.endNodeId)) {
                return it.key
            }

            // if not a directed graph, check the other way too
            if (mDirected == false) {
                if ((startNodeId == it.value.endNodeId) && (endNodeId == it.value.startNodeId)) {
                    return it.key
                }
            }
        }

        // not found
        return -1
    }


    /**
     * Creates an exact duplicate of this graph
     *
     * Returns NULL if something was wrong with the graph that prevents making a clone
     * (probably a duplicate node Id).
     */
    fun clone() : Graph<T>? {

        val newGraph = Graph<T>()

        // Copying the nodes is a little tricky as it's based on HashMap.
        //
        // Get a Set of keys (aka IDs) and then copy them one-by-one into the new
        // graph

        val ids = mNodes.keys
        val iterator = ids.iterator()

        try {
            while (iterator.hasNext()) {
                val id = iterator.next()
                val node = mNodes[id]
                if (node == null) {
                    throw NullPointerException("somehow we found a null node in clone()!!!")
                }
                else {
                    newGraph.addNode(node, id)
                }
            }
        }
        catch (e : GraphNodeDuplicateIdException) {
            e.printStackTrace()
            return null
        }

        // edges are a bit easier
        mEdges.forEach() {
            newGraph.addEdge(it.key, it.value)
        }

        return newGraph
    }


    /**
     * Returns a list of all the node IDs adjacent to a given node.
     * If none, this returns an empty list
     *
     * O(n)
     *
     * @param   nodeId      The id of the node in question.
     *
     * @param   directed    True indicates direction is important.  Defaults to
     *                      the current directedness of this Graph.
     */
    fun getAllAdjacentTo(nodeId : Int, directed : Boolean = mDirected) : List<Int> {

        val adjacenList = ArrayList<Int>()
        val edgeList = getEdges(nodeId)

        for (i in 0 until edgeList.size) {
            val edge = edgeList[i]
            if (directed) {
                if (edge.startNodeId == nodeId) {
                    // this is an edge that starts with our node
                    // add it to our list.
                    adjacenList.add(edge.endNodeId)
                }
            }
            else {
                // NOT directed--just include the other node
                if (edge.startNodeId == nodeId) {
                    adjacenList.add(edge.endNodeId)
                }
                else {
                    adjacenList.add(edge.startNodeId)
                }
            }
        }

        return adjacenList
    }


    /**
     * Returns a copy of the List of all the node IDs for this Graph.
     */
    fun getAllNodeIds() : List<Int> {
        return ArrayList(mNodes.keys)
    }


    /**
     * Returns a list of all the node data.
     *
     * Note:    IDs are NOT part of this list!
     *
     * Note:    Returns a copy of the data (not the data itself)
     */
    fun getAllNodeData() : List<T> {
        return ArrayList(mNodes.values)
    }


    /**
     * Returns the data associated with a node id.
     * Returns NULL if no data found for this id.
     */
    fun getNodeData(nodeId : Int) : T? {
        return mNodes[nodeId]
    }


    /**
     * Returns the id of the first node to match the given data.
     *
     * O(n)
     *
     * @return  the key (id) or null if not found
     */
    fun getNodeId(data : T) : Int? {

        // note:  the id is also the key

        mNodes.forEach { (key, value) ->
            if (value == data) {
                return key
            }
        }

        return null
    }


    /**
     * Curious if two nodes are adjacent?  Use this to find out!
     * For undirected graphs the order doesn't matter.
     */
    fun isAdjacent(startNodeId : Int, endNodeId : Int) : Boolean {
        var found = false

        // Simply go through all the edges and see if we have a match.
        // Because this is kotlin we have to go through EVERYTHING even after
        // we found what we were looking for (hence the if (!found) hack).
        mEdges.forEach() { it ->
            if (!found) {
                val edge = it.value
                if ((edge.startNodeId == startNodeId) && (edge.endNodeId == endNodeId)) {
                    found = true
                }

                if (mDirected == false) {
                    // special case for undirected graphs
                    if ((edge.startNodeId == endNodeId) && (edge.endNodeId == startNodeId)) {
                        found = true
                    }
                }
            }
        }

        return found
    }


    /**
     * Returns the genus of the Graph.  Uses Dr. Kreiger's algorithm.
     *
     * Genus = edges - vertices + 1
     *
     * @throws  GraphNotConnectedException if the Graph is not connected
     */
    fun getGenus() : Int {
        if (!isConnected()) {
            throw GraphNotConnectedException()
        }

        return mEdges.size - mNodes.size + 1
    }


    /**
     * Figures out if this Graph is connected or not.
     *
     * For undirected Graphs, this simply means that all the nodes are accessible
     * from any other node (by one or more steps).  This is pretty straight-forward.
     *
     * For directed graphs, I'm using the official term of "weakly connected."
     * That is, it would be a connected graph were it undirected.
     *
     * Note that a graph with no nodes is NOT connected.  And a graph with just 1 node
     * is connected ONLY if it connects to itself.
     *
     * todo:  write a Strongly Connected graph routine.  It will tell if in a directed
     *  graph any node can get to any node (nice to have, but not used in this app).
     */
    fun isConnected() : Boolean {

        // easy case first
        if ((mNodes.size == 0) || (mEdges.size == 0)) {
            return false
        }

        // create a list of visited nodes
        val visited = ArrayList<Int>()

        // start with any key/ID.  How about the first one?
        val anId = mNodes.keys.iterator().next()
        isConnectedHelper(anId, visited)

        // If the size of the visited list is the same as our number of nodes,
        // then we know that all were visited.  This can only happen if the
        // graph is connected.
        return (visited.size == mNodes.size)
    }


    /**
     * Does the recursive depth-first search of the Graph's edges
     * (assumes that the Graph is undirected!).
     *
     * @param   nodeId          An unvisited node in the Graph.  This method will
     *                          find all the edges that it connects to and so on.
     *
     * @param   visited         A list of visited node.  These will be added to as the
     *                          nodes are visited.  Yes this data structure will BE MODIFIED.
     */
    private fun isConnectedHelper(nodeId: Int, visited : ArrayList<Int>) {

        // start by adding this node to the visited list
        visited.add(nodeId)

        // For considering connectivity, we always use an undirected graph
        val adjacentNodeIds = getAllAdjacentTo(nodeId, false)
        adjacentNodeIds.forEach() { adjacentNodeId ->
            if (visited.contains(adjacentNodeId) == false) {
                // not found in the visited list, recurse on it!
                isConnectedHelper(adjacentNodeId, visited)
            }
        }
    }


    /**
     * Find all the edges that use the given node.
     * If none are found, the returned list will be empty.
     *
     * O(n)
     *
     * @param   nodeId      The id of the node in question
     *
     * @return      List of all IDs of the nodes that are immediately connected
     *              (share an edge) with the give node.
     */
    protected fun getEdges(nodeId : Int) : List<Edge> {

        val edgeList = ArrayList<Edge>()

        mEdges.forEach() {
            val edge = it.value
            if ((edge.startNodeId == nodeId) || (edge.endNodeId == nodeId)) {
                edgeList.add(edge)
            }
        }
        return edgeList
    }


    /**
     * Returns the number of nodes in this graph.  Hope you didn't make any duplicates!
     */
    fun numNodes() : Int {
        return mNodes.size
    }

    /**
     * Returns the number of edges in this graph.  For undirected graphs this may return
     * the count for both A->B and B->A IF YOU WERE DUMB ENOUGH TO ENTER THOSE EDGES!
     */
    fun numEdges() : Int {
        return mEdges.size
    }


    /**
     * Removes the given node.  Will remove any edges associated with this node too.
     *
     * If there are MORE THAN ONE nodes with the same id (and there shouldn't be!),
     * this will remove only the first that was found.  Really--you should be more
     * careful with your graphs!
     *
     * @param   id      The id of the node to be removed
     *
     * @return      TRUE if the node was removed.
     *              FALSE if the node can't be found.
     */
    fun removeNode(id : Int) : Boolean {
        removeEdgesWithNode(id)
        if (mNodes.remove(id) == null) {
            return false
        }
        return true
    }


    /**
     * Removes every node from this Graph.  But this is not a stupid function; all the
     * edges are removed first!
     */
    fun removeAllNodes() {
        removeAllEdges()
        mNodes.clear()
    }


    /**
     * Removes all the edges that use a given node.  Does not remove that node.
     *
     * O(n)
     *
     * @return      The number of edges that were removed.
     */
    fun removeEdgesWithNode(nodeId : Int) : Int {
        var count = 0

        // Holds the ids of the edges we want to remove
        val edgesToRemove = mutableSetOf<Int>()

        // determine which edges use this node and put them in our remove list
        mEdges.forEach() {
            val edge = it.value
            if ((edge.startNodeId == nodeId) || (edge.endNodeId == nodeId)) {
                edgesToRemove.add(it.key)
            }
        }

        // finally remove the edges we collected
        edgesToRemove.forEach() { id ->
            count++
            mEdges.remove(id)
        }

        return count
    }


    /**
     * Removes the specified edge.  For undirected graphs this will try both directions,
     * removing both if they both exist.
     *
     * O(n)
     *
     * @return  True if an edge was successfully removed.
     */
    fun removeEdge(startNodeId: Int, endNodeId: Int) : Boolean {
        var found = false
        var id = 0      // dummy value to keep kotlin from complaining

        // find the edge in question
        mEdges.forEach() {
            val edge = it.value
            if ((edge.startNodeId == startNodeId) && (edge.endNodeId == endNodeId)) {
                found = true
                id = it.key
            }

            // check the reverse for undirected graphs
            if (mDirected == false) {
                if ((edge.startNodeId == endNodeId) && (edge.endNodeId == startNodeId)) {
                    found = true
                    id = it.key
                }
            }
        }

        if (found) {
            mEdges.remove(id)
        }

        return found
    }


    /**
     * Quite simply does what it says.  Nodes will remain untouched.
     */
    fun removeAllEdges() {
        mEdges.clear()
    }


    /**
     * @param   id      The id of this edge.
     *
     * @return      - the edge data with the given id
     *              - null if the id is not valid
     */
    fun getEdgeFromId(id : Int) : Edge? {
        return mEdges.get(id)
    }


    /**
     * Finds the edge data from the endpoints of this edge.  If the graph is undirected,
     * then start/end doesn't matter.
     *
     * @return      - relevant edge data
     *              - null if could not be found
     */
    fun getEdgeFromNodes(startNodeId: Int, endNodeId: Int) : Edge? {

        var foundEdge : Edge? = null

        mEdges.forEach { (_, edge) ->
            if ((edge.startNodeId == startNodeId) && (edge.endNodeId == endNodeId)) {
                foundEdge = edge
            }

            if (mDirected == false) {
                if ((edge.startNodeId == endNodeId) && (edge.endNodeId == startNodeId)) {
                    foundEdge = edge
                }
            }
        }
        return foundEdge
    }

    /**
     * Returns all the ids for all the edges in a list
     */
    fun getAllEdgeIds() : Set<Int> {
        return mEdges.keys
    }

    /**
     * Returns a list of all the edges in the graph.
     *
     * Note that this does NOT inlude the ids.  Use [getAllEdgeIds] for that.
     */
    fun getAllEdges() : List<Edge> {
        return ArrayList(mEdges.values)
    }


    /**
     * Prints the contents of this graph to a String.
     *
     * preconditions:
     *      Uses the .toString() method of the T class
     */
    override fun toString(): String {
        var nodeStr = " Nodes[${mNodes.size}]:"

        // display the nodes.
        mNodes.forEach() {
            nodeStr += " (${it.key}: ${it.value.toString()})"
        }

        // the edges
        var edgeStr = "Edges[${mEdges.size}]:"
        mEdges.forEach() {
            val edge = it.value
            edgeStr += " (${edge.startNodeId}, ${edge.endNodeId}: ${edge.weight})"
        }

        return nodeStr + "\n" + edgeStr
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  internal classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    companion object {
        const val TAG = "Graph"
    }

}


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  external classes
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Defines an edge (connects two nodes) in the Graph.
 */
data class Edge (
    var startNodeId : Int,
    var endNodeId: Int,
    var weight: Int = 0
)


/**
 * This exception is thrown if a node ID is duplicated within a Graph.
 */
class GraphNodeDuplicateIdException() : Exception("GraphNodeDuplicateException")


/**
 * Thrown if a Graph is not connected when it is expected to be connected.
 */
class GraphNotConnectedException() : Exception("GraphNotConnectedException")
