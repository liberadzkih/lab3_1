package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BookKeeperTest {
    InvoiceFactory invoiceFactory;
    TaxPolicy taxPolicy;
    InvoiceRequest invoiceRequest;

    @Before
    public void init() {
        invoiceFactory = new InvoiceFactory();
        taxPolicy = Mockito.mock(TaxPolicy.class);
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "Jan Nowak"));
    }

    @Test
    public void shouldReturnInvoiceWithOnePosition() {
        Money money = new Money(BigDecimal.valueOf(50));
        Product product = new Product(Id.generate(), money, "Orange", ProductType.FOOD);
        ProductData productData = product.generateSnapshot();
        invoiceRequest.add(new RequestItem(productData, 2, money));

        Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class))).thenReturn(new Tax(money, "tax"));

        BookKeeper bookKeeper = new BookKeeper(invoiceFactory);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(1, equalTo(invoice.getItems().size()));
    }
}
