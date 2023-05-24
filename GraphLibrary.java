import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Library that implements methods to find paths between items somehow connected
 * @author Renata Edaes Hoh, Emiko Rohn
 * CS10, winter 2023
 */
public class GraphLibrary {

    /**
     * method to create a sorted graph, returning a graph tree sorted by shortest path from center of universe
     * @param g source graph with Vertices as actors and edges as movies connecting them, but without any sorting
     * @param source new center of the universe, the node for which the new tree will be created
     * @return graph sorted by center of the universe, with alll nodes connected to source
     * @param <V> node type
     * @param <E> edge type
     * @throws Exception if either original graph is empty, or source is not contained in graph
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) throws Exception {
        System.out.println("Center of the universe is " + source);

        if (g.numVertices() == 0) throw new Exception("Empty graph!");
        if (!g.hasVertex(source)) throw new Exception("Invalid universe center!");

        AdjacencyMapGraph<V, E> pathTree = new AdjacencyMapGraph<V, E>();
        pathTree.insertVertex(source);
        Set<V> visited = new HashSet<V>();
        Queue<V> queue = new LinkedList<V>();

        queue.add(source);
        visited.add(source);
        while(!queue.isEmpty()){
            V u = queue.remove();
            for(V v : g.outNeighbors(u)){
                if(!visited.contains(v)){
                    visited.add(v);
                    queue.add(v);
                    pathTree.insertVertex(v);
                    pathTree.insertDirected(v, u, g.getLabel(v, u));
                }
            }
        }
        return pathTree;
    }

    /**
     * find shortest path between center of universe and a node
     * @param graph sorted graph by BFS
     * @param v goal node for which we want to get the path from center of universe
     * @return list of the path of nodes between goal node and center of the universe
     * @param <V> type of vertex
     * @param <E> type of edge
     */
    public static <V,E> List<V> getPath(Graph<V,E> graph, V v){
        //If the tree doesn't have the vertex in the tree
        ArrayList<V> path = new ArrayList<V>();
        if (!graph.hasVertex(v)) {
            System.out.println(v + " is not connected to tree");
            // what if the vertex is simply not connected to Bacon?
        }
        else {
            V curr = v;
            while (graph.outDegree(curr) != 0) {
                path.add(curr);
                for (V s :graph.outNeighbors(curr)) {
                    curr = s;
                }
                if (graph.outDegree(curr) == 0) path.add(curr);
            }
        }
        return path;
    }

    /**
     * find vertices that are contained in the universe, but not connected to the center of universe (e.g. are in the original graph but not in BFS graph)
     * @param graph original graph, not sorted by BFS
     * @param subgraph graph sorted by BFS
     * @return list of nodes that are not connected to center of universe
     * @param <V> type of vertex
     * @param <E> type of edge
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        Set<V> missingV = new HashSet<>();
        for (V ele : graph.vertices()) {
            if (!subgraph.hasVertex(ele)) missingV.add(ele);
        }
        return missingV;
    }

    /**
     * find average separation in a BFS-sorted graph between center of universe and vertices
     * @param tree sorted graph by BFS
     * @param root center of universe
     * @return average separation
     * @param <V> type of vertex
     * @param <E> type of edge
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
        // is this graph the original graph or the BFS graph?
        double numNodes = tree.numVertices() - 1;
        double totalSeparation = averageHelper(tree, root, 0);

        return (totalSeparation / numNodes);
    }

    /**
     * helper method to recurse through graph in order to find total number of edges between center of universe and each node
     * @param tree BFS-sorted graph
     * @param startNode current node we are in
     * @param total total of the separation between root and all vertices
     * @return total edges
     * @param <V> type of vertex
     * @param <E> type of edge
     */
    private static <V, E> double averageHelper(Graph<V, E> tree, V startNode, double total){
        double num = total;
        for (V v : tree.inNeighbors(startNode)){
            num += averageHelper(tree, v, total + 1);
        }
        return num;
    }

    public static void main(String[] args) throws Exception {

        // maps to connect actors to movies directly, without IDs
        Map<String, String> actorsID = new HashMap<String, String>();
        Map<String, String> moviesID = new HashMap<String, String>();
        Map<String, Set<String>> actorsMovies = new HashMap<>();

        try {
            //Reads text from actorsTest.txt and adds to map
            BufferedReader br = new BufferedReader(new FileReader("inputs/actorsTest.txt"));
            String line;
            while((line = br.readLine()) != null){
                String[] actors = (line.split("\\|", 30));

                //Add actor ID and actor name to hash map
                actorsID.put(actors[0], actors[1]);
            }

            //Reads text from moviesTest.txt and adds to map
            br = new BufferedReader(new FileReader("inputs/moviesTest.txt"));
            while((line = br.readLine()) != null){
                String[] movies = (line.split("\\|", 30));

                //Add actor ID and actor name to hash map
                moviesID.put(movies[0], movies[1]);
            }

            //Reads text from movie-actorsTest, connecting values for movies and actors through IDs
            br = new BufferedReader(new FileReader("inputs/movie-actorsTest.txt"));
            while((line = br.readLine()) != null) {
                String[] a2m = (line.split("\\|", 20));

                //connect IDs to names
                String movie = moviesID.get(a2m[0]);
                String actor = actorsID.get(a2m[1]);

                //if map doesn't contain actor, then add it
                if (!actorsMovies.containsKey(actor)) {
                    actorsMovies.put(actor, new HashSet<>());
                }
                //add movie
                actorsMovies.get(actor).add(movie);
            }
        }
        finally {
            Graph<String, Set<String>> actorsGraph = new AdjacencyMapGraph<>();

            // insert all actors to new graph
            for (String a : actorsMovies.keySet()) {
                actorsGraph.insertVertex(a);
            }
            // loop over to find movies in common
            for (String a : actorsGraph.vertices()) {
                // loops over each movie of an actor
                for (String m : actorsMovies.get(a)) {
                    // loop over other actors
                    for (String a2 : actorsMovies.keySet()) {
                        // if other actor is also in the same movie and actors are different people
                        if (actorsMovies.get(a2).contains(m) && !a.equals(a2)){
                            // then add edge between them with edge label containing movie
                            if (!actorsGraph.hasEdge(a, a2)) actorsGraph.insertUndirected(a, a2, new HashSet<>());
                            actorsGraph.getLabel(a, a2).add(m);
                        }
                    }
                }
            }
            // just testing the code
            String centerUni = "Kevin Bacon";
            Graph<String, Set<String>> actor2actor = bfs(actorsGraph, centerUni);
            System.out.println(getPath(actor2actor, "Charlie"));
            System.out.println(getPath(actor2actor, "Nobody"));
            System.out.println(getPath(actor2actor, "Alice"));
            System.out.println(missingVertices(actorsGraph, actor2actor));
            System.out.println(averageSeparation(actor2actor, centerUni));

        }
    }

}
