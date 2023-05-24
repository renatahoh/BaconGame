import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * create Bacon game
 * @author Renata Edaes Hoh, Emiko Rohn
 * CS10, winter 2023
 */
public class BaconGame {
    Graph<String, Set<String>> originalGraph, pathTree;
    String center;

    Map<String, String> actorsID, moviesID;
    Map<String, Set<String>> actorsMovies;

    /**
     * Constructor makes BFS tree from three files and center of the universe
     * @param file1 - file with actors
     * @param file2 - file with movies
     * @param file3 - file with actors and movies
     * @param center - center of the universe
     * @throws Exception if one of files is not found
     */
    public BaconGame(String file1, String file2, String file3, String center) throws Exception {
        originalGraph = new AdjacencyMapGraph<>();
        try {
            makeMaps(file1, file2, file3);
        } catch (Exception e) {
            System.err.println("File not found!");;
        } finally {
            this.center = center;
            pathTree = GraphLibrary.bfs(originalGraph, center);
        }
    }

    /**
     * c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation
     *
     * @param num number of best bacons to print
     * @return list with the num number of best bacons
     */
    public List<String> bestBacons(int num) {
        Map<String, Integer> m = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<String>((String s1, String s2) -> m.get(s2) - m.get(s1));

        for (String vertex : originalGraph.vertices()) {
            m.put(vertex, (int) GraphLibrary.averageSeparation(pathTree, vertex));
            pq.add(vertex);
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i <= num; i++) {
            list.add(pq.remove());
        }
        return list;
    }

    /**
     * p <name>: find path from <name> to current center of the universe
     *
     * @param node starting node
     * @return return path from node to center of the universe
     */
    public List<String> findPath(String node) {
        List<String> path = GraphLibrary.getPath(pathTree, node);
        ArrayList<String> output = new ArrayList<String>();
        for (int i = 0; i < path.size() - 1; i++) {
            output.add(path.get(i) + " appeared in [" + pathTree.getLabel(path.get(i), path.get(i + 1)) + "] with " + path.get(i + 1));
        }
        return output;
    }

    /**
     * u <name>: make <name> the center of the universe
     * @param newCenter new center of the universe
     * @throws Exception if newCenter is not valid
     */
    public void changeCenter(String newCenter) throws Exception {
        center = newCenter;
        pathTree = GraphLibrary.bfs(originalGraph, newCenter);
        System.out.println("Average separation is: " + GraphLibrary.averageSeparation(originalGraph, newCenter));
    }

    /**
     * d <low> <high>: list actors sorted by degree, with degree between low and high
     * @param low lower limit of degrees for list
     * @param high upper limit of degrees for list
     * @return
     */
    public List<String> listByDegree(int low, int high) {
        List<String> list = new ArrayList<>();
        for (String vertex : originalGraph.vertices()) {
            if (originalGraph.outDegree(vertex) >= low && originalGraph.outDegree(vertex) <= high) {
                list.add(vertex);
            }
        }
        list.sort(new CompareVertices<>(originalGraph));
        return list;
    }

    /**
     * i: list actors with infinite separation from the current center
     * @return return list of actors with infinite separation from the current actor
     */
    public List<String> infiniteSeparation() {
        List<String> i = new ArrayList<String>();
        Set<String> missingVertices = GraphLibrary.missingVertices(originalGraph, pathTree);
        i.addAll(missingVertices);
        return i;
    }

    /**
     * s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high
     * @param low lower limit for degree of separation from current center
     * @param high upper limit for degree of separation from current center
     * @return list of actors within limits, sorted
     */
    public List<String> listBySeparation(int low, int high) {
        Map<String, Integer> m = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<String>((String s1, String s2) -> m.get(s1) - m.get(s2));
        for (String vertex : pathTree.vertices()) {
            int sep = findPath(vertex).size();
            if (sep >= low && sep <= high) {
                m.put(vertex, sep);
                pq.add(vertex);
            }
        }
        List<String> i = new ArrayList<>();
        while(!pq.isEmpty()) {
            i.add(pq.remove());
        }

        return i;
    }

