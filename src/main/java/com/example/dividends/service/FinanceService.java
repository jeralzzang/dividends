package com.example.dividends.service;

import com.example.dividends.exception.impl.NoCompanyException;
import com.example.dividends.model.Company;
import com.example.dividends.model.Dividend;
import com.example.dividends.model.ScrapedResult;
import com.example.dividends.model.constants.CacheKey;
import com.example.dividends.persist.CompanyRepository;
import com.example.dividends.persist.DividendRepository;
import com.example.dividends.persist.entity.CompanyEntity;
import com.example.dividends.persist.entity.DividendEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FinanceService {
  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  @Cacheable(key ="#companyName", value = CacheKey.KEY_FINANCE)
  public ScrapedResult getDividendByCompanyName(String companyName){
    //회사명 기준 회사 정보 조회
    CompanyEntity company = this.companyRepository.findByName(companyName).
        orElseThrow(()-> new NoCompanyException());
    //조회된 회사 ID로 배당금 조회
    List<DividendEntity> dividendEntities =  this.dividendRepository.findAllByCompanyId(company.getId());

    List<Dividend> dividends = new ArrayList<>();
    for(var entity : dividendEntities){
      dividends.add(new Dividend(entity.getDate(), entity.getDividend()));

    }

    //결과반환
    return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
  }
}
