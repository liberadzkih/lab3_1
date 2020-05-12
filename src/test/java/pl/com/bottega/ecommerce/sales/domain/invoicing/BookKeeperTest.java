package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

class BookKeeperTest {

    private TaxPolicy taxPolicy;
    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private ClientData clientData;
    private ProductData productData;

    @Test
    public void invoiceRequestWithOnePositionShouldReturnInvoiceWithOnePositionTest(){

        bookKeeper = new BookKeeper(new InvoiceFactory());
        clientData = new ClientData(Id.generate(),"client");
        invoiceRequest = new InvoiceRequest(clientData);

        taxPolicy = mock(TaxPolicy.class);
        when(taxPolicy.calculateTax(ProductType.STANDARD, new Money(3))).thenReturn(new Tax(new Money(0.23),"23%"));

        productData = mock(ProductData.class);
        when(productData.getType()).thenReturn(ProductType.STANDARD);

        RequestItem requestItem = new RequestItem(productData, 1, new Money(3));
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);

        Assertions.assertEquals(1,invoice.getItems().size());

    }

    @Test
    public void invoiceRequestWithTwoPositionsShouldCallCalculateTaxTwoTimesTest(){

        bookKeeper = new BookKeeper(new InvoiceFactory());
        clientData = new ClientData(Id.generate(),"client");
        invoiceRequest = new InvoiceRequest(clientData);

        Money money1 = new Money(3);
        Money money2 = new Money(5);

        TaxPolicy taxPolicy1 = mock(TaxPolicy.class);
        when(taxPolicy1.calculateTax(ProductType.STANDARD, money1)).thenReturn(new Tax(new Money(0.23),"23%"));
        when(taxPolicy1.calculateTax(ProductType.FOOD,money2)).thenReturn(new Tax(new Money(0.46),"46%"));

        ProductData productData1 = mock(ProductData.class);
        when(productData1.getType()).thenReturn(ProductType.STANDARD);

        ProductData productData2 = mock(ProductData.class);
        when(productData2.getType()).thenReturn(ProductType.FOOD);

        RequestItem requestItem1 = new RequestItem(productData1, 5, new Money(3));
        invoiceRequest.add(requestItem1);

        RequestItem requestItem2 = new RequestItem(productData2, 2, new Money(5));
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest,taxPolicy1);

        Mockito.verify(taxPolicy1, times(1)).calculateTax(ProductType.STANDARD, money1);
        Mockito.verify(taxPolicy1, times(1)).calculateTax(ProductType.FOOD, money2);



    }
}