package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.mockito.ArgumentMatchers.any;

public class BookKeeperTest {

    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private TaxPolicy taxPolicy;

    @BeforeEach
    public void init() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(),"Imie Nazwisko"));
        taxPolicy = Mockito.mock(TaxPolicy.class);
        Mockito.when(taxPolicy.calculateTax(any(ProductType.class),
                any(Money.class))).thenReturn(new Tax(Money.ZERO, "tax"));
    }

    @Test
    public void shouldReturnInvoiceWithOneElement() {
        Product product = new Product(Id.generate(),Money.ZERO,"name",ProductType.STANDARD);
        RequestItem requestItem = new RequestItem(product.generateSnapshot(),1, Money.ZERO);
        invoiceRequest.add(requestItem);

        Invoice invoice =bookKeeper.issuance(invoiceRequest, taxPolicy);
        Assertions.assertEquals(1, invoice.getItems().size());
    }

}