package com.example.dividends.scraper;

import com.example.dividends.model.Company;
import com.example.dividends.model.ScrapedResult;
import org.springframework.context.annotation.Bean;

public interface Scraper {

  Company scrapCompanyByTicker(String ticker);
  ScrapedResult scrap(Company company);
}
