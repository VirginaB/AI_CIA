import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphAOStar {
    static class Node {
        String name;
        double heuristic;
        String nodeType;  // "AND" or "OR"
        List<String> children;
        double cost;
        boolean solved;
        String bestChild;

        public Node(String name, double heuristic, String nodeType, List<String> children) {
            this.name = name;
            this.heuristic = heuristic;
            this.nodeType = nodeType;
            this.children = children != null ? children : new ArrayList<>();
            this.cost = Double.POSITIVE_INFINITY;
            this.solved = false;
            this.bestChild = null;
        }
    }

    static Map<String, Node> graph = new HashMap<>();
    static GraphPanel panel;

    public static void main(String[] args) {
        // Initialize the graph
        graph.put("A", new Node("A", 2, "OR", Arrays.asList("B", "C")));
        graph.put("B", new Node("B", 1, "AND", Arrays.asList("D", "E")));
        graph.put("C", new Node("C", 3, "OR", Arrays.asList("F")));
        graph.put("D", new Node("D", 2, "AND", new ArrayList<>()));
        graph.put("E", new Node("E", 1, "AND", new ArrayList<>()));
        graph.put("F", new Node("F", 4, "AND", new ArrayList<>()));

        // Set goal nodes
        graph.get("D").cost = 0;
        graph.get("D").solved = true;
        graph.get("E").cost = 0;
        graph.get("E").solved = true;
        graph.get("F").cost = 0;
        graph.get("F").solved = true;

        // Create and show visualization
        createVisualization();

        // Run AO* algorithm
        System.out.println("Starting AO* Search...");
        double cost = aoStar("A");
        System.out.println("Final cost to reach goal: " + cost);
        System.out.println("Best child of A: " + graph.get("A").bestChild);

        // Update final visualization
        updateVisualization();
    }

    public static double aoStar(String startNode) {
        Node currentNode = graph.get(startNode);
        System.out.println("\nExploring node: " + startNode);

        // Base case: if node is already solved
        if (currentNode.solved) {
            System.out.println("Node " + startNode + " is already solved with cost " + currentNode.cost);
            return currentNode.cost;
        }

        // OR node
        if (currentNode.nodeType.equals("OR")) {
            double minCost = Double.POSITIVE_INFINITY;
            String bestChild = null;

            for (String child : currentNode.children) {
                System.out.println("Examining OR child: " + child);
                double childCost = aoStar(child);
                double totalCost = childCost + currentNode.heuristic;

                if (totalCost < minCost) {
                    minCost = totalCost;
                    bestChild = child;
                }

                // Update visualization after examining each child
                updateVisualization();
                sleep(1000);
            }

            currentNode.cost = minCost;
            currentNode.bestChild = bestChild;
            currentNode.solved = true;
            System.out.println("OR node " + startNode + " solved with cost " + minCost + ", best child: " + bestChild);
            return minCost;
        }
        // AND node
        else {
            double totalCost = currentNode.heuristic;
            System.out.println("Processing AND node: " + startNode);

            for (String child : currentNode.children) {
                double childCost = aoStar(child);
                totalCost += childCost;

                // Update visualization after examining each child
                updateVisualization();
                sleep(1000);
            }

            currentNode.cost = totalCost;
            currentNode.solved = true;
            System.out.println("AND node " + startNode + " solved with total cost " + totalCost);
            return totalCost;
        }
    }

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createVisualization() {
        JFrame frame = new JFrame("AO* Search Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        panel = new GraphPanel();
        frame.add(panel);
        frame.setVisible(true);
    }

    private static void updateVisualization() {
        if (panel != null) {
            panel.repaint();
        }
    }

    static class GraphPanel extends JPanel {
        private Map<String, Point> positions = new HashMap<>();
        private final int nodeRadius = 25;

        public GraphPanel() {
            setupNodePositions();
        }

        private void setupNodePositions() {
            // Layer-based positioning
            positions.put("A", new Point(400, 50));  // Root
            positions.put("B", new Point(300, 150)); // Layer 1
            positions.put("C", new Point(500, 150)); // Layer 1
            positions.put("D", new Point(200, 250)); // Layer 2
            positions.put("E", new Point(400, 250)); // Layer 2
            positions.put("F", new Point(500, 250)); // Layer 2
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw edges
            for (Map.Entry<String, Node> entry : graph.entrySet()) {
                Node node = entry.getValue();
                Point parentPos = positions.get(node.name);

                for (String childName : node.children) {
                    Point childPos = positions.get(childName);

                    // Set edge color based on whether it's the best path
                    if (node.bestChild != null && node.bestChild.equals(childName)) {
                        g2d.setColor(Color.RED);
                        g2d.setStroke(new BasicStroke(3.0f));
                    } else {
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1.0f));
                    }

                    g2d.drawLine(parentPos.x, parentPos.y, childPos.x, childPos.y);
                }
            }

            // Draw nodes
            for (Map.Entry<String, Node> entry : graph.entrySet()) {
                Node node = entry.getValue();
                Point pos = positions.get(node.name);

                // Node background color
                if (node.solved) {
                    g2d.setColor(new Color(144, 238, 144)); // Light green for solved
                } else {
                    g2d.setColor(Color.WHITE);
                }

                // Draw node circle
                g2d.fillOval(pos.x - nodeRadius, pos.y - nodeRadius,
                        2 * nodeRadius, 2 * nodeRadius);

                // Node border
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(pos.x - nodeRadius, pos.y - nodeRadius,
                        2 * nodeRadius, 2 * nodeRadius);

                // Draw node information
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String nodeText = String.format("%s (%s)", node.name, node.nodeType);
                g2d.drawString(nodeText, pos.x - 20, pos.y);

                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                String costText = String.format("h=%.1f c=%.1f",
                        node.heuristic,
                        node.cost == Double.POSITIVE_INFINITY ?
                                0 : node.cost);
                g2d.drawString(costText, pos.x - 25, pos.y + 15);
            }
        }
    }
}