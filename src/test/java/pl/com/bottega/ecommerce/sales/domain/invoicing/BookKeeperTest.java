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
        product = new ProductBuilder().build();
        when(taxPolicyMock.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(BigDecimal.ONE), "testTax"));
    }

    @Test
    public void testZeroInvoice() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertThat(invoice.getItems(), hasSize(0));
    }

    @Test
    public void testZeroInvoiceBehaviour() {
        bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        verify(taxPolicyMock, never()).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void testOneInvoice() {
        invoiceRequest.add(new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(12).withTotalCost(new Money(BigDecimal.ONE)).build());
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertThat(invoice.getItems(), hasSize(1));
    }

    @Test
    public void testOneInvoiceBehaviour() {
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(21).withTotalCost(new Money(BigDecimal.ONE)).build();

        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicyMock);

        verify(taxPolicyMock).calculateTax(requestItem.getProductData().getType(), requestItem.getProductData().getPrice());
        verify(taxPolicyMock, times(1)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void testTwoInvoiceCallsBehaviour() {
        RequestItem requestItemOne = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(21).withTotalCost(new Money(BigDecimal.ONE)).build();
        RequestItem requestItemTwo = new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(22).withTotalCost(new Money(BigDecimal.ROUND_CEILING)).build();;

        invoiceRequest.add(requestItemOne);
        invoiceRequest.add(requestItemTwo);

        bookKeeper.issuance(invoiceRequest, taxPolicyMock);

        verify(taxPolicyMock).calculateTax(requestItemOne.getProductData().getType(), requestItemOne.getProductData().getPrice());
        verify(taxPolicyMock).calculateTax(requestItemTwo.getProductData().getType(), requestItemTwo.getProductData().getPrice());

        verify(taxPolicyMock, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void testTwoInvoice() {
        invoiceRequest.add(new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(12).withTotalCost(new Money(BigDecimal.ONE)).build());
        invoiceRequest.add(new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(13).withTotalCost(new Money(BigDecimal.ROUND_CEILING)).build());
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertThat(invoice.getItems(), hasSize(2));
    }
}