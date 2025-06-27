package PdfFile;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class WebsiteCategoryAnalyzer extends JFrame implements ActionListener {
    private final JButton findcat,Backlinks,PageTime,uiAnalyser,checkBrokenLinksButton ;
    private JProgressBar progressBar;
    private JTextField urlField;
    //    private JButton analyzeButton;
    private JTextArea resultTextArea;
    private Map<String, Set<String>> categoryKeywords;
    private WebsiteInfoNode head;
    private WebsiteInfoNode tail;
    private static final int MAX_CONCURRENT_THREADS = 20;
    private final ExecutorService executorService;
    private static final double PAGE_SPEED_WEIGHT = 0.3;
    private static final double MOBILE_FRIENDLINESS_WEIGHT = 0.2;
    public WebsiteCategoryAnalyzer() {
        setTitle("SEO Auditing Tool");
        setSize(1200, 1200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        // Initialize category keywords
        initializeCategoryKeywords();
        // Load logo image
        // Load logo image using getResourceAsStream()

        // Create main panel with BorderLayout
//        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create panel for URL input with FlowLayout

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

//    // Custom JPanel with glassmorphism effect
//    class GlassPanel extends JPanel {
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            Graphics2D g2d = (Graphics2D) g;
//            int width = getWidth();
//            int height = getHeight();
//
//            // Draw gradient background
//            GradientPaint gp = new GradientPaint(0, 0, new Color(245, 3, 32), width, height, new Color(5, 62, 248));
//            g2d.setPaint(gp);
//            g2d.fillRect(0, 0, width, height);
//
//            // Apply semi-transparent overlay
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
//            g2d.setColor(new Color(255, 255, 255, 50));
//            g2d.fillRoundRect(20, 20, width - 40, height - 40, 30, 30);
//        }
//    }

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
        JTextArea textArea = new JTextArea(38, 90);
        textArea.setEditable(false);
        textArea.setBackground(Color.decode("#000000"));
        textArea.setForeground(Color.decode("#61DAFB"));
        textArea.setFont(new Font("Calibri", Font.BOLD, 14));
        textArea.setCaretColor(Color.decode("#61DAFB"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private void initializeCategoryKeywords() {
        categoryKeywords = new HashMap<>();
        // Add keywords for each category
        categoryKeywords.put("E-commerce", new HashSet<>(Arrays.asList("ecommerce", "shop", "store", "buy", "purchase", "products", "online shopping", "shopping cart", "checkout", "sales", "discounts", "deals", "merchandise", "retail", "marketplace")));
        categoryKeywords.put("Education", new HashSet<>(Arrays.asList("education", "learning", "courses", "study", "knowledge", "school", "university", "online learning", "academics", "tutorial", "study material", "educational resources", "distance learning", "e-learning", "training","programming", "coding","developer","interview","conferences","publications")));
        categoryKeywords.put("News and Media", new HashSet<>(Arrays.asList("news", "media", "headlines", "journalism", "breaking news", "current events", "newspapers", "articles", "press", "reporting","world news", "local news", "news portal", "news website")));
        categoryKeywords.put("Technology", new HashSet<>(Arrays.asList("gadgets", "devices", "electronics", "innovation", "digital", "computer", "hardware", "internet", "IT")));
        categoryKeywords.put("Health and Wellness", new HashSet<>(Arrays.asList("health", "wellness", "fitness", "nutrition", "lifestyle", "wellbeing", "exercise", "diet", "healthy living", "medical", "healthcare", "wellness tips", "mental health", "self-care", "yoga")));
        categoryKeywords.put("Travel and Tourism", new HashSet<>(Arrays.asList("travel", "tourism", "destination", "vacation", "holiday", "trip", "travel guide", "travel tips", "adventure", "tourism industry", "travel agency", "sightseeing", "hotel", "accommodations","flights","trains","ticketing")));
        categoryKeywords.put("Entertainment", new HashSet<>(Arrays.asList("entertainment", "movies", "music", "TV shows", "celebrity", "gossip", "film", "cinema", "entertainment news", "Hollywood", "celebrities", "entertainment industry", "movie reviews", "movie trailers")));
        categoryKeywords.put("Business and Finance", new HashSet<>(Arrays.asList("business", "finance", "entrepreneurship", "startups", "investing", "money", "economy", "business news", "financial markets", "stocks", "trading", "banking", "investment", "business strategies", "economic trends")));
        categoryKeywords.put("Food and Cooking", new HashSet<>(Arrays.asList("food", "cooking", "recipes", "cuisine", "culinary", "gastronomy", "food blog", "cooking tips", "recipe ideas", "food culture", "foodie", "culinary arts", "gourmet", "restaurant", "food reviews")));
        categoryKeywords.put("Sports and Recreation", new HashSet<>(Arrays.asList("sports", "recreation", "athletics", "fitness", "games", "outdoor", "adventure", "sports news", "sports events", "sports equipment", "sports gear", "fitness tips", "exercise", "sports leagues", "sports clubs","fixtures","points")));
        categoryKeywords.put("Social Media", new HashSet<>(Arrays.asList("social media", "networking", "social networking", "social platform", "social network", "social interaction", "friends", "followers", "posts", "sharing", "likes", "comments", "messages", "profiles", "status updates", "photos", "videos", "community", "engagement", "viral")));
        categoryKeywords.put("Office Suite", new HashSet<>(Arrays.asList("productivity tools", "document", "office software",
                "collaboration tools", "business solutions", "cloud storage", "office applications", "spreadsheet", "presentation",
                "word processing", "document sharing", "project management", "workflow automation", "data analysis", "office productivity")));
        categoryKeywords.put("Paper Publications", new HashSet<>(Arrays.asList("academic papers", "journals", "publications", "research papers", "scientific articles", "academic journals", "scholarly publications", "research publications")));
        categoryKeywords.put("Fashion", new HashSet<>(Arrays.asList("fashion", "style", "clothing", "apparel", "outfits", "designer", "boutique", "trends", "wardrobe", "couture", "fashionista", "runway", "accessories", "fashion week", "modeling", "fashion blog", "stylish", "dress", "shoes", "bags")));
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

                    // Increment the progress by 5%
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

                // Display the output after the progress bar reaches 100%
                switch (taskName) {
                    case "Find Category" -> FindCategory(urls);
                    case "Find Backlinks" -> BacklinksPrinter(urls);
                    case "Broken Links"-> checkBrokenLinks(urls);
                    case "Page Load Time" -> PageLoadPrinter(urls);
                    case "UI Analysis" -> uiAnalysis(urls);
                }
                // Add similar conditions for other tasks if needed
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

    private void FindCategory(String urls[]){
        resultTextArea.setText("");
        for(String url:urls){
            String category = getCategoryForURL(url);
            resultTextArea.append("URL: "+url+"- " + category + "\n");
        }
    }
    private void PageLoadPrinter(String urls[]){
        resultTextArea.setText("");
        for(String url:urls){
            long loadTime = calculateLoadTime(url);
            enqueue(new WebsiteInfo(url,loadTime));
        }
        while (!isEmpty()) {
            WebsiteInfo websiteInfo = dequeue();
            resultTextArea.append("URL: "+websiteInfo.getUrl()+"\n"+"Page Load Time: "+websiteInfo.getLoadTime()+" ms "+"\n\n");
        }
    }
    private void BacklinksPrinter(String urls[]){
        resultTextArea.setText("");
        for(String url:urls){
            Set<String> backlinks = fetchBacklinks(url);
            enqueue(new WebsiteInfo(url,backlinks));
        }
        while (!isEmpty()) {
            WebsiteInfo websiteInfo = dequeue();
            resultTextArea.append("URL: "+websiteInfo.getUrl()+"\n"+"Backlinks: \n");
            displayBacklinks(websiteInfo);
            resultTextArea.append("\n\n");
        }
    }

    private void uiAnalysis(String[] urls){
        resultTextArea.setText("");
        for(String url:urls){
            long loadTime = calculateLoadTime(url);
            double pageSpeedScore = calculatePageSpeedScore(loadTime);
            double mobileFriendlinessScore = calculateMobileFriendlinessScore(url);
            displayResult(url,pageSpeedScore, mobileFriendlinessScore);
        }
    }
    //    private void analyzeWebsites(String[] urls) {
//        for (String url : urls) {
//            long loadTime = calculateLoadTime(url);
//            String category = getCategoryForURL(url);
//            Set<String> backlinks = fetchBacklinks(url); // Fetch backlinks
//            enqueue(new WebsiteInfo(url, category, loadTime, backlinks));
//            double pageSpeedScore = calculatePageSpeedScore(loadTime);
//            double mobileFriendlinessScore = calculateMobileFriendlinessScore(url);
//            displayResult(url, loadTime, pageSpeedScore, mobileFriendlinessScore);
//        }
//        // Analyze the categories one by one and add them to the result
//        while (!isEmpty()) {
//            WebsiteInfo websiteInfo = dequeue();
//            displayResult(websiteInfo);
//            checkBrokenLinks(websiteInfo.getUrl());
//        }
//
//    }
    private synchronized void enqueue(WebsiteInfo websiteInfo) {
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

    private synchronized WebsiteInfo dequeue() {
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
        WebsiteInfo info;
        WebsiteInfoNode next;

        WebsiteInfoNode(WebsiteInfo info) {
            this.info = info;
            this.next = null;
        }
    }


    private long calculateLoadTime(String url) {
        long startTime = System.currentTimeMillis();
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect(); // Establish connection to measure time
            // Read data from the connection if necessary
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    private double calculatePageSpeedScore(long loadTime) {
        // Placeholder logic for calculating page speed score
        return 1 - (loadTime / 5000.0); // Placeholder logic: Assuming 5 seconds as maximum acceptable load time
    }

    private double calculateMobileFriendlinessScore(String url) {
        // Placeholder logic for calculating mobile friendliness score
        return 1.0; // Placeholder logic: Always return 1.0 for demonstration purposes
    }
    private String getCategoryForURL(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return getCategoryFromContent(doc);
        } catch (IOException e) {
            e.printStackTrace();
            // If the URL is malformed, throw MalformedURLException
            return "Other"; // Default category if unable to fetch category
        }

    }

    private String getCategoryFromContent(Document doc) {
        // Analyze the content of the website to infer website category
        Elements paragraphs = doc.select("p,h1,div,body,meta,navbar"); // Select all paragraphs
        Map<String, Integer> categoryCounts = new HashMap<>();
//        String k="";
        for (Element paragraph : paragraphs) {
            String text = paragraph.text().toLowerCase();
            for (Map.Entry<String, Set<String>> entry : categoryKeywords.entrySet()) {
                String category = entry.getKey();
                Set<String> keywords = entry.getValue();
                for (String keyword : keywords) {
                    if (text.contains(keyword)) {
//                        k=keyword;
                        // Increment count for the matched category
                        categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                    }
                }
            }
        }

        // Find the category with the highest count
        String mostFrequentCategory = "Other";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentCategory = entry.getKey();
            }
        }
        return mostFrequentCategory;
    }
    private Set<String> fetchBacklinks(String url) {
        Set<String> backlinks = new HashSet<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements backlinkElements = doc.select("a[href^=http]");
            for (Element backlinkElement : backlinkElements) {
                String backlink = backlinkElement.attr("abs:href");
                backlinks.add(backlink);
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

    // Method to check if a link is broken
    private boolean isBrokenLink(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode >= 400;
        } catch (IOException e) {
            e.printStackTrace();
            return true; // Treat as broken if unable to connect
        }
    }

    private void displayBacklinks(WebsiteInfo websiteInfo){
        for (String backlink : websiteInfo.getBacklinks()) {
            resultTextArea.append("- " + backlink + "\n");
        }
        resultTextArea.append("\n");
    }
    private void displayResult(String url,double pageSpeedScore,double mobileFriendlinessScore){
        resultTextArea.append("URL: " + url + "\n");
        resultTextArea.append("Page Speed Score: " + (pageSpeedScore * 100) + "%\n");
        resultTextArea.append("Mobile Friendliness Score: " + (mobileFriendlinessScore * 100) + "%\n");
        resultTextArea.append("\n");
    }
    private void displayResult(String url, long loadTime, double pageSpeedScore, double mobileFriendlinessScore) {
        resultTextArea.append("URL: " + url + "\n");
        resultTextArea.append("Load Time: " + loadTime + " milliseconds\n");
        resultTextArea.append("Page Speed Score: " + (pageSpeedScore * 100) + "%\n");
        resultTextArea.append("Mobile Friendliness Score: " + (mobileFriendlinessScore * 100) + "%\n");
        resultTextArea.append("\n");
    }

    public static void main(String[] args) {
        // Use the event dispatch thread to create the GUI
        SwingUtilities.invokeLater(() -> {
            new WebsiteCategoryAnalyzer();
        });
    }
}
// Collecting Info about the website like URL, category, loadTime
class WebsiteInfo {
    private String url;
    private String category;
    private long loadTime;
    private Set<String> backlinks;
    public WebsiteInfo(String url,long loadTime){
        this.url=url;
        this.loadTime=loadTime;
    }
    public WebsiteInfo(String url, Set<String> backlinks){
        this.url=url;
        this.backlinks=backlinks;
    }
    public WebsiteInfo(String url, String category, long loadTime, Set<String> backlinks) {
        this.url = url;
        this.category = category;
        this.loadTime = loadTime;
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
}