import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebScraper extends JFrame {

    private JTextField urlField;
    private JTextArea statusArea;
    private JButton scrapeBtn;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public WebScraper() {
        setTitle("🛒 Welcome to E-commerce Data Scraper webpage ✨");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

       
        JPanel topPan = new JPanel();
        topPan.setLayout(new BoxLayout(topPan, BoxLayout.Y_AXIS));
        topPan.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Enter the e-commerce URL:");
        label.setFont(new Font("Times New Romen", Font.BOLD, 16));

        urlField = new JTextField("http://books.toscrape.com/");
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        scrapeBtn = new JButton("Result Display");
        scrapeBtn.setFont(new Font("Times New Romen", Font.BOLD, 16));

        topPan.add(label);
        topPan.add(Box.createVerticalStrut(5));
        topPan.add(urlField);
        topPan.add(Box.createVerticalStrut(10));
        topPan.add(scrapeBtn);

        String[] columns = { "Name", "Price", "Rating" };
        tableModel = new DefaultTableModel(columns, 0);
        resultTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Scraped Product Details"));

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JScrollPane StatusScrollPane = new JScrollPane(statusArea);
        StatusScrollPane.setBorder(BorderFactory.createTitledBorder("Status "));
        StatusScrollPane.setPreferredSize(new Dimension(800, 120));

     
        add(topPan, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(StatusScrollPane, BorderLayout.SOUTH);

        scrapeBtn.addActionListener(e -> scrapeData());

        setVisible(true);
    }

    private void scrapeData() {
        String url = urlField.getText().trim();
        statusArea.setText("Scraping from: " + url + "\n");
        scrapeBtn.setEnabled(false);
        tableModel.setRowCount(0); 
        statusArea.append("Please wait, scraping in progress....\n");

        try {
            Document doc = Jsoup.connect(url).get();

            Elements names = doc.select("h3 a");
            Elements prices = doc.select(".price_color");
            Elements ratings = doc.select(".star-rating");

            BufferedWriter csvWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("products.csv"), StandardCharsets.UTF_8)
            );
            csvWriter.write("Name,Price,Rating\n");

            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i).attr("title").replace("\"", "\"\"");
                String price = prices.get(i).text().replace("\"", "\"\"");
                String rating = ratings.get(i).classNames().stream()
                        .filter(cls -> !cls.equals("star-rating"))
                        .findFirst().orElse("N/A");

                // Add to CSV
                csvWriter.write(String.format("\"%s\",\"%s\",\"%s\"\n", name, price, rating));

                // Add to table
                tableModel.addRow(new Object[] { name, price, rating });
            }

            csvWriter.close();
            statusArea.append("Data displayed and exported to products.csv\n");
        } catch (IOException ex) {
            statusArea.append(" Sorry! Failed to scrape data: " + ex.getMessage() + "\n");
        } finally {
            scrapeBtn.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WebScraper::new);
    }
}
