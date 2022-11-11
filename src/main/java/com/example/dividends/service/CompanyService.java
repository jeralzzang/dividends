package com.example.dividends.service;

import com.example.dividends.exception.impl.NoCompanyException;
import com.example.dividends.model.Company;
import com.example.dividends.model.ScrapedResult;
import com.example.dividends.persist.CompanyRepository;
import com.example.dividends.persist.DividendRepository;
import com.example.dividends.persist.entity.CompanyEntity;
import com.example.dividends.persist.entity.DividendEntity;
import com.example.dividends.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
public class CompanyService {
  private final Scraper yahooFinanceScrapper;
  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;
  private final Trie trie;

  public Company save(String ticker){
    boolean exists = this.companyRepository.existsByTicker(ticker);
    if(exists){
      throw new RuntimeException("Already exists ticker ->" + ticker);
    }
    return this.storeCompanyAndDividend(ticker);
  }

  private Company storeCompanyAndDividend(String ticker){
    //ticker  기준 회사 스크랩
    //회사 존재할 경우 배당금 정보 스크랩
    Company company = this.yahooFinanceScrapper.scrapCompanyByTicker(ticker);
    if(ObjectUtils.isEmpty(company)){
      throw new RuntimeException("failed to scrap ticker -> "+ ticker);
    }

    ScrapedResult scrapedResult = this.yahooFinanceScrapper.scrap(company);

    CompanyEntity  companyEntity = this.companyRepository.save(new CompanyEntity(company));
    List<DividendEntity> dividendEntities =  scrapedResult.getDividends().stream()
        .map(e -> new DividendEntity(companyEntity.getId(), e))
        .collect(Collectors.toList());

    this.dividendRepository.saveAll(dividendEntities);
    return company;
  }

  public Page<CompanyEntity> getAllCompany(Pageable pageable){
    return this.companyRepository.findAll(pageable);
  }

  public void addAutoCompleteKeyword(String keyword){
    this.trie.put(keyword, null);
  }
  public List<String> autoComplete(String keyword){
    return (List<String>) this.trie.prefixMap(keyword).
        keySet().stream().collect(Collectors.toList());
  }
  public void deleteAutoCompleteKeyword(String keyword){
    this.trie.remove(keyword);
  }

  public String deleteCompany(String ticker) {
    var company = this.companyRepository.findByTicker(ticker)
        .orElseThrow(() -> new NoCompanyException());

    this.dividendRepository.deleteByCompanyId(company.getId());
    this.companyRepository.delete(company);
    this.deleteAutoCompleteKeyword(company.getName());

    return company.getName();
  }
}
