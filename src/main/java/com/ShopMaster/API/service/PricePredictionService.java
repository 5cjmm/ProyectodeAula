package com.ShopMaster.API.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.time.Year;
import java.util.*;

@Service
public class PricePredictionService {

    private Classifier model;
    private List<Attribute> attributes;

    // Optional: a comma-separated list of products if Mongo is not configured
    @Value("${app.products:}")
    private String productsProperty;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Value("${app.model.filename:price.model}")
    private String modelFilename;

    public PricePredictionService() {
        // constructor left intentionally empty; model is loaded in init() after injections
    }

    @jakarta.annotation.PostConstruct
    private void init() {
        try {
            // load model (use configured filename if provided)
            String filenameToLoad = (modelFilename != null && !modelFilename.isBlank()) ? modelFilename : "price.model";
            InputStream is = getClass().getClassLoader().getResourceAsStream(filenameToLoad);
            if (is == null) {
                // try fallback
                is = getClass().getClassLoader().getResourceAsStream("price.model");
            }
            if (is == null) {
                throw new RuntimeException("No se encontró el fichero de modelo '" + filenameToLoad + "' en resources");
            }
            ObjectInputStream ois = new ObjectInputStream(is);
            model = (Classifier) ois.readObject();
            ois.close();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando el modelo Weka: " + e.getMessage(), e);
        }
    }

    // Build attributes once we know product categories
    private synchronized void ensureAttributes(List<String> productCategories) {
        if (attributes != null) return;

        attributes = new ArrayList<>();
        // Use the same attribute names as the ARFF used to train the model
        // año numeric
        attributes.add(new Attribute("año"));
        // producto nominal
        List<String> prods = new ArrayList<>(productCategories);
        attributes.add(new Attribute("producto", prods));
        // precio (class) numeric
        attributes.add(new Attribute("precio"));
    }

