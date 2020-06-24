package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class ProductBuilder {
    private Money price = new Money(21.37, "PLN");
    private String name = "unrelevant";
    private ProductType type = ProductType.FOOD;

    public ProductBuilder() {
    }

    public ProductBuilder withPrice(Money price) {
        this.price = price;
        return this;
    }

    public ProductBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder withType(ProductType productType) {
        this.type = productType;
        return this;
    }

    public Product build() {
        return new Product(Id.generate(), this.price, this.name, this.type);
    }
}
