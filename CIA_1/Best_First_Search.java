import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphBest {
    static Map<String, Map<String, Integer>> d = new HashMap<>();
    static Graph<String, DefaultEdge> g;
    static GraphPanel panel;
    static List<List<String>> allPaths = new ArrayList<>();

    public static void main(String[] args) {
        // Initialize graph data
        d.put("S", Map.of("A", 3, "B", 5));
        d.put("A", Map.of("S", 3, "B", 4, "D", 3));
        d.put("B", Map.of("S", 5, "A", 4, "C", 4));
        d.put("C", Map.of("B", 4, "E", 6));
        d.put("E", Map.of("C", 6));
        d.put("D", Map.of("A", 3, "G", 5));
        d.put("G", Map.of("D", 5));

        // Create the graph and visualize it
        g = formGraph(d);
        Viz(g);

        // Perform Best First Search
        List<List<String>> paths = bestFirstSearch(d, "S", "G");

        // Visualize each step
        for (List<String> path : paths) {
            System.out.println("Current path being explored: " + path);
            updateVisualization(g, path);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Best First Search implementation
    public static List<List<String>> bestFirstSearch(Map<String, Map<String, Integer>> graph, String start, String end) {
        PriorityQueue<SearchNode> queue = new PriorityQueue<>();
        List<List<String>> exploredPaths = new ArrayList<>();

        // Initialize with start node
        queue.add(new SearchNode(0, Arrays.asList(start)));

        while (!queue.isEmpty()) {
            SearchNode current = queue.poll();
            List<String> currentPath = current.path;
            String currentNode = currentPath.get(currentPath.size() - 1);

            exploredPaths.add(currentPath);

            // Check if we reached the goal
            if (currentNode.equals(end)) {
                break;
            }

            // Explore neighbors
            Map<String, Integer> neighbors = graph.get(currentNode);
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String nextNode = neighbor.getKey();
                if (!currentPath.contains(nextNode)) {
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(nextNode);
                    queue.add(new SearchNode(current.cost + neighbor.getValue(), newPath));
                }
            }
        }

        return exploredPaths;
    }

    // Helper class for Best First Search
    static class SearchNode implements Comparable<SearchNode> {
        int cost;
        List<String> path;

        SearchNode(int cost, List<String> path) {
            this.cost = cost;
            this.path = path;
        }

        @Override
        public int compareTo(SearchNode other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    // Convert graph data to JGraphT graph
    public static Graph<String, DefaultEdge> formGraph(Map<String, Map<String, Integer>> d) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (String node : d.keySet()) {
            graph.addVertex(node);
        }
        for (String node : d.keySet()) {
            for (String neighbor : d.get(node).keySet()) {
                graph.addEdge(node, neighbor);
            }
        }
        return graph;
    }

    // Initialize visualization
    public static void Viz(Graph<String, DefaultEdge> g) {
        JFrame frame = new JFrame("Best First Search Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        panel = new GraphPanel(g);
        frame.add(panel);
        frame.setVisible(true);
    }

    // Update visualization with current path
    public static void updateVisualization(Graph<String, DefaultEdge> g, List<String> path) {
        panel.setCurrentPath(path);
        panel.repaint();
    }

    // Graph visualization panel
    static class GraphPanel extends JPanel {
        Graph<String, DefaultEdge> graph;
        Map<String, Point> positions;
        List<String> currentPath;

        public GraphPanel(Graph<String, DefaultEdge> graph) {
            this.graph = graph;
            this.positions = new HashMap<>();
            this.currentPath = new ArrayList<>();
            initializePositions();
        }

        private void initializePositions() {
            // Set fixed positions for better visualization
            positions.put("S", new Point(100, 300));
            positions.put("A", new Point(250, 200));
            positions.put("B", new Point(250, 400));
            positions.put("C", new Point(400, 400));
            positions.put("D", new Point(400, 200));
            positions.put("E", new Point(550, 400));
            positions.put("G", new Point(550, 200));
        }

        public void setCurrentPath(List<String> path) {
            this.currentPath = path;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw edges
            for (DefaultEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                Point sourcePoint = positions.get(source);
                Point targetPoint = positions.get(target);

                // Check if edge is in current path
                boolean isInPath = isEdgeInCurrentPath(source, target);
                g2d.setColor(isInPath ? Color.RED : Color.BLACK);
                g2d.setStroke(new BasicStroke(isInPath ? 3.0f : 1.0f));
                g2d.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);
            }

            // Draw vertices
            for (String vertex : graph.vertexSet()) {
                Point p = positions.get(vertex);
                boolean isInPath = currentPath.contains(vertex);

                g2d.setColor(isInPath ? Color.YELLOW : Color.WHITE);
                g2d.fillOval(p.x - 20, p.y - 20, 40, 40);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(p.x - 20, p.y - 20, 40, 40);
                g2d.drawString(vertex, p.x - 5, p.y + 5);
            }
        }

        private boolean isEdgeInCurrentPath(String source, String target) {
            for (int i = 0; i < currentPath.size() - 1; i++) {
                String pathSource = currentPath.get(i);
                String pathTarget = currentPath.get(i + 1);
                if ((source.equals(pathSource) && target.equals(pathTarget)) ||
                        (source.equals(pathTarget) && target.equals(pathSource))) {
                    return true;
                }
            }
            return false;
        }
    }
}