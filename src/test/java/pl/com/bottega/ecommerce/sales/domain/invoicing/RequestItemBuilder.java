package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

public class RequestItemBuilder {
    private ProductData productData;

    private int quantity;

    private Money totalCost;

    public RequestItemBuilder productData(ProductData productData){
        this.productData = productData;
        return this;
    }

    public RequestItemBuilder quantity(int quantity){
        this.quantity = quantity;
        return this;
    }

    public RequestItemBuilder totalCost(Money totalCost){
        this.totalCost = totalCost;
        return this;
    }
    public RequestItemBuilder totalCost(BigDecimal denomination){
        this.totalCost = new Money(denomination);
        return this;
    }

    public RequestItem build(){
        return new RequestItem(productData,quantity,totalCost);
    }
}
