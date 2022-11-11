package com.example.dividends.web;

import com.example.dividends.model.Company;
import com.example.dividends.model.constants.CacheKey;
import com.example.dividends.persist.entity.CompanyEntity;
import com.example.dividends.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

  private final CompanyService companyService;
  private final CacheManager redisCacheManager;

  @GetMapping("/autocomplete")
  public ResponseEntity<?> autocomplete(@RequestParam String keyword){
    var result = this.companyService.autoComplete(keyword);
    return ResponseEntity.ok(result);
  }

  @GetMapping()
  @PreAuthorize("hasRole('READ')")
  public ResponseEntity<?> searchCompany(final Pageable pageable){
    Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
    return ResponseEntity.ok(companies);
  }

  @PostMapping()
  @PreAuthorize("hasRole('WRITE')")
  public ResponseEntity<?> addCompany(@RequestBody Company request){
    String ticker = request.getTicker().trim();
    if(ObjectUtils.isEmpty(ticker)) {
      throw new RuntimeException("Ticker is empty");
    }
    Company company = this.companyService.save(ticker);
    this.companyService.addAutoCompleteKeyword(company.getName());
    return ResponseEntity.ok(company);
  }

  @DeleteMapping("/{ticker}")
  @PreAuthorize("hasRole('WRITE')")
  public ResponseEntity<?> deleteCompany(@PathVariable String ticker){
    String companyName = this.companyService.deleteCompany(ticker);
    this.clearFinanceCache(companyName);

    return ResponseEntity.ok(companyName);
  }

  public void clearFinanceCache(String companyName){
    this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
  }

}