    private List<String> loadProductCategories() {
        // 1) If app.products property provided, use it
        if (productsProperty != null && !productsProperty.isBlank()) {
            String[] tokens = productsProperty.split(",");
            List<String> trimmed = new ArrayList<>();
            for (String t : tokens) {
                if (!t.isBlank()) trimmed.add(t.trim());
            }
            if (!trimmed.isEmpty()) return trimmed;
        }

        // 2) Try to parse Dataset_final.arff in resources to extract the nominal product list (preferred)
        try (Scanner sc = new Scanner(getClass().getClassLoader().getResourceAsStream("Dataset_final.arff"), "UTF-8")) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.toLowerCase().startsWith("@attribute producto")) {
                    // line contains the nominal list, e.g.
                    // @attribute producto {'Gaseosa Postobón 1.5L','Agua Brisa 600ml',...}
                    int start = line.indexOf('{');
                    int end = line.lastIndexOf('}');
                    if (start >= 0 && end > start) {
                        String inside = line.substring(start + 1, end);
                        // split by comma but values may be quoted
                        List<String> values = new ArrayList<>();
                        // simple parser: split on comma that are outside quotes
                        StringBuilder cur = new StringBuilder();
                        boolean inQuote = false;
                        for (int i = 0; i < inside.length(); i++) {
                            char c = inside.charAt(i);
                            if (c == '\'' ) {
                                inQuote = !inQuote;
                                continue; // omit quotes
                            }
                            if (c == ',' && !inQuote) {
                                String v = cur.toString().trim();
                                if (!v.isEmpty()) values.add(v);
                                cur.setLength(0);
                            } else {
                                cur.append(c);
                            }
                        }
                        if (cur.length() > 0) {
                            String v = cur.toString().trim();
                            if (!v.isEmpty()) values.add(v);
                        }
                        if (!values.isEmpty()) return values;
                    }
                }
            }
        } catch (Exception ignored) {
            // fallthrough
        }

        // 3) If MongoTemplate is available, try to fetch distinct product values from collection 'prediccion'
        if (mongoTemplate != null) {
            try {
                // attempt distinct on 'producto' or 'product' field
                List<String> distinct = mongoTemplate.getCollection("prediccion").distinct("producto", String.class).into(new ArrayList<>());
                if (!distinct.isEmpty()) return distinct;
                // fallback to 'product' field if the collection uses that key
                distinct = mongoTemplate.getCollection("prediccion").distinct("product", String.class).into(new ArrayList<>());
                if (!distinct.isEmpty()) return distinct;
            } catch (Exception ignored) {
                // fallthrough
            }
        }

        // 4) As a last resort, leave a single placeholder category - the caller must ensure matching category
        return List.of("unknown");
    }

    public Map<Integer, Double> predictMultiYear(String product, Integer startYear, int years) {
        if (product == null || product.isBlank()) throw new IllegalArgumentException("product is required");
        if (years <= 0) throw new IllegalArgumentException("years must be > 0");

        int baseYear = (startYear == null) ? Year.now().getValue() : startYear;

        List<String> categories = loadProductCategories();
        // If the provided product is not in categories, add it (helps when categories were minimal)
        if (!categories.contains(product)) {
            List<String> mod = new ArrayList<>(categories);
            mod.add(product);
            categories = mod;
        }

        ensureAttributes(categories);

        Map<Integer, Double> results = new LinkedHashMap<>();

        for (int i = 0; i < years; i++) {
            int y = baseYear + i;

            Instances dataset = new Instances("PriceInstance", new ArrayList<>(attributes), 1);
            dataset.setClassIndex(dataset.numAttributes() - 1);

            DenseInstance instance = new DenseInstance(dataset.numAttributes());
            instance.setDataset(dataset);

            // set año
            instance.setValue(attributes.get(0), y);
            // set producto (nominal)
            instance.setValue(attributes.get(1), product);
            // leave price missing (to predict)
            instance.setMissing(attributes.get(2));

            try {
                double pred = model.classifyInstance(instance);
                results.put(y, pred);
            } catch (Exception e) {
                results.put(y, Double.NaN);
            }
        }

        return results;
    }

    // expose product categories for frontend
    public List<String> getProductCategories() {
        List<String> categories = loadProductCategories();
        return categories;
    }

    // Return historical year->price map for a given product
    public Map<Integer, Double> getHistory(String product) {
        Map<Integer, Double> history = new TreeMap<>();
        if (product == null || product.isBlank()) return history;

        // 1) Try Mongo
        if (mongoTemplate != null) {
            try {
                com.mongodb.client.MongoCollection<org.bson.Document> coll = mongoTemplate.getCollection("prediccion");
                org.bson.Document filter = new org.bson.Document();
                filter.put("producto", product);
                com.mongodb.client.FindIterable<org.bson.Document> docs = coll.find(filter);
                for (org.bson.Document d : docs) {
                    Object yearObj = d.get("año");
                    if (yearObj == null) yearObj = d.get("year");
                    Object priceObj = d.get("precio");
                    if (priceObj == null) priceObj = d.get("price");
                    Integer y = null;
                    Double p = null;
                    if (yearObj instanceof Number) y = ((Number) yearObj).intValue();
                    else if (yearObj instanceof String) {
                        try { y = Integer.parseInt((String) yearObj); } catch (Exception ignored) {}
                    }
                    if (priceObj instanceof Number) p = ((Number) priceObj).doubleValue();
                    else if (priceObj instanceof String) {
                        try { p = Double.parseDouble((String) priceObj); } catch (Exception ignored) {}
                    }
                    if (y != null && p != null) history.put(y, p);
                }
                if (!history.isEmpty()) return history;
            } catch (Exception ignored) {
                // fallthrough to arff fallback
            }
        }

        // 2) Fallback: parse Dataset_final.arff
        try (Scanner sc = new Scanner(getClass().getClassLoader().getResourceAsStream("Dataset_final.arff"), "UTF-8")) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("%") || line.startsWith("@")) continue;
                // data lines like: 2015,'Gaseosa Postobón 1.5L',4253.51
                // split on commas but respect quoted product
                List<String> parts = new ArrayList<>();
                StringBuilder cur = new StringBuilder();
                boolean inQuote = false;
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == '\'') { inQuote = !inQuote; continue; }
                    if (c == ',' && !inQuote) { parts.add(cur.toString().trim()); cur.setLength(0); }
                    else cur.append(c);
                }
                if (cur.length() > 0) parts.add(cur.toString().trim());
                if (parts.size() >= 3) {
                    String yearS = parts.get(0);
                    String prodS = parts.get(1);
                    String priceS = parts.get(2);
                    // remove surrounding quotes if present
                    if (prodS.startsWith("'") && prodS.endsWith("'")) prodS = prodS.substring(1, prodS.length()-1);
                    if (prodS.equals(product)) {
                        try {
                            int y = Integer.parseInt(yearS);
                            double p = Double.parseDouble(priceS);
                            history.put(y, p);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}

        return history;
    }

}
