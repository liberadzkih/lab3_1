package pl.com.bottega.ecommerce.sales.domain.productscatalog;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

public class ProductBuilder {

    private Id id = Id.generate();
    private Money price = new Money(13.37);
    private String name = "Peanut butter";
    private ProductType productType = ProductType.FOOD;

    public Product build() {
        return new Product(id, price, name, productType);
    }

    public ProductBuilder withId(Id id) {
        this.id = id;
        return this;
    }

    public ProductBuilder withPrice(Money price) {
        this.price = price;
        return this;
    }

    public ProductBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder withProductType(ProductType productType) {
        this.productType = productType;
        return this;
    }
}
