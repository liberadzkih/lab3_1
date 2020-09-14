package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    private BookKeeper bookKeeper;
    private TaxPolicy taxPolicyMock;
    private Money amount;
    private ClientData clientDataMock;

    @BeforeEach
    void setUp() {
        taxPolicyMock = mock(TaxPolicy.class);
        clientDataMock = mock(ClientData.class);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        amount = new Money(BigDecimal.TEN);
        when(taxPolicyMock.calculateTax(any(), any())).thenReturn(new Tax(amount, "tax"));
    }

    //status
    @Test
    public void invoiceIssuanceRequest_oneItem_itemsCountOnInvoice() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);
        Product testProduct = new ProductTestBuilder().id(Id.generate())
                                                      .productType(ProductType.DRUG)
                                                      .name("cigarettes")
                                                      .price(amount)
                                                      .build();
        ProductData productData = testProduct.generateSnapshot();
        RequestItem requestItem = new RequestItem(productData, 1, amount);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    public void invoiceIssuanceRequest_999Items_itemsCountOnInvoice() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);
        ProductData productData;
        RequestItem requestItem;
        for (int i = 1; i <= 999; i++) {
            Product testProduct = new ProductTestBuilder().id(Id.generate())
                                                          .productType(ProductType.STANDARD)
                                                          .name("item of type: " + i)
                                                          .price(amount)
                                                          .build();
            productData = testProduct.generateSnapshot();
            requestItem = new RequestItem(productData, 1, amount);
            invoiceRequest.add(requestItem);
        }
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(999, invoice.getItems().size());
    }

    @Test
    public void invoiceIssuanceRequest_zeroItems_itemsCountOnInvoice() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(0, invoice.getItems().size());
    }

    //behaviour
    @Test
    public void invoiceIssuanceRequest_CurrencyMismatch_IllegalArgumentExceptionTest() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);

        amount = new Money(BigDecimal.ONE, Currency.getInstance(Locale.KOREA));
        Product testProduct = new ProductTestBuilder().id(Id.generate())
                                                      .productType(ProductType.STANDARD)
                                                      .name("socks")
                                                      .price(amount)
                                                      .build();
        ProductData productData = testProduct.generateSnapshot();
        RequestItem requestItem = new RequestItem(productData, 1, amount);
        invoiceRequest.add(requestItem);

        amount = new Money(BigDecimal.ONE, Currency.getInstance(Locale.US));
        testProduct = new ProductTestBuilder().id(Id.generate())
                                              .productType(ProductType.STANDARD)
                                              .name("pants")
                                              .price(amount)
                                              .build();
        productData = testProduct.generateSnapshot();
        requestItem = new RequestItem(productData, 1, amount);
        invoiceRequest.add(requestItem);

        assertThrows(IllegalArgumentException.class, () -> bookKeeper.issuance(invoiceRequest, taxPolicyMock));
    }

    @Test
    public void invoiceIssuanceRequest_twoItems_countCalculateTaxMethodCalls() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);
        Product testProduct = new ProductTestBuilder().id(Id.generate())
                                              .productType(ProductType.DRUG)
                                              .name("cigarettes")
                                              .price(amount)
                                              .build();
        ProductData productData = testProduct.generateSnapshot();
        RequestItem requestItem = new RequestItem(productData, 1, amount);
        invoiceRequest.add(requestItem);

        testProduct = new ProductTestBuilder().id(Id.generate())
                                               .productType(ProductType.FOOD)
                                               .name("banana")
                                               .price(amount)
                                               .build();
        productData = testProduct.generateSnapshot();
        requestItem = new RequestItem(productData, 1, amount);
        invoiceRequest.add(requestItem);

        bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        verify(taxPolicyMock).calculateTax(ProductType.FOOD, amount);
        verify(taxPolicyMock).calculateTax(ProductType.DRUG, amount);
        verify(taxPolicyMock, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void invoiceIssuanceRequest_zeroItems_countCalculateTaxMethodCalls() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);
        bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        verify(taxPolicyMock, never()).calculateTax(any(ProductType.class), any(Money.class));
    }

}
