package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class ItemBuilder {
    private ProductData productData;
    private int quantity;
    private Money totalCost;

    public ItemBuilder setProductData(ProductData productData) {
        this.productData = productData;
        return this;
    }

    public ItemBuilder setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public ItemBuilder setTotalCost(Money totalCost) {
        this.totalCost = totalCost;
        return this;
    }

    public RequestItem build() {
        return new RequestItem(productData, quantity, totalCost);
    }
}