package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

public class ProductBuilder {
    private Money price;

    private String name;

    private ProductType productType;

    public ProductBuilder price(Money price){
        this.price = price;
        return this;
    }

    public ProductBuilder price(BigDecimal price){
        this.price = new Money(price);
        return this;
    }

    public ProductBuilder name(String name){
        this.name = name;
        return this;
    }

    public ProductBuilder productType(ProductType productType){
        this.productType = productType;
        return this;
    }

    public Product build(){
        return new Product(Id.generate(),price,name,productType);
    }
}
