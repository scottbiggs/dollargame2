package com.sleepfuriously.dollargame2.model

/**
 * Describes a node of the graph.  Just
 */
class Node (
    /** current amount of dollars for this node (could be negative) */
    var amount : Int = 0,

    /** number of times this node has given its money away */
    var giveCount : Int,

    /** number of times this node has taken money from its neighbors */
    var takeCount : Int,

    /** Coordinates for this node (for drawing purposes). */
    var x : Int, var y: Int
) {

    override fun toString(): String {
        return "Node: $amount"
    }

}