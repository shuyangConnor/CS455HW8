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
		int[] position_reverse;
		List<Integer> neighbors = new ArrayList<>();
    /* Class constructor */
    public Node() { }
    
	 int[] smallestInRow(int[] row){
		int minimum = row[0];
		int index = 0;
		for (int x = 0; x < row.length; x++){
			if (row[x] < minimum){
				minimum = row[x];
				index=x;
			}
		}
		int[]result = new int[2];
		result[0] = minimum;
		result[1] = index;
		return result;
	 }

    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) { 
			this.nodename = nodename;
      this.costs = new int[4][4];
			this.lkcost = initial_lkcost;
			this.direct_lkcost = this.lkcost.clone();
			this.position_reverse = new int[4];
			
			//Initialize neighbors.
			for (int x = 0; x < 4; x++){
				if (this.lkcost[x] != 9999){
					this.neighbors.add(x);
					this.position_reverse[x]=x;
				}
				else {
					this.position_reverse[x] = -1;
				}
			}

			//Initialize entry table.
			for (int x = 0; x < 4; x++){
				for (int y = 0; y < 4; y++){
					this.costs[x][y] = 9999;
					if (x == y){
						this.costs[x][y] = this.lkcost[x];
					}
				}
			}

			//Boardcasting.
			for (int i = 0; i < 4; i++){
				if (this.nodename != i && this.lkcost[i] < 9999){
					NetworkSimulator.tolayer2(new Packet(this.nodename, i, this.lkcost));
				}
			}
		}    
    
    void rtupdate(Packet rcvdpkt) { 
			//See if there is any change of lkcost.
			boolean changed = false;

			//Update costs
			for (int x = 0; x < 4; x++){
				this.costs[x][rcvdpkt.sourceid] = this.direct_lkcost[rcvdpkt.sourceid] + rcvdpkt.mincost[x];
				if (this.costs[x][rcvdpkt.sourceid] > 9999){
					this.costs[x][rcvdpkt.sourceid] = 9999;
				}
			}

			//Update lkcost
			for (int i = 0; i< 4; i++){
				int min = smallestInRow(this.costs[i])[0];
				if (min != this.lkcost[i]){
					this.position_reverse[i] = smallestInRow(this.costs[i])[1];
					this.lkcost[i] = Math.min(this.lkcost[i], min);
					changed = true;
				}
			}

			//Boardcast if there is a change.
			if (changed){
				for (int i = 0; i < 4; i++){
					if (this.nodename != i && this.neighbors.contains(i)){
						int[] sndpkt = new int[4];
						for(int j = 0; j < 4; j++) {
    					if(position_reverse[j] == i) {
    						sndpkt[j] = 9999;
    					}
    					else {
    						sndpkt[j] = this.lkcost[j];
    					}
    				}
						NetworkSimulator.tolayer2(new Packet(this.nodename, i, sndpkt));
					}
				}
			}
		}
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) { 
			this.direct_lkcost[linkid] = newcost;
			int oldlkcost;
			int lowest;
			boolean changed = false;
			int oldcost = this.costs[linkid][linkid];
			for (int x = 0; x < 4; x++){
				this.costs[x][linkid] = this.costs[x][linkid] - oldcost + newcost;
				if (this.costs[x][linkid] > 9999){
					this.costs[x][linkid] = 9999;
				}
				oldlkcost = this.lkcost[x];
				lowest = smallestInRow(this.costs[x])[0];
				if (lowest != oldlkcost){
					this.position_reverse[x] = smallestInRow(this.costs[x])[1];
					this.lkcost[x] = lowest;
					changed = true;
				}
			}
			if (changed){
				for (int i = 0; i < 4; i++){
					if (this.nodename != i && this.neighbors.contains(i)){
						int[] sndpkt = new int[4];
						for(int j = 0; j < 4; j++) {
    					if(position_reverse[j] == i) {
    						sndpkt[j] = 9999;
    					}
    					else {
    						sndpkt[j] = this.lkcost[j];
    					}
    				}
						NetworkSimulator.tolayer2(new Packet(this.nodename, i, sndpkt));
					}
				}
			}
			printdt();
		}


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
