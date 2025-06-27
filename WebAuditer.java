package PdfFile;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.*;
import java.awt.*;

import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.series.Series;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import static org.apache.fontbox.cff.CharStringCommand.Key.RANDOM;

public class WebAuditer extends JFrame implements ActionListener {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();
    private Map<String, double[]> urlScoresMap = new HashMap<>();
    private Map<String,String> categoryMap = new HashMap<>();
    private JFrame frame; // Declare frame at the class level
    private final JButton findcat,Backlinks,PageTime,uiAnalyser,checkBrokenLinksButton,socialAnalysis,GeneratePDF,abort;
    private HashMap<String, Trie> categoryTries;
    private JProgressBar progressBar;
    private JTextField urlField;
    private JTextArea resultTextArea;
    private Map<String, Set<String>> categoryKeywords;
    private WebsiteInfoNode head;
    private WebsiteInfoNode tail;
    private static final int MAX_CONCURRENT_THREADS = 20;
    private final ExecutorService executorService;
    private static final double PAGE_SPEED_WEIGHT = 0.3;
    private static final double MOBILE_FRIENDLINESS_WEIGHT = 0.2;
    public double pageSpeedScore;
    public double mobileFriendlinessScore;
    public double navigationScore;
    public double accessibilityScore;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private long loadTime,start,end,CatAnalysisTime,BacklinkAnalysisTime,BrokenLinkAnalysisTime,PageLoadTimeAnalysisTime,UIAnalysisTime,SocialAnalysisTime,GeneratePDFTime;
    public WebAuditer() {
        setTitle("SEO Auditing Tool");
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
//        initializeCategoryKeywords();
        initializeCategoryTries();
        urlField = createStyledTextField();
        add(urlField);

        // Create panel for analyze button with FlowLayout
        GradientPanel mainPanel = new GradientPanel(new Color(245, 3, 32), new Color(5, 62, 248));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        findcat = createGlassyButton("Find Category");
        findcat.addActionListener(this);
        buttonPanel.add(findcat);
        findcat.setPreferredSize(new Dimension(150,20));
        Backlinks = createGlassyButton("Find Backlinks");
        Backlinks.addActionListener(this);
        Backlinks.setPreferredSize(new Dimension(150,20));
        buttonPanel.add(Backlinks);
        checkBrokenLinksButton = createGlassyButton("Check Broken Links");
        checkBrokenLinksButton.addActionListener(this);
        checkBrokenLinksButton.setPreferredSize(new Dimension(150,20));
        buttonPanel.add(checkBrokenLinksButton);
        PageTime = createGlassyButton("Page Load Time");
        PageTime.addActionListener(this);
        PageTime.setPreferredSize(new Dimension(150,20));
        buttonPanel.add(PageTime);
        uiAnalyser = createGlassyButton("UI Analysis");
        uiAnalyser.addActionListener(this);
        uiAnalyser.setPreferredSize(new Dimension(150,20));
        buttonPanel.add(uiAnalyser);
        socialAnalysis = createGlassyButton("Social Analysis");
        socialAnalysis.addActionListener(this);
        socialAnalysis.setPreferredSize(new Dimension(150,20));
        buttonPanel.add(socialAnalysis);
        GeneratePDF = createGlassyButton("GeneratePDF");
        GeneratePDF.addActionListener(this);
        GeneratePDF.setPreferredSize(new Dimension(150,20));
        buttonPanel.add(GeneratePDF);
        abort = createGlassyButton("Abort the URL");
        abort.addActionListener(this);
        // Initialize the abort button
        abort.setEnabled(false); // Initially disabled
        abort.addActionListener(e -> {
            // Dispose the running frame
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                WebAuditer newFrame = new WebAuditer();
                newFrame.setVisible(true);
            });
        });
        // Create text area for result
        resultTextArea = createStyledTextArea();
        resultTextArea.setEditable(false);
//        resultTextArea.setFont(new Font("SansSerif", Font.BOLD,15));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        // Add components to main panel
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true); // Show percentage text
        progressBar.setValue(0); // Start with 0% progress
        // Add progressBar to mainPanel or any suitable container
        // Create panel for result text area and progress bar with BorderLayout
        JPanel resultPanel = new JPanel(new BorderLayout());
        // Add scrollPane containing resultTextArea to the result panel
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        // Add progressBar to the result panel
        resultPanel.add(progressBar, BorderLayout.SOUTH);

        // Add components to main panel
        mainPanel.add(urlField, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        // Add main panel to frame
        add(mainPanel);
        // Set frame visibility
        setVisible(true);
        executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_THREADS);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        progressBar.requestFocusInWindow();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Enable abort button if process exceeds 1 minute (60,000 milliseconds)
                if (processIsHung()) {
                    SwingUtilities.invokeLater(() -> abort.setEnabled(true));
                }
            }
        }, 120000, 120000);
    }
    private boolean processIsHung() {
        // Check if the resultTextArea is empty
        return resultTextArea.getText().isEmpty();
    }

    Map<String, Long> urlPageLoadTimes = new HashMap<>();

    Map<String, Integer> socialLinksgraph = new HashMap<>();

    // ... existing code ...

    private boolean graphDisplayed = false;


private void AnalysisTimeGraph(long catAnalysisTime, long backlinkAnalysisTime, long brokenLinkAnalysisTime, long pageLoadTimeAnalysisTime, long uiAnalysisTime, long socialAnalysisTime) {
    if (frame == null || !frame.isVisible()) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Create a new dataset

        dataset.addValue(catAnalysisTime, "Time", "Category Analysis");
        dataset.addValue(backlinkAnalysisTime, "Time", "Backlink Analysis");
        dataset.addValue(brokenLinkAnalysisTime, "Time", "Broken Link Analysis");
        dataset.addValue(pageLoadTimeAnalysisTime, "Time", "Page Load Time Analysis");
        dataset.addValue(uiAnalysisTime, "Time", "UI Analysis");
        dataset.addValue(socialAnalysisTime, "Time", "Social Analysis");

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Analysis Times",
                "Analysis Type",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = lineChart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
        plot.setRenderer(renderer);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickUnit(new NumberTickUnit(300));

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        frame = new JFrame("Analysis Times Chart");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);

        graphDisplayed = true; // Set the flag to true after displaying the graph
    }
}