    /**
     * Make map that connects actors with movies
     * @param file1 actors file
     * @param file2 movies file
     * @param file3 movie-actors file
     * @throws Exception if files aren't valid
     */
    public void makeMaps(String file1, String file2, String file3) throws Exception {

        // maps to connect actors to movies directly, without IDs
        actorsID = new HashMap<String, String>();
        moviesID = new HashMap<String, String>();
        actorsMovies = new HashMap<>();

        try {
            //Reads text from actors.txt and adds to map
            BufferedReader br = new BufferedReader(new FileReader(file1));
            String line;
            while ((line = br.readLine()) != null) {
                String[] actors = (line.split("\\|", 30));

                //Add actor ID and actor name to hash map
                actorsID.put(actors[0], actors[1]);
            }

            //Reads text from movies.txt and adds to map
            br = new BufferedReader(new FileReader(file2));
            while ((line = br.readLine()) != null) {
                String[] movies = (line.split("\\|", 30));

                //Add actor ID and actor name to hash map
                moviesID.put(movies[0], movies[1]);
            }

            //Reads text from movie-actors, connecting values for movies and actors through IDs
            br = new BufferedReader(new FileReader(file3));
            while ((line = br.readLine()) != null) {
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
        } finally {

            for (String a : actorsMovies.keySet()) {
                originalGraph.insertVertex(a);
            }

            // insert all actors to new graph
            for (String a : originalGraph.vertices()) {
                for (String m : actorsMovies.get(a)) {
                    for (String a2 : actorsMovies.keySet()) {
                        if (actorsMovies.get(a2).contains(m) && !a.equals(a2)) {
                            if (!originalGraph.hasEdge(a, a2)) originalGraph.insertUndirected(a, a2, new HashSet<>());
                            originalGraph.getLabel(a, a2).add(m);
                        }
                    }
                }
            }
        }
    }

    //main
    //use a while(true) unless q is clicked
    //check length
    public static void main(String[] args) throws Exception {
        BaconGame BG = new BaconGame("inputs/actors.txt", "inputs/movies.txt", "inputs/movie-actors.txt", "Kevin Bacon");

        boolean qpressed = false;

        while (!qpressed) {
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            String[] inputArray = input.split(" ");

            //q
            if (input.equals("q")) qpressed = !qpressed;

            //d
            if (inputArray[0].equals("d")) {
                int low = Integer.parseInt(inputArray[1]);
                int high = Integer.parseInt(inputArray[2]);
                List<String> list = BG.listByDegree(low, high);

                for (String s : list) {
                    System.out.println(s);
                }
            }

            //i
            if (inputArray[0].equals("i")) {
                List<String> path = BG.infiniteSeparation();

                for (String s : path) {
                    System.out.println(s);
                }
            }

            //p
            if (inputArray[0].equals("p")) {
                String name = inputArray[1];
                List<String> path = BG.findPath(name);

                for (String s : path) {
                    System.out.println(s);
                }
            }

            //u
            if (inputArray[0].equals("u")) {
                String name = "";
                for(int i = 1; i < inputArray.length; i++){
                    name += inputArray[i];
                    name += " ";
                }
                name = name.substring(0, name.length()-1);
                BG.changeCenter(name);

                System.out.println("Average separation is: " + GraphLibrary.averageSeparation(BG.pathTree, name));
            }

            //s
            if (inputArray[0].equals("s")) {
                int low = Integer.parseInt(inputArray[1]);
                int high = Integer.parseInt(inputArray[2]);

                List<String> list = BG.listBySeparation(low, high);

                for (String s : list) {
                    System.out.println(s);
                }
            }

            //c
            if (inputArray[0].equals("c")) {
                int num = Integer.parseInt(inputArray[1]);

                List<String> list = BG.bestBacons(num);

                for (String s : list) {
                    System.out.println(s);
                }
            }
        }
    }
}