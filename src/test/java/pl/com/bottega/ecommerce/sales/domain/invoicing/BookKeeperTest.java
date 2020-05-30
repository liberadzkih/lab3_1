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


@ExtendWith(MockitoExtension.class)
public class BookKeeperTest {
    private static final Money UNRELEVANT_MONEY = new Money(2.99);

    private BookKeeper bookKeeper;

    @Mock
    private ClientData clientData;
    private InvoiceRequest invoiceRequest;

    private TaxPolicy taxPolicy;

    private ProductData productData;
    private RequestItem requestItem;

    @BeforeEach
    public void setUp() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(clientData);

        productData = new ProductBuilder()
                .build()
                .generateSnapshot();

        taxPolicy = (productType, net) -> new Tax(UNRELEVANT_MONEY, "unrelevant");

        requestItem = new RequestItem(productData, 1, UNRELEVANT_MONEY);
    }

    @Test
    public void invoiceRequestWithOnePositionShouldReturnProperInvoice() {
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertEquals(invoice.getItems().size(), 1);
    }
    
}