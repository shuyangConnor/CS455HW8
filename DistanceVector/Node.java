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
    
    int[] lkcost;		/*The link cost between this node and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/
		int[] direct_lkcost;
    List<Integer> boardcasted = new ArrayList<>();
		int nextboardcast;
		List<Integer> neighbors = new ArrayList<>();
    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) { 
			this.nodename = nodename;
      this.costs = new int[4][4];
			this.lkcost = initial_lkcost;
			this.direct_lkcost = this.lkcost;
			this.nextboardcast = this.nodename;
			//Initialize neighbors.
			for (int x = 0; x < 4; x++){
				if (this.lkcost[x] != 9999){
					this.neighbors.add(x);
				}
			}

			//Initialize entry table.
			for (int x = 0; x < 4; x++){
				// System.out.println(this.nodename + ":   "+ this.lkcost[x]);
				for (int y = 0; y < 4; y++){
					this.costs[x][y] = 9999;
					if (x == y){
						this.costs[x][y] = this.lkcost[x];
					}
				}
			}

			//Boardcasting.
			for (int i = 0; i < 4; i++){
				if (this.nodename != i && this.lkcost[i] != 9999){
					NetworkSimulator.tolayer2(new Packet(this.nodename, i, this.lkcost));
				}
			}
			this.boardcasted.add(this.nextboardcast);
		}    
    
    void rtupdate(Packet rcvdpkt) { 
			//See if there is any change of lkcost.
			boolean changed = false;

			//Update costs
			for (int x = 0; x < 4; x++){
				this.costs[x][rcvdpkt.sourceid] = this.direct_lkcost[rcvdpkt.sourceid] + rcvdpkt.mincost[x];
			}

			//Update lkcost
			for (int i = 0; i< 4; i++){
				if (this.costs[i][rcvdpkt.sourceid] < this.lkcost[i]){
					changed = true;
				}
				this.lkcost[i] = Math.min(this.lkcost[i], this.costs[i][rcvdpkt.sourceid]);
			}

			//Boardcast if there is a change.
			if (changed){
				nextboardcast += 4;
				for (int i = 0; i < 4; i++){
					if (this.nodename != i && this.neighbors.contains(i) && i != rcvdpkt.sourceid){
						NetworkSimulator.tolayer2(new Packet(this.nodename, i, this.lkcost));
					}
				}
				this.boardcasted.add(this.nextboardcast);
			}

			// if (this.nodename == 0){
			// 	for (int x = 0; x <4; x++){
			// 		System.out.println("After reciving packet from: "+rcvdpkt.sourceid+"   "+this.nodename + ": " + this.lkcost[x]);
			// 	}
			// }
		}
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {  }    


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }
    
}
