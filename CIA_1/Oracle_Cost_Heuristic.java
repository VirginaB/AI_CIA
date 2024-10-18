import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphOracleCH {

    static Map<String, Map<String, Integer>> d = new HashMap<>();
    static Map<String, Map<String, Double>> d_h = new HashMap<>();
    static Graph<String, DefaultEdge> g;
    static GraphPanel panel;

    public static void main(String[] args) {
        // Initialize graph data (costs and heuristic)
        d.put("S", Map.of("A", 3, "B", 5));
        d.put("A", Map.of("S", 3, "B", 4, "D", 3));
        d.put("B", Map.of("S", 5, "A", 4, "C", 4));
        d.put("C", Map.of("B", 4, "E", 6));
        d.put("E", Map.of("C", 6));
        d.put("D", Map.of("A", 3, "G", 5));
        d.put("G", Map.of("D", 5));

        d_h.put("S", Map.of("A", 7.38, "B", 6.0));
        d_h.put("A", Map.of("S", Double.POSITIVE_INFINITY, "B", 6.0, "D", 5.0));
        d_h.put("B", Map.of("S", Double.POSITIVE_INFINITY, "A", 7.38, "C", 7.58));
        d_h.put("C", Map.of("B", 6.0, "E", Double.POSITIVE_INFINITY));
        d_h.put("E", Map.of("C", 7.58));
        d_h.put("D", Map.of("A", 7.38, "G", 0.0));
        d_h.put("G", Map.of("D", 5.0));

        // Create the graph and visualize it
        g = formGraph(d);
        Viz(g);

        // Perform the oracle search (you can change to other algorithms)
        List<List<String>> FR = oracleCostHeuristic(d, "S", "G", d_h, 10);

        // Visualize and print each step
        for (List<String> path : FR) {
            System.out.println("Exploring path: " + path);
            updateVisualization(g, path);
            try {
                Thread.sleep(1000); // Pause to simulate the search process
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Oracle Cost Heuristic Search
    public static List<List<String>> oracleCostHeuristic(Map<String, Map<String, Integer>> d, String start, String end,
                                                         Map<String, Map<String, Double>> d_h, int maxNoGoalCutoff) {
        List<List<String>> res = new ArrayList<>();

        // Priority queue with custom class to handle cost separately
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingDouble(pn -> pn.cost));
        queue.add(new PathNode(0.0, List.of(start)));
        int goalCount = 0;

        while (!queue.isEmpty() && goalCount < maxNoGoalCutoff) {
            PathNode currPathNode = queue.poll();
            List<String> currPath = currPathNode.path;
            String currNode = currPath.get(currPath.size() - 1);

            // Explore neighbors
            for (String neighbor : d.get(currNode).keySet()) {
                if (!currPath.contains(neighbor)) {  // prevent cycles
                    List<String> newPath = new ArrayList<>(currPath);
                    newPath.add(neighbor);

                    double newCost = currPathNode.cost + d.get(currNode).get(neighbor) + d_h.get(currNode).get(neighbor);
                    queue.add(new PathNode(newCost, newPath));
                    res.add(newPath);

                    if (neighbor.equals(end)) {
                        goalCount++;
                    }
                }
            }
        }
        return res;
    }

    // Helper class to manage cost and path
    static class PathNode {
        double cost;
        List<String> path;

        PathNode(double cost, List<String> path) {
            this.cost = cost;
            this.path = new ArrayList<>(path);
        }
    }

    // Convert paths to graph
    public static Graph<String, DefaultEdge> formGraph(Map<String, Map<String, Integer>> d) {
        g = new SimpleGraph<>(DefaultEdge.class);
        for (String node : d.keySet()) {
            g.addVertex(node);
        }

        for (String node : d.keySet()) {
            for (String neighbor : d.get(node).keySet()) {
                g.addEdge(node, neighbor);
            }
        }
        return g;
    }

    // Visualization using Java Swing
    public static void Viz(Graph<String, DefaultEdge> g) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        panel = new GraphPanel(g);
        frame.add(panel);
        frame.setVisible(true);
    }

    // Update the graph visualization based on the current path being explored
    public static void updateVisualization(Graph<String, DefaultEdge> g, List<String> path) {
        panel.setCurrentPath(path);
        panel.repaint();
    }

    // Swing panel for graph rendering
    static class GraphPanel extends JPanel {
        Graph<String, DefaultEdge> graph;
        Map<String, Point> positions;
        List<String> currentPath;

        public GraphPanel(Graph<String, DefaultEdge> graph) {
            this.graph = graph;
            positions = new HashMap<>();
            currentPath = new ArrayList<>();
            setRandomPositions(graph);
        }

        private void setRandomPositions(Graph<String, DefaultEdge> graph) {
            Random random = new Random();
            for (String vertex : graph.vertexSet()) {
                positions.put(vertex, new Point(50 + random.nextInt(700), 50 + random.nextInt(500)));
            }
        }

        public void setCurrentPath(List<String> path) {
            this.currentPath = path;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw edges
            for (DefaultEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                Point sourcePoint = positions.get(source);
                Point targetPoint = positions.get(target);
                if (isEdgeInCurrentPath(source, target)) {
                    g2d.setColor(Color.RED); // Current path
                } else {
                    g2d.setColor(Color.GREEN); // Explored edges
                }
                g2d.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);
            }

            // Draw vertices
            for (String vertex : graph.vertexSet()) {
                Point p = positions.get(vertex);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillOval(p.x - 15, p.y - 15, 30, 30);
                g2d.setColor(Color.BLACK);
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
