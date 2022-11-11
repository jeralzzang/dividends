package com.example.dividends.scraper;

import com.example.dividends.model.Company;
import com.example.dividends.model.Dividend;
import com.example.dividends.model.ScrapedResult;
import com.example.dividends.model.constants.Month;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class YahooFinanceScraper implements Scraper{

  private static final String STATICSTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
  private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

  private static final long START_TIME = 86400; ///60*60*24

  @Override
  public ScrapedResult scrap(Company company){
    var scrapedResult = new ScrapedResult();
    scrapedResult.setCompany(company);
    try {
      long now = System.currentTimeMillis() / 1000;

      String url = String.format(STATICSTICS_URL, company.getTicker(), START_TIME, now);
      Connection connection =
          Jsoup.connect(url);
      Document document = connection.get();

      Elements parsingDivs =
          document.getElementsByAttributeValue("data-test","historical-prices");
      Element tableEle = parsingDivs.get(0);

      Element tBody = tableEle.children().get(1);

      List<Dividend> dividends = new ArrayList<>();
      for(Element e : tBody.children()){
        String text = e.text();
        if(!text.endsWith("Dividend")){
          continue;
        }

        String[] splits = text.split(" ");
        int month = Month.strToNumber(splits[0]);
        int day = Integer.valueOf(splits[1].replace("," , ""));
        int year = Integer.valueOf(splits[2]);
        String dividend = splits[3];

        if(month<0){
          throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
        }
        dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0,0), dividend));

      }
      scrapedResult.setDividends(dividends);
    }catch(Exception e){
      e.printStackTrace();
    }

    return scrapedResult;
  }

  @Override
  public Company scrapCompanyByTicker(String ticker){
    String url = String.format(SUMMARY_URL, ticker, ticker);

    try {
      Document document = Jsoup.connect(url).get();
      Element titleElement = document.getElementsByTag("h1").get(0);
      String title = titleElement.text().split(" - ")[1].trim();
      return new Company(ticker, title);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
