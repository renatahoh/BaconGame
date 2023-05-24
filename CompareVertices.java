import java.util.Comparator;

public class CompareVertices<V, E> implements Comparator<V> {
    private final Graph<V, E> graph;

    public CompareVertices(Graph<V, E> g){
        this.graph = g;
    }

    @Override
    public int compare(V o1, V o2) {
        if(graph.inDegree(o1) > graph.inDegree(o2)) return -1;
        else return 1;
    }
}
