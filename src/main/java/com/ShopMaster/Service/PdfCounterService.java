package com.ShopMaster.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.PdfCounter;
import com.ShopMaster.Repository.PdfCounterRepository;

@Service
public class PdfCounterService {

    @Autowired
    private PdfCounterRepository counterRepository;

    public void increment() {
        PdfCounter counter = counterRepository.findById("pdfCounter")
                .orElseGet(() -> new PdfCounter("pdfCounter", 0L));
        counter.setCount(counter.getCount() + 1);
        counterRepository.save(counter);
    }

    public long getCurrentCount() {
        return counterRepository.findById("pdfCounter")
                .map(PdfCounter::getCount)
                .orElse(0L);
    }

}

