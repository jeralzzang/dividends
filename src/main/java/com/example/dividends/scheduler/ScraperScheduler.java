package com.example.dividends.scheduler;

import com.example.dividends.model.Company;
import com.example.dividends.model.ScrapedResult;
import com.example.dividends.model.constants.CacheKey;
import com.example.dividends.persist.CompanyRepository;
import com.example.dividends.persist.DividendRepository;
import com.example.dividends.persist.entity.CompanyEntity;
import com.example.dividends.persist.entity.DividendEntity;
import com.example.dividends.scraper.Scraper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

  private final CompanyRepository companyRepository;
  private final Scraper yahooFinanceScraper;
  private final DividendRepository dividendRepository;

  @Scheduled(cron = "${scheduler.scrap.yahoo}")
  @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
  public void yahooFinanceScheduling(){
    log.info("스크래핑 스케쥴러 시작");
    //저장 된 회사 목록 조회
    List<CompanyEntity> companies =  this.companyRepository.findAll();
    //회사 배당금 정보 스크래핑
    for(var company : companies){
      ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getName(), company.getTicker()));


      //스크래핑한 배당금 정보 중 디비에 없는 값 저장
      scrapedResult.getDividends().stream()
          .map(e -> new DividendEntity(company.getId(), e))
          .forEach(e -> {
            boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
            if(!exists){
              this.dividendRepository.save(e);
              log.info("insert new dividends");
            }
          });

      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    }
  }
}
