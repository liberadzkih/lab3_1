package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sharedkernel.Money;

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
    Invoice invoice;
    InvoiceFactory invoiceFactorySpy;

    @Before
    public void init() {
        clientData = new ClientData(Id.generate(), "Bob");
        invoiceRequest = new InvoiceRequest(clientData);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        product = new Product(Id.generate(), new Money(13.37), "Peanut butter", ProductType.FOOD);
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
}