// ... existing code ...

    private void generateCombinedPDF(String[] urls) {
        for (String url : urls) {
            List<String> combinedContent = new ArrayList<>();
            String shortened = urlshortener(url);
            // Retrieve data for the specific URL from urlPageLoadTimes map
            Long loadTime = urlPageLoadTimes.get(url);
            // Shortened URL
            combinedContent.add("Category analysis for "+shortened+": "+categoryMap.get(url));
            if (loadTime != null) {
                combinedContent.add("Page Load Time for "+shortened+": "+loadTime+" ms");
            }
    
            // Retrieve data for the specific URL from urlScores map
            Map<String, Double> scores = urlScores.get(url);
            if (scores != null) {
                combinedContent.add("UI Analysis for "+shortened+": ");
                // Add individual scores to the combined content
                for (Map.Entry<String, Double> entry : scores.entrySet()) {
                    combinedContent.add(entry.getKey() + " Score: " + entry.getValue());
                }
            }
    
            // Retrieve data for the specific URL from socialLinksgraph map
            Integer socialLinksCount = socialLinksgraph.get(url);
            if (socialLinksCount != null) {
                combinedContent.add("Social Media Count Analysis for URL: "+socialLinksCount);
            }
            String fileName = generateUniqueFilename(url);
            if (!Files.exists(Paths.get(fileName))) {
                generatePDF(fileName, combinedContent);
            }
        }
        SpeedTest();
    }
    private static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        return stringBuilder.toString();
    }
    private void SpeedTest(){
        String randomString = generateRandomString(10);
        String filename = "FinalReport_"+randomString+".pdf";
        List<String> combinedContent = new ArrayList<>();
        combinedContent.add("Find Category Analysis: "+CatAnalysisTime+" ms");
        combinedContent.add("Backlink Analysis: "+BacklinkAnalysisTime+" ms");
        combinedContent.add("Broken Link Analysis: "+BrokenLinkAnalysisTime+" ms");
        combinedContent.add("Page Load Time Analysis: "+PageLoadTimeAnalysisTime+" ms");
        combinedContent.add("UI Analysis Analysis: "+UIAnalysisTime+" ms");
        combinedContent.add("Social Media Analysis: "+SocialAnalysisTime+" ms");

        if (!Files.exists(Paths.get(filename))) {
            generatePDF(filename, combinedContent);
        }
    }

    private String urlshortener(String url){
        String regex = "^(https?://)?(www\\.)?([^/.]+).*";
        String extractedPart = url.replaceAll(regex, "$3");
        // Replace any non-alphanumeric characters with underscores
        return extractedPart.replaceAll("[^a-zA-Z0-9\\.]", "_")+".com ";
    }
    private void PageLoadGraph(DefaultCategoryDataset dataset) {
        if (frame == null) {
            JFrame pageLoadFrame = new JFrame("Page Load Times Chart"); // Create a new frame for the chart
            JFreeChart lineChart = ChartFactory.createLineChart(
                    "Page Load Times",
                    "URL",
                    "Load Time (ms)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false);
            CategoryPlot plot = lineChart.getCategoryPlot();
            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
            plot.setRenderer(renderer);
            // Set the range axis to have 300ms intervals
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setTickUnit(new NumberTickUnit(300));
            ChartPanel chartPanel = new ChartPanel(lineChart);  // Class-level variable
            chartPanel.setPreferredSize(new Dimension(800, 600));
    
            // Create a new frame for the chart
            frame = new JFrame("Page Load Times Chart");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(chartPanel);
            frame.pack();
        }
    
        // Make the frame visible
        frame.setVisible(true);
    }
    private void generateSocialLinksGraph(Map<String, Integer> socialLinksCount) {
        boolean existingSocialLinksFrameVisible = urlFrames.containsKey("socialLinks") && urlFrames.get("socialLinks").isVisible();

        if (existingSocialLinksFrameVisible) {
            urlFrames.get("socialLinks").toFront();
        } else {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (Map.Entry<String, Integer> entry : socialLinksCount.entrySet()) {
                dataset.addValue(entry.getValue(), "Social Links Count", urlshortener(entry.getKey()));
            }

            JFreeChart lineChart = ChartFactory.createLineChart(
                    "Social Links Count Analysis",
                    "URLs",
                    "Social Links Count",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            CategoryPlot plot = lineChart.getCategoryPlot();
            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
            plot.setRenderer(renderer);

            ChartPanel chartPanel = new ChartPanel(lineChart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

            JFrame frame = new JFrame("Social Links Count Analysis");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(chartPanel);

            frame.pack();
            frame.setVisible(true);

            urlFrames.put("socialLinks", frame);
        }
    }

    Map<String, Map<String, Double>> urlScores = new HashMap<>();

    public void displayGraphs(Map<String, Map<String, Double>> urlScores) {
        for (Map.Entry<String, Map<String, Double>> entry : urlScores.entrySet()) {
            String url = urlshortener(entry.getKey());
            Map<String, Double> scores = entry.getValue();

            // Generate a graph based on the scores for the current URL
            generateGraph(url, scores);
        }
    }

    private Map<String, JFrame> urlFrames = new HashMap<>();

    // Updated generateGraphFromAnalysis method
    public void generateGraph(String url, Map<String, Double> scores) {
        // Check if a frame for the URL already exists
        JFrame existingFrame = urlFrames.get(url);
        if (existingFrame != null && existingFrame.isVisible()) {
            // If a frame for the URL already exists and is visible, bring it to the front
            existingFrame.toFront();
        } else {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (Map.Entry<String, Double> entry : scores.entrySet()) {
                dataset.addValue(entry.getValue(), "Scores", entry.getKey());
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Scores for URL: " + url,
                    "Category",
                    "Score",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );

            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));

            JFrame frame = new JFrame("Graph for URL: " + url);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(chartPanel);

            frame.pack();
            frame.setVisible(true);

            // Store the reference to the frame for the URL
            urlFrames.put(url, frame);
        }
    }

    private JButton createGlassyButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    int w = getWidth();
                    int h = getHeight();
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (getModel().isRollover()) {
                        // If the button is being hovered over, adjust appearance
                        g2.setColor(new Color(0, 0, 0, 50));
                        g2.fillRoundRect(5, 5, w - 10, h - 10, 3, 3);

                        GradientPaint gradientPaint = new GradientPaint(0, 0, new Color(0, 255, 0, 220), 0, h, new Color(0, 255, 0, 180));
                        g2.setPaint(gradientPaint);
                        g2.fillRoundRect(0, 0, w, h, 5, 5);

                        g2.setColor(new Color(0, 0, 0, 150));
                        g2.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
                    } else {
                        // Normal appearance
                        g2.setColor(new Color(0, 0, 0, 50));
                        g2.fillRoundRect(5, 5, w - 10, h - 10, 3, 3);

                        GradientPaint gradientPaint = new GradientPaint(0, 0, new Color(0, 0, 0, 220), 0, h, new Color(0, 0, 0, 220));
                        g2.setPaint(gradientPaint);
                        g2.fillRoundRect(0, 0, w, h, 3, 3);

                        g2.setColor(new Color(255, 255, 255, 150));
                        g2.drawRoundRect(0, 0, w - 1, h - 1, 3, 3);
                    }

                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        // Add mouse listener for hover functionality
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.getModel().setRollover(true);
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.getModel().setRollover(false);
                button.repaint();
            }
        });

        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(Color.CYAN); // Set text color to cyan
        button.setPreferredSize(new Dimension(150, 40));
        button.addActionListener(this);

        return button;
    }

    private class GradientPanel extends JPanel {
        private Color startColor;
        private Color endColor;

        public GradientPanel(Color startColor, Color endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int width = getWidth();
            int height = getHeight();
            GradientPaint gradientPaint = new GradientPaint(0, 0, startColor, 0, height, endColor);
            g2d.setPaint(gradientPaint);
            g2d.fillRect(0, 0, width, height);
            g2d.dispose();
        }
    }
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField("Enter URLs (comma or space separated):", 40);

        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#61DAFB"), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        textField.setBackground(Color.decode("#000000"));
        textField.setForeground(Color.decode("#61DAFB")); // Set text color
        textField.setFont(new Font("HP Simplified", Font.PLAIN, 14));
        textField.setCaretColor(Color.decode("#61DAFB"));
        textField.setOpaque(true);

        // Add focus listener to manage the placeholder text
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textField.getText().equals("Enter URLs (comma or space separated):")) {
                    textField.setText("");
                    textField.setForeground(Color.decode("#61DAFB")); // Change text color when focused
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setText("Enter URLs (comma or space separated):");
                    textField.setForeground(Color.decode("#61DAFB")); // Restore placeholder text color
                }
            }
        });

        return textField;
    }

    private JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea(34, 90);
        textArea.setEditable(false);
        textArea.setBackground(Color.decode("#000000"));
        textArea.setForeground(Color.decode("#61DAFB"));
        textArea.setFont(new Font("Calibri", Font.BOLD, 14));
        textArea.setCaretColor(Color.decode("#61DAFB"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }
    class TrieNode {
        TrieNode[] children;
        boolean isEndOfWord;

        public TrieNode() {
            children = new TrieNode[26]; // Assuming only lowercase English letters
            isEndOfWord = false;
        }
    }

    class Trie {
        private TrieNode root;

        public Trie() {
            root = new TrieNode();
        }

        public void insert(String word) {
            TrieNode node = root;
            for (char c : word.toLowerCase().toCharArray()) {
                if (c < 'a' || c > 'z') {
                    continue; // Skip non-alphabetic characters
                }
                int index = c - 'a';
                if (node.children[index] == null) {
                    node.children[index] = new TrieNode();
                }
                node = node.children[index];
            }
            node.isEndOfWord = true;
        }



        public boolean search(String word) {
            TrieNode node = root;
            for (char c : word.toLowerCase().toCharArray()) {
                if (c < 'a' || c > 'z') {
                    continue; // Skip non-alphabetic characters
                }
                int index = c - 'a';
                if (node.children[index] == null) {
                    return false;
                }
                node = node.children[index];
            }
            return node.isEndOfWord;
        }
    }
    private void initializeCategoryTries(){
        categoryTries = new HashMap<String, Trie>();
        String[] EcomKeywords={"ecommerce", "shop", "store", "buy", "purchase", "products", "online shopping", "shopping cart", "checkout", "sales", "discounts", "deals", "merchandise", "retail", "marketplace"};
        String[] EducationKeywords={"education", "learning", "courses", "study", "knowledge", "school", "university", "online learning", "academics", "tutorial", "study material", "educational resources", "distance learning", "e-learning", "training","programming", "coding","developer","interview","conferences","publications"};
        String[] NewsMediaKeywords={"news", "media", "headlines", "journalism", "breaking news", "current events", "newspapers", "articles", "press", "reporting","world news", "local news", "news portal", "news website"};
        String[] TechnologyKeywords = {"gadgets", "devices", "electronics", "innovation", "digital", "computer", "hardware", "internet", "IT"};
        String[] HealthKeywords = {"health", "wellness", "fitness", "nutrition", "lifestyle", "wellbeing", "exercise", "diet", "healthy living", "medical", "healthcare", "wellness tips", "mental health", "self-care", "yoga"};
        String[] TravelKeywords={"travel", "tourism", "destination", "vacation", "holiday", "trip", "travel guide", "travel tips", "adventure", "tourism industry", "travel agency", "sightseeing", "hotel", "accommodations","flights","trains","ticketing"};
        String[] EntertainmentKeywords = {"entertainment", "movies", "music", "TV shows", "celebrity", "gossip", "film", "cinema", "entertainment news", "Hollywood", "celebrities", "entertainment industry", "movie reviews", "movie trailers"};
        String[] BusinessFinanceKeywords = {"business", "finance", "entrepreneurship", "startups", "investing", "economy", "business news", "financial markets", "stocks", "trading", "banking", "investment", "business strategies", "economic trends"};
        String[] FoodKeywords = {"food", "cooking", "recipes", "cuisine", "culinary", "gastronomy", "food blog", "cooking tips", "recipe ideas", "food culture", "foodie", "culinary arts", "gourmet", "restaurant", "food reviews"};
        String[] SportsKeywords = {"sports", "recreation", "athletics", "fitness", "games", "outdoor", "adventure", "sports news", "sports events", "sports equipment", "sports gear", "fitness tips", "exercise", "sports leagues", "sports clubs","fixtures","points"};
        String[] SocialMediaKeywords = {"social media", "networking", "social networking", "social platform", "social network", "social interaction", "friends", "followers", "posts", "sharing", "likes", "comments", "messages", "profiles", "status updates", "photos", "videos", "community", "engagement", "viral"};
        String[] OfficeKeywords = {"productivity tools", "document", "office software", "collaboration tools", "business solutions", "cloud storage", "office applications", "spreadsheet", "presentation", "word processing", "document sharing", "project management", "workflow automation", "data analysis", "office productivity"};
        String[] PublicationKeywords = {"academic papers", "journals", "publications", "research papers", "scientific articles", "academic journals", "scholarly publications", "research publications"};
        String[] FashionKeywords = {"fashion", "style", "clothing", "apparel", "outfits", "designer", "boutique", "trends", "wardrobe", "couture", "fashionista", "runway", "accessories", "fashion week", "modeling", "fashion blog", "stylish", "dress", "shoes", "bags"};

        // Creation of Tries to store them in category Tries
        Trie EcomTrie = new Trie();
        Trie EduTrie = new Trie();
        Trie NewsTrie = new Trie();
        Trie TechTrie = new Trie();
        Trie HealthTrie = new Trie();
        Trie TravelTrie = new Trie();
        Trie EntertainTrie = new Trie();
        Trie BusinessTrie = new Trie();
        Trie FoodTrie = new Trie();
        Trie SportsTrie = new Trie();
        Trie SocialTrie = new Trie();
        Trie OfficeTrie = new Trie();
        Trie FashionTrie = new Trie();
        Trie PaperTrie = new Trie();

        for(String keyword:EcomKeywords){
            EcomTrie.insert(keyword);
        }for(String keyword:EducationKeywords){
            EduTrie.insert(keyword);
        }for(String keyword:NewsMediaKeywords){
            NewsTrie.insert(keyword);
        }for(String keyword:TechnologyKeywords){
            TechTrie.insert(keyword);
        }for(String keyword:HealthKeywords){
            HealthTrie.insert(keyword);
        }for(String keyword:TravelKeywords){
            TravelTrie.insert(keyword);
        }for(String keyword:EntertainmentKeywords){
            EntertainTrie.insert(keyword);
        }for(String keyword:BusinessFinanceKeywords){
            BusinessTrie.insert(keyword);
        }for(String keyword:FoodKeywords){
            FoodTrie.insert(keyword);
        }for(String keyword:SportsKeywords){
            SportsTrie.insert(keyword);
        }for(String keyword:SocialMediaKeywords){
            SocialTrie.insert(keyword);
        }for(String keyword:OfficeKeywords){
            OfficeTrie.insert(keyword);
        }for(String keyword:FashionKeywords){
            FashionTrie.insert(keyword);
        }for(String keyword:PublicationKeywords){
            PaperTrie.insert(keyword);
        }
        categoryTries.put("E_Commerce",EcomTrie);
        categoryTries.put("Education",EduTrie);
        categoryTries.put("News and Media",NewsTrie);
        categoryTries.put("Technology",TechTrie);
        categoryTries.put("Health",HealthTrie);
        categoryTries.put("Travel",TravelTrie);
        categoryTries.put("Business and Finance",BusinessTrie);
        categoryTries.put("Food and Cooking",FoodTrie);
        categoryTries.put("Sports and Recreation",SportsTrie);
        categoryTries.put("Social Media",SocialTrie);
        categoryTries.put("Office Suite",OfficeTrie);
        categoryTries.put("Fashion",FashionTrie);
        categoryTries.put("Paper Publications",PaperTrie);
    }
    private void analyzeCategory(String[] urls) {
        resultTextArea.setText("");
        for (String url : urls) {
            try {
                // Fetch the entire webpage content including title, head, meta, and body using Jsoup
                Document doc = Jsoup.connect(url).get();
                String title = doc.title(); // Get the title of the webpage
                String head = doc.head().text(); // Get the text content of the head section
                String meta = doc.select("meta").text(); // Get the text content of all meta tags
                String body = doc.body().text(); // Get the text content of the body

                // Combine all content into a single string
                String content = title + " " + head + " " + meta + " " + body;

                // Split the content into words
                String[] words = content.split("\\W+");

                // Initialize map to store keyword count for each category
                Map<String, Integer> categoryKeywordCount = new HashMap<>();

                // Iterate through categories
                for (String category : categoryTries.keySet()) {
                    Trie trie = categoryTries.get(category);
                    int count = 0;
                    // Iterate through words
                    for (String word : words) {
                        // Search for word in Trie
                        if (trie.search(word.toLowerCase())) {
                            count++;
                        }
                    }
                    // Store keyword count for current category
                    categoryKeywordCount.put(category, count);
                }

                // Determine the category with the highest keyword count
                String bestCategory = Collections.max(categoryKeywordCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                int maxCount = categoryKeywordCount.get(bestCategory);

                // Output analysis report to text area
                resultTextArea.append("Category Analysis Report for URL '" + url + "':\n\n");
                for (Map.Entry<String, Integer> entry : categoryKeywordCount.entrySet()) {
                    resultTextArea.append("Category: " + entry.getKey() + " - Keyword Count: " + entry.getValue() + "\n");
                }
                resultTextArea.append("Most relevant category: " + bestCategory + " with " + maxCount + " keywords found.\n\n");
                categoryMap.put(url,bestCategory);
            } catch (IOException e) {
                // Handle error fetching content
                resultTextArea.append("Error fetching content from URL: " + url + "\n\n");
                e.printStackTrace();
            }

        }
    }



    public void actionPerformed(ActionEvent e) {
        String[] urls = urlField.getText().split("[,\\s]+");
        if (e.getSource() == findcat) {
            if (urls.length > 0) {
                startTask("Find Category", urls, urls.length);
            } else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        } else if (e.getSource() == Backlinks) {
            if (urls.length > 0) {
                startTask("Find Backlinks", urls, urls.length);
            } else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        } else if (e.getSource() == PageTime) {
            if (urls.length > 0) {
                urlPageLoadTimes.clear();
                startTask("Page Load Time", urls, urls.length);
            } else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        } else if (e.getSource() == uiAnalyser) {
            if (urls.length > 0) {
                startTask("UI Analysis", urls, urls.length);
            } else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        } else if(e.getSource()== checkBrokenLinksButton){
            if(urls.length>0){
                startTask("Broken Links",urls,urls.length);
            }
            else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        } else if(e.getSource()==socialAnalysis){
            if(urls.length>0){
                startTask("Social Analysis",urls,urls.length);
            }
            else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        }
        else if(e.getSource()==GeneratePDF){
            if(urls.length>0){
                startTask("GeneratePDF",urls,urls.length);
            }
            else {
                resultTextArea.setText("Please enter at least one URL!");
            }
        }
    }

    private void startTask(String taskName, String[] urls, int totalTasks) {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < totalTasks; i++) {
                    // Simulate task processing for each URL
                    try {
                        // Simulate time-consuming task
                        Thread.sleep(500); // Adjust this to simulate real task duration
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int progress = (i + 1) * 100 / totalTasks; // Calculate progress percentage
                    publish(progress);
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                // Update the progress bar with the last value in chunks
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
                progressBar.setString(progress + "%"); // Display percentage text
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                progressBar.setString("Task completed: " + taskName);
                String category;
                // Display the output after the progress bar reaches 100%
                switch (taskName) {
                    case "Find Category" -> {
                        start = System.currentTimeMillis();
                        analyzeCategory(urls);
                        end = System.currentTimeMillis();
                        CatAnalysisTime=end-start;
                    }
                    case "Find Backlinks" -> {
                        start = System.currentTimeMillis();
                        BacklinksPrinter(urls);
                        end = System.currentTimeMillis();
                        BacklinkAnalysisTime=end-start;
                    }
                    case "Broken Links"-> {
                        start = System.currentTimeMillis();
                        checkBrokenLinks(urls);
                        end = System.currentTimeMillis();
                        BrokenLinkAnalysisTime=end-start;
                    }
                    case "Page Load Time" -> {
                        start = System.currentTimeMillis();
                        PageLoadPrinter(urls);
                        end = System.currentTimeMillis();
                        PageLoadTimeAnalysisTime=end-start;
                    }
                    case "UI Analysis" -> {
                        try {
                            start = System.currentTimeMillis();
                            uiAnalysis(urls);
                            displayGraphs(urlScores);
                            end = System.currentTimeMillis();
                            UIAnalysisTime=end-start;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "Social Analysis" -> {
                        start = System.currentTimeMillis();
                        socialAnalyzer(urls);
                        generateSocialLinksGraph(socialLinksgraph);
                        end = System.currentTimeMillis();
                        SocialAnalysisTime=end-start;
                    }
                    case "GeneratePDF" -> {
                        generateCombinedPDF(urls);
                        AnalysisTimeGraph(CatAnalysisTime, BacklinkAnalysisTime, BrokenLinkAnalysisTime, PageLoadTimeAnalysisTime, UIAnalysisTime, SocialAnalysisTime);
                    }
                }
            }
        };

        // Reset progress bar to 0% and set text to "0%"
        progressBar.setValue(0);
        progressBar.setString("0%");

        // Set progress bar color to green
        progressBar.setForeground(Color.GREEN);
        progressBar.setFont(new Font("Arial", Font.BOLD, 20));
        progressBar.setStringPainted(true); // Show percentage text
        // Execute the SwingWorker
        worker.execute();
    }
    private void PageLoadPrinter(String urls[]){
        resultTextArea.setText("");
        for(String url:urls){
            loadTime = calculateLoadTime(url);
            urlPageLoadTimes.put(url, loadTime);
            resultTextArea.append("URL: "+url+"\n"+"Page Load Time: "+loadTime+"ms"+"\n");
        }
        dataset.clear();
        for (Map.Entry<String, Long> entry : urlPageLoadTimes.entrySet()) {
            dataset.addValue(entry.getValue(), "Load Time", entry.getKey());
        }
        PageLoadGraph(dataset);
    }
    private void BacklinksPrinter(String urls[]){
        resultTextArea.setText("");
        for(String url:urls){
            Set<String> backlinks = fetchBacklinks(url);
            enqueue(new WebInfo(url,backlinks));
        }
        while (!isEmpty()) {
            WebInfo websiteInfo = dequeue();
            resultTextArea.append("URL: "+websiteInfo.getUrl()+"\n"+"Backlinks: \n");
            displayBacklinks(websiteInfo);
            resultTextArea.append("\n\n");
        }
    }

    public void uiAnalysis(String[] urls) throws IOException {
        this.resultTextArea.setText("");

        for (String url : urls) {
            Document document = Jsoup.connect(url).get();
            String content = document.body().text();
            long loadTime = calculateLoadTime(url);
            double pageSpeedScore = this.calculatePageSpeedScore(loadTime);
            double navigationScore = this.calculateNavigationScore(url);
            double accessibilityScore = this.calculateAccessibilityScore(url);

            // Store the scores for the current URL
            Map<String, Double> scoresMap = new HashMap<>();
            scoresMap.put("PageSpeedScore", pageSpeedScore);
            scoresMap.put("NavigationScore", navigationScore*100);
            scoresMap.put("AccessibilityScore", accessibilityScore*100);

            // Store the scores map for the current URL in the outer map
            urlScores.put(url, scoresMap);

            this.displayResult(url, pageSpeedScore, navigationScore, accessibilityScore);
        }

        // Now you have a map of all URLs with their corresponding scores
        // You can access the scores for a specific URL like this: urlScores.get("your_url_here")
    }

    private long calculateLoadTime(String url) {
        long startTime = System.currentTimeMillis();
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();

            // Optional: Read data to ensure full connection establishment
            try (InputStream inputStream = connection.getInputStream()) {
                while (inputStream.read() != -1) {
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }


    private double calculatePageSpeedScore(long loadTime) {
        if(loadTime<=1000L){
            return 100.0;
        }
        else if(loadTime<=2500L){
            return 75.0;
        }
        else if(loadTime<=3000L){
            return 50.0;
        }
        else if(loadTime<=5000L){
            return 35.0;
        }
        return 0.0;
    }
    // Method to calculate mobile friendliness score
    private int extractFontSize(String content) {
        int startIndex = content.indexOf("font-size:") + 10;
        int endIndex = content.indexOf(";", startIndex);
        String fontSizeValue = content.substring(startIndex, endIndex).trim();
        return Integer.parseInt(fontSizeValue.replaceAll("[^0-9]", ""));
    }
    private double extractLineHeight(String content) {
        int startIndex = content.indexOf("line-height:") + 12;
        int endIndex = content.indexOf(";", startIndex);
        String lineHeightValue = content.substring(startIndex, endIndex).trim();
        return Double.parseDouble(lineHeightValue.replaceAll("[^0-9.]", ""));
    }
    // Debug: print the criteria checks



    private void displayResult(String url, double pageSpeedScore,double navigationScore,double accessibilityScore) {
        resultTextArea.append("URL: " + url + "\n");
        resultTextArea.append("Page Speed Score: " + (pageSpeedScore  + "%\n"));
        resultTextArea.append("Navigation Score: " + (navigationScore * 100) + "%\n");
        resultTextArea.append("Accessibility Score: " + (accessibilityScore * 100) + "%\n");
        resultTextArea.append("\n");
    }
    private String generateUniqueFilename(String url) {
        // Extract the part of the URL after "www." up to ".com"
        String regex = "^(https?://)?(www\\.)?([^/.]+).*";
        String extractedPart = url.replaceAll(regex, "$3");

        // Replace any non-alphanumeric characters with underscores
        String filename = extractedPart.replaceAll("[^a-zA-Z0-9\\.]", "_")+ ".pdf";
        return filename;
    }


    public static void generatePDF(String fileName, List<String> lines) {
        try(PDDocument document = new PDDocument()) {
            // Create a new page for appending content
            PDPage newPage = new PDPage();
            document.addPage(newPage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, newPage)) {
                // Set font to Helvetica
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 20);

                float y = 700; // Initial Y-coordinate
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(20, y);

                    // Split the line into segments if it contains newlines
                    String[] segments = line.split("\n");
                    for (String segment : segments) {
                        contentStream.showText(segment);
                        contentStream.newLineAtOffset(0, -25); // Move down for the next segment
                    }

                    contentStream.endText();
                    y -= 50; // Adjust line spacing as needed
                }
            }

            // Save the modified PDF with the original filename
            document.save(fileName);
            System.out.println("PDF content added successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private double calculateNavigationScore(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Check for main navigation menu
            Elements navElements = doc.select("nav");
            boolean hasMainNav = !navElements.isEmpty();

            // Check for breadcrumbs
            Elements breadcrumbElements = doc.select("nav[aria-label=breadcrumb]");
            boolean hasBreadcrumbs = !breadcrumbElements.isEmpty();

            // Check for search bar
            Elements searchElements = doc.select("input[type=search]");
            boolean hasSearchBar = !searchElements.isEmpty();

            // Check for internal links
            Elements internalLinks = doc.select("a[href^=" + url + "]");
            boolean hasInternalLinks = internalLinks.size() > 0;

            // Check for footer navigation
            Elements footerNavElements = doc.select("footer nav");
            boolean hasFooterNav = !footerNavElements.isEmpty();

            // Check for skip links
            Elements skipLinkElements = doc.select("a[href^='#']");
            boolean hasSkipLinks = skipLinkElements.stream().anyMatch(el -> el.text().toLowerCase().contains("skip"));

            // Check for sitemap
            Elements sitemapElements = doc.select("a[href*='sitemap']");
            boolean hasSitemap = !sitemapElements.isEmpty();

            // Check the number of navigation items
            int navItemCount = navElements.select("a").size();
            boolean reasonableNavItemCount = navItemCount >= 3 && navItemCount <= 10;

            // Scoring logic
            double score = 100.0;

            if (!hasMainNav) score -= 20;
            if (!hasBreadcrumbs) score -=10.0;
            if (!hasSearchBar) score -= 10.0;
            if (!hasInternalLinks) score -=10.0;
            if (!hasFooterNav) score -= 10.0;
            if (!hasSkipLinks) score -= 10.0;
            if (!hasSitemap) score -= 10.0;
            if (!reasonableNavItemCount) score -= 20.0;

            return (score/100);
        } catch (IOException e) {
            e.printStackTrace();
            return 0.0; // Return 0.0 in case of any errors
        }
    }


    private double calculateAccessibilityScore(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Initialize a map to keep track of different accessibility issues
            Map<String, Boolean> accessibilityIssues = new HashMap<>();

            // Check for images with alt attributes
            Elements images = doc.select("img");
            boolean allImagesHaveAlt = images.stream().allMatch(img -> img.hasAttr("alt") && !img.attr("alt").isEmpty());
            accessibilityIssues.put("Images have alt attributes", allImagesHaveAlt);

            // Check for ARIA roles and landmarks
            Elements ariaRoles = doc.select("[role]");
            boolean hasAriaRoles = !ariaRoles.isEmpty();
            accessibilityIssues.put("ARIA roles present", hasAriaRoles);

            // Check for semantic HTML elements
            Elements semanticElements = doc.select("header, main, footer, article, section, nav, aside");
            boolean hasSemanticElements = !semanticElements.isEmpty();
            accessibilityIssues.put("Semantic HTML elements present", hasSemanticElements);

            // Check for form labels
            Elements formElements = doc.select("input, select, textarea");
            boolean allFormElementsHaveLabels = formElements.stream().allMatch(el -> {
                String id = el.id();
                if (!id.isEmpty()) {
                    return !doc.select("label[for=" + id + "]").isEmpty();
                } else {
                    return el.parent().tagName().equals("label");
                }
            });
            accessibilityIssues.put("Form elements have labels", allFormElementsHaveLabels);

            // Check for contrast ratio (simplified check for presence of inline styles)
            Elements textElements = doc.select("p, span, div, a, li, h1, h2, h3, h4, h5, h6");
            boolean hasSufficientContrast = textElements.stream().allMatch(el -> {
                String style = el.attr("style");
                return !(style.contains("color") && style.contains("background-color"));
            });
            accessibilityIssues.put("Sufficient contrast ratio", hasSufficientContrast);

            // Check for keyboard navigability (presence of tabindex attributes)
            Elements interactiveElements = doc.select("a, button, input, select, textarea, area, object");
            boolean keyboardNavigable = interactiveElements.stream().allMatch(el -> el.hasAttr("tabindex") || el.tagName().equals("a"));
            accessibilityIssues.put("Keyboard navigable", keyboardNavigable);

            // Check for lang attribute
            boolean hasLangAttribute = doc.hasAttr("lang");
            accessibilityIssues.put("Lang attribute present", hasLangAttribute);

            // Scoring logic
            double totalChecks = accessibilityIssues.size();
            long passingChecks = accessibilityIssues.values().stream().filter(value -> value).count();
            double score = (passingChecks / totalChecks) ;

            return score;

        } catch (IOException e) {
            e.printStackTrace();
            return 0.0; // Return 0.0 in case of any errors
        }
    }

    private synchronized void enqueue(WebInfo websiteInfo) {
        WebsiteInfoNode newNode = new WebsiteInfoNode(websiteInfo);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            if(newNode.info.getLoadTime()<=head.info.getLoadTime()){
                newNode.next=head;
                head=newNode;
            }
            if(newNode.info.getLoadTime()>head.info.getLoadTime()){
                WebsiteInfoNode prev = head;
                WebsiteInfoNode current = head.next;
                while (current != null && newNode.info.getLoadTime() > current.info.getLoadTime()) {
                    prev = current;
                    current = current.next;
                }
                // The case of adding the slowest loading URL
                if (current == null) {
                    tail.next = newNode;
                    tail = newNode;
                }
                // The case of adding the URL between the other two slower and faster URL
                else{
                    newNode.next=current;
                    prev.next=newNode;
                }

            }
        }
    }
    private synchronized void socialEnqueue(WebInfo websiteInfo) {
        WebsiteInfoNode newNode = new WebsiteInfoNode(websiteInfo);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            if (newNode.info.getScount() > head.info.getScount()) {
                newNode.next = head;
                head = newNode;
            } else {
                WebsiteInfoNode prev = null;
                WebsiteInfoNode current = head;
                while (current != null && newNode.info.getScount() <= current.info.getScount()) {
                    prev = current;
                    current = current.next;
                }
                newNode.next = current;
                if (prev != null) {
                    prev.next = newNode;
                }
                if (current == null) {
                    tail = newNode;
                }
            }
        }
    }
    private synchronized WebInfo dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        WebsiteInfoNode temp = head;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        temp.next = null;
        return temp.info;
    }

    private boolean isEmpty() {
        return head == null;
    }

    // Nested class representing a node in the linked list
    private class WebsiteInfoNode {
        WebInfo info;
        WebsiteInfoNode next;

        WebsiteInfoNode(WebInfo info) {
            this.info = info;
            this.next = null;
        }
    }

    private Set<String> fetchBacklinks(String url) {
        Set<String> backlinks = new HashSet<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements backlinkElements = doc.select("a[href^=http]");

            // Get the domain of the input URL to filter out same-domain links
            String inputDomain = new URL(url).getHost();

            for (Element backlinkElement : backlinkElements) {
                String backlink = backlinkElement.attr("abs:href");
                String backlinkDomain = new URL(backlink).getHost();

                // Only add the link if it's from a different domain
                if (!backlinkDomain.equals(inputDomain)) {
                    backlinks.add(backlink);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return backlinks;
    }

    class UrlAndBrokenLinks {
        private String url;
        private Set<String> brokenLinks;

        public UrlAndBrokenLinks(String url, Set<String> brokenLinks) {
            this.url = url;
            this.brokenLinks = brokenLinks;
        }

        public String getUrl() {
            return url;
        }

        public Set<String> getBrokenLinks() {
            return brokenLinks;
        }
    }
    private void checkBrokenLinks(String[] urls) {
        resultTextArea.setText(""); // Clear previous content
        SwingWorker<Void, UrlAndBrokenLinks> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (String url : urls) {
                    Set<String> brokenLinks = findBrokenLinks(url);
                    publish(new UrlAndBrokenLinks(url, brokenLinks));
                }
                return null;
            }

            @Override
            protected void process(List<UrlAndBrokenLinks> chunks) {
                resultTextArea.append("Displaying Broken Links......"+"\n");
                for (UrlAndBrokenLinks urlAndBrokenLinks : chunks) {
                    String url = urlAndBrokenLinks.getUrl();
                    Set<String> brokenLinks = urlAndBrokenLinks.getBrokenLinks();
                    if (!brokenLinks.isEmpty()) {
                        for (String brokenLink : brokenLinks) {
                            // Append main URL and broken link
                            resultTextArea.append("Main URL: " + url + "\n");
                            resultTextArea.append("Broken link: " + brokenLink + "\n");
                            resultTextArea.append("\n");
                        }
                    } else {
                        resultTextArea.append("No broken links found for main URL: " + url + "\n");
                    }
                }
            }
        };
        worker.execute();
    }

    // Method to find broken links for a given URL
    private Set<String> findBrokenLinks(String url) {
        Set<String> brokenLinks = new HashSet<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("abs:href");
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(href).openConnection();
                    connection.setRequestMethod("HEAD");
                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 400) {
                        brokenLinks.add(href);
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    // Handle URL connection error
                    e.printStackTrace();
                    // You can log or display an error message if needed
                }
            }
        } catch (HttpStatusException e) {
            // Handle HTTP status exception (e.g., 400 Bad Request)
            e.printStackTrace();
            // You can display a meaningful message to the user
            System.err.println("Error fetching URL: " + url + ". HTTP status code: " + e.getStatusCode());
        } catch (IOException e) {
            // Handle general IO exception
            e.printStackTrace();
            // You can display an error message to the user
            System.err.println("Error fetching URL: " + url);
        }
        return brokenLinks;
    }
    private void socialAnalyzer(String[] urls) {
        resultTextArea.setText("");
        for (String url : urls) {
            try {
                Document doc = Jsoup.connect(url).get();
                int socialLinksCount = countSocialLinks(url);
                WebInfo y = new WebInfo(url,socialLinksCount);
                socialLinksgraph.put(url, socialLinksCount);
                socialEnqueue(y);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (!isEmpty()) {
            WebInfo websiteInfo = dequeue();
            resultTextArea.append("URL: " + websiteInfo.getUrl() + "\n");
            resultTextArea.append("Social Links Count: " + websiteInfo.getScount() + "\n\n");
        }
    }


    private int countSocialLinks(String url) throws IOException {
        int count = 0;
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("abs:href");
            if (isSocialLink(href)) {
                count++;
            }
        }
        return count;
    }

    private boolean isSocialLink(String link) {
        return link.contains("facebook") || link.contains("twitter") || link.contains("instagram") ||
                link.contains("linkedin") || link.contains("youtube") || link.contains("pinterest");
    }
    private void displayBacklinks(WebInfo websiteInfo){
        for (String backlink : websiteInfo.getBacklinks()) {
            resultTextArea.append("- " + backlink + "\n");
        }
        resultTextArea.append("\n");
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WebAuditer();
        });
    }
}
// Collecting Info about the website like URL, category, loadTime
class WebInfo {
    private String url;
    private String category;
    private long loadTime;
    private Set<String> backlinks;
    private int scount;
    public WebInfo(String url,long loadTime){
        this.url=url;
        this.loadTime=loadTime;
    }
    public WebInfo(String url, Set<String> backlinks){
        this.url=url;
        this.backlinks=backlinks;
    }
    public WebInfo(String url, String category, long loadTime, Set<String> backlinks) {
        this.url = url;
        this.category = category;
        this.loadTime = loadTime;
        this.backlinks = backlinks;
    }
    public WebInfo(String url, String category, long loadTime, Set<String> backlinks,int scount) {
        this.url = url;
        this.category = category;
        this.loadTime = loadTime;
        this.backlinks = backlinks;
        this.scount=scount;
    }
    public WebInfo(String url,int scount){
        this.url=url;
        this.scount=scount;
    }
    public WebInfo(String url, Set<String> backlinks, int socialLinksCount) {
        this.url = url;
        this.backlinks = backlinks;
    }

    public String getUrl() {
        return url;
    }

    public String getCategory() {
        return category;
    }


    public long getLoadTime() {
        return loadTime;
    }

    public Set<String> getBacklinks() {
        return backlinks;
    }
    public int getScount(){
        return scount;
    }
}
