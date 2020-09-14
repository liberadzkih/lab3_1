package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class ProductTestBuilder {

    private Id id;
    private Money price;
    private String name;
    private ProductType productType;

    public ProductTestBuilder id(Id id) {
        this.id = id;
        return this;
    }

    public ProductTestBuilder price(Money price) {
        this.price = price;
        return this;
    }

    public ProductTestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductTestBuilder productType(ProductType type) {
        this.productType = type;
        return this;
    }

    public Product build() {
        return new Product(this.id, this.price, this.name, this.productType);
    }

}
