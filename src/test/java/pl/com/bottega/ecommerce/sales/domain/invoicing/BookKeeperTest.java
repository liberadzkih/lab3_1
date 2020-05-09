package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sharedkernel.Money;

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
        product = new ProductBuilder().build();
        requestItem = new RequestItem(product.generateSnapshot(), 1, Money.ZERO);
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "client"));
        bookKeeper = new BookKeeper(new InvoiceFactory());
        when(taxPolicy.calculateTax(any(), any())).thenReturn(tax);
    }

    @Test
    public void shouldReturnInvoiceWithOneItemWhenGivenOneItem() {
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems().size(), is(1));
    }

    @Test
    public void shouldReturnEmptyInvoiceWhenNoItemsGiven() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems().size(), is(0));
    }

    @Test
    public void shouldReturnInvoiceWithFiveItemsWhenGivenFiveItems() {
        final int numberOfElements = 5;
        for (int i = 0; i < numberOfElements; i++) {
            invoiceRequest.add(requestItem);
        }
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems().size(), is(numberOfElements));
    }

    @Test
    public void shouldInvokeCalculateTaxTwoTimesWhenTwoItems() {
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);
        bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

    @Test
    public void shouldNotInvokeCalculateTaxWhenNoItems() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, never()).calculateTax(any(), any());
    }

    @Test
    public void shouldInvokeCalculateTaxFiveTimesWhenGivenFiveItems() {
        final int numberOfElements = 5;
        for (int i = 0; i < numberOfElements; i++) {
            invoiceRequest.add(requestItem);
        }
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(numberOfElements)).calculateTax(any(), any());
    }
}
