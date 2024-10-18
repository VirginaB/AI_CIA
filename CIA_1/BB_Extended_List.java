import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphBB_EL {
    static Map<String, Map<String, Integer>> d = new HashMap<>();
    static Graph<String, DefaultEdge> g;
    static GraphPanel panel;

    public static void main(String[] args) {
        // Initialize graph data
        d.put("S", Map.of("A", 3, "B", 5));
        d.put("A", Map.of("S", 3, "B", 4, "D", 3));
        d.put("B", Map.of("S", 5, "A", 4, "C", 4));
        d.put("C", Map.of("B", 4, "E", 6));
        d.put("E", Map.of("C", 6));
        d.put("D", Map.of("A", 3, "G", 5));
        d.put("G", Map.of("D", 5));

        // Create and visualize the graph
        g = formGraph(d);
        Viz(g);

        // Calculate oracle value and run branch and bound with extended list
        int oracle = calculateOracleCost();
        branchAndBoundExtendedList(d, "S", "G", oracle);
    }

    public static void branchAndBoundExtendedList(
            Map<String, Map<String, Integer>> d,
            String start,
            String end,
            int oracle) {

        List<List<String>> queue = new ArrayList<>();
        queue.add(Arrays.asList("0", start));
        Set<String> visitedNodes = new HashSet<>();
        List<List<String>> allPaths = new ArrayList<>();

        System.out.println("Starting Branch and Bound with Extended List search...");
        System.out.println("Start node: " + start);
        System.out.println("Goal node: " + end);
        System.out.println("Oracle value: " + oracle);

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.remove(0);
            String currentNode = currentPath.get(currentPath.size() - 1);

            System.out.println("\nExploring node: " + currentNode);
            System.out.println("Current path: " + currentPath);
            System.out.println("Visited nodes: " + visitedNodes);

            if (currentNode.equals(end)) {
                System.out.println("Goal found! Final path: " + currentPath);
                updateVisualization(g, currentPath);
                return;
            }

            List<List<String>> successors = new ArrayList<>();

            // Explore neighbors
            for (String neighbor : d.get(currentNode).keySet()) {
                if (!currentPath.contains(neighbor) && !visitedNodes.contains(neighbor)) {
                    int cost = Integer.parseInt(currentPath.get(0)) + d.get(currentNode).get(neighbor);

                    if (cost <= oracle) {
                        List<String> newPath = new ArrayList<>(currentPath);
                        newPath.set(0, String.valueOf(cost));
                        newPath.add(neighbor);
                        successors.add(newPath);
                        visitedNodes.add(neighbor);
                    }
                }
            }

            // Sort successors by cost
            successors.sort(Comparator.comparingInt(p -> Integer.parseInt(p.get(0))));

            // Add sorted successors to queue and update allPaths
            queue.addAll(successors);
            allPaths.addAll(successors);

            System.out.println("Generated successors: " + successors);
            System.out.println("Updated queue: " + queue);

            // Visualize current exploration
            if (!currentPath.isEmpty()) {
                updateVisualization(g, currentPath);

                try {
                    Thread.sleep(1000);  // Delay for visualization
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("No path found within oracle cost!");
    }

    private static int calculateOracleCost() {
        // Example oracle calculation - you can modify this based on your needs
        return 15;
    }

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

    public static void Viz(Graph<String, DefaultEdge> g) {
        JFrame frame = new JFrame("Branch and Bound with Extended List");
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

            // Draw edges
            for (DefaultEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                Point sourcePoint = positions.get(source);
                Point targetPoint = positions.get(target);

                // Draw edge costs
                int midX = (sourcePoint.x + targetPoint.x) / 2;
                int midY = (sourcePoint.y + targetPoint.y) / 2;
                String cost = String.valueOf(d.get(source).get(target));

                if (isEdgeInCurrentPath(source, target)) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3.0f));
                } else {
                    g2d.setColor(Color.GREEN);
                    g2d.setStroke(new BasicStroke(1.0f));
                }
                g2d.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);

                // Draw cost labels
                g2d.setColor(Color.BLUE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString(cost, midX, midY);
            }

            // Draw vertices
            for (String vertex : graph.vertexSet()) {
                Point p = positions.get(vertex);

                // Highlight current path nodes
                if (currentPath.contains(vertex)) {
                    g2d.setColor(new Color(255, 200, 200));  // Light red
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