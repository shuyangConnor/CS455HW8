import java.io.*;
import java.util.*;
import java.lang.*;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 

	public static final int INFINITY = 9999;

	int[] lkcost;				/*The link cost between node 0 and other nodes*/
	int nodename;           	/*Name of this node*/
	int[][] costs;				/*forwarding table, where index is destination node, [i][0] is cost to destination node and
  	  							  [i][1] is the next hop towards the destination node */

	int[][] graph;				/*Adjacency metric for the network, where (i,j) is cost to go from node i to j */
	ShortestPath t;             /*Have Dijkstra's implementation */

	List<Integer> boardcasted = new ArrayList<>();
	int nextboardcast;
	/* Class constructor */
	public Node() { }

	int[][] dijkstra(int graph[][], int src){
		int[][] result = new int[4][2];
		List<Integer> visited = new ArrayList<>();
		// List<Integer> rcvdinfo = new ArrayList<>();
		for (int i = 0; i < 4; i++){
			if (i != src){
				result[i][0] = 9999; 
				result[i][1] = -1;
			}
			else {
				result[i][0] = 0; 
				result[i][1] = -1;
			}
			visited.add(i);
		}
		while (visited.size() != 0){
			int smallestNode = visited.get(0);
			int smallestDist = result[visited.get(0)][0];
			for (int x = 0; x < visited.size(); x++){
				if (result[visited.get(x)][0] < smallestDist){
					smallestDist = result[visited.get(x)][0];
					smallestNode = visited.get(x);
				}
			}

			visited.remove(Integer.valueOf(smallestNode));

			for (int v = 0; v < 4; v++){
				int alt = result[smallestNode][0] + graph[smallestNode][v];
				if (alt < result[v][0]){
					result[v][0] = alt;
					result[v][1] = smallestNode;
				}
			}
		}
		return result;
	}

	int[][] dijkstraResultToCosts(int[][] dijkstraResult, int[][] costs, int src){
		List<List<Integer>> S = new ArrayList<List<Integer>>();
		for (int x = 0; x < 4; x++){
			List<Integer> list = new ArrayList<Integer>();
			S.add(list);
		}
		
			for (int y = 0; y < 4; y++){
				
				int u = y;
				if (dijkstraResult[u][1] != -1 || u == src){
					while (u != -1){
						S.get(y).add(0, u);
						u = dijkstraResult[u][1];
					}
				}
			}
		
		int[][] result = new int [4][2];
		for (int i = 0; i < 4; i++){
		 	result[i][0] = dijkstraResult[i][0];
			if (S.get(i).size() > 1){
				result[i][1] = S.get(i).get(1);
			}
		 	else {
				result[i][1] = this.nodename;
			}
		}
		return result;
	}

	/* students to write the following two routines, and maybe some others */
	void rtinit(int nodename, int[] initial_lkcost) {
			this.nodename =  nodename;
			this.lkcost = initial_lkcost;
			this.nextboardcast = nodename;
			//Initialize the forwarding table.
			this.costs = new int[4][2];
			for (int i = 0; i < 4; i++){
				costs[i][0] = this.lkcost[i];
				costs[i][1] = i;
				if (costs[i][0] == 9999){
					costs[i][1] = -1;
				}
			}
			//Initialize the adjacency metric.
			this.graph = new int[4][4];
			for (int x = 0; x < 4; x++){
				for (int y = 0; y < 4; y++){
					if (x == this.nodename){
						this.graph[x][y] = this.costs[y][0];
					}
					else if (y == this.nodename){
						this.graph[x][y] = this.costs[x][0];
					}
					else {
						this.graph[x][y] = 9999;
					}
				}
			}
			//Boardcast packet.
			for (int i = 0; i < 4; i++){
				if (this.nodename != i && this.lkcost[i] != 9999){
					NetworkSimulator.tolayer2(new Packet(this.nodename, i, this.nodename, this.lkcost, nextboardcast));
				}
			}
			this.boardcasted.add(nextboardcast);
	 }    

	void rtupdate(Packet rcvdpkt) {
		
		//If rcvdpkt is received by this node before, do nothing.
		if (this.boardcasted.contains(rcvdpkt.seqNo)){
			return;
		}

		//Record unreceived packet's sequence number.
		this.boardcasted.add(rcvdpkt.seqNo);

		//Update the adjancency table.
		for (int i = 0; i < 4; i++){
			this.graph[rcvdpkt.nodename][i] =  rcvdpkt.mincost[i];
			this.graph[i][rcvdpkt.nodename] = rcvdpkt.mincost[i];
		}
		
		if (this.boardcasted.size() == 4 || rcvdpkt.seqNo >= 4){
			int dijkstraResult[][] = dijkstra(this.graph, this.nodename);
			this.costs = dijkstraResultToCosts(dijkstraResult, this.costs, this.nodename);
			printdt();
		}

		//Boardcast rcvdpkt to neighbors
		for (int i = 0; i < 4; i++){
			if (this.nodename != i && this.lkcost[i] != 9999){
				NetworkSimulator.tolayer2(new Packet(this.nodename, i, rcvdpkt.nodename, rcvdpkt.mincost, rcvdpkt.seqNo));
			}
		}
		
	}

	/* called when cost from the node to linkid changes from current value to newcost*/
	void linkhandler(int linkid, int newcost) { 
		this.lkcost[linkid] = newcost;
		this.graph[linkid][this.nodename] = newcost;
		this.graph[this.nodename][linkid] = newcost;
		int dijkstraResult[][] = dijkstra(this.graph, this.nodename);
		this.costs = dijkstraResultToCosts(dijkstraResult, this.costs, this.nodename);
		this.nextboardcast += 4;
		this.boardcasted.add(this.nextboardcast);
		for (int i = 0; i < 4; i++){
			if (this.nodename != i && this.lkcost[i] != 9999){
			 NetworkSimulator.tolayer2(new Packet(this.nodename, i, this.nodename, this.lkcost, this.nextboardcast));
			}
		}
	}  

	/* Prints the current costs to reaching other nodes in the network */
	void printdt() {

		System.out.printf("                    \n");
		System.out.printf("   D%d |   cost  next-hop \n", nodename);
		System.out.printf("  ----|-----------------------\n");
		System.out.printf("     0|  %3d   %3d\n",costs[0][0],costs[0][1]);
		System.out.printf("dest 1|  %3d   %3d\n",costs[1][0],costs[1][1]);
		System.out.printf("     2|  %3d   %3d\n",costs[2][0],costs[2][1]);
		System.out.printf("     3|  %3d   %3d\n",costs[3][0],costs[3][1]);
		System.out.printf("                    \n");
	}
}
