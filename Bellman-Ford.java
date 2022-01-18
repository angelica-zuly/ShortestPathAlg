import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShortestPathAlgorithm2 {

	public static void main(String[] args) {
		new ShortestPathAlgorithmTest();
	}

	public static class ShortestPathAlgorithmTest {

		private static final String INPUT_DATA = "cop3503-asn3-input.txt";
		private static final String OUTPUT_DATA1 = "cop3503-asn3-output-longo-angelica-bf.txt";
		private static final String OUTPUT_DATA2 = "cop3503-asn3-output-longo-angelica-fw.txt";

		private Map<Integer,Node> nodeMap;
		private int startingVertex;
		private int numOfVertices;
		private boolean flag = false;

		/** Default constructor. */
		public ShortestPathAlgorithmTest() {
			initializeNodeData();
		}

		private void populateNodeData(String line) {
			String[] split = line.split(" ");

			Integer node1 = Integer.valueOf(split[0]);			    //[X x x]
			Integer node2 = Integer.valueOf(split[1]); 				//[x X x]
			Integer weightBetweenNodes = Integer.valueOf(split[2]);	//[x x X]
			Node node;

			// Node1 to Node2:
			if (nodeMap.containsKey(node1)) {
				// Grabbing node from HashMap
				node = nodeMap.get(node1);
			} else {
				// Creating new node in HaspMap
				node = new Node();
				node.nodeNumber = node1;
				nodeMap.put(node1, node); //Associating node value with its key
			}
			//Connecting nodes via given weight
			node.destinationToWeightMap.put(node2, weightBetweenNodes);		

			// Node2 to Node1:
			if (nodeMap.containsKey(node2)) {
				// Grabbing node from HashMap
				node = nodeMap.get(node2);
			} else {
				// Creating new node in HaspMap
				node = new Node();
				node.nodeNumber = node2;
				nodeMap.put(node2, node);
			}
			node.destinationToWeightMap.put(node1, weightBetweenNodes);
		}

		private void initializeNodeData() {
			File file = new File(INPUT_DATA);
			Scanner scan;
			PrintWriter pw1,pw2;

			try {
				scan = new Scanner(file);
				int count = 0;       
				while (scan.hasNextLine()) {  
					String eachLine = scan.nextLine();
					if (!eachLine.isEmpty()) {
						String[] split = eachLine.split("#");
						if (!split[0].isEmpty()) {
							count++;
							if (count == 1) {
								// Number of vertices from file
								numOfVertices = Integer.valueOf(split[0].trim());
								nodeMap = new HashMap<>();
							} else if (count == 2) {
								// Starting vertex from file
								startingVertex = Integer.valueOf(split[0].trim());
							} else if (count == 3) {
								// Number of edges from file
							} else { 
								populateNodeData(split[0].trim());
							}
							// Displaying file line by line
							System.out.println("[" + split[0].trim() + "]");							
						}
					}
				}
				scan.close();
			} catch (FileNotFoundException e) {
				System.out.println("Following file was not found: " + INPUT_DATA);
			}

			// Calling shortest path algorithms:
			bellmanFordAlgorithm();			
			int matrix[][] = floydWarshallAlgorithm(makeMatrix());

			//writing to output files:
			try {
				pw1 = new PrintWriter(OUTPUT_DATA1);		
				pw2 = new PrintWriter(OUTPUT_DATA2);	
				
				printGraph(pw1);
				displayMatrix(matrix,pw2);
				
				if (pw1.checkError() || pw2.checkError()) {
					System.out.println("error found in file output");
				} 
				pw1.close();
				pw2.close();

			} catch (FileNotFoundException e) {
				System.out.println("Following file was not found: " + OUTPUT_DATA1);
				System.out.println("Following file was not found: " + OUTPUT_DATA2);
			}			
		}

		private void bellmanFordAlgorithm() {
			// Bellman Ford's algorithm finds the shortest path of each node from start
			System.out.println("HashMap Nodes: " + nodeMap.keySet());

			Node source = nodeMap.get(startingVertex); // Setting start vertex
			source.shortestDistance = 0;
			Node current  = source;		

			int numOfIterations = 1;						
			while(numOfIterations != numOfVertices-1) {		
				// Update all the current's adjacent nodes and mark current as checked
				updateAdjNodes(current);				
				// Make next node the new current node
				current = nextNode(current);				

				if(allChecked()) {					
					//Check for changes made during iteration, if none - break
					if(flag == false) {
						source.shortestDistance = 0;
						source.parent = 0;
						System.out.println("finished!");
						System.out.println("Data displayed to output file");
						break;
					}					
					//Changes were made, so do another iteration
					int index = 1;
					while(index <= numOfVertices) {
						//Reset all nodes to "unchecked"
						Node temp = nodeMap.get(index);	
						temp.checked = false;
						index++;
					}
					numOfIterations++;
					current  = source;	//Starting back again at source vertex	
					flag = false;
				}
			} //end of while

		}
		
		private int[][] makeMatrix() {		
			int[][] matrix = new int[numOfVertices][numOfVertices];

			for(int i = 0; i < numOfVertices; i++) {
				//Grabbing each node:
				Node node = nodeMap.get(i+1);
				//Make array of adjacent nodes
				Object[] array = node.destinationToWeightMap.keySet().toArray();							

				for(int j = 0; j <numOfVertices; j++) {					
					if(i == j) {
						//node connected to itself is 0
						matrix[i][j] = 0;
					}
					else if(contains(array,j+1)) {
						//place weight for nodes that connect
						matrix[i][j] = node.destinationToWeightMap.get(j+1);
					}
					else {
						//place infinity for nodes that don't connect
						matrix[i][j] = 999;
					}					
				}				
			}	
			return matrix;
		}

		private int[][] floydWarshallAlgorithm(int[][] matrix) {
			// Floyd Warshall's algorithm makes a matrix of shortest paths from each node
			int[][] distance = new int[numOfVertices][numOfVertices];
			int i, j, k;

			//Make copy of matrix
			for(i = 0; i < numOfVertices; i++) {
				for(j = 0; j < numOfVertices; j++) {
					
					distance[i][j] = matrix[i][j];

				}
			}			
			//Updating shorter paths
			for(k = 0; k < numOfVertices; k++) {
				for(i = 0; i < numOfVertices; i++) {						
					for(j = 0; j < numOfVertices; j++) {
						if(distance[i][k] + distance[k][j] < distance[i][j]) {
							distance[i][j] = distance[i][k] + distance[k][j];
						}
					}
				}
			}
			return distance;
		}

		private boolean allChecked() {			
			boolean allChecked = false;
			int index = 1;
			int found = 0;
			while(index <= numOfVertices) {
				Node current = nodeMap.get(index);	
				if(current.checked) {
					found++;
				}
				index++;
			}
			if(found == numOfVertices) {
				allChecked = true;
			}			
			return allChecked;
		}
		
		private void updateAdjNodes(Node current) {
			// Creating array of all nodes connected to current
			Object[] adjacentNodes = current.destinationToWeightMap.keySet().toArray();

			//loop through adjacent nodes of current to update their data:
			for(int index = 0; index < adjacentNodes.length; index++) { 					

				// Set temp to node in adjacent array
				Node temp = nodeMap.get(adjacentNodes[index]);	
				temp.visited = true;

				if(temp.shortestDistance > current.destinationToWeightMap.get(adjacentNodes[index]) + current.shortestDistance) {
					// Update the adjacent node's total distance and parent
					temp.shortestDistance = current.destinationToWeightMap.get(adjacentNodes[index]) + current.shortestDistance;									
					temp.parent = current.nodeNumber;
					temp.visited = true;
					flag = true;
				}			
			} // done looping through each adj node of current
			current.checked = true;
			current.visited = true;
		}

		private Node nextNode(Node node) {
			int index = 1;
			while(index <= numOfVertices) {				
				Node current = nodeMap.get(index);
				//return the next unchecked visited node
				if(current.checked) {
					//Node has already been made current
					index++;
				}
				else if(!current.visited) {
					//Node has yet to be updated - skip it
					index++;
				}
				else{
					//Node has been visited before but not made current, so return it
					node = current;
					break;
				}	
			}
			return node;
		}

		private boolean contains(Object[] array, int item) {
			boolean found = false;			
			//Checks if item is found in array
			for(int i = 0; i < array.length; i++) {
				if(array[i] == (Object)item) {
					found = true;
				}
			}			
			return found;		
		}

		private void printGraph(PrintWriter pw) {			
			pw.println(numOfVertices);
			int index = 1;
			while(index <= numOfVertices) {
				Node current = nodeMap.get(index);
				pw.println(current.nodeNumber + " " +current.shortestDistance + " " + current.parent);
				index++;
			}
		}

		private void displayMatrix(int[][] matrix,PrintWriter pw) {
			pw.println(numOfVertices);			
			for(int i=0; i<numOfVertices; i++) {
				for(int j=0; j<numOfVertices; j++) {
					if(matrix[i][j] == 999) {
						pw.printf("><  ");
					}
					else {
						pw.printf(matrix[i][j] + "   ");
					}
				}
				pw.println();				
			}
		}
		
		public class Node {
			public int nodeNumber = 0;	
			public HashMap<Integer, Integer> destinationToWeightMap = new HashMap<Integer, Integer>();
			public boolean checked = false;
			public boolean visited = false;
			public int shortestDistance = Integer.MAX_VALUE;
			public int parent = 0;
		}

	}

}
