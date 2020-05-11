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

    @Test
    public void testZeroInvoice() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertThat(invoice.getItems(), hasSize(0));
    }

    @Test
    public void testOneInvoice() {
        invoiceRequest.add(new RequestItem(product.generateSnapshot(), 12, new Money(BigDecimal.ONE)));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertThat(invoice.getItems(), hasSize(1));
    }

    @Test
    public void testTwoInvoiceCalls() {
        RequestItem requestItemOne = new RequestItem(product.generateSnapshot(), 21, new Money(BigDecimal.ONE));
        RequestItem requestItemTwo = new RequestItem(product.generateSnapshot(), 22, new Money(BigDecimal.ROUND_CEILING));

        invoiceRequest.add(requestItemOne);
        invoiceRequest.add(requestItemTwo);

        bookKeeper.issuance(invoiceRequest, taxPolicyMock);

        verify(taxPolicyMock).calculateTax(requestItemOne.getProductData().getType(), requestItemOne.getProductData().getPrice());
        verify(taxPolicyMock).calculateTax(requestItemTwo.getProductData().getType(), requestItemTwo.getProductData().getPrice());

        verify(taxPolicyMock, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void testTwoInvoice() {
        invoiceRequest.add(new RequestItem(product.generateSnapshot(), 12, new Money(BigDecimal.ONE)));
        invoiceRequest.add(new RequestItem(product.generateSnapshot(), 13, new Money(BigDecimal.ROUND_CEILING)));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertThat(invoice.getItems(), hasSize(2));
    }
}