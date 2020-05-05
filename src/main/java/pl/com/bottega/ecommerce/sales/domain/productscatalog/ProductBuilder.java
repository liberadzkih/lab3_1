package pl.com.bottega.ecommerce.sales.domain.productscatalog;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

public class ProductBuilder {
    //wartosci defaultowe na wypadek nie ustawienia ich przez wywolanie "seterow"
    private Id id = Id.generate();
    private Money price = new Money(BigDecimal.TEN);
    private String name = "Kiełbaska";
    private ProductType productType = ProductType.FOOD;

    public ProductBuilder withId(Id id){
        this.id = id;
        return this;
    }
    public ProductBuilder withPrice(Money price){
        this.price = price;
        return this;
    }
    public ProductBuilder withName(String name){
        this.name = name;
        return this;
    }
    public ProductBuilder withProductType(ProductType productType){
        this.productType = productType;
        return this;
    }

    public Product build(){
        return new Product(this.id,this.price,this.name,this.productType);
    }

}
