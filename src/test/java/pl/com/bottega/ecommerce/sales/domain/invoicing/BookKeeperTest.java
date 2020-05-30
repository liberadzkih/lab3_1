package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BookKeeperTest {
    private static final Money UNRELEVANT_MONEY = new Money(2.99);
    public static final int UNRELEVANT_QUANTITY = 12;

    private BookKeeper bookKeeper;
    @Mock
    private ClientData clientData;
    private InvoiceRequest invoiceRequest;
    @Mock
    private TaxPolicy taxPolicy;
    private ProductData productData;

    @BeforeEach
    public void setUp() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(clientData);

        productData = new ProductBuilder()
                .build()
                .generateSnapshot();

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(UNRELEVANT_MONEY, "unrelevant"));
    }

    //State tests
    @Test
    public void invoiceRequestWithOnePositionShouldReturnProperInvoice() {
        invoiceRequest.add(new RequestItem(productData, UNRELEVANT_QUANTITY, UNRELEVANT_MONEY));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertEquals(invoice.getItems().size(), 1);
    }

    //Behaviour tests
    @Test
    void invoiceRequestWithTwoPositionsShouldCalculateTaxTwoTimes() {
        RequestItem requestItem1 = new RequestItem(productData, UNRELEVANT_QUANTITY, new Money(13.11));
        RequestItem requestItem2 = new RequestItem(productData, UNRELEVANT_QUANTITY, new Money(12.22));
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy).calculateTax(requestItem1.getProductData().getType(), requestItem1.getTotalCost());
        verify(taxPolicy).calculateTax(requestItem2.getProductData().getType(), requestItem2.getTotalCost());
    }
}