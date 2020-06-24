package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductBuilder;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookKeeperTest {

    @Mock
    TaxPolicy taxPolicy;
    ClientData clientData;
    InvoiceRequest invoiceRequest;
    BookKeeper bookKeeper;
    Product product;
    ProductData productData;
    InvoiceFactory invoiceFactorySpy;

    @Before
    public void init() {
        clientData = new ClientData(Id.generate(), "Bob");
        invoiceRequest = new InvoiceRequest(clientData);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        product = new ProductBuilder().build();
        productData = product.generateSnapshot();
        invoiceFactorySpy = Mockito.spy(new InvoiceFactory());
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(21.37), "Tax Description"));
    }

    @Test
    public void testIssuanceWithOnePositionShouldReturnInvoiceWithOnePosition() {
        invoiceRequest.add(new RequestItem(productData, 1, new Money(1.99)));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems(), hasSize(1));
    }

    @Test
    public void testIssuanceWithTwoPositionsShouldInvokeCalculateTaxMethodTwice() {
        invoiceRequest.add(new RequestItem(productData, 1, new Money(2.02)));
        invoiceRequest.add(new RequestItem(productData, 1, new Money(2.01)));
        bookKeeper.issuance(invoiceRequest, taxPolicy);
        Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void testTotalCostOfInvoice() {
        for (int i = 0; i < 5; i++) {
            invoiceRequest.add(new RequestItem(productData, 1, new Money(2.02)));
        }
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(new Money(10.10), is(equalTo(invoice.getNet())));
    }

    @Test
    public void testInvoiceRequestShouldHaveClientData() {
        for (int i = 0; i < 5; i++) {
            invoiceRequest.add(new RequestItem(productData, 1, new Money(2.02)));
        }
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getClient(), equalTo(invoiceRequest.getClient()));
    }

    @Test
    public void testIssuanceWithManyPositionsShouldCreateInvoiceOnce() {
        for (int i = 0; i < 10; i++) {
            invoiceRequest.add(new RequestItem(productData, 1, new Money(2.05)));
        }
        bookKeeper = new BookKeeper(invoiceFactorySpy);
        bookKeeper.issuance(invoiceRequest, taxPolicy);
        Mockito.verify(invoiceFactorySpy, Mockito.times(1)).create(any(ClientData.class));
    }

    @Test
    public void testIssuanceWithTenPositionsShouldCallCalculateTaxTenTimes() {
        for (int i = 0; i < 10; i++) {
            invoiceRequest.add(new RequestItem(productData, 1, new Money(2.06)));
        }
        bookKeeper.issuance(invoiceRequest, taxPolicy);
        Mockito.verify(taxPolicy, Mockito.times(10)).calculateTax(any(ProductType.class), any(Money.class));
    }

}
