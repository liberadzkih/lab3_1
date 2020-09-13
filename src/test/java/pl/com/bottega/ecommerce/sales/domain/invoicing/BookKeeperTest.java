package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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

    //Test case 1 żądanie wydania faktury z jedną pozycją powinno zwrócić fakturę z jedną pozycją
    @Test
    public void invoiceIssuanceRequest_oneItem() {
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientDataMock);
        RequestItem requestItem = new RequestItem(new Product(Id.generate(), amount, "cigarettes", ProductType.DRUG).generateSnapshot(), 1, amount);
        invoiceRequest.add(requestItem);
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicyMock);
        assertEquals(1, invoice.getItems().size());
    }

}
