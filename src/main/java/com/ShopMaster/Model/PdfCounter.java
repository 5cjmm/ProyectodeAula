package com.ShopMaster.Model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pdf_counter")
public class PdfCounter {
    
    @Id
    private String id;
    private long count;

    public PdfCounter() {}
    public PdfCounter(String id, long count) {
        this.id = id;
        this.count = count;
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}




