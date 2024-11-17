package com.engsoft2.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CurrencyConversionController {
    private CurrencyExchangeProxy proxy;

    public CurrencyConversionController(CurrencyExchangeProxy proxy) {
        this.proxy = proxy;
    }

    @Value("${currency-exchange-service.url}")
    private String currencyExchangeBaseUrl;
    
    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(@PathVariable String from, @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        HashMap<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        String url = String.format("%s/from/{from}/to/{to}", currencyExchangeBaseUrl);
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
                url, CurrencyConversion.class, uriVariables);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            CurrencyConversion currencyConversion = responseEntity.getBody();
            return new CurrencyConversion(
                    currencyConversion.getId(),
                    from, to, quantity,
                    currencyConversion.getConversionMultiple(),
                    quantity.multiply(currencyConversion.getConversionMultiple()),
                    currencyConversion.getEnvironment() + " " + "rest template");
        }
        throw new ResourceNotFoundException("From " + from + "To " + to + " not found");
    }

    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(@PathVariable String from, @PathVariable String to,
            @PathVariable BigDecimal quantity) {
        CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);
        return new CurrencyConversion(
                currencyConversion.getId(),
                from, to, quantity,
                currencyConversion.getConversionMultiple(),
                quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment() + " " + "feign");
    }

}
