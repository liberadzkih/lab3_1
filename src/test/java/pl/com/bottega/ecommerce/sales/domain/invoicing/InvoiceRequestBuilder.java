package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvoiceRequestBuilder {

    private ClientData client;
    private List<RequestItem> items = new ArrayList<>();

    public InvoiceRequestBuilder withClientOfName(String name){
        this.client(new ClientData(Id.generate(),name));
        return this;
    }

    public InvoiceRequestBuilder client(ClientData client){
        this.client = client;
        return this;
    }

    public InvoiceRequestBuilder addRequestItem(RequestItem... requestItem){
        Collections.addAll(items, requestItem);
        return this;
    }

    public InvoiceRequest build(){
        InvoiceRequest invoiceRequest = new InvoiceRequest(client);
        items.forEach(invoiceRequest::add);
        return invoiceRequest;
    }
}
