package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class BookKeeperTest {
    @Mock
    TaxPolicy taxPolicyMock;
    BookKeeper bookKeeper;
    InvoiceRequest invoiceRequest;
    Product product;

    @Before
    public void initTests() {
        taxPolicyMock = mock(TaxPolicy.class);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "testClient"));
        product = new Product(Id.generate(), new Money(BigDecimal.ONE), "testProduct", ProductType.FOOD);
        when(taxPolicyMock.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(BigDecimal.ONE), "testTax"));
    }
}