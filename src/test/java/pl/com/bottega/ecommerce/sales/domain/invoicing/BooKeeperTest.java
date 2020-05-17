package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BooKeeperTest {
    BookKeeper bookKeeper;

    @BeforeEach
    void initialize() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
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

        TaxPolicy taxPolicy = getTaxPolicyMockWithTax(BigDecimal.TEN, "Some tax");

        bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

    @Test
    void shouldNotUseCalculateTax() {
        InvoiceRequest invoiceRequest = getInvoiceRequestWithNumberOfItems(0);

        TaxPolicy taxPolicy = getTaxPolicyMockWithTax(BigDecimal.TEN, "Some tax");

        bookKeeper.issuance(invoiceRequest, taxPolicy);
        verifyNoInteractions(taxPolicy);
    }

    @Test
    void shouldUseMoneyFromProductData() {
        TaxPolicy taxPolicy = getTaxPolicyMockWithTax(BigDecimal.TEN, "Some tax");

        RequestItem requestItem = getRequestItem();
        InvoiceRequest invoiceRequest = new InvoiceRequestBuilder()
                .addRequestItem(requestItem)
                .build();
        Money totalCost = requestItem.getTotalCost();

        bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy).calculateTax(any(), eq(totalCost));
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
