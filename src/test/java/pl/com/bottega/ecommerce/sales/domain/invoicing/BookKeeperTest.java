package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BookKeeperTest {
    TaxPolicy taxPolicy;
    Tax tax;
    InvoiceFactory invoiceFactory;
    Product product;
    RequestItem requestItem;
    InvoiceRequest invoiceRequest;
    BookKeeper bookKeeper;

    @Before
    public void setUp() {
        taxPolicy = mock(TaxPolicy.class);
        tax = new Tax(Money.ZERO, "tax");
        product = new Product(Id.generate(), Money.ZERO, "product", ProductType.STANDARD);
        requestItem = new RequestItem(product.generateSnapshot(), 1, Money.ZERO);
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "client"));
        bookKeeper = new BookKeeper(new InvoiceFactory());
    }

    @Test
    public void shouldReturnInvoiceWithOneItemWhenGivenOneItem() {
        when(taxPolicy.calculateTax(any(), any())).thenReturn(tax);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems().size(), is(1));
    }

    @Test
    public void shouldInvokeCalculateTaxTwoTimesWhenTwoItems() {
        when(taxPolicy.calculateTax(any(), any())).thenReturn(tax);
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);
        bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

}
