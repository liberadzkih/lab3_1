package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class ProductBuilder {
    private Id aggregateId;
    private Money price;
    private String name;
    private ProductType productType;

    public ProductBuilder setAggregateId(Id aggregateId) {
        this.aggregateId = aggregateId;
        return this;
    }

    public ProductBuilder setPrice(Money price) {
        this.price = price;
        return this;
    }

    public ProductBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder setProductType(ProductType productType) {
        this.productType = productType;
        return this;
    }

    public Product build() {
        return new Product(aggregateId, price, name, productType);
    }
}