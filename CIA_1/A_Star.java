import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphAStar {
    static Map<String, Map<String, Double>> d = new HashMap<>();      // Distance map
    static Map<String, Map<String, Double>> d_heur = new HashMap<>(); // Heuristic map
    static Graph<String, DefaultEdge> g;
    static GraphPanel panel;

    public static void main(String[] args) {
        // Initialize distance graph
        d.put("S", new HashMap<>(Map.of("A", 3.0, "B", 5.0)));
        d.put("A", new HashMap<>(Map.of("S", 3.0, "B", 4.0, "D", 3.0)));
        d.put("B", new HashMap<>(Map.of("S", 5.0, "A", 4.0, "C", 4.0)));
        d.put("C", new HashMap<>(Map.of("B", 4.0, "E", 6.0)));
        d.put("E", new HashMap<>(Map.of("C", 6.0)));
        d.put("D", new HashMap<>(Map.of("A", 3.0, "G", 5.0)));
        d.put("G", new HashMap<>(Map.of("D", 5.0)));

        // Initialize heuristic values
        d_heur.put("S", new HashMap<>(Map.of("A", 7.38, "B", 6.0)));
        d_heur.put("A", new HashMap<>(Map.of("S", Double.POSITIVE_INFINITY, "B", 6.0, "D", 5.0)));
        d_heur.put("B", new HashMap<>(Map.of("S", Double.POSITIVE_INFINITY, "A", 7.38, "C", 7.58)));
        d_heur.put("C", new HashMap<>(Map.of("B", 6.0, "E", Double.POSITIVE_INFINITY)));
        d_heur.put("E", new HashMap<>(Map.of("C", 7.58)));
        d_heur.put("D", new HashMap<>(Map.of("A", 7.38, "G", 0.0)));
        d_heur.put("G", new HashMap<>(Map.of("D", 5.0)));

        // Create and visualize the graph
        g = formGraph(d);
        Viz(g);

        // Calculate oracle cost and run A* search
        double oracleCost = calculateOracleCost(d, "S", "G", d_heur, 2);
        aStarSearch(d, "S", "G", d_heur, oracleCost);
    }

    public static void aStarSearch(
            Map<String, Map<String, Double>> d,
            String start,
            String end,
            Map<String, Map<String, Double>> d_heur,
            double oracleCost) {

        List<List<String>> queue = new ArrayList<>();
        queue.add(Arrays.asList("0", start));
        Set<String> visitedNodes = new HashSet<>();
        boolean goalFound = false;

        System.out.println("Starting A* Search...");
        System.out.println("Start node: " + start);
        System.out.println("Goal node: " + end);
        System.out.println("Oracle cost: " + oracleCost);

        while (!queue.isEmpty() && !goalFound) {
            List<String> currentPath = queue.remove(0);
            String currentNode = currentPath.get(currentPath.size() - 1);

            System.out.println("\nExploring node: " + currentNode);
            System.out.println("Current path: " + currentPath);
            System.out.println("Visited nodes: " + visitedNodes);

            if (currentNode.equals(end)) {
                System.out.println("Goal found! Final path: " + currentPath);
                updateVisualization(g, currentPath);
                goalFound = true;
                continue;
            }

            List<List<String>> successors = new ArrayList<>();

            // Explore neighbors
            for (String neighbor : d.get(currentNode).keySet()) {
                if (!currentPath.contains(neighbor) && !visitedNodes.contains(neighbor)) {
                    double gCost = Double.parseDouble(currentPath.get(0)) +
                            d.get(currentNode).get(neighbor);
                    double hCost = d_heur.get(currentNode).getOrDefault(neighbor, 0.0);
                    double totalCost = gCost + hCost;

                    if (totalCost <= oracleCost) {
                        List<String> newPath = new ArrayList<>(currentPath);
                        newPath.set(0, String.valueOf(totalCost));
                        newPath.add(neighbor);
                        successors.add(newPath);
                        visitedNodes.add(neighbor);
                    }
                }
            }

            // Sort successors by f(n) = g(n) + h(n)
            successors.sort(Comparator.comparingDouble(p -> Double.parseDouble(p.get(0))));
            queue.addAll(successors);
            queue.sort(Comparator.comparingDouble(p -> Double.parseDouble(p.get(0))));

            System.out.println("Generated successors: " + successors);
            System.out.println("Updated queue: " + queue);

            // Visualize current exploration
            updateVisualization(g, currentPath);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!goalFound) {
            System.out.println("No path found within oracle cost!");
        }
    }

    private static double calculateOracleCost(
            Map<String, Map<String, Double>> d,
            String start,
            String end,
            Map<String, Map<String, Double>> d_heur,
            double factor) {
        // Simple oracle cost calculation - can be modified based on needs
        return 30.0;
    }

    public static Graph<String, DefaultEdge> formGraph(Map<String, Map<String, Double>> d) {
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

    public static void Viz(Graph<String, DefaultEdge> g) {
        JFrame frame = new JFrame("A* Search Algorithm Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        panel = new GraphPanel(g);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void updateVisualization(Graph<String, DefaultEdge> g, List<String> path) {
        panel.setCurrentPath(path);
        panel.repaint();
    }

    static class GraphPanel extends JPanel {
        Graph<String, DefaultEdge> graph;
        Map<String, Point> positions;
        List<String> currentPath;

        public GraphPanel(Graph<String, DefaultEdge> graph) {
            this.graph = graph;
            this.positions = new HashMap<>();
            this.currentPath = new ArrayList<>();
            setPositionsInCircle();
        }

        private void setPositionsInCircle() {
            int centerX = 400;
            int centerY = 300;
            int radius = 200;
            int numVertices = graph.vertexSet().size();
            int i = 0;

            for (String vertex : graph.vertexSet()) {
                double angle = 2 * Math.PI * i / numVertices;
                int x = (int) (centerX + radius * Math.cos(angle));
                int y = (int) (centerY + radius * Math.sin(angle));
                positions.put(vertex, new Point(x, y));
                i++;
            }
        }

        public void setCurrentPath(List<String> path) {
            this.currentPath = path;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw edges with costs and heuristics
            for (DefaultEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                Point sourcePoint = positions.get(source);
                Point targetPoint = positions.get(target);

                // Calculate midpoint for labels
                int midX = (sourcePoint.x + targetPoint.x) / 2;
                int midY = (sourcePoint.y + targetPoint.y) / 2;

                // Draw the edge
                if (isEdgeInCurrentPath(source, target)) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3.0f));
                } else {
                    g2d.setColor(Color.GREEN);
                    g2d.setStroke(new BasicStroke(1.0f));
                }
                g2d.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);

                // Draw cost and heuristic labels
                g2d.setColor(Color.BLUE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                String cost = String.format("%.1f", d.get(source).get(target));
                String heuristic = String.format("%.1f",
                        d_heur.get(source).getOrDefault(target, Double.POSITIVE_INFINITY));
                g2d.drawString("c:" + cost + " h:" + heuristic, midX, midY);
            }

            // Draw vertices
            for (String vertex : graph.vertexSet()) {
                Point p = positions.get(vertex);

                // Highlight nodes in current path
                if (currentPath.contains(vertex)) {
                    g2d.setColor(new Color(255, 200, 200));  // Light red for visited
                } else {
                    g2d.setColor(Color.WHITE);
                }

                g2d.fillOval(p.x - 20, p.y - 20, 40, 40);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(p.x - 20, p.y - 20, 40, 40);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString(vertex, p.x - 6, p.y + 6);
            }
        }

        private boolean isEdgeInCurrentPath(String source, String target) {
            for (int i = 1; i < currentPath.size() - 1; i++) {
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