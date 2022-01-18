import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShortestPathAlgorithm {

	public static void main(String[] args) {
		new ShortestPathAlgorithmTest();
	}

	public static class ShortestPathAlgorithmTest {

		private static final String INPUT_DATA = "cop3503-asn2-input.txt";
		private static final String OUTPUT_DATA = "cop3503-asn2-output-longo-angelica.txt";

		private Map<Integer,Node> nodeMap;
		private int startingVertex;
		private int numOfVertices;

		/** Default constructor. */
		public ShortestPathAlgorithmTest() {
			initializeNodeData();
		}
		
		private void initializeNodeData() {
			File file = new File(INPUT_DATA);
			Scanner scan;
			PrintWriter pw;

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

			// Calling the shortest path algorithm:
			dijkstrasAlgorithm();
			
			//writing to output.txt file:
			try {
				pw = new PrintWriter(OUTPUT_DATA);				
				printGraph(pw);
				if (pw.checkError()) {
					System.out.println("error found in file output");
				} 
				pw.close();

			} catch (FileNotFoundException e) {
				System.out.println("Following file was not found: " + OUTPUT_DATA);
			}			
		}

		private void populateNodeData(String line) {
			String[] split = line.split(" ");

			Integer node1 = Integer.valueOf(split[0]);
			Integer node2 = Integer.valueOf(split[1]);
			Integer weightBetweenNodes = Integer.valueOf(split[2]);	
			Node node;

			// Node1 to Node2:
			if (nodeMap.containsKey(node1)) {
				// Grabbing node from HashMap
				node = nodeMap.get(node1);
			} else {
				// Creating new node in HaspMap
				node = new Node();
				node.nodeNumber = node1;
				nodeMap.put(node1, node);
			}
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
		
		private void dijkstrasAlgorithm() {
			// Dijkstra's algorithm finds the shortest path of each node from start
			System.out.println("HashMap Nodes: " + nodeMap.keySet());

			Node source = nodeMap.get(startingVertex); // Setting the given source vertex
			source.shortestDistance = 0;
			Node current  = source;
			
			while(allVisited() == false) {	
				// While there still exists some nodes that have not been visited...
				// Update all the current's adjacent nodes and mark it as visited
				updateAdjNodes(current);
				// Return the node with the shortest path and make it the new current node
				current = findShortPath(current);					
			}
			
			if(allVisited() == true) {
				// Once all the nodes in the graph have been visited,
				// Assign the starting vertex values to -1
				source.shortestDistance = -1;
				source.parent = -1;
				System.out.println("finished!");
			}
		}

		private Node findShortPath(Node node) {
			ArrayList<Node> paths = new ArrayList<Node>();
			int shortestDist = Integer.MAX_VALUE;

			int index = 1;
			while(index <= numOfVertices) {
				// Loop through all the nodes in the graph
				Node current = nodeMap.get(index);				
				if(current.visited == false) {
					// If not visited, add node to "paths" ArrayList
					paths.add(current);
				}
				index++;
			}
			
			if(!paths.isEmpty()) {				
				// If ArrayList not empty, loop to find node with shortest path
				index = 0;
				for(index = 0; index < paths.size(); index++) {
					if(paths.get(index).shortestDistance < shortestDist) {
						shortestDist = paths.get(index).shortestDistance;
						node = paths.get(index);
					}
				}
				
			}	
			return node;
		}
		
		private boolean allVisited() {			
			boolean visited = false;
			int index = 1;
			int found = 0;
			while(index <= numOfVertices) {
				Node current = nodeMap.get(index);				
				if(current.visited) {
					found++;
				}
				index++;
			}
			if(found == numOfVertices) {
				visited = true;
			}			
			return visited;
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

		private void updateAdjNodes(Node current) {
			
			// Creating array of all nodes connected to current
			Object[]adjacentNodes = current.destinationToWeightMap.keySet().toArray();
			System.out.println(current.nodeNumber + "'s set: " + current.destinationToWeightMap.entrySet());

			//loop through adjacent nodes of current to update their data:
			for(int index = 0; index < adjacentNodes.length; index++) { 					

				// Set temp to node in adjacent array
				Node temp = nodeMap.get(adjacentNodes[index]);	
				System.out.println("Node: " + temp.nodeNumber);

				if(temp.visited) {
					System.out.println("Node: " + temp.nodeNumber + " already visited");
					continue;
				}

				if(temp.shortestDistance > current.destinationToWeightMap.get(adjacentNodes[index]) + current.shortestDistance) {
					// Update the adjacent node's data (distance and parent):
					temp.shortestDistance = current.destinationToWeightMap.get(adjacentNodes[index]) + current.shortestDistance;					
					temp.parent = current.nodeNumber;					
				}	
				
				System.out.println("Distance: " + temp.shortestDistance);
				System.out.println("parent is: " + temp.parent);
				
			} // done looping through each adj node of current
			System.out.println(current.nodeNumber + " has now been visted");
			// Current can now be marked as "visited"
			current.visited = true;
		}
		
		public class Node {
			public int nodeNumber = 0;	
			public HashMap<Integer, Integer> destinationToWeightMap = new HashMap<Integer, Integer>();
			public boolean visited = false;
			public int shortestDistance = Integer.MAX_VALUE;
			public int parent = 0;
		}

	}

}
