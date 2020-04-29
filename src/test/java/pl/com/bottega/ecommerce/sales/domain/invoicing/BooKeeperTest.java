package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

;

public class BooKeeperTest {
    TaxPolicy taxPolicyMock;
    BookKeeper bookKeeper;
    Tax tax;

    @BeforeEach
    void initialize() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        tax = new Tax(
                new Money(BigDecimal.TEN),
                "Somze Tax"
        );
        taxPolicyMock = mock(TaxPolicy.class);
    }

    @Test
    void shouldReturnInvoiceWithOnePosition() {
        TaxPolicy taxPolicy = getTaxPolicyMockWithTax(BigDecimal.TEN, "Some tax");
        InvoiceRequest invoiceRequest = getInvoiceRequestWithNumberOfItems(1);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    void shouldReturnInvoiceWithoutPosition() {
        TaxPolicy taxPolicy = getTaxPolicyMockWithTax(BigDecimal.TEN, "Some tax");
        InvoiceRequest invoiceRequest = getInvoiceRequestWithNumberOfItems(0);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertEquals(0, invoice.getItems().size());
    }

    @Test
    void shouldAddNetToGros() {
        Money money = new Money(BigDecimal.TEN);
        TaxPolicy taxPolicy = getTaxPolicyMockWithTax(money, "Some tax");
        InvoiceRequest invoiceRequest = getInvoiceRequestWithNumberOfItems(1);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        Money gros = invoice.getGros();
        Money grosWithoutTax = gros.subtract(money);
        assertEquals(invoice.getNet(), grosWithoutTax);
    }

    @Test
    void shouldUseCalculateTaxTwice() {
        InvoiceRequest invoiceRequest = getInvoiceRequestWithNumberOfItems(2);
        AtomicInteger invocationCount = new AtomicInteger();

        when(taxPolicyMock.calculateTax(any(), any()))
                .thenAnswer(invocationOnMock -> {
                    invocationCount.getAndIncrement();
                    return tax;
                });

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(2, invocationCount.get());
    }

    @Test
    void shouldNotUseCalculateTax() {
        InvoiceRequest invoiceRequest = getInvoiceRequestWithNumberOfItems(0);
        AtomicInteger invocationCount = new AtomicInteger();

        when(taxPolicyMock.calculateTax(any(), any()))
                .thenAnswer(invocationOnMock -> {
                    invocationCount.getAndIncrement();
                    return tax;
                });

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(0, invocationCount.get());
    }

    @Test
    void shouldUseMoneyFromProductData() {
        AtomicReference<Money> moneyAtomicReference = new AtomicReference<>();

        when(taxPolicyMock.calculateTax(any(), any()))
                .thenAnswer(invocationOnMock -> {
                    Money money = invocationOnMock.getArgument(1, Money.class);
                    moneyAtomicReference.set(money);
                    return tax;
                });

        RequestItem requestItem = getRequestItem();

        InvoiceRequest invoiceRequest = new InvoiceRequestBuilder()
                .addRequestItem(requestItem)
                .build();

        Money totalCost = requestItem.getTotalCost();
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(totalCost, moneyAtomicReference.get());
    }


    TaxPolicy getTaxPolicyMockWithTax(BigDecimal denomination, String description) {
        Money money = new Money(denomination);
        return getTaxPolicyMockWithTax(money, description);
    }

    TaxPolicy getTaxPolicyMockWithTax(Money money, String description) {
        TaxPolicy taxPolicy = mock(TaxPolicy.class);
        when(taxPolicy.calculateTax(any(), any()))
                .thenReturn(
                        new Tax(money, description)
                );
        return taxPolicy;
    }

    InvoiceRequest getInvoiceRequestWithNumberOfItems(int numberOfItems) {
        InvoiceRequestBuilder invoiceRequestBuilder = new InvoiceRequestBuilder()
                .withClientOfName("Tomasz Nowak");

        IntStream.range(0, numberOfItems).forEach(
                value -> invoiceRequestBuilder.addRequestItem(getRequestItem())
        );
        return invoiceRequestBuilder.build();
    }

    RequestItem getRequestItem() {
        ProductData productData = new ProductBuilder()
                .productType(ProductType.FOOD)
                .name("Kiełbasa")
                .price(BigDecimal.ZERO)
                .build()
                .generateSnapshot();

        return new RequestItemBuilder()
                .productData(productData)
                .quantity(2)
                .totalCost(BigDecimal.TEN)
                .build();
    }

}
