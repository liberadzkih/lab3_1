package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class BookKeeperTests {

    private Tax tax;
    private TaxPolicy taxPolicy;
    private BookKeeper bookKeeper;
    private ClientData clientData;
    private InvoiceRequest invoiceRequest;

    @BeforeEach
    public void prepareTest() {
        tax = mock(Tax.class);
        taxPolicy = mock(TaxPolicy.class);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(clientData);
        clientData = new ClientData(Id.generate(), "ClientName");
        // Tax
        when(tax.getAmount()).thenReturn(Money.ZERO);
        // TaxPolicy
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
    }

    // STATE TESTS
    @Test()
    public void nullInvoiceRequestIssuanceThrowsNullException() {
        assertThrows(NullPointerException.class, () -> bookKeeper.issuance(null, taxPolicy));
    }

    @Test
    public void zeroPositionIssuanceReturnsZeroPositionInvoice() {
        assertEquals(0, bookKeeper.issuance(invoiceRequest, taxPolicy)
                                  .getItems()
                                  .size());
    }

    @Test
    public void singlePositionIssuanceReturnsSinglePositionInvoice() {
        Money money = new Money(100);
        Product product = new ProductBuilder().withPrice(money)
                                              .withName("Facemask")
                                              .withProductType(ProductType.STANDARD)
                                              .build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot())
                                                          .withTotalCost(money)
                                                          .build();
        invoiceRequest.add(requestItem);

        assertEquals(1, bookKeeper.issuance(invoiceRequest, taxPolicy)
                                  .getItems()
                                  .size());
    }

    // BEHAVIORAL TESTS
    @Test
    public void calculateTaxShouldBeCalledZeroTimes() {
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, never()).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void calculateTaxShouldBeCalledTwoTimes() {
        Money money = new Money(100);
        Product product = new ProductBuilder().withPrice(money)
                                              .withName("Facemask")
                                              .withProductType(ProductType.STANDARD)
                                              .build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot())
                                                          .withTotalCost(money)
                                                          .build();
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void calculateTaxShouldBeCalledThreeTimes() {
        Money money = new Money(100);
        Product product = new ProductBuilder().withPrice(money)
                                              .withName("Facemask")
                                              .withProductType(ProductType.STANDARD)
                                              .build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot())
                                                          .withTotalCost(money)
                                                          .build();
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(3)).calculateTax(any(ProductType.class), any(Money.class));
    }
}
